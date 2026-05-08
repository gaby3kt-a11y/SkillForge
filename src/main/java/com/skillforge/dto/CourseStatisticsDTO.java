package com.skillforge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsDTO {

  private String courseId;
  private String courseTitle;

  // Enrollment stats
  private Integer totalEnrollments;
  private Integer activeEnrollments;
  private Integer completedEnrollments;
  private Integer droppedEnrollments;
  private Double completionRate;

  // Revenue stats
  private BigDecimal totalRevenue;
  private BigDecimal instructorEarnings;
  private BigDecimal platformCommission;

  // Engagement stats
  private Double averageWatchTimeMinutes;
  private Double averageProgressPercentage;
  private Map<String, Integer> lessonCompletionBreakdown;

  // Rating stats
  private Double averageRating;
  private Integer totalReviews;
  private Map<Integer, Integer> ratingDistribution; // 5 stars: 100, 4 stars: 50, etc.

  // Time-based stats
  private LocalDateTime lastEnrollmentDate;
  private LocalDateTime lastAccessDate;
  private Integer enrollmentsLast7Days;
  private Integer enrollmentsLast30Days;
}
