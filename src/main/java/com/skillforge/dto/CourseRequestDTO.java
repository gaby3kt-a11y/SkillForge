package com.skillforge.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO {

    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = "9999.99", message = "Price cannot exceed 9999.99")
    private BigDecimal price;

    @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
    private String thumbnailUrl;

    @Min(value = 0, message = "Estimated hours cannot be negative")
    @Max(value = 500, message = "Estimated hours cannot exceed 500")
    private Integer estimatedHours;
}