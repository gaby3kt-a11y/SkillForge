package com.skillforge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseListDTO {

  private String id;
  private String title;
  private String thumbnailUrl;
  private String category;
  private String difficultyLevel;
  private BigDecimal price;
  private Double averageRating;
  private Integer totalEnrollments;
  private Integer totalReviews;
  private Integer estimatedHours;
  private Boolean isPublished;
  private InstructorBasicDTO instructor;
  private LocalDateTime createdAt;

  // For display optimization
  private String formattedPrice;
  private String ratingStars; // e.g., "★★★★☆"
}
