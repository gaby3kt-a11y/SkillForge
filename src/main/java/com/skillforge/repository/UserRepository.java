package com.skillforge.repository;

import com.skillforge.model.User;
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
public interface UserRepository extends JpaRepository<User, String> {

    // Basic queries
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Search queries
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> searchUsers(@Param("name") String name, Pageable pageable);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRole(@Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_INSTRUCTOR'")
    long countInstructors();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_STUDENT'")
    long countStudents();

    // Active users
    Page<User> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLoginAt < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    // Email verification
    Optional<User> findByEmailAndIsEmailVerifiedFalse(String email);

    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true WHERE u.email = :email")
    void verifyEmail(@Param("email") String email);

    // Login tracking
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId);

    // New users statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :since")
    long countNewUsersSince(@Param("since") LocalDateTime since);

    // Instructor earnings (if you implement payment splitting)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.course.instructor.id = :instructorId AND p.status = 'COMPLETED'")
    Double getTotalEarnings(@Param("instructorId") String instructorId);
}