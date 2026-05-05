package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDTO {

    private String id;
    private String enrollmentId;
    private String lessonId;
    private String lessonTitle;
    private Integer lessonOrderIndex;
    private String moduleTitle;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer watchTimeSeconds;
    private Integer lastPositionSeconds;
    private Integer quizScore;
    private Integer durationMinutes;
    private Double completionPercentage;
    private LocalDateTime lastAccessedAt;

    public Double getCompletionPercentage() {
        if (durationMinutes == null || durationMinutes == 0) {
            return isCompleted ? 100.0 : 0.0;
        }
        double watchTimeMinutes = watchTimeSeconds / 60.0;
        return Math.min((watchTimeMinutes / durationMinutes) * 100, 100.0);
    }
}