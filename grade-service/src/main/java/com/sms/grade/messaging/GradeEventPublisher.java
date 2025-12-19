package com.sms.grade.messaging;

import com.sms.grade.config.RabbitMQConfig;
import com.sms.grade.dto.event.AverageCalculatedEvent;
import com.sms.grade.dto.event.GradeEnteredEvent;
import com.sms.grade.model.Grade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publisher for grade-related events to RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GradeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish grade entered event.
     */
    public void publishGradeEntered(Grade grade) {
        GradeEnteredEvent event = buildEvent(grade, GradeEnteredEvent.EventType.CREATED);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GRADE_EXCHANGE,
                RabbitMQConfig.GRADE_ENTERED_ROUTING_KEY,
                event
        );
        log.info("Published grade entered event for grade {}", grade.getId());
    }

    /**
     * Publish grade updated event.
     */
    public void publishGradeUpdated(Grade grade) {
        GradeEnteredEvent event = buildEvent(grade, GradeEnteredEvent.EventType.UPDATED);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GRADE_EXCHANGE,
                RabbitMQConfig.GRADE_UPDATED_ROUTING_KEY,
                event
        );
        log.info("Published grade updated event for grade {}", grade.getId());
    }

    /**
     * Publish grade deleted event.
     */
    public void publishGradeDeleted(Grade grade) {
        GradeEnteredEvent event = buildEvent(grade, GradeEnteredEvent.EventType.DELETED);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GRADE_EXCHANGE,
                RabbitMQConfig.GRADE_DELETED_ROUTING_KEY,
                event
        );
        log.info("Published grade deleted event for grade {}", grade.getId());
    }

    /**
     * Publish average calculated event.
     */
    public void publishAverageCalculated(AverageCalculatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GRADE_EXCHANGE,
                RabbitMQConfig.AVERAGE_CALCULATED_ROUTING_KEY,
                event
        );
        log.info("Published average calculated event for student {} type {}",
                event.getStudentId(), event.getAverageType());
    }

    private GradeEnteredEvent buildEvent(Grade grade, GradeEnteredEvent.EventType eventType) {
        return GradeEnteredEvent.builder()
                .gradeId(grade.getId())
                .teacherId(grade.getTeacherId())
                .studentId(grade.getStudentId())
                .classId(grade.getClassId())
                .subjectId(grade.getSubject().getId())
                .subjectName(grade.getSubject().getName())
                .assessmentType(grade.getAssessmentType().getName())
                .assessmentCode(grade.getAssessmentType().getCode())
                .score(grade.getScore())
                .maxScore(grade.getMaxScore())
                .percentage(grade.getPercentage())
                .letterGrade(grade.getLetterGrade())
                .semester(grade.getSemester())
                .academicYear(grade.getAcademicYear())
                .enteredAt(LocalDateTime.now())
                .eventType(eventType)
                .build();
    }
}
