package com.skillforge.service;

import com.skillforge.dto.*;
import com.skillforge.exception.BusinessException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.mapper.CourseMapper;
import com.skillforge.model.Course;
import com.skillforge.model.Enrollment;
import com.skillforge.model.User;
import com.skillforge.model.Wishlist;
import com.skillforge.repository.CourseRepository;
import com.skillforge.repository.EnrollmentRepository;
import com.skillforge.repository.UserRepository;
import com.skillforge.repository.WishlistRepository;
import com.skillforge.specification.CourseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WishlistRepository wishlistRepository;
    private final CourseMapper courseMapper;
    private final EmailService emailService;
    private final ReviewService reviewService;

    /**
     * Get all published courses with filtering and pagination
     */
    @Cacheable(value = "courses", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #filters.hashCode()")
    public Page<CourseListDTO> getAllPublishedCourses(Pageable pageable, CourseFilterDTO filters) {
        log.debug("Fetching all published courses with filters: {}", filters);

        var specification = CourseSpecification.withFilters(filters);
        Page<Course> courses = courseRepository.findAll(specification, pageable);

        return courses.map(course -> {
            CourseListDTO dto = courseMapper.toListDTO(course);

            // Add additional computed fields for list view
            dto.setFormattedPrice(formatPrice(course.getPrice()));
            dto.setRatingStars(generateRatingStars(course.getAverageRating()));

            return dto;
        });
    }

    /**
     * Get detailed course information by ID
     */
    @Cacheable(value = "courses", key = "#courseId + '_' + (#userId != null ? #userId : 'anonymous')")
    public CourseResponseDTO getCourseById(String courseId, String userId) {
        log.debug("Fetching course details for ID: {} for user: {}", courseId, userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.isPublished() && !isAuthorizedToViewUnpublished(userId, course)) {
            throw new AccessDeniedException("You don't have permission to view this unpublished course");
        }

        CourseResponseDTO dto = courseMapper.toResponseDTO(course);

        // Add user-specific information if authenticated
        if (userId != null) {
            enrichWithUserSpecificData(dto, userId, courseId);
        }

        // Add computed statistics
        dto.setTotalStudents(course.getStudents().size());
        dto.setTotalReviews(course.getReviews().size());
        dto.setTotalModules(course.getModules().size());
        dto.setTotalLessons(course.getModules().stream()
                .mapToInt(module -> module.getLessons().size())
                .sum());

        return dto;
    }

    /**
     * Create a new course
     */
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseResponseDTO createCourse(CourseRequestDTO courseRequest, String userId) {
        log.info("Creating new course for instructor: {}", userId);

        User instructor = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with ID: " + userId));

        // Validate instructor role
        if (!instructor.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_INSTRUCTOR"))) {
            throw new BusinessException("User does not have instructor privileges");
        }

        // Check for duplicate course title for this instructor
        boolean titleExists = courseRepository.existsByTitleAndInstructorId(courseRequest.getTitle(), userId);
        if (titleExists) {
            throw new BusinessException("You already have a course with this title");
        }

        Course course = courseMapper.toEntity(courseRequest, instructor);
        course.setInstructor(instructor);

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());

        // Send notification to instructor
        emailService.sendCourseCreationConfirmation(instructor.getEmail(), savedCourse.getTitle());

        return courseMapper.toResponseDTO(savedCourse);
    }

    /**
     * Update entire course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseResponseDTO updateCourse(String courseId, CourseRequestDTO courseRequest) {
        log.info("Updating course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Validate if course can be updated
        validateCourseUpdate(course);

        // Update fields
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());
        course.setCategory(courseRequest.getCategory());
        course.setDifficultyLevel(Course.DifficultyLevel.valueOf(courseRequest.getDifficultyLevel()));
        course.setPrice(courseRequest.getPrice());
        course.setThumbnailUrl(courseRequest.getThumbnailUrl());
        course.setEstimatedHours(courseRequest.getEstimatedHours());

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully: {}", courseId);

        return courseMapper.toResponseDTO(updatedCourse);
    }

    /**
     * Partially update course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseResponseDTO patchCourse(String courseId, CourseUpdateDTO courseUpdate) {
        log.info("Partially updating course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Validate if course can be updated
        validateCourseUpdate(course);

        // Update only provided fields
        if (courseUpdate.getTitle() != null) {
            course.setTitle(courseUpdate.getTitle());
        }
        if (courseUpdate.getDescription() != null) {
            course.setDescription(courseUpdate.getDescription());
        }
        if (courseUpdate.getCategory() != null) {
            course.setCategory(courseUpdate.getCategory());
        }
        if (courseUpdate.getDifficultyLevel() != null) {
            course.setDifficultyLevel(Course.DifficultyLevel.valueOf(courseUpdate.getDifficultyLevel()));
        }
        if (courseUpdate.getPrice() != null) {
            validatePrice(courseUpdate.getPrice());
            course.setPrice(courseUpdate.getPrice());
        }
        if (courseUpdate.getThumbnailUrl() != null) {
            course.setThumbnailUrl(courseUpdate.getThumbnailUrl());
        }
        if (courseUpdate.getEstimatedHours() != null) {
            course.setEstimatedHours(courseUpdate.getEstimatedHours());
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Course partially updated successfully: {}", courseId);

        return courseMapper.toResponseDTO(updatedCourse);
    }

    /**
     * Soft delete course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void deleteCourse(String courseId) {
        log.info("Soft deleting course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (course.isDeleted()) {
            throw new BusinessException("Course is already deleted");
        }

        course.setDeleted(true);
        course.setPublished(false);
        courseRepository.save(course);

        // Notify enrolled students about course deletion
        notifyStudentsAboutDeletion(course);

        log.info("Course soft deleted successfully: {}", courseId);
    }

    /**
     * Publish course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseResponseDTO publishCourse(String courseId) {
        log.info("Publishing course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Validate course is ready for publishing
        validateCourseForPublishing(course);

        course.setPublished(true);
        Course publishedCourse = courseRepository.save(course);

        // Notify interested users (wishlist)
        notifyWishlistUsers(course);

        log.info("Course published successfully: {}", courseId);

        return courseMapper.toResponseDTO(publishedCourse);
    }

    /**
     * Unpublish course
     */
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseResponseDTO unpublishCourse(String courseId) {
        log.info("Unpublishing course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!course.isPublished()) {
            throw new BusinessException("Course is already unpublished");
        }

        course.setPublished(false);
        Course unpublishedCourse = courseRepository.save(course);

        log.info("Course unpublished successfully: {}", courseId);

        return courseMapper.toResponseDTO(unpublishedCourse);
    }

    /**
     * Get courses by instructor
     */
    public Page<CourseResponseDTO> getCoursesByInstructor(String instructorId, Pageable pageable) {
        log.debug("Fetching courses for instructor: {}", instructorId);

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Page<Course> courses = courseRepository.findByInstructor(instructor, pageable);

        return courses.map(course -> {
            CourseResponseDTO dto = courseMapper.toResponseDTO(course);
            dto.setTotalStudents(course.getStudents().size());
            dto.setTotalReviews(course.getReviews().size());
            return dto;
        });
    }

    /**
     * Get course statistics for instructor dashboard
     */
    public CourseStatisticsDTO getCourseStatistics(String courseId) {
        log.debug("Fetching statistics for course: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId,Pageable.unpaged()).toList();

        // Calculate enrollment statistics
        long activeEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                .count();
        long completedEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count();
        long droppedEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.DROPPED)
                .count();

        double completionRate = enrollments.isEmpty() ? 0.0 :
                (completedEnrollments * 100.0) / enrollments.size();

        // Calculate revenue
        BigDecimal totalRevenue = course.getPrice().multiply(BigDecimal.valueOf(enrollments.size()));
        BigDecimal platformCommission = totalRevenue.multiply(BigDecimal.valueOf(0.30)); // 30% platform fee
        BigDecimal instructorEarnings = totalRevenue.subtract(platformCommission);

        // Calculate engagement metrics
        double averageProgress = enrollments.stream()
                .mapToDouble(Enrollment::getProgressPercentage)
                .average()
                .orElse(0.0);

        // Get rating distribution
        Map<Integer, Integer> ratingDistribution = reviewService.getRatingDistributionForCourse(courseId);

        // Get recent enrollments
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);

        long enrollmentsLast7Days = enrollments.stream()
                .filter(e -> e.getEnrollmentDate().isAfter(oneWeekAgo))
                .count();
        long enrollmentsLast30Days = enrollments.stream()
                .filter(e -> e.getEnrollmentDate().isAfter(oneMonthAgo))
                .count();

        return CourseStatisticsDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalEnrollments(enrollments.size())
                .activeEnrollments((int) activeEnrollments)
                .completedEnrollments((int) completedEnrollments)
                .droppedEnrollments((int) droppedEnrollments)
                .completionRate(completionRate)
                .totalRevenue(totalRevenue)
                .instructorEarnings(instructorEarnings)
                .platformCommission(platformCommission)
                .averageWatchTimeMinutes(0.0) // Would need lesson tracking implementation
                .averageProgressPercentage(averageProgress)
                .averageRating(course.getAverageRating())
                .totalReviews(course.getReviews().size())
                .ratingDistribution(ratingDistribution)
                .lastEnrollmentDate(getLastEnrollmentDate(enrollments))
                .lastAccessDate(getLastAccessDate(enrollments))
                .enrollmentsLast7Days((int) enrollmentsLast7Days)
                .enrollmentsLast30Days((int) enrollmentsLast30Days)
                .build();
    }

    /**
     * Search courses by keyword
     */
    public Page<CourseListDTO> searchCourses(String keyword, Pageable pageable) {
        log.debug("Searching courses with keyword: {}", keyword);

        Page<Course> courses = courseRepository.searchCourses(keyword, pageable);

        return courses.map(course -> {
            CourseListDTO dto = courseMapper.toListDTO(course);
            dto.setFormattedPrice(formatPrice(course.getPrice()));
            dto.setRatingStars(generateRatingStars(course.getAverageRating()));
            return dto;
        });
    }

    /**
     * Check if user is course owner
     */
    public boolean isCourseOwner(String courseId, String username) {
        return courseRepository.findById(courseId)
                .map(course -> course.getInstructor().getUsername().equals(username))
                .orElse(false);
    }

    /**
     * Get popular courses
     */
    @Cacheable(value = "courses", key = "'popular_' + #limit")
    public List<CourseListDTO> getPopularCourses(int limit) {
        log.debug("Fetching top {} popular courses", limit);

        Pageable pageable = Pageable.ofSize(limit);
        Page<Course> courses = courseRepository.findByIsPublishedTrueAndIsDeletedFalse(pageable);

        return courses.stream()
                .map(courseMapper::toListDTO)
                .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void enrichWithUserSpecificData(CourseResponseDTO dto, String userId, String courseId) {
        // Check if user is enrolled
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
                userId, courseId, Enrollment.EnrollmentStatus.ACTIVE);
        dto.setIsEnrolled(isEnrolled);

        // Get user's progress if enrolled
        if (isEnrolled) {
            enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                    .ifPresent(enrollment -> {
                        dto.setUserProgress(enrollment.getProgressPercentage());
                    });
        }

        // Check if in wishlist
        boolean inWishlist = wishlistRepository.existsByUserIdAndCourseId(userId, courseId);
        dto.setIsInWishlist(inWishlist);
    }

    private void validateCourseForPublishing(Course course) {
        if (course.isPublished()) {
            throw new BusinessException("Course is already published");
        }

        if (course.isDeleted()) {
            throw new BusinessException("Cannot publish a deleted course");
        }

        if (course.getModules() == null || course.getModules().isEmpty()) {
            throw new BusinessException("Course must have at least one module to publish");
        }

        // Validate each module has at least one lesson
        boolean hasLessons = course.getModules().stream()
                .anyMatch(module -> module.getLessons() != null && !module.getLessons().isEmpty());

        if (!hasLessons) {
            throw new BusinessException("Course must have at least one lesson to publish");
        }

        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new BusinessException("Course title is required");
        }

        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Valid price is required");
        }
    }

    private void validateCourseUpdate(Course course) {
        if (course.isPublished()) {
            throw new BusinessException("Cannot update a published course. Unpublish it first.");
        }

        if (course.isDeleted()) {
            throw new BusinessException("Cannot update a deleted course");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Price cannot be negative");
        }
        if (price.compareTo(BigDecimal.valueOf(9999.99)) > 0) {
            throw new BusinessException("Price cannot exceed $9,999.99");
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return "Free";
        }
        return String.format("$%.2f", price);
    }

    private String generateRatingStars(Double rating) {
        if (rating == null || rating == 0) {
            return "☆☆☆☆☆";
        }

        int fullStars = (int) Math.floor(rating);
        boolean hasHalfStar = (rating - fullStars) >= 0.5;

        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("½");
        }
        while (stars.length() < 5) {
            stars.append("☆");
        }

        return stars.toString();
    }

    private boolean isAuthorizedToViewUnpublished(String userId, Course course) {
        if (userId == null) {
            return false;
        }

        // Instructors can view their own unpublished courses
        if (course.getInstructor().getId().equals(userId)) {
            return true;
        }

        // Admins can view any unpublished courses
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .orElse(false);
    }

    private void notifyStudentsAboutDeletion(Course course) {
        List<Enrollment> activeEnrollments = enrollmentRepository.findByCourseIdAndStatus(
                course.getId(), Enrollment.EnrollmentStatus.ACTIVE);

        activeEnrollments.forEach(enrollment -> {
            emailService.sendCourseDeletionNotification(
                    enrollment.getUser().getEmail(),
                    course.getTitle()
            );
        });
    }

    private void notifyWishlistUsers(Course course) {
        List<Wishlist> wishlistItems = wishlistRepository.findByCourseId(course.getId());

        wishlistItems.forEach(wishlist -> {
            emailService.sendCoursePublishedNotification(
                    wishlist.getUser().getEmail(),
                    course.getTitle()
            );
        });
    }

    private LocalDateTime getLastEnrollmentDate(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(Enrollment::getEnrollmentDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime getLastAccessDate(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(Enrollment::getLastAccessedAt)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}