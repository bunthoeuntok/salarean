package com.sms.student.service;

import com.sms.student.dto.*;
import com.sms.student.enums.ClassShift;
import com.sms.student.exception.ClassNotFoundException;
import com.sms.student.exception.UnauthorizedClassAccessException;
import com.sms.student.model.*;
import com.sms.student.repository.*;
import com.sms.student.service.interfaces.IScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService implements IScheduleService {

    private final TimeSlotTemplateRepository templateRepository;
    private final ClassScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository entryRepository;
    private final ClassRepository classRepository;

    // ===================== TIME SLOT TEMPLATES =====================

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotTemplateResponse> getAvailableTemplates(UUID teacherId) {
        log.info("Fetching available templates for teacher: {}", teacherId);
        List<TimeSlotTemplate> templates = templateRepository.findAllByTeacher(teacherId);
        return templates.stream()
            .map(this::mapToTemplateResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotTemplateResponse> getTemplatesByShift(ClassShift shift, UUID teacherId) {
        log.info("Fetching templates for shift: {} and teacher: {}", shift, teacherId);
        List<TimeSlotTemplate> templates = templateRepository.findByShiftAndTeacher(shift, teacherId);
        return templates.stream()
            .map(this::mapToTemplateResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSlotTemplateResponse getTemplateById(UUID templateId, UUID teacherId) {
        log.info("Fetching template: {} for teacher: {}", templateId, teacherId);
        TimeSlotTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.TEMPLATE_NOT_FOUND));

        // Verify access: system defaults or owned templates
        if (template.getTeacherId() != null && !template.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException(ScheduleErrorCode.UNAUTHORIZED_TEMPLATE_ACCESS);
        }

        return mapToTemplateResponse(template);
    }

    @Override
    @Transactional
    public TimeSlotTemplateResponse createTemplate(CreateTimeSlotTemplateRequest request, UUID teacherId) {
        log.info("Creating custom template for teacher: {}", teacherId);

        TimeSlotTemplate template = TimeSlotTemplate.builder()
            .teacherId(teacherId)
            .name(request.getName())
            .nameKm(request.getNameKm())
            .shift(request.getShift())
            .slots(request.getSlots().stream()
                .map(this::mapToTimeSlot)
                .collect(Collectors.toList()))
            .isDefault(false)
            .build();

        TimeSlotTemplate saved = templateRepository.save(template);
        log.info("Created template: {}", saved.getId());
        return mapToTemplateResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTemplate(UUID templateId, UUID teacherId) {
        log.info("Deleting template: {} by teacher: {}", templateId, teacherId);

        TimeSlotTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.TEMPLATE_NOT_FOUND));

        // Cannot delete system defaults
        if (template.getIsDefault()) {
            throw new IllegalArgumentException(ScheduleErrorCode.CANNOT_DELETE_DEFAULT_TEMPLATE);
        }

        // Verify ownership
        if (!teacherId.equals(template.getTeacherId())) {
            throw new IllegalArgumentException(ScheduleErrorCode.UNAUTHORIZED_TEMPLATE_ACCESS);
        }

        templateRepository.delete(template);
        log.info("Deleted template: {}", templateId);
    }

    // ===================== CLASS SCHEDULES =====================

    @Override
    @Transactional(readOnly = true)
    public ClassScheduleResponse getClassSchedule(UUID classId, UUID teacherId) {
        log.info("Fetching schedule for class: {} by teacher: {}", classId, teacherId);

        // Verify class ownership
        SchoolClass schoolClass = verifyClassOwnership(classId, teacherId);

        ClassSchedule schedule = scheduleRepository.findByClassIdAndIsActiveTrue(classId)
            .orElse(null);

        if (schedule == null) {
            return null;
        }

        return mapToScheduleResponse(schedule, schoolClass);
    }

    @Override
    @Transactional
    public ClassScheduleResponse createClassSchedule(CreateClassScheduleRequest request, UUID teacherId) {
        log.info("Creating schedule for class: {} by teacher: {}", request.getClassId(), teacherId);

        // Verify class ownership
        SchoolClass schoolClass = verifyClassOwnership(request.getClassId(), teacherId);

        // Check if schedule already exists
        if (scheduleRepository.existsByClassIdAndAcademicYear(request.getClassId(), request.getAcademicYear())) {
            throw new IllegalArgumentException(ScheduleErrorCode.SCHEDULE_ALREADY_EXISTS);
        }

        // Determine template to use
        UUID templateId = request.getTimeSlotTemplateId();
        if (templateId == null) {
            // Use default template for class shift
            TimeSlotTemplate defaultTemplate = templateRepository.findDefaultByShift(schoolClass.getShift());
            if (defaultTemplate != null) {
                templateId = defaultTemplate.getId();
            }
        }

        ClassSchedule schedule = ClassSchedule.builder()
            .classId(request.getClassId())
            .timeSlotTemplateId(templateId)
            .customSlots(request.getCustomSlots() != null
                ? request.getCustomSlots().stream().map(this::mapToTimeSlot).collect(Collectors.toList())
                : null)
            .academicYear(request.getAcademicYear())
            .isActive(true)
            .entries(new ArrayList<>())
            .build();

        ClassSchedule saved = scheduleRepository.save(schedule);
        log.info("Created schedule: {} for class: {}", saved.getId(), request.getClassId());

        return mapToScheduleResponse(saved, schoolClass);
    }

    @Override
    @Transactional
    public ClassScheduleResponse updateScheduleEntries(UUID classId, UpdateScheduleEntriesRequest request, UUID teacherId) {
        log.info("Updating schedule entries for class: {} by teacher: {}", classId, teacherId);

        // Verify class ownership
        SchoolClass schoolClass = verifyClassOwnership(classId, teacherId);

        ClassSchedule schedule = scheduleRepository.findByClassIdAndIsActiveTrue(classId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // Clear existing entries if requested
        if (Boolean.TRUE.equals(request.getClearExisting())) {
            entryRepository.deleteByClassScheduleId(schedule.getId());
            schedule.getEntries().clear();
        }

        // Add or update entries
        for (ScheduleEntryDto dto : request.getEntries()) {
            // Validate day and period
            if (dto.getDayOfWeek() < 1 || dto.getDayOfWeek() > 6) {
                throw new IllegalArgumentException(ScheduleErrorCode.INVALID_DAY_OF_WEEK);
            }

            // Check for existing entry at this slot
            ScheduleEntry existing = entryRepository.findByClassScheduleIdAndDayOfWeekAndPeriodNumber(
                schedule.getId(), dto.getDayOfWeek(), dto.getPeriodNumber()
            ).orElse(null);

            if (existing != null) {
                // Update existing entry
                existing.setSubjectId(dto.getSubjectId());
                existing.setRoom(dto.getRoom());
                existing.setNotes(dto.getNotes());
                entryRepository.save(existing);
            } else {
                // Create new entry
                ScheduleEntry entry = ScheduleEntry.builder()
                    .classSchedule(schedule)
                    .dayOfWeek(dto.getDayOfWeek())
                    .periodNumber(dto.getPeriodNumber())
                    .subjectId(dto.getSubjectId())
                    .room(dto.getRoom())
                    .notes(dto.getNotes())
                    .build();
                entryRepository.save(entry);
            }
        }

        // Refresh schedule with entries
        ClassSchedule updated = scheduleRepository.findById(schedule.getId()).orElse(schedule);
        log.info("Updated {} entries for schedule: {}", request.getEntries().size(), schedule.getId());

        return mapToScheduleResponse(updated, schoolClass);
    }

    @Override
    @Transactional
    public void deleteClassSchedule(UUID classId, UUID teacherId) {
        log.info("Deleting schedule for class: {} by teacher: {}", classId, teacherId);

        // Verify class ownership
        verifyClassOwnership(classId, teacherId);

        ClassSchedule schedule = scheduleRepository.findByClassIdAndIsActiveTrue(classId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        scheduleRepository.delete(schedule);
        log.info("Deleted schedule: {} for class: {}", schedule.getId(), classId);
    }

    @Override
    @Transactional
    public void clearScheduleEntries(UUID classId, UUID teacherId) {
        log.info("Clearing schedule entries for class: {} by teacher: {}", classId, teacherId);

        // Verify class ownership
        verifyClassOwnership(classId, teacherId);

        ClassSchedule schedule = scheduleRepository.findByClassIdAndIsActiveTrue(classId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        entryRepository.deleteByClassScheduleId(schedule.getId());
        log.info("Cleared all entries for schedule: {}", schedule.getId());
    }

    @Override
    @Transactional
    public ClassScheduleResponse copySchedule(UUID targetClassId, UUID sourceClassId, UUID teacherId) {
        log.info("Copying schedule from class: {} to class: {} by teacher: {}", sourceClassId, targetClassId, teacherId);

        // Verify ownership of both classes
        SchoolClass targetClass = verifyClassOwnership(targetClassId, teacherId);
        verifyClassOwnership(sourceClassId, teacherId);

        // Get source schedule
        ClassSchedule sourceSchedule = scheduleRepository.findByClassIdAndIsActiveTrue(sourceClassId)
            .orElseThrow(() -> new IllegalArgumentException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // Check if target already has a schedule
        ClassSchedule targetSchedule = scheduleRepository.findByClassIdAndIsActiveTrue(targetClassId)
            .orElse(null);

        if (targetSchedule == null) {
            // Create new schedule for target
            targetSchedule = ClassSchedule.builder()
                .classId(targetClassId)
                .timeSlotTemplateId(sourceSchedule.getTimeSlotTemplateId())
                .customSlots(sourceSchedule.getCustomSlots())
                .academicYear(targetClass.getAcademicYear())
                .isActive(true)
                .entries(new ArrayList<>())
                .build();
            targetSchedule = scheduleRepository.save(targetSchedule);
        } else {
            // Clear existing entries
            entryRepository.deleteByClassScheduleId(targetSchedule.getId());
            targetSchedule.getEntries().clear();
        }

        // Copy entries from source
        List<ScheduleEntry> sourceEntries = entryRepository.findByClassScheduleIdOrdered(sourceSchedule.getId());
        for (ScheduleEntry sourceEntry : sourceEntries) {
            ScheduleEntry newEntry = ScheduleEntry.builder()
                .classSchedule(targetSchedule)
                .dayOfWeek(sourceEntry.getDayOfWeek())
                .periodNumber(sourceEntry.getPeriodNumber())
                .subjectId(sourceEntry.getSubjectId())
                .room(sourceEntry.getRoom())
                .notes(sourceEntry.getNotes())
                .build();
            entryRepository.save(newEntry);
        }

        log.info("Copied {} entries from schedule: {} to schedule: {}",
            sourceEntries.size(), sourceSchedule.getId(), targetSchedule.getId());

        // Refresh and return
        ClassSchedule finalSchedule = scheduleRepository.findById(targetSchedule.getId()).orElse(targetSchedule);
        return mapToScheduleResponse(finalSchedule, targetClass);
    }

    // ===================== HELPER METHODS =====================

    private SchoolClass verifyClassOwnership(UUID classId, UUID teacherId) {
        return classRepository.findByIdAndTeacherId(classId, teacherId)
            .orElseThrow(() -> {
                if (classRepository.existsById(classId)) {
                    return new UnauthorizedClassAccessException(
                        "Teacher " + teacherId + " is not authorized to access class " + classId
                    );
                } else {
                    return new ClassNotFoundException("Class with ID " + classId + " not found");
                }
            });
    }

    private TimeSlotTemplateResponse mapToTemplateResponse(TimeSlotTemplate template) {
        List<TimeSlotDto> slotDtos = template.getSlots().stream()
            .map(this::mapToTimeSlotDto)
            .collect(Collectors.toList());

        int periodCount = (int) template.getSlots().stream()
            .filter(s -> !Boolean.TRUE.equals(s.getIsBreak()))
            .count();

        return TimeSlotTemplateResponse.builder()
            .id(template.getId())
            .teacherId(template.getTeacherId())
            .name(template.getName())
            .nameKm(template.getNameKm())
            .shift(template.getShift())
            .slots(slotDtos)
            .isDefault(template.getIsDefault())
            .periodCount(periodCount)
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
    }

    private TimeSlot mapToTimeSlot(TimeSlotDto dto) {
        return TimeSlot.builder()
            .periodNumber(dto.getPeriodNumber())
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .label(dto.getLabel())
            .labelKm(dto.getLabelKm())
            .isBreak(dto.getIsBreak())
            .build();
    }

    private TimeSlotDto mapToTimeSlotDto(TimeSlot slot) {
        return TimeSlotDto.builder()
            .periodNumber(slot.getPeriodNumber())
            .startTime(slot.getStartTime())
            .endTime(slot.getEndTime())
            .label(slot.getLabel())
            .labelKm(slot.getLabelKm())
            .isBreak(slot.getIsBreak())
            .build();
    }

    private ClassScheduleResponse mapToScheduleResponse(ClassSchedule schedule, SchoolClass schoolClass) {
        TimeSlotTemplateResponse templateResponse = null;
        List<TimeSlotDto> effectiveSlots = null;

        if (schedule.getTimeSlotTemplateId() != null) {
            templateRepository.findById(schedule.getTimeSlotTemplateId())
                .ifPresent(template -> {});

            TimeSlotTemplate template = templateRepository.findById(schedule.getTimeSlotTemplateId()).orElse(null);
            if (template != null) {
                templateResponse = mapToTemplateResponse(template);
                effectiveSlots = templateResponse.getSlots();
            }
        }

        // Custom slots override template slots
        if (schedule.getCustomSlots() != null && !schedule.getCustomSlots().isEmpty()) {
            effectiveSlots = schedule.getCustomSlots().stream()
                .map(this::mapToTimeSlotDto)
                .collect(Collectors.toList());
        }

        // Map entries
        List<ScheduleEntryResponse> entryResponses = entryRepository.findByClassScheduleIdOrdered(schedule.getId())
            .stream()
            .map(this::mapToEntryResponse)
            .collect(Collectors.toList());

        return ClassScheduleResponse.builder()
            .id(schedule.getId())
            .classId(schedule.getClassId())
            .timeSlotTemplateId(schedule.getTimeSlotTemplateId())
            .template(templateResponse)
            .customSlots(schedule.getCustomSlots() != null
                ? schedule.getCustomSlots().stream().map(this::mapToTimeSlotDto).collect(Collectors.toList())
                : null)
            .effectiveSlots(effectiveSlots)
            .academicYear(schedule.getAcademicYear())
            .isActive(schedule.getIsActive())
            .entries(entryResponses)
            .createdAt(schedule.getCreatedAt())
            .updatedAt(schedule.getUpdatedAt())
            .build();
    }

    private ScheduleEntryResponse mapToEntryResponse(ScheduleEntry entry) {
        return ScheduleEntryResponse.builder()
            .id(entry.getId())
            .dayOfWeek(entry.getDayOfWeek())
            .periodNumber(entry.getPeriodNumber())
            .subjectId(entry.getSubjectId())
            .room(entry.getRoom())
            .notes(entry.getNotes())
            .createdAt(entry.getCreatedAt())
            .updatedAt(entry.getUpdatedAt())
            .build();
    }
}
