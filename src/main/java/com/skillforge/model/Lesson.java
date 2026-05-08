package com.skillforge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "lessons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Lesson {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String content; // HTML content or markdown

  @Column(name = "video_url")
  private String videoUrl; // YouTube or Vimeo URL

  @Column(name = "duration_minutes")
  private Integer durationMinutes;

  @Column(name = "order_index")
  private Integer orderIndex;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private LessonType type = LessonType.VIDEO;

  @Column(name = "is_preview")
  @Builder.Default
  private boolean isPreview = false; // Can students preview without enrollment?

  @Column(name = "is_free")
  @Builder.Default
  private boolean isFree = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "module_id", nullable = false)
  private Module module;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public enum LessonType {
    VIDEO,
    TEXT,
    QUIZ,
    ASSIGNMENT,
    RESOURCE
  }
}
