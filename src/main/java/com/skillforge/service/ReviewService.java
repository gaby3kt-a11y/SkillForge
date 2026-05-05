package com.skillforge.service;

import com.skillforge.model.Course;
import com.skillforge.model.Review;
import com.skillforge.model.User;
import com.skillforge.repository.CourseRepository;
import com.skillforge.repository.EnrollmentRepository;
import com.skillforge.repository.ReviewRepository;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    /**
     * Get rating distribution for a course
     */
    public Map<Integer, Integer> getRatingDistributionForCourse(String courseId) {
        List<Object[]> distribution = reviewRepository.getRatingDistribution(courseId);

        Map<Integer, Integer> ratingMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(i, 0);
        }

        for (Object[] row : distribution) {
            Integer rating = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            ratingMap.put(rating, count.intValue());
        }

        return ratingMap;
    }

    /**
     * Calculate and update course average rating
     */
    @Transactional
    public void updateCourseAverageRating(String courseId) {
        Double averageRating = reviewRepository.getAverageRatingForCourse(courseId);

        if (averageRating != null) {
            courseRepository.updateAverageRating(courseId, averageRating);
            log.info("Updated average rating for course {} to {}", courseId, averageRating);
        }
    }
}