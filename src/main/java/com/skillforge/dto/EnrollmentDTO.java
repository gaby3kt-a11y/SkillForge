package com.skillforge.dto;

import com.skillforge.model.Enrollment.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String courseId;
    private String courseTitle;
    private String courseThumbnailUrl;
    private String instructorName;
    private LocalDateTime enrollmentDate;
    private LocalDateTime completionDate;
    private LocalDateTime lastAccessedAt;
    private Double progressPercentage;
    private EnrollmentStatus status;
    private LocalDateTime expiresAt;
    private String certificateUrl;

    // Additional computed fields
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer remainingDays;
    private Boolean isExpiringSoon;

    public Integer getRemainingDays() {
        if (expiresAt == null || status != EnrollmentStatus.ACTIVE) {
            return null;
        }
        return (int) java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }

    public Boolean getIsExpiringSoon() {
        Integer remaining = getRemainingDays();
        return remaining != null && remaining <= 7 && remaining >= 0;
    }
}