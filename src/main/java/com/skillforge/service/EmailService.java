package com.skillforge.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.support-email}")
  private String supportEmail;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMMM dd, yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

  // ==================== AUTHENTICATION EMAILS ====================

  @Async
  public void sendWelcomeEmail(String to, String fullName) {
    log.info("Sending welcome email to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("loginUrl", frontendUrl + "/login");
    variables.put("exploreUrl", frontendUrl + "/courses");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Welcome to SkillForge! 🚀", "welcome", variables);
  }

  @Async
  public void sendEmailVerification(String to, String fullName, String token) {
    log.info("Sending email verification to: {}", to);

    String verificationUrl = frontendUrl + "/verify-email?token=" + token;

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("verificationUrl", verificationUrl);
    variables.put("supportEmail", supportEmail);
    variables.put("expiryHours", 24);

    sendHtmlEmail(to, "Verify Your Email Address", "email-verification", variables);
  }

  @Async
  public void sendPasswordResetEmail(String to, String fullName, String token) {
    log.info("Sending password reset email to: {}", to);

    String resetUrl = frontendUrl + "/reset-password?token=" + token;

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("resetUrl", resetUrl);
    variables.put("supportEmail", supportEmail);
    variables.put("expiryHours", 1);

    sendHtmlEmail(to, "Reset Your Password", "password-reset", variables);
  }

  @Async
  public void sendPasswordChangedNotification(String to, String fullName) {
    log.info("Sending password changed notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("loginUrl", frontendUrl + "/login");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Your Password Has Been Changed", "password-changed", variables);
  }

  @Async
  public void sendAccountLockedNotification(String to, String fullName, int failedAttempts) {
    log.info("Sending account locked notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("failedAttempts", failedAttempts);
    variables.put("unlockUrl", frontendUrl + "/unlock-account");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Your Account Has Been Locked", "account-locked", variables);
  }

  // ==================== COURSE RELATED EMAILS ====================

  @Async
  public void sendCourseCreationConfirmation(String to, String courseTitle) {
    log.info("Sending course creation confirmation to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("coursesUrl", frontendUrl + "/instructor/courses");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Course Created: " + courseTitle, "course-created", variables);
  }

  @Async
  public void sendCoursePublishedNotification(String to, String courseTitle) {
    log.info("Sending course published notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("courseUrl", frontendUrl + "/courses");
    variables.put("dashboardUrl", frontendUrl + "/instructor/dashboard");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Your Course is Now Live! 🎉", "course-published", variables);
  }

  @Async
  public void sendCourseDeletionNotification(String to, String courseTitle) {
    log.info("Sending course deletion notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Course Deleted: " + courseTitle, "course-deleted", variables);
  }

  @Async
  public void sendCourseUpdateNotification(
      String to, String fullName, String courseTitle, String updateDetails) {
    log.info("Sending course update notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("updateDetails", updateDetails);
    variables.put("courseUrl", frontendUrl + "/courses");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Course Updated: " + courseTitle, "course-updated", variables);
  }

  // ==================== ENROLLMENT EMAILS ====================

  @Async
  public void sendEnrollmentConfirmation(String to, String courseTitle) {
    log.info("Sending enrollment confirmation to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("myLearningUrl", frontendUrl + "/my-learning");
    variables.put("courseUrl", frontendUrl + "/courses");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "You're Enrolled in " + courseTitle, "enrollment-confirmation", variables);
  }

  @Async
  public void sendEnrollmentWithPaymentConfirmation(
      String to, String courseTitle, BigDecimal amount, String transactionId, String invoiceUrl) {
    log.info("Sending enrollment with payment confirmation to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("amount", amount);
    variables.put("transactionId", transactionId);
    variables.put("invoiceUrl", invoiceUrl);
    variables.put("myLearningUrl", frontendUrl + "/my-learning");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to, "Payment Confirmed: " + courseTitle, "enrollment-payment-confirmation", variables);
  }

  @Async
  public void sendCourseAccessReminder(
      String to, String fullName, String courseTitle, int daysRemaining) {
    log.info("Sending course access reminder to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("daysRemaining", daysRemaining);
    variables.put("continueUrl", frontendUrl + "/my-learning");
    variables.put("supportEmail", supportEmail);

    String subject =
        daysRemaining <= 3
            ? "⚠️ Urgent: Your course access expires soon!"
            : "Don't lose access to " + courseTitle;

    sendHtmlEmail(to, subject, "course-access-reminder", variables);
  }

  @Async
  public void sendCourseAccessExpired(String to, String fullName, String courseTitle) {
    log.info("Sending course access expired notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("renewUrl", frontendUrl + "/courses/" + courseTitle);
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Your Course Access Has Expired", "course-access-expired", variables);
  }

  @Async
  public void sendCourseDropNotification(String to, String courseTitle, String reason) {
    log.info("Sending course drop notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("reason", reason != null ? reason : "Not specified");
    variables.put("feedbackUrl", frontendUrl + "/feedback");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Course Dropped: " + courseTitle, "course-dropped", variables);
  }

  // ==================== CERTIFICATE EMAILS ====================

  @Async
  public void sendCertificateIssuedEmail(
      String to, String fullName, String courseTitle, String certificateUrl) {
    log.info("Sending certificate issued email to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("certificateUrl", certificateUrl);
    variables.put("shareUrl", frontendUrl + "/share-certificate");
    variables.put(
        "linkedInUrl", "https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to,
        "🎓 Congratulations! You've Earned a Certificate for " + courseTitle,
        "certificate-issued",
        variables);
  }

  @Async
  public void sendCertificateExpiringNotification(
      String to, String fullName, String courseTitle, int daysUntilExpiry, String certificateUrl) {
    log.info("Sending certificate expiring notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("daysUntilExpiry", daysUntilExpiry);
    variables.put("certificateUrl", certificateUrl);
    variables.put("renewalUrl", frontendUrl + "/renew-certificate");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Your Certificate Will Expire Soon", "certificate-expiring", variables);
  }

  // ==================== PAYMENT EMAILS ====================

  @Async
  public void sendPaymentReceipt(
      String to,
      String fullName,
      String courseTitle,
      BigDecimal amount,
      String transactionId,
      String invoiceUrl) {
    log.info("Sending payment receipt to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("amount", amount);
    variables.put("transactionId", transactionId);
    variables.put("invoiceUrl", invoiceUrl);
    variables.put("supportEmail", supportEmail);
    variables.put("date", LocalDateTime.now().format(DATE_FORMATTER));

    sendHtmlEmail(to, "Your Payment Receipt for " + courseTitle, "payment-receipt", variables);
  }

  @Async
  public void sendPaymentFailedNotification(
      String to, String fullName, String courseTitle, BigDecimal amount, String failureReason) {
    log.info("Sending payment failed notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("amount", amount);
    variables.put("failureReason", failureReason);
    variables.put("retryUrl", frontendUrl + "/payment/retry");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Payment Failed for " + courseTitle, "payment-failed", variables);
  }

  @Async
  public void sendRefundProcessedEmail(
      String to, String fullName, String courseTitle, BigDecimal refundAmount, String reason) {
    log.info("Sending refund processed email to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("refundAmount", refundAmount);
    variables.put("reason", reason);
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Refund Processed for " + courseTitle, "refund-processed", variables);
  }

  // ==================== INSTRUCTOR EMAILS ====================

  @Async
  public void sendNewEnrollmentNotification(
      String to, String courseTitle, String studentName, String studentEmail) {
    log.info("Sending new enrollment notification to instructor: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("studentName", studentName);
    variables.put("studentEmail", studentEmail);
    variables.put("dashboardUrl", frontendUrl + "/instructor/dashboard");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to, "New Student Enrolled in " + courseTitle, "new-enrollment-instructor", variables);
  }

  @Async
  public void sendNewReviewNotification(
      String to, String courseTitle, String reviewerName, int rating, String comment) {
    log.info("Sending new review notification to instructor: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("courseTitle", courseTitle);
    variables.put("reviewerName", reviewerName);
    variables.put("rating", rating);
    variables.put("comment", comment);
    variables.put("reviewUrl", frontendUrl + "/instructor/reviews");
    variables.put("replyUrl", frontendUrl + "/instructor/reviews/reply");
    variables.put("supportEmail", supportEmail);

    String ratingStars = "★".repeat(rating) + "☆".repeat(5 - rating);
    variables.put("ratingStars", ratingStars);

    sendHtmlEmail(to, "New Review for " + courseTitle, "new-review", variables);
  }

  @Async
  public void sendWeeklyInstructorReport(
      String to,
      String fullName,
      int newEnrollments,
      int totalStudents,
      BigDecimal revenue,
      double averageRating,
      int totalReviews) {
    log.info("Sending weekly instructor report to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("newEnrollments", newEnrollments);
    variables.put("totalStudents", totalStudents);
    variables.put("revenue", revenue);
    variables.put("averageRating", averageRating);
    variables.put("totalReviews", totalReviews);
    variables.put("dashboardUrl", frontendUrl + "/instructor/dashboard");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to, "Your Weekly Course Performance Report", "weekly-instructor-report", variables);
  }

  @Async
  public void sendInstructorPayoutNotification(
      String to, String fullName, BigDecimal amount, String payoutMethod, String transactionId) {
    log.info("Sending instructor payout notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("amount", amount);
    variables.put("payoutMethod", payoutMethod);
    variables.put("transactionId", transactionId);
    variables.put("earningsUrl", frontendUrl + "/instructor/earnings");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to, "Payment Sent: $" + amount + " Has Been Deposited", "instructor-payout", variables);
  }

  // ==================== PROMOTIONAL & REMINDER EMAILS ====================

  @Async
  public void sendWishlistCoursePublishedNotification(
      String to,
      String fullName,
      String courseTitle,
      BigDecimal price,
      BigDecimal discountedPrice) {
    log.info("Sending wishlist course published notification to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("price", price);
    variables.put("discountedPrice", discountedPrice);
    variables.put("courseUrl", frontendUrl + "/courses");
    variables.put("enrollUrl", frontendUrl + "/enroll");
    variables.put("supportEmail", supportEmail);

    String subject =
        discountedPrice != null && discountedPrice.compareTo(price) < 0
            ? "🎉 " + courseTitle + " is now available with a special launch discount!"
            : courseTitle + " is now available on SkillForge!";

    sendHtmlEmail(to, subject, "wishlist-course-published", variables);
  }

  @Async
  public void sendPriceDropAlert(
      String to,
      String fullName,
      String courseTitle,
      BigDecimal oldPrice,
      BigDecimal newPrice,
      String discountEndDate) {
    log.info("Sending price drop alert to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("oldPrice", oldPrice);
    variables.put("newPrice", newPrice);
    variables.put("savings", oldPrice.subtract(newPrice));
    variables.put("discountEndDate", discountEndDate);
    variables.put("courseUrl", frontendUrl + "/courses");
    variables.put("enrollUrl", frontendUrl + "/enroll");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to,
        "Price Drop Alert: " + courseTitle + " is Now " + newPrice,
        "price-drop-alert",
        variables);
  }

  @Async
  public void sendCourseCompletionReminder(
      String to, String fullName, String courseTitle, double progressPercentage) {
    log.info("Sending course completion reminder to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("courseTitle", courseTitle);
    variables.put("progressPercentage", progressPercentage);
    variables.put("remainingPercentage", 100 - progressPercentage);
    variables.put("continueUrl", frontendUrl + "/my-learning");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(
        to,
        "Keep Going! You're "
            + String.format("%.0f", progressPercentage)
            + "% Through "
            + courseTitle,
        "course-completion-reminder",
        variables);
  }

  @Async
  public void sendInactivityReminder(
      String to, String fullName, List<String> inactiveCourseTitles, int daysInactive) {
    log.info("Sending inactivity reminder to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("fullName", fullName);
    variables.put("inactiveCourseTitles", inactiveCourseTitles);
    variables.put("daysInactive", daysInactive);
    variables.put("resumeUrl", frontendUrl + "/my-learning");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "We Miss You! Come Back to Your Courses", "inactivity-reminder", variables);
  }

  // ==================== ADMIN EMAILS ====================

  @Async
  public void sendAdminUserReport(
      String to, long newUsers, long newEnrollments, BigDecimal totalRevenue, int activeCourses) {
    log.info("Sending admin user report to: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("newUsers", newUsers);
    variables.put("newEnrollments", newEnrollments);
    variables.put("totalRevenue", totalRevenue);
    variables.put("activeCourses", activeCourses);
    variables.put("adminUrl", frontendUrl + "/admin/dashboard");
    variables.put("supportEmail", supportEmail);

    sendHtmlEmail(to, "Weekly Platform Report", "admin-weekly-report", variables);
  }

  @Async
  public void sendSystemAlert(String to, String alertType, String message, String severity) {
    log.info("Sending system alert to admin: {}", to);

    Map<String, Object> variables = new HashMap<>();
    variables.put("alertType", alertType);
    variables.put("message", message);
    variables.put("severity", severity);
    variables.put("severityColor", getSeverityColor(severity));
    variables.put("dashboardUrl", frontendUrl + "/admin/system");

    sendHtmlEmail(to, "⚠️ System Alert: " + alertType, "system-alert", variables);
  }

  // ==================== PRIVATE HELPER METHODS ====================

  @Async
  protected void sendHtmlEmail(
      String to, String subject, String templateName, Map<String, Object> variables) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);

      Context context = new Context();
      context.setVariables(variables);
      context.setVariable("year", LocalDateTime.now().getYear());
      context.setVariable("frontendUrl", frontendUrl);
      context.setVariable("currentYear", LocalDateTime.now().getYear());

      String htmlContent = templateEngine.process(templateName, context);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("Email sent successfully to: {} with subject: {}", to, subject);

    } catch (MessagingException e) {
      log.error("Failed to send email to: {} with subject: {}", to, subject, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  @Async
  public void sendSimpleEmail(String to, String subject, String text) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(text);

      mailSender.send(message);
      log.info("Simple email sent successfully to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send simple email to: {}", to, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  private String getSeverityColor(String severity) {
    switch (severity.toUpperCase()) {
      case "CRITICAL":
        return "#FF0000";
      case "HIGH":
        return "#FF6600";
      case "MEDIUM":
        return "#FFCC00";
      case "LOW":
        return "#00CC00";
      default:
        return "#000000";
    }
  }

  // Generate unique token for email verification
  public String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }
}
