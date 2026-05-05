package com.skillforge.repository;

import com.skillforge.model.Wishlist;
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
public interface WishlistRepository extends JpaRepository<Wishlist, String> {

    Optional<Wishlist> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    Page<Wishlist> findByUserId(String userId, Pageable pageable);

    List<Wishlist> findByCourseId(String courseId);

    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.notificationSent = false")
    List<Wishlist> findUnnotifiedWishlistItems(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Wishlist w SET w.notificationSent = true WHERE w.course.id = :courseId")
    void markNotificationsAsSent(@Param("courseId") String courseId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.course.id IN :courseIds")
    void removeMultipleFromWishlist(@Param("userId") String userId, @Param("courseIds") List<String> courseIds);
}