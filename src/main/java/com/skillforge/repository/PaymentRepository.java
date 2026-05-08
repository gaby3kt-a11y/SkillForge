package com.skillforge.repository;

import com.skillforge.model.Payment;
import com.skillforge.model.Payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

  Optional<Payment> findByTransactionId(String transactionId);

  Page<Payment> findByUserId(String userId, Pageable pageable);

  List<Payment> findByCourseId(String courseId);

  Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

  @Query(
      "SELECT p FROM Payment p WHERE p.user.id = :userId AND p.course.id = :courseId AND p.status = 'COMPLETED'")
  Optional<Payment> findSuccessfulPayment(
      @Param("userId") String userId, @Param("courseId") String courseId);

  @Query(
      "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.course.instructor.id = :instructorId AND p.status = 'COMPLETED'")
  BigDecimal getTotalRevenueForInstructor(@Param("instructorId") String instructorId);

  @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
  BigDecimal getTotalPlatformRevenue();

  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate > :since")
  long countSuccessfulPaymentsSince(@Param("since") LocalDateTime since);

  @Modifying
  @Query(
      "UPDATE Payment p SET p.status = 'COMPLETED', p.paymentDate = CURRENT_TIMESTAMP "
          + "WHERE p.id = :paymentId AND p.status = 'PENDING'")
  int markAsCompleted(@Param("paymentId") String paymentId);

  @Modifying
  @Query(
      "UPDATE Payment p SET p.status = 'REFUNDED', p.refundDate = CURRENT_TIMESTAMP, p.refundReason = :reason "
          + "WHERE p.id = :paymentId")
  void refundPayment(@Param("paymentId") String paymentId, @Param("reason") String reason);

  @Query(
      "SELECT FUNCTION('MONTH', p.paymentDate), COALESCE(SUM(p.amount), 0) FROM Payment p "
          + "WHERE p.status = 'COMPLETED' AND YEAR(p.paymentDate) = :year "
          + "GROUP BY FUNCTION('MONTH', p.paymentDate)")
  List<Object[]> getMonthlyRevenue(@Param("year") int year);
}
