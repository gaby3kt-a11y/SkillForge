package com.skillforge.mapper;

import com.skillforge.dto.*;
import com.skillforge.model.Course;
import com.skillforge.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CourseMapper {

  @Autowired private UserMapper userMapper;

  @Autowired private ModuleMapper moduleMapper;

  /** Convert CourseRequestDTO to Course entity */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "instructor", ignore = true)
  @Mapping(target = "modules", ignore = true)
  @Mapping(target = "students", ignore = true)
  @Mapping(target = "enrollments", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "totalEnrollments", constant = "0")
  @Mapping(target = "averageRating", constant = "0.0")
  @Mapping(target = "isPublished", constant = "false")
  @Mapping(target = "isDeleted", constant = "false")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(
      target = "difficultyLevel",
      source = "difficultyLevel",
      qualifiedByName = "stringToDifficultyLevel")
  public abstract Course toEntity(CourseRequestDTO dto, @Context User instructor);

  /** Convert Course entity to CourseResponseDTO */
  @Mapping(target = "modules", source = "modules")
  @Mapping(target = "totalStudents", expression = "java(course.getStudents().size())")
  @Mapping(target = "totalReviews", expression = "java(course.getReviews().size())")
  @Mapping(target = "totalModules", expression = "java(course.getModules().size())")
  @Mapping(target = "totalLessons", expression = "java(getTotalLessons(course))")
  @Mapping(target = "isEnrolled", ignore = true)
  @Mapping(target = "userProgress", ignore = true)
  @Mapping(target = "isInWishlist", ignore = true)
  @Mapping(
      target = "difficultyLevel",
      source = "difficultyLevel",
      qualifiedByName = "difficultyLevelToString")
  public abstract CourseResponseDTO toResponseDTO(Course course);

  /** Convert Course entity to CourseListDTO for list views */
  @Mapping(target = "instructor", source = "instructor")
  @Mapping(target = "formattedPrice", expression = "java(formatPrice(course.getPrice()))")
  @Mapping(
      target = "ratingStars",
      expression = "java(generateRatingStars(course.getAverageRating()))")
  @Mapping(
      target = "difficultyLevel",
      source = "difficultyLevel",
      qualifiedByName = "difficultyLevelToString")
  public abstract CourseListDTO toListDTO(Course course);

  /** Convert Course entity to CourseListDTO with custom formatting */
  public List<CourseListDTO> toListDTOs(List<Course> courses) {
    if (courses == null) {
      return null;
    }
    return courses.stream().map(this::toListDTO).collect(Collectors.toList());
  }

  /** Update existing Course entity with values from CourseUpdateDTO */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "instructor", ignore = true)
  @Mapping(target = "modules", ignore = true)
  @Mapping(target = "students", ignore = true)
  @Mapping(target = "enrollments", ignore = true)
  @Mapping(target = "reviews", ignore = true)
  @Mapping(target = "totalEnrollments", ignore = true)
  @Mapping(target = "averageRating", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "published", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(
      target = "difficultyLevel",
      source = "difficultyLevel",
      qualifiedByName = "stringToDifficultyLevel")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  public abstract void updateEntity(@MappingTarget Course course, CourseUpdateDTO dto);

  /** Convert Course entity to CourseStatisticsDTO */
  @Mapping(target = "courseId", source = "id")
  @Mapping(target = "courseTitle", source = "title")
  @Mapping(target = "totalReviews", expression = "java(course.getReviews().size())")
  @Mapping(target = "ratingDistribution", ignore = true)
  @Mapping(target = "lessonCompletionBreakdown", ignore = true)
  @Mapping(target = "enrollmentsLast7Days", ignore = true)
  @Mapping(target = "enrollmentsLast30Days", ignore = true)
  @Mapping(target = "lastEnrollmentDate", ignore = true)
  @Mapping(target = "lastAccessDate", ignore = true)
  public abstract CourseStatisticsDTO toStatisticsDTO(Course course);

  /** Convert Course entity to basic info DTO */
  @Mapping(target = "instructorName", expression = "java(course.getInstructor().getFullName())")
  @Mapping(target = "instructorId", expression = "java(course.getInstructor().getId())")
  public abstract CourseBasicDTO toBasicDTO(Course course);

  // ==================== QUALIFIERS AND HELPER METHODS ====================

  @Named("stringToDifficultyLevel")
  protected Course.DifficultyLevel stringToDifficultyLevel(String difficultyLevel) {
    if (difficultyLevel == null) {
      return Course.DifficultyLevel.BEGINNER;
    }
    try {
      return Course.DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Course.DifficultyLevel.BEGINNER;
    }
  }

  @Named("difficultyLevelToString")
  protected String difficultyLevelToString(Course.DifficultyLevel difficultyLevel) {
    if (difficultyLevel == null) {
      return "BEGINNER";
    }
    return difficultyLevel.name();
  }

  protected String formatPrice(BigDecimal price) {
    if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
      return "Free";
    }
    return String.format("$%.2f", price);
  }

  protected String generateRatingStars(BigDecimal rating) {
    if (rating == null || rating.compareTo(BigDecimal.ZERO)== 0) {
      return "☆☆☆☆☆";
    }

    int fullStars = rating.intValue();
    boolean hasHalfStar = (rating.subtract(BigDecimal.valueOf(fullStars)).compareTo(BigDecimal.valueOf(0.5) ) >=0 );

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

  protected int getTotalLessons(Course course) {
    if (course.getModules() == null) {
      return 0;
    }
    return course.getModules().stream()
        .mapToInt(module -> module.getLessons() != null ? module.getLessons().size() : 0)
        .sum();
  }

  // Batch conversion methods
  public List<CourseResponseDTO> toResponseDTOs(List<Course> courses) {
    if (courses == null) {
      return null;
    }
    return courses.stream().map(this::toResponseDTO).collect(Collectors.toList());
  }

  public List<CourseBasicDTO> toBasicDTOs(List<Course> courses) {
    if (courses == null) {
      return null;
    }
    return courses.stream().map(this::toBasicDTO).collect(Collectors.toList());
  }
}
