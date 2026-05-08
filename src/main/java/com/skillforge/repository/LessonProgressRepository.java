package com.skillforge.repository;

import com.skillforge.model.LessonProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, String> {

  Optional<LessonProgress> findByEnrollmentIdAndLessonId(String enrollmentId, String lessonId);

  List<LessonProgress> findByEnrollmentId(String enrollmentId);

  @Query(
      "SELECT lp FROM LessonProgress lp WHERE lp.enrollment.id = :enrollmentId AND lp.isCompleted = true")
  List<LessonProgress> findCompletedLessons(@Param("enrollmentId") String enrollmentId);

  @Query(
      "SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.enrollment.id = :enrollmentId AND lp.isCompleted = true")
  long countCompletedLessons(@Param("enrollmentId") String enrollmentId);

  @Modifying
  @Query(
      "UPDATE LessonProgress lp SET lp.isCompleted = true, lp.completedAt = CURRENT_TIMESTAMP "
          + "WHERE lp.id = :progressId AND lp.isCompleted = false")
  int markLessonAsCompleted(@Param("progressId") String progressId);

  @Modifying
  @Query(
      "UPDATE LessonProgress lp SET lp.watchTimeSeconds = :watchTime, lp.lastPositionSeconds = :position "
          + "WHERE lp.id = :progressId")
  void updateWatchProgress(
      @Param("progressId") String progressId,
      @Param("watchTime") Integer watchTime,
      @Param("position") Integer position);

  @Query("SELECT AVG(lp.watchTimeSeconds) FROM LessonProgress lp WHERE lp.lesson.id = :lessonId")
  Double getAverageWatchTimeForLesson(@Param("lessonId") String lessonId);
}
