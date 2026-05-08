package com.skillforge.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "enrollment_date", nullable = false)
  private LocalDateTime enrollmentDate;

  @Column(name = "completion_date")
  private LocalDateTime completionDate;

  @Column(name = "last_accessed_at")
  private LocalDateTime lastAccessedAt;

  @Column(name = "progress_percentage")
  @Builder.Default
  private BigDecimal progressPercentage = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

  @Column(name = "certificate_url")
  private String certificateUrl; // Link to generated PDF certificate

  @Column(name = "expires_at")
  private LocalDateTime expiresAt; // For subscription-based access

  @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL)
  @Builder.Default
  private Set<LessonProgress> lessonProgresses = new HashSet<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public enum EnrollmentStatus {
    ACTIVE,
    COMPLETED,
    DROPPED,
    EXPIRED
  }

  // Helper method to calculate progress based on lessons completed
  public void calculateProgress() {
    if (lessonProgresses.isEmpty()) {
      this.progressPercentage = BigDecimal.ZERO;
      return;
    }

    long completedCount = lessonProgresses.stream().filter(LessonProgress::isCompleted).count();

    this.progressPercentage = BigDecimal.valueOf((completedCount * 100.0) / lessonProgresses.size());

    if (this.progressPercentage.compareTo(BigDecimal.valueOf(100))>=0 && this.status != EnrollmentStatus.COMPLETED) {
      this.status = EnrollmentStatus.COMPLETED;
      this.completionDate = LocalDateTime.now();
    }
  }
}
