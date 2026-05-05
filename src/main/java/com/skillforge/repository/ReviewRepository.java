package com.skillforge.repository;

import com.skillforge.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Optional<Review> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    Page<Review> findByCourseId(String courseId, Pageable pageable);

    Page<Review> findByUserId(String userId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double getAverageRatingForCourse(@Param("courseId") String courseId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.course.id = :courseId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("courseId") String courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.rating >= 4")
    long countPositiveReviews(@Param("courseId") String courseId);

    @Modifying
    @Query("UPDATE Review r SET r.instructorReply = :reply, r.replyDate = CURRENT_TIMESTAMP " +
            "WHERE r.id = :reviewId")
    void addInstructorReply(@Param("reviewId") String reviewId, @Param("reply") String reply);

    @Query("SELECT r FROM Review r WHERE r.isVerifiedPurchase = false AND r.course.instructor.id = :instructorId")
    List<Review> findUnverifiedReviewsForInstructor(@Param("instructorId") String instructorId);
}