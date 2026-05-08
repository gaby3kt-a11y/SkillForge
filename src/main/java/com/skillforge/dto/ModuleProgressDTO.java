package com.skillforge.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgressDTO {
  private String moduleId;
  private String moduleTitle;
  private Integer totalLessons;
  private Integer completedLessons;
  private Double progressPercentage;
  private List<LessonProgressDTO> lessons;
}
