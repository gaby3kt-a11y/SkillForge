package com.skillforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.skillforge.dto.CourseRequestDTO;
import com.skillforge.dto.CourseResponseDTO;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.mapper.CourseMapper;
import com.skillforge.model.Course;
import com.skillforge.model.Role;
import com.skillforge.model.User;
import com.skillforge.repository.CourseRepository;
import com.skillforge.repository.EnrollmentRepository;
import com.skillforge.repository.UserRepository;
import com.skillforge.repository.WishlistRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private EnrollmentRepository enrollmentRepository;

  @Mock private WishlistRepository wishlistRepository;

  @Mock private CourseMapper courseMapper;

  @Mock private EmailService emailService;

  @Mock private ReviewService reviewService;

  @InjectMocks private CourseService courseService;

  private User instructor;
  private Course course;

  @BeforeEach
  void setUp() {
    Role instructorRole = Role.builder().name("ROLE_INSTRUCTOR").build();

    instructor =
        User.builder()
            .id("user123")
            .fullName("John Doe")
            .email("john@example.com")
            .roles(Set.of(instructorRole))
            .build();

    course =
        Course.builder()
            .id("course123")
            .title("Spring Boot Masterclass")
            .description("Learn Spring Boot from scratch")
            .price(BigDecimal.valueOf(99.99))
            .instructor(instructor)
            .isPublished(false)
            .build();
  }

  @Test
  void createCourse_Success() {
    // Arrange
    CourseRequestDTO courseDTO =
        CourseRequestDTO.builder()
            .title("Spring Boot Masterclass")
            .description("Learn Spring Boot from scratch")
            .category("Programming")
            .difficultyLevel("BEGINNER")
            .price(BigDecimal.valueOf(99.99))
            .build();

    CourseResponseDTO mappedResponse =
        CourseResponseDTO.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .category(course.getCategory())
            .difficultyLevel(
                course.getDifficultyLevel() != null
                    ? course.getDifficultyLevel().name()
                    : "BEGINNER")
            .price(course.getPrice())
            .thumbnailUrl(course.getThumbnailUrl())
            .estimatedHours(course.getEstimatedHours())
            .isPublished(course.isPublished())
            .build();

    when(userRepository.findById("user123")).thenReturn(Optional.of(instructor));
    when(courseRepository.save(any(Course.class))).thenReturn(course);
    when(courseMapper.toEntity(any(CourseRequestDTO.class), any(User.class))).thenReturn(course);
    when(courseMapper.toResponseDTO(any(Course.class))).thenReturn(mappedResponse);

    // Act
    CourseResponseDTO result = courseService.createCourse(courseDTO, "user123");

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Spring Boot Masterclass");
    assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
  }

  @Test
  void createCourse_InstructorNotFound_ThrowsException() {
    // Arrange
    CourseRequestDTO courseDTO =
        CourseRequestDTO.builder()
            .title("Spring Boot Masterclass")
            .category("Programming")
            .difficultyLevel("BEGINNER")
            .price(BigDecimal.valueOf(99.99))
            .build();

    when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> courseService.createCourse(courseDTO, "invalidId"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Instructor not found");
  }
}
