package com.skillforge.service;

import com.skillforge.dto.CourseDTO;
import com.skillforge.model.Course;
import com.skillforge.model.User;
import com.skillforge.repository.CourseRepository;
import com.skillforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private User instructor;
    private Course course;

    @BeforeEach
    void setUp() {
        instructor = User.builder()
                .id("user123")
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        course = Course.builder()
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
        CourseDTO courseDTO = CourseDTO.builder()
                .title("Spring Boot Masterclass")
                .description("Learn Spring Boot from scratch")
                .price(BigDecimal.valueOf(99.99))
                .build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // Act
        CourseDTO result = courseService.createCourse(courseDTO, "user123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Spring Boot Masterclass");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void createCourse_InstructorNotFound_ThrowsException() {
        // Arrange
        CourseDTO courseDTO = CourseDTO.builder()
                .title("Spring Boot Masterclass")
                .build();

        when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(courseDTO, "invalidId"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Instructor not found");
    }
}