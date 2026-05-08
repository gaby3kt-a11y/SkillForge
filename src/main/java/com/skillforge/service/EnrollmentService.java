package com.skillforge.service;

import com.skillforge.dto.*;
import com.skillforge.exception.BusinessException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;
  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final LessonProgressRepository lessonProgressRepository;
  private final LessonRepository lessonRepository;
  private final PaymentRepository paymentRepository;
  private final WishlistRepository wishlistRepository;
  private final CertificateRepository certificateRepository;
  private final EmailService emailService;

  @Value("${app.max-courses-per-user}")
  private int maxCoursesPerUser;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.course-access-days:180}")
  private int courseAccessDays;

  /** Enroll a student in a course */
  @Transactional
  @CacheEvict(
      value = {"enrollments", "courses"},
      allEntries = true)
  public EnrollmentDTO enrollStudent(String userId, EnrollmentRequestDTO request) {
    log.info("Enrolling user {} in course {}", userId, request.getCourseId());

    // Validate user
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

    // Validate course
    Course course =
        courseRepository
            .findByIdAndIsDeletedFalse(request.getCourseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with ID: " + request.getCourseId()));

    // Validate terms acceptance
    if (!request.getAcceptTerms()) {
      throw new BusinessException("You must accept the terms and conditions to enroll");
    }

    // Business validations
    validateEnrollment(user, course);

    // Process payment if course is not free
    Payment payment = null;
    if (course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
      payment =
          processPayment(user, course, request.getPaymentMethod(), request.getPromotionCode());
    }

    // Create enrollment
    Enrollment enrollment =
        Enrollment.builder()
            .user(user)
            .course(course)
            .enrollmentDate(LocalDateTime.now())
            .status(Enrollment.EnrollmentStatus.ACTIVE)
            .progressPercentage(BigDecimal.ZERO)
            .expiresAt(calculateExpiryDate(course))
            .build();

    Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

    // Initialize lesson progress for all lessons
    initializeLessonProgress(savedEnrollment);

    // Update course statistics
    courseRepository.incrementEnrollments(course.getId());

    // Remove from wishlist if present
    wishlistRepository
        .findByUserIdAndCourseId(userId, course.getId())
        .ifPresent(wishlistRepository::delete);

    // Send confirmation email asynchronously
    if (payment != null) {
      emailService.sendEnrollmentWithPaymentConfirmation(
          user.getEmail(),
          course.getTitle(),
          payment.getAmount(),
          payment.getTransactionId(),
          payment.getInvoiceUrl());
    } else {
      emailService.sendEnrollmentConfirmation(user.getEmail(), course.getTitle());
    }

    // Notify instructor
    emailService.sendNewEnrollmentNotification(
        course.getInstructor().getEmail(), course.getTitle(), user.getFullName(), user.getEmail());

    log.info("Successfully enrolled user {} in course {}", userId, course.getId());
    return convertToDTO(savedEnrollment);
  }

  /** Get all enrollments for a user with pagination */
  @Cacheable(value = "enrollments", key = "#userId + '_' + #pageable.pageNumber")
  public Page<EnrollmentDTO> getUserEnrollments(String userId, Pageable pageable) {
    log.debug("Fetching enrollments for user: {}", userId);

    userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Page<Enrollment> enrollments = enrollmentRepository.findByUserId(userId, pageable);
    return enrollments.map(this::convertToDTO);
  }

  /** Get active enrollments for a user */
  public List<EnrollmentDTO> getActiveUserEnrollments(String userId) {
    log.debug("Fetching active enrollments for user: {}", userId);

    List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByUser(userId);
    return enrollments.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /** Get enrollment by ID */
  public EnrollmentDTO getEnrollmentById(String enrollmentId, String userId) {
    log.debug("Fetching enrollment: {} for user: {}", enrollmentId, userId);

    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    // Verify ownership
    if (!enrollment.getUser().getId().equals(userId)) {
      throw new BusinessException("You don't have permission to view this enrollment");
    }

    return convertToDTO(enrollment);
  }

  /** Get all students enrolled in a course (for instructors) */
  public Page<EnrollmentDTO> getCourseEnrollments(
      String courseId, String instructorId, Pageable pageable) {
    log.debug("Fetching enrollments for course: {} by instructor: {}", courseId, instructorId);

    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    // Verify instructor owns the course
    if (!course.getInstructor().getId().equals(instructorId)) {
      throw new BusinessException("You don't have permission to view enrollments for this course");
    }

    Page<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId, pageable);
    return enrollments.map(this::convertToDTO);
  }

  /** Update lesson progress */
  @Transactional
  public LessonProgressDTO updateLessonProgress(
      String enrollmentId, String lessonId, Integer watchTimeSeconds, Integer lastPosition) {
    log.debug("Updating progress for enrollment: {}, lesson: {}", enrollmentId, lessonId);

    LessonProgress progress =
        lessonProgressRepository
            .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson progress not found"));

    // Update watch time and position
    if (watchTimeSeconds != null) {
      progress.setWatchTimeSeconds(watchTimeSeconds);
    }
    if (lastPosition != null) {
      progress.setLastPositionSeconds(lastPosition);
    }
    progress.setLastAccessedAt(LocalDateTime.now());

    lessonProgressRepository.save(progress);

    // Update overall enrollment progress
    updateEnrollmentProgress(enrollmentId);

    return convertToLessonProgressDTO(progress);
  }

  /** Mark a lesson as completed */
  @Transactional
  public LessonProgressDTO completeLesson(String enrollmentId, String lessonId, Integer quizScore) {
    log.info("Completing lesson {} for enrollment {}", lessonId, enrollmentId);

    LessonProgress progress =
        lessonProgressRepository
            .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson progress not found"));

    if (!progress.isCompleted()) {
      progress.complete();
      if (quizScore != null) {
        progress.setQuizScore(quizScore);
      }
      lessonProgressRepository.save(progress);

      // Update enrollment progress
      boolean completed = updateEnrollmentProgress(enrollmentId);

      // If course is completed, generate certificate
      if (completed) {
        generateCertificate(enrollmentId);
      }
    }

    return convertToLessonProgressDTO(progress);
  }

  /** Drop a course */
  @Transactional
  @CacheEvict(value = "enrollments", key = "#userId + '_*'", allEntries = true)
  public void dropCourse(String enrollmentId, String userId, String reason) {
    log.info("Dropping course enrollment: {} by user: {}", enrollmentId, userId);

    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    // Verify ownership
    if (!enrollment.getUser().getId().equals(userId)) {
      throw new BusinessException("You don't have permission to drop this enrollment");
    }

    if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
      throw new BusinessException("Cannot drop a completed or already dropped enrollment");
    }

    enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
    enrollmentRepository.save(enrollment);

    // Update course statistics
    courseRepository.decrementEnrollments(enrollment.getCourse().getId());

    // Send drop notification for analysis
    emailService.sendCourseDropNotification(
        enrollment.getUser().getEmail(), enrollment.getCourse().getTitle(), reason);

    log.info("User {} dropped course {}", userId, enrollment.getCourse().getId());
  }

  /** Get course progress for a user */
  public Double getCourseProgress(String userId, String courseId) {
    return enrollmentRepository
        .findByUserIdAndCourseId(userId, courseId)
        .map(Enrollment::getProgressPercentage)
        .orElse(BigDecimal.ZERO).doubleValue();
  }

  /** Get detailed course progress with lesson-by-lesson breakdown */
  public CourseProgressDTO getDetailedCourseProgress(String userId, String courseId) {
    log.debug("Fetching detailed progress for user: {} in course: {}", userId, courseId);

    Enrollment enrollment =
        enrollmentRepository
            .findByUserIdAndCourseId(userId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

    List<LessonProgress> lessonProgresses =
        lessonProgressRepository.findByEnrollmentId(enrollment.getId());

    Map<String, LessonProgressDTO> progressMap =
        lessonProgresses.stream()
            .collect(
                Collectors.toMap(lp -> lp.getLesson().getId(), this::convertToLessonProgressDTO));

    List<ModuleProgressDTO> moduleProgress =
        course.getModules().stream()
            .map(
                module -> {
                  List<LessonProgressDTO> lessonProgressList =
                      module.getLessons().stream()
                          .map(
                              lesson ->
                                  progressMap.getOrDefault(
                                      lesson.getId(),
                                      LessonProgressDTO.builder()
                                          .lessonId(lesson.getId())
                                          .lessonTitle(lesson.getTitle())
                                          .isCompleted(false)
                                          .build()))
                          .collect(Collectors.toList());

                  long completedCount =
                      lessonProgressList.stream().filter(LessonProgressDTO::getIsCompleted).count();

                  return ModuleProgressDTO.builder()
                      .moduleId(module.getId())
                      .moduleTitle(module.getTitle())
                      .totalLessons(module.getLessons().size())
                      .completedLessons((int) completedCount)
                      .progressPercentage((completedCount * 100.0) / module.getLessons().size())
                      .lessons(lessonProgressList)
                      .build();
                })
            .collect(Collectors.toList());

    return CourseProgressDTO.builder()
        .enrollmentId(enrollment.getId())
        .courseId(courseId)
        .courseTitle(course.getTitle())
        .totalProgress(enrollment.getProgressPercentage().doubleValue())
        .status(enrollment.getStatus())
        .enrollmentDate(enrollment.getEnrollmentDate())
        .lastAccessedAt(enrollment.getLastAccessedAt())
        .estimatedCompletionDate(estimateCompletionDate(enrollment))
        .modules(moduleProgress)
        .build();
  }

  /** Check if user is enrolled in course */
  public boolean isUserEnrolled(String userId, String courseId) {
    return enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
        userId, courseId, Enrollment.EnrollmentStatus.ACTIVE);
  }

  /** Get enrollment statistics for instructor dashboard */
  @Cacheable(value = "enrollmentStats", key = "#instructorId")
  public EnrollmentStatisticsDTO getEnrollmentStatistics(String instructorId) {
    log.debug("Fetching enrollment statistics for instructor: {}", instructorId);

    long totalStudents = enrollmentRepository.countUniqueStudentsForInstructor(instructorId);
    long totalEnrollments = courseRepository.getTotalEnrollmentsForInstructor(instructorId);
    double averageCompletionRate = calculateAverageCompletionRate(instructorId);

    // Get detailed statistics
    List<Course> instructorCourses = courseRepository.findActiveByInstructorId(instructorId);

    int totalActiveEnrollments = 0;
    int totalCompletedEnrollments = 0;
    int totalDroppedEnrollments = 0;
    BigDecimal totalRevenue = BigDecimal.ZERO;

    for (Course course : instructorCourses) {
      List<Enrollment> enrollments =
          enrollmentRepository.findByCourseId(course.getId(), Pageable.unpaged()).getContent();
      totalActiveEnrollments +=
          enrollments.stream()
              .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
              .count();
      totalCompletedEnrollments +=
          enrollments.stream()
              .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
              .count();
      totalDroppedEnrollments +=
          enrollments.stream()
              .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.DROPPED)
              .count();

      // Calculate revenue (70% to instructor, 30% platform fee)
      BigDecimal courseRevenue =
          course
              .getPrice()
              .multiply(BigDecimal.valueOf(enrollments.size()))
              .multiply(BigDecimal.valueOf(0.7));
      totalRevenue = totalRevenue.add(courseRevenue);
    }

    double dropoutRate =
        totalEnrollments > 0 ? (totalDroppedEnrollments * 100.0) / totalEnrollments : 0.0;

    return EnrollmentStatisticsDTO.builder()
        .totalStudents(totalStudents)
        .totalEnrollments(totalEnrollments)
        .averageCompletionRate(averageCompletionRate)
        .activeEnrollmentsToday(countActiveEnrollmentsToday(instructorId))
        .newEnrollmentsThisWeek(countNewEnrollmentsThisWeek(instructorId))
        .totalActiveEnrollments(totalActiveEnrollments)
        .totalCompletedEnrollments(totalCompletedEnrollments)
        .totalDroppedEnrollments(totalDroppedEnrollments)
        .totalRevenue(totalRevenue)
        .averageRevenuePerStudent(
            totalStudents > 0
                ? totalRevenue.divide(
                    BigDecimal.valueOf(totalStudents), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO)
        .dropoutRate(dropoutRate)
        .build();
  }

  /** Renew an expired enrollment */
  @Transactional
  public EnrollmentDTO renewEnrollment(String enrollmentId, String userId) {
    log.info("Renewing enrollment: {} for user: {}", enrollmentId, userId);

    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    if (!enrollment.getUser().getId().equals(userId)) {
      throw new BusinessException("You don't have permission to renew this enrollment");
    }

    if (enrollment.getStatus() != Enrollment.EnrollmentStatus.EXPIRED) {
      throw new BusinessException("Only expired enrollments can be renewed");
    }

    // Process renewal payment if course is not free
    if (enrollment.getCourse().getPrice().compareTo(BigDecimal.ZERO) > 0) {
      processRenewalPayment(enrollment);
    }

    // Extend expiry date
    enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
    enrollment.setExpiresAt(calculateExpiryDate(enrollment.getCourse()));
    enrollment.setUpdatedAt(LocalDateTime.now());

    Enrollment renewedEnrollment = enrollmentRepository.save(enrollment);

    emailService.sendEnrollmentConfirmation(
        enrollment.getUser().getEmail(), enrollment.getCourse().getTitle() + " (Renewed)");

    return convertToDTO(renewedEnrollment);
  }

  /** Process expired enrollments (scheduled job) */
  @Transactional
  public void processExpiredEnrollments() {
    log.info("Processing expired enrollments");

    List<Enrollment> expiredEnrollments = enrollmentRepository.findExpiredEnrollments();

    if (!expiredEnrollments.isEmpty()) {
      List<String> expiredIds =
          expiredEnrollments.stream().map(Enrollment::getId).collect(Collectors.toList());

      enrollmentRepository.markAsExpired(expiredIds);

      // Send expiration notifications
      for (Enrollment enrollment : expiredEnrollments) {
        emailService.sendCourseAccessExpired(
            enrollment.getUser().getEmail(),
            enrollment.getUser().getFullName(),
            enrollment.getCourse().getTitle());
      }

      log.info("Processed {} expired enrollments", expiredEnrollments.size());
    }
  }

  /** Send course completion reminders (scheduled job) */
  @Transactional
  public void sendCompletionReminders() {
    log.info("Sending course completion reminders");

    List<Enrollment> activeEnrollments = enrollmentRepository.findActiveEnrollments();

    for (Enrollment enrollment : activeEnrollments) {
      if (enrollment.getProgressPercentage().doubleValue() > 50 && enrollment.getProgressPercentage().doubleValue() < 90) {
        LocalDateTime lastAccess = enrollment.getLastAccessedAt();
        if (lastAccess != null && ChronoUnit.DAYS.between(lastAccess, LocalDateTime.now()) > 7) {
          emailService.sendCourseCompletionReminder(
              enrollment.getUser().getEmail(),
              enrollment.getUser().getFullName(),
              enrollment.getCourse().getTitle(),
              enrollment.getProgressPercentage().doubleValue());
        }
      }
    }
  }

  // ==================== PRIVATE HELPER METHODS ====================

  private void validateEnrollment(User user, Course course) {
    // Check if course is published
    if (!course.isPublished()) {
      throw new BusinessException("Course is not published yet");
    }

    // Check if course is deleted
    if (course.isDeleted()) {
      throw new BusinessException("Course is no longer available");
    }

    // Check enrollment limit
    long currentEnrollments = enrollmentRepository.countActiveEnrollmentsByUser(user.getId());
    if (currentEnrollments >= maxCoursesPerUser) {
      throw new BusinessException("Maximum course enrollment limit reached: " + maxCoursesPerUser);
    }

    // Check if already enrolled
    boolean alreadyEnrolled =
        enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
            user.getId(), course.getId(), Enrollment.EnrollmentStatus.ACTIVE);

    if (alreadyEnrolled) {
      throw new BusinessException("You are already enrolled in this course");
    }

    // Check if user is the instructor (can't enroll in own course)
    if (course.getInstructor().getId().equals(user.getId())) {
      throw new BusinessException("You cannot enroll in your own course");
    }

    // Check if user has a dropped enrollment that hasn't expired
    boolean hasDropped =
        enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
            user.getId(), course.getId(), Enrollment.EnrollmentStatus.DROPPED);

    if (hasDropped) {
      throw new BusinessException(
          "You previously dropped this course. Please contact support to re-enroll.");
    }
  }

  private Payment processPayment(
      User user, Course course, String paymentMethod, String promotionCode) {
    BigDecimal finalAmount = course.getPrice();

    // Apply promotion code if provided
    if (promotionCode != null && !promotionCode.isEmpty()) {
      BigDecimal discount = applyPromotionCode(promotionCode, course.getPrice());
      finalAmount = finalAmount.subtract(discount);
    }

    Payment payment =
        Payment.builder()
            .user(user)
            .course(course)
            .amount(finalAmount)
            .currency("USD")
            .paymentMethod(mapPaymentMethod(paymentMethod))
            .transactionId(generateTransactionId())
            .status(Payment.PaymentStatus.COMPLETED)
            .paymentDate(LocalDateTime.now())
            .invoiceUrl(generateInvoiceUrl(user, course, finalAmount))
            .build();

    Payment savedPayment = paymentRepository.save(payment);

    // Integrate with actual payment gateway here
    // processWithStripe(paymentMethod, finalAmount, user);

    return savedPayment;
  }

  private void processRenewalPayment(Enrollment enrollment) {
    Payment payment =
        Payment.builder()
            .user(enrollment.getUser())
            .course(enrollment.getCourse())
            .amount(enrollment.getCourse().getPrice())
            .currency("USD")
            .paymentMethod(Payment.PaymentMethod.RENEWAL)
            .transactionId(generateTransactionId())
            .status(Payment.PaymentStatus.COMPLETED)
            .paymentDate(LocalDateTime.now())
            .invoiceUrl(
                generateInvoiceUrl(
                    enrollment.getUser(),
                    enrollment.getCourse(),
                    enrollment.getCourse().getPrice()))
            .build();

    paymentRepository.save(payment);
  }

  private Payment.PaymentMethod mapPaymentMethod(String paymentMethod) {
    if (paymentMethod == null || paymentMethod.isBlank()) {
      return Payment.PaymentMethod.MANUAL;
    }

    return switch (paymentMethod.toUpperCase()) {
      case "STRIPE" -> Payment.PaymentMethod.STRIPE;
      case "PAYPAL" -> Payment.PaymentMethod.PAYPAL;
      case "CARD" -> Payment.PaymentMethod.CREDIT_CARD;
      case "FREE" -> Payment.PaymentMethod.FREE;
      default -> Payment.PaymentMethod.MANUAL;
    };
  }

  private void initializeLessonProgress(Enrollment enrollment) {
    List<Lesson> lessons = lessonRepository.findAllLessonsByCourse(enrollment.getCourse().getId());

    for (Lesson lesson : lessons) {
      LessonProgress progress =
          LessonProgress.builder()
              .enrollment(enrollment)
              .lesson(lesson)
              .isCompleted(false)
              .watchTimeSeconds(0)
              .lastPositionSeconds(0)
              .build();

      lessonProgressRepository.save(progress);
    }

    log.info(
        "Initialized {} lesson progress records for enrollment {}",
        lessons.size(),
        enrollment.getId());
  }

  private boolean updateEnrollmentProgress(String enrollmentId) {
    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    List<LessonProgress> progresses = lessonProgressRepository.findByEnrollmentId(enrollmentId);

    if (progresses.isEmpty()) {
      return false;
    }

    long completedCount = progresses.stream().filter(LessonProgress::isCompleted).count();

    double newProgress = (completedCount * 100.0) / progresses.size();
    enrollment.setProgressPercentage(BigDecimal.valueOf(newProgress));
    enrollment.setLastAccessedAt(LocalDateTime.now());

    boolean isCompleted = newProgress >= 100.0;
    if (isCompleted && enrollment.getStatus() != Enrollment.EnrollmentStatus.COMPLETED) {
      enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
      enrollment.setCompletionDate(LocalDateTime.now());
    }

    enrollmentRepository.save(enrollment);

    return isCompleted;
  }

  private void generateCertificate(String enrollmentId) {
    Enrollment enrollment =
        enrollmentRepository
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

    // Check if certificate already exists
    if (certificateRepository.findByEnrollmentId(enrollmentId).isPresent()) {
      log.info("Certificate already exists for enrollment: {}", enrollmentId);
      return;
    }

    String certificateNumber = generateCertificateNumber();
    String certificateUrl = generateCertificatePdf(enrollment, certificateNumber);
    String verificationUrl = frontendUrl + "/verify-certificate/" + certificateNumber;

    Certificate certificate =
        Certificate.builder()
            .certificateNumber(certificateNumber)
            .user(enrollment.getUser())
            .course(enrollment.getCourse())
            .enrollment(enrollment)
            .issueDate(LocalDateTime.now())
            .expiryDate(calculateCertificateExpiryDate())
            .pdfUrl(certificateUrl)
            .verificationUrl(verificationUrl)
            .isVerified(true)
            .build();

    certificateRepository.save(certificate);

    // Send certificate email
    emailService.sendCertificateIssuedEmail(
        enrollment.getUser().getEmail(),
        enrollment.getUser().getFullName(),
        enrollment.getCourse().getTitle(),
        certificateUrl);

    log.info("Generated certificate for enrollment: {}", enrollmentId);
  }

  private LocalDateTime calculateExpiryDate(Course course) {
    // Courses expire after courseAccessDays days
    return LocalDateTime.now().plusDays(courseAccessDays);
  }

  private LocalDateTime calculateCertificateExpiryDate() {
    // Certificates are valid for 1 year
    return LocalDateTime.now().plusYears(1);
  }

  private double calculateAverageCompletionRate(String instructorId) {
    List<Enrollment> allEnrollments = enrollmentRepository.findByCourseInstructorId(instructorId);

    if (allEnrollments.isEmpty()) {
      return 0.0;
    }

    long completedCount =
        allEnrollments.stream()
            .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
            .count();

    return (completedCount * 100.0) / allEnrollments.size();
  }

  private long countActiveEnrollmentsToday(String instructorId) {
    LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    return enrollmentRepository.countByCourseInstructorIdAndEnrollmentDateBetween(
        instructorId, startOfDay, endOfDay);
  }

  private long countNewEnrollmentsThisWeek(String instructorId) {
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

    return enrollmentRepository.countByCourseInstructorIdAndEnrollmentDateAfter(
        instructorId, oneWeekAgo);
  }

  private LocalDateTime estimateCompletionDate(Enrollment enrollment) {
    if (enrollment.getProgressPercentage().doubleValue() < 5) {
      return null;
    }

    LocalDateTime startDate = enrollment.getEnrollmentDate();
    LocalDateTime lastAccess =
        enrollment.getLastAccessedAt() != null ? enrollment.getLastAccessedAt() : startDate;

    long daysSinceStart = ChronoUnit.DAYS.between(startDate, lastAccess);
    double progressPerDay = enrollment.getProgressPercentage().doubleValue() / Math.max(1, daysSinceStart);

    if (progressPerDay <= 0) {
      return null;
    }

    double remainingProgress = 100.0 - enrollment.getProgressPercentage().doubleValue();
    long remainingDays = (long) Math.ceil(remainingProgress / progressPerDay);

    return lastAccess.plusDays(remainingDays);
  }

  private BigDecimal applyPromotionCode(String code, BigDecimal originalPrice) {
    // Implement promotion code logic here
    // This could query a promotions table
    if ("WELCOME20".equalsIgnoreCase(code)) {
      return originalPrice.multiply(BigDecimal.valueOf(0.2)); // 20% off
    }
    return BigDecimal.ZERO;
  }

  private String generateTransactionId() {
    return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
  }

  private String generateInvoiceUrl(User user, Course course, BigDecimal amount) {
    return frontendUrl + "/invoices/" + UUID.randomUUID().toString();
  }

  private String generateCertificateNumber() {
    return "CERT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  private String generateCertificatePdf(Enrollment enrollment, String certificateNumber) {
    // Implement PDF generation logic here
    // For now, return a URL
    return frontendUrl + "/certificates/" + certificateNumber;
  }

  private EnrollmentDTO convertToDTO(Enrollment enrollment) {
    return EnrollmentDTO.builder()
        .id(enrollment.getId())
        .userId(enrollment.getUser().getId())
        .userName(enrollment.getUser().getFullName())
        .userEmail(enrollment.getUser().getEmail())
        .courseId(enrollment.getCourse().getId())
        .courseTitle(enrollment.getCourse().getTitle())
        .courseThumbnailUrl(enrollment.getCourse().getThumbnailUrl())
        .instructorName(enrollment.getCourse().getInstructor().getFullName())
        .enrollmentDate(enrollment.getEnrollmentDate())
        .completionDate(enrollment.getCompletionDate())
        .lastAccessedAt(enrollment.getLastAccessedAt())
        .progressPercentage(enrollment.getProgressPercentage().doubleValue())
        .status(enrollment.getStatus())
        .expiresAt(enrollment.getExpiresAt())
        .certificateUrl(generateCertificateUrl(enrollment))
        .build();
  }

  private LessonProgressDTO convertToLessonProgressDTO(LessonProgress progress) {
    return LessonProgressDTO.builder()
        .id(progress.getId())
        .enrollmentId(progress.getEnrollment().getId())
        .lessonId(progress.getLesson().getId())
        .lessonTitle(progress.getLesson().getTitle())
        .lessonOrderIndex(progress.getLesson().getOrderIndex())
        .moduleTitle(progress.getLesson().getModule().getTitle())
        .isCompleted(progress.isCompleted())
        .completedAt(progress.getCompletedAt())
        .watchTimeSeconds(progress.getWatchTimeSeconds())
        .lastPositionSeconds(progress.getLastPositionSeconds())
        .quizScore(progress.getQuizScore())
        .durationMinutes(progress.getLesson().getDurationMinutes())
        .lastAccessedAt(progress.getLastAccessedAt())
        .build();
  }

  private String generateCertificateUrl(Enrollment enrollment) {
    if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
      return certificateRepository
          .findByEnrollmentId(enrollment.getId())
          .map(Certificate::getPdfUrl)
          .orElse(null);
    }
    return null;
  }
}
