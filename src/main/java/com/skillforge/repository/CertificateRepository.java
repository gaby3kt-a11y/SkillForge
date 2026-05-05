package com.skillforge.repository;

import com.skillforge.model.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    Optional<Certificate> findByEnrollmentId(String enrollmentId);

    Page<Certificate> findByUserId(String userId, Pageable pageable);

    @Query("SELECT c FROM Certificate c WHERE c.user.id = :userId AND c.course.id = :courseId")
    Optional<Certificate> findByUserAndCourse(@Param("userId") String userId, @Param("courseId") String courseId);

    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.course.id = :courseId")
    long countCertificatesIssuedForCourse(@Param("courseId") String courseId);

    @Query("SELECT c FROM Certificate c WHERE c.issueDate < CURRENT_TIMESTAMP AND c.expiryDate < CURRENT_TIMESTAMP")
    List<Certificate> findExpiredCertificates();

    @Query("SELECT c.course.id, COUNT(c) FROM Certificate c GROUP BY c.course.id ORDER BY COUNT(c) DESC")
    List<Object[]> getCertificateCountsByCourse();
}