package com.skillforge.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateDTO {

  @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
  private String title;

  @Size(max = 2000, message = "Description cannot exceed 2000 characters")
  private String description;

  private String category;

  private String difficultyLevel;

  @DecimalMin(value = "0.00", message = "Price cannot be negative")
  @DecimalMax(value = "9999.99", message = "Price cannot exceed 9999.99")
  private BigDecimal price;

  @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
  private String thumbnailUrl;

  @Min(value = 0, message = "Estimated hours cannot be negative")
  @Max(value = 500, message = "Estimated hours cannot exceed 500")
  private Integer estimatedHours;
}
