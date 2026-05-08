package com.skillforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_user", columnList = "user_id"),
      @Index(name = "idx_payment_course", columnList = "course_id"),
      @Index(name = "idx_payment_transaction", columnList = "transaction_id"),
      @Index(name = "idx_payment_status", columnList = "status"),
      @Index(name = "idx_payment_date", columnList = "payment_date")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  @Builder.Default
  private String currency = "USD";

  @Column(name = "payment_method", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(name = "transaction_id", unique = true)
  private String transactionId;

  @Column(name = "stripe_payment_intent_id")
  private String stripePaymentIntentId;

  @Column(name = "paypal_order_id")
  private String paypalOrderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Column(name = "refund_date")
  private LocalDateTime refundDate;

  @Column(name = "refund_reason", length = 500)
  private String refundReason;

  @Column(name = "refund_amount", precision = 10, scale = 2)
  private BigDecimal refundAmount;

  @Column(name = "invoice_url", length = 500)
  private String invoiceUrl;

  @Column(name = "receipt_url", length = 500)
  private String receiptUrl;

  @Column(name = "platform_fee", precision = 10, scale = 2)
  private BigDecimal platformFee;

  @Column(name = "instructor_earnings", precision = 10, scale = 2)
  private BigDecimal instructorEarnings;

  @Column(name = "tax_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "discount_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "coupon_code")
  private String couponCode;

  @Column(name = "payment_description", length = 500)
  private String paymentDescription;

  @Column(name = "billing_address", length = 500)
  private String billingAddress;

  @Column(name = "billing_city")
  private String billingCity;

  @Column(name = "billing_country")
  private String billingCountry;

  @Column(name = "billing_postal_code")
  private String billingPostalCode;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @Column(name = "failure_code")
  private String failureCode;

  @Column(name = "retry_count")
  @Builder.Default
  private Integer retryCount = 0;

  @Column(name = "metadata", length = 2000)
  private String metadata; // JSON string for additional data

  @Column(name = "webhook_received")
  @Builder.Default
  private boolean webhookReceived = false;

  @Column(name = "webhook_processed_at")
  private LocalDateTime webhookProcessedAt;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Enums
  public enum PaymentMethod {
    STRIPE,
    PAYPAL,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    RENEWAL,
    FREE,
    MANUAL
  }

  public enum PaymentStatus {
    PENDING("Payment initiated but not completed"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment successfully completed"),
    FAILED("Payment failed"),
    REFUNDED("Payment has been refunded"),
    PARTIALLY_REFUNDED("Partially refunded"),
    DISPUTED("Payment is under dispute"),
    CANCELLED("Payment was cancelled"),
    EXPIRED("Payment session expired");

    private final String description;

    PaymentStatus(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public boolean isCompleted() {
      return this == COMPLETED;
    }

    public boolean isFailed() {
      return this == FAILED;
    }

    public boolean isRefunded() {
      return this == REFUNDED || this == PARTIALLY_REFUNDED;
    }
  }

  // Helper methods
  public boolean isSuccessful() {
    return status == PaymentStatus.COMPLETED;
  }

  public boolean isRefundable() {
    return status == PaymentStatus.COMPLETED
        && refundDate == null
        && paymentDate != null
        && paymentDate.isAfter(LocalDateTime.now().minusDays(30));
  }

  public BigDecimal getNetAmount() {
    if (amount == null) return BigDecimal.ZERO;
    BigDecimal netAmount = amount;
    if (taxAmount != null) {
      netAmount = netAmount.subtract(taxAmount);
    }
    if (discountAmount != null) {
      netAmount = netAmount.add(discountAmount);
    }
    return netAmount;
  }

  public BigDecimal getPlatformFeePercentage() {
    // 30% platform fee for non-free courses
    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
      return new BigDecimal("0.30");
    }
    return BigDecimal.ZERO;
  }

  public void calculateEarnings() {
    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
      this.platformFee = amount.multiply(getPlatformFeePercentage());
      this.instructorEarnings = amount.subtract(platformFee).subtract(taxAmount);
    } else {
      this.platformFee = BigDecimal.ZERO;
      this.instructorEarnings = BigDecimal.ZERO;
    }
  }

  public void markAsCompleted() {
    this.status = PaymentStatus.COMPLETED;
    this.paymentDate = LocalDateTime.now();
    calculateEarnings();
  }

  public void markAsFailed(String reason, String failureCode) {
    this.status = PaymentStatus.FAILED;
    this.failureReason = reason;
    this.failureCode = failureCode;
  }

  public void markAsRefunded(String reason, BigDecimal refundAmount) {
    this.status = PaymentStatus.REFUNDED;
    this.refundDate = LocalDateTime.now();
    this.refundReason = reason;
    this.refundAmount = refundAmount;
  }

  public void incrementRetryCount() {
    this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
  }

  public boolean canRetry() {
    return status == PaymentStatus.FAILED && retryCount != null && retryCount < 3;
  }

  public String getFormattedAmount() {
    return String.format("%s %.2f", currency, amount);
  }

  public String getMaskedTransactionId() {
    if (transactionId == null) return null;
    if (transactionId.length() <= 8) return "****";
    return "****" + transactionId.substring(transactionId.length() - 4);
  }
}
