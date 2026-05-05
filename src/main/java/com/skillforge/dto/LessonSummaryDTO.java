package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonSummaryDTO {

    private String id;
    private String title;
    private Integer durationMinutes;
    private Integer orderIndex;
    private String lessonType; // VIDEO, TEXT, QUIZ, ASSIGNMENT
    private Boolean isPreview;
    private Boolean isFree;
    private String videoUrl;

    // For enrolled users only
    private Boolean isCompleted;
    private Integer watchTimeSeconds;
    private Integer lastPositionSeconds;
}