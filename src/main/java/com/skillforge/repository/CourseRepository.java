package com.skillforge.repository;

import com.skillforge.model.Course;
import com.skillforge.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
    extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {

  // Basic queries
  Optional<Course> findByIdAndIsDeletedFalse(String id);

  Page<Course> findByInstructor(User instructor, Pageable pageable);

  Page<Course> findByIsPublishedTrueAndIsDeletedFalse(Pageable pageable);

  boolean existsByTitleAndInstructorId(String title, String instructorId);

  // Search queries
  @Query(
      "SELECT c FROM Course c WHERE c.isPublished = true AND c.isDeleted = false "
          + "AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);

  // Top courses
  Page<Course> findTopByIsPublishedTrueAndIsDeletedFalseOrderByTotalEnrollmentsDesc(
      Pageable pageable);

  // Category-based queries
  Page<Course> findByCategoryAndIsPublishedTrueAndIsDeletedFalse(
      String category, Pageable pageable);

  List<Course> findByCategoryAndIsPublishedTrueAndIsDeletedFalse(String category);

  // Instructor statistics
  @Query(
      "SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId AND c.isPublished = true")
  Integer countPublishedCoursesByInstructor(@Param("instructorId") String instructorId);

  @Query(
      "SELECT COALESCE(SUM(c.totalEnrollments), 0) FROM Course c WHERE c.instructor.id = :instructorId")
  Integer getTotalEnrollmentsForInstructor(@Param("instructorId") String instructorId);

  @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId AND c.isDeleted = false")
  List<Course> findActiveByInstructorId(@Param("instructorId") String instructorId);

  // Rating queries
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
  Double calculateAverageRating(@Param("courseId") String courseId);

  // Bulk operations
  @Modifying
  @Query("UPDATE Course c SET c.averageRating = :rating WHERE c.id = :courseId")
  void updateAverageRating(@Param("courseId") String courseId, @Param("rating") Double rating);

  @Modifying
  @Query("UPDATE Course c SET c.totalEnrollments = c.totalEnrollments + 1 WHERE c.id = :courseId")
  int incrementEnrollments(@Param("courseId") String courseId);

  @Modifying
  @Query(
      "UPDATE Course c SET c.totalEnrollments = CASE WHEN c.totalEnrollments > 0 THEN c.totalEnrollments - 1 ELSE 0 END WHERE c.id = :courseId")
  int decrementEnrollments(@Param("courseId") String courseId);

  // Recent courses
  Page<Course> findByIsPublishedTrueAndIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

  // Courses by difficulty
  Page<Course> findByDifficultyLevelAndIsPublishedTrueAndIsDeletedFalse(
      Course.DifficultyLevel difficultyLevel, Pageable pageable);

  // Price range queries
  Page<Course> findByPriceBetweenAndIsPublishedTrueAndIsDeletedFalse(
      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);

  // Trending courses (based on recent enrollments)
  @Query(
      value =
          "SELECT c.* FROM courses c "
              + "INNER JOIN enrollments e ON e.course_id = c.id "
              + "WHERE e.enrollment_date > NOW() - INTERVAL '30 days' "
              + "AND c.is_published = true AND c.is_deleted = false "
              + "GROUP BY c.id ORDER BY COUNT(e.id) DESC",
      nativeQuery = true)
  List<Course> findTrendingCourses(Pageable pageable);
}
