package com.skillforge.repository;

import com.skillforge.model.Enrollment;
import com.skillforge.model.Enrollment.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    // Basic queries
    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseIdAndStatus(String userId, String courseId, EnrollmentStatus status);

    // User enrollments
    Page<Enrollment> findByUserId(String userId, Pageable pageable);

    List<Enrollment> findByUserIdAndStatus(String userId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByUser(@Param("userId") String userId);

    // Course enrollments
    Page<Enrollment> findByCourseId(String courseId, Pageable pageable);

    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByCourse(@Param("courseId") String courseId);

    // Progress tracking
    @Query("SELECT AVG(e.progressPercentage) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    Double getAverageProgressForCourse(@Param("courseId") String courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.progressPercentage = 100")
    long countCompletionsByCourse(@Param("courseId") String courseId);

    // Completion rate
    @Query("SELECT (COUNT(CASE WHEN e.progressPercentage = 100 THEN 1 END) * 100.0 / COUNT(e)) " +
            "FROM Enrollment e WHERE e.course.id = :courseId")
    Double getCompletionRateForCourse(@Param("courseId") String courseId);

    // Recent enrollments
    @Query("SELECT e FROM Enrollment e WHERE e.enrollmentDate > :since ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findRecentEnrollments(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrollmentDate > :since")
    long countEnrollmentsSince(@Param("since") LocalDateTime since);

    // Expired enrollments
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'ACTIVE' AND e.expiresAt < CURRENT_TIMESTAMP")
    List<Enrollment> findExpiredEnrollments();

    @Modifying
    @Query("UPDATE Enrollment e SET e.status = 'EXPIRED' WHERE e.id IN :enrollmentIds")
    void markAsExpired(@Param("enrollmentIds") List<String> enrollmentIds);

    // Dropout analysis
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'DROPPED' AND e.updatedAt > :since")
    List<Enrollment> findRecentDropouts(@Param("since") LocalDateTime since);

    @Query("SELECT e.course.id, COUNT(e) FROM Enrollment e WHERE e.status = 'DROPPED' GROUP BY e.course.id")
    List<Object[]> getDropoutCountsByCourse();

    // Certificate generation
    Optional<Enrollment> findByUserIdAndCourseIdAndStatus(String userId, String courseId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.progressPercentage = 100 AND e.status != 'COMPLETED'")
    List<Enrollment> findUnmarkedCompletions();

    @Modifying
    @Query("UPDATE Enrollment e SET e.status = 'COMPLETED', e.completionDate = CURRENT_TIMESTAMP " +
            "WHERE e.id = :enrollmentId AND e.progressPercentage = 100")
    void markAsCompleted(@Param("enrollmentId") String enrollmentId);

    // Statistics
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    long countUniqueStudentsForInstructor(@Param("instructorId") String instructorId);

    @Query("SELECT FUNCTION('DATE', e.enrollmentDate), COUNT(e) FROM Enrollment e " +
            "WHERE e.enrollmentDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', e.enrollmentDate)")
    List<Object[]> getDailyEnrollmentStats(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Add to EnrollmentRepository.java

    @Query("SELECT e FROM Enrollment e WHERE e.course.instructor.id = :instructorId")
    List<Enrollment> findByCourseInstructorId(@Param("instructorId") String instructorId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.instructor.id = :instructorId " +
            "AND e.enrollmentDate BETWEEN :startDate AND :endDate")
    long countByCourseInstructorIdAndEnrollmentDateBetween(@Param("instructorId") String instructorId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.instructor.id = :instructorId " +
            "AND e.enrollmentDate > :since")
    long countByCourseInstructorIdAndEnrollmentDateAfter(@Param("instructorId") String instructorId,
                                                         @Param("since") LocalDateTime since);

    @Query("SELECT e FROM Enrollment e WHERE e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollments();
}