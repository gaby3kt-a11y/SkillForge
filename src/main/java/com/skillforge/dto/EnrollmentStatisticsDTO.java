package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatisticsDTO {

    // Overview stats
    private Long totalStudents;
    private Long totalEnrollments;
    private Double averageCompletionRate;
    private Long activeEnrollmentsToday;
    private Long newEnrollmentsThisWeek;

    // Detailed stats
    private Integer totalActiveEnrollments;
    private Integer totalCompletedEnrollments;
    private Integer totalDroppedEnrollments;
    private Integer totalExpiredEnrollments;

    // Revenue stats
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerStudent;
    private BigDecimal projectedRevenue;

    // Engagement stats
    private Double averageProgressPercentage;
    private Double averageTimeSpentMinutes;
    private Integer mostPopularCourseEnrollments;
    private String mostPopularCourseTitle;

    // Dropout analysis
    private Double dropoutRate;
    private Integer dropoutCountLast30Days;
    private Map<String, Integer> dropoutReasons;
    private List<CourseDropoutDTO> coursesWithHighestDropout;

    // Trends
    private List<DailyEnrollmentDTO> dailyEnrollmentsLast30Days;
    private Map<String, Long> enrollmentsByDifficulty;
    private Map<String, Long> enrollmentsByCategory;

    // Predictions
    private Integer estimatedNextMonthEnrollments;
    private Double growthRate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CourseDropoutDTO {
    private String courseId;
    private String courseTitle;
    private Integer dropoutCount;
    private Double dropoutRate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DailyEnrollmentDTO {
    private String date;
    private Long count;
    private BigDecimal revenue;
}