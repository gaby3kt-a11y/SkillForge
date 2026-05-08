package com.skillforge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "lesson_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "lesson_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LessonProgress {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "enrollment_id", nullable = false)
  private Enrollment enrollment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lesson_id", nullable = false)
  private Lesson lesson;

  @Column(name = "is_completed")
  @Builder.Default
  private boolean isCompleted = false;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "watch_time_seconds")
  @Builder.Default
  private Integer watchTimeSeconds = 0; // Track how long they watched

  @Column(name = "last_position_seconds")
  @Builder.Default
  private Integer lastPositionSeconds = 0; // Resume playback position

  @Column(name = "quiz_score")
  private Integer quizScore; // For quiz-type lessons

  @Column(name = "notes")
  private String notes; // User's personal notes

  @LastModifiedDate
  @Column(name = "last_accessed_at")
  private LocalDateTime lastAccessedAt;

  // Mark lesson as complete
  public void complete() {
    this.isCompleted = true;
    this.completedAt = LocalDateTime.now();
  }
}
