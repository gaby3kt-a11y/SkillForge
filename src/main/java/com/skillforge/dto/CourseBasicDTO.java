package com.skillforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Basic DTO for course information used in lists, recommendations, and simple references
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBasicDTO {

    private String id;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private String instructorId;
    private String instructorName;
    private Double averageRating;
    private Integer totalEnrollments;
    private String category;
    private String difficultyLevel;

    // Computed fields
    private String formattedPrice;
    private String ratingStars;
    private Boolean isFree;

    // Optional user-specific fields (populated when user is authenticated)
    private Boolean isEnrolled;
    private Boolean isInWishlist;
    private Double userProgress;

    /**
     * Helper method to check if course is free
     */
    public Boolean getIsFree() {
        return price != null && price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Get formatted price string
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "Free";
        }
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return "Free";
        }
        return String.format("$%.2f", price);
    }

    /**
     * Generate rating stars as string (e.g., "★★★★☆" for 4.5)
     */
    public String getRatingStars() {
        if (averageRating == null || averageRating == 0) {
            return "☆☆☆☆☆";
        }

        int fullStars = (int) Math.floor(averageRating);
        boolean hasHalfStar = (averageRating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("½");
        }
        while (stars.length() < 5) {
            stars.append("☆");
        }

        return stars.toString();
    }

    /**
     * Get numeric rating (e.g., 4.5)
     */
    public Double getNumericRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    /**
     * Check if course has high rating (4.0 or above)
     */
    public boolean isHighlyRated() {
        return averageRating != null && averageRating >= 4.0;
    }

    /**
     * Check if course is popular (100+ enrollments)
     */
    public boolean isPopular() {
        return totalEnrollments != null && totalEnrollments >= 100;
    }

    /**
     * Get enrollment count formatted (e.g., "1.2k" for 1200)
     */
    public String getFormattedEnrollmentCount() {
        if (totalEnrollments == null) {
            return "0";
        }
        if (totalEnrollments >= 1000) {
            return String.format("%.1fk", totalEnrollments / 1000.0);
        }
        return String.valueOf(totalEnrollments);
    }

    /**
     * Get difficulty level with icon (for UI display)
     */
    public String getDifficultyWithIcon() {
        if (difficultyLevel == null) {
            return "📚 Beginner";
        }
        switch (difficultyLevel.toUpperCase()) {
            case "BEGINNER":
                return "🌱 Beginner";
            case "INTERMEDIATE":
                return "⚡ Intermediate";
            case "ADVANCED":
                return "🚀 Advanced";
            default:
                return "📚 " + difficultyLevel;
        }
    }

    /**
     * Builder with convenience method for free courses
     */
    public static class CourseBasicDTOBuilder {
        public CourseBasicDTOBuilder free() {
            this.price = BigDecimal.ZERO;
            return this;
        }

        public CourseBasicDTOBuilder withRating(Double rating) {
            this.averageRating = rating;
            return this;
        }
    }

    /**
     * Create a minimal version (for memory-constrained scenarios)
     */
    public CourseBasicDTO toMinimal() {
        return CourseBasicDTO.builder()
                .id(this.id)
                .title(this.title)
                .thumbnailUrl(this.thumbnailUrl)
                .formattedPrice(this.getFormattedPrice())
                .build();
    }
}