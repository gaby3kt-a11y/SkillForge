package com.skillforge.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CourseProgressDTO {

    private Long courseId;
    private String courseName;
    private Long userId;
    private String userName;
    private Double completionPercentage;
    private Integer completedLessons;
    private Integer totalLessons;
    private Boolean isCompleted;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Constructors
    public CourseProgressDTO() {}

    public CourseProgressDTO(Long courseId, String courseName, Long userId,
                             String userName, Double completionPercentage,
                             Integer completedLessons, Integer totalLessons) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.userId = userId;
        this.userName = userName;
        this.completionPercentage = completionPercentage;
        this.completedLessons = completedLessons;
        this.totalLessons = totalLessons;
        this.isCompleted = completionPercentage >= 100.0;
        this.lastAccessedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
        this.isCompleted = completionPercentage >= 100.0;
    }

    public Integer getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(Integer completedLessons) {
        this.completedLessons = completedLessons;
    }

    public Integer getTotalLessons() { return totalLessons; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}