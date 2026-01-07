package com.sms.student.service.interfaces;

import com.sms.student.dto.*;
import com.sms.student.enums.ClassShift;

import java.util.List;
import java.util.UUID;

public interface IScheduleService {

    // Time Slot Templates
    List<TimeSlotTemplateResponse> getAvailableTemplates(UUID teacherId);
    List<TimeSlotTemplateResponse> getTemplatesByShift(ClassShift shift, UUID teacherId);
    TimeSlotTemplateResponse getTemplateById(UUID templateId, UUID teacherId);
    TimeSlotTemplateResponse createTemplate(CreateTimeSlotTemplateRequest request, UUID teacherId);
    void deleteTemplate(UUID templateId, UUID teacherId);

    // Class Schedules
    ClassScheduleResponse getClassSchedule(UUID classId, UUID teacherId);
    ClassScheduleResponse createClassSchedule(CreateClassScheduleRequest request, UUID teacherId);
    ClassScheduleResponse updateScheduleEntries(UUID classId, UpdateScheduleEntriesRequest request, UUID teacherId);
    void deleteClassSchedule(UUID classId, UUID teacherId);
    void clearScheduleEntries(UUID classId, UUID teacherId);
    ClassScheduleResponse copySchedule(UUID targetClassId, UUID sourceClassId, UUID teacherId);
}
