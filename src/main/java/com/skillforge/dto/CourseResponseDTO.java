package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private String id;
    private String title;
    private String description;
    private String category;
    private String difficultyLevel;
    private BigDecimal price;
    private String thumbnailUrl;
    private Integer estimatedHours;
    private Integer totalEnrollments;
    private Double averageRating;
    private Boolean isPublished;
    private InstructorInfoDTO instructor;
    private List<ModuleSummaryDTO> modules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Integer totalStudents;
    private Integer totalReviews;
    private Integer totalModules;
    private Integer totalLessons;

    // User-specific (if authenticated)
    private Boolean isEnrolled;
    private Double userProgress;
    private Boolean isInWishlist;
}