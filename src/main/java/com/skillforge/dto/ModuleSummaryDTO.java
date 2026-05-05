package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSummaryDTO {

    private String id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer totalLessons;
    private Integer totalDurationMinutes;
    private List<LessonSummaryDTO> lessons;
}