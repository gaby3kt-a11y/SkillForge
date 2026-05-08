package com.skillforge.dto;

import com.skillforge.model.Enrollment.EnrollmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressDTO {
  private String enrollmentId;
  private String courseId;
  private String courseTitle;

  private Double totalProgress;
  private EnrollmentStatus status;

  private LocalDateTime enrollmentDate;
  private LocalDateTime lastAccessedAt;
  private LocalDateTime estimatedCompletionDate;

  private List<ModuleProgressDTO> modules;
}
