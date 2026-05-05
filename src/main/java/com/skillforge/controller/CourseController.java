package com.skillforge.controller;

import com.skillforge.dto.*;
import com.skillforge.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "Endpoints for managing courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all published courses with pagination and filtering")
    public ResponseEntity<PageResponseDTO<CourseListDTO>> getAllCourses(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute CourseFilterDTO filters) {

        Page<CourseListDTO> courses = courseService.getAllPublishedCourses(pageable, filters);
        return ResponseEntity.ok(PageResponseDTO.fromPage(courses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed course information by ID")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(
            @PathVariable String id,
            @RequestAttribute(required = false) String userId) {

        CourseResponseDTO course = courseService.getCourseById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Course retrieved successfully", course));
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a new course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponseDTO>> createCourse(
            @Valid @RequestBody CourseRequestDTO courseRequest,
            @RequestAttribute String userId) {

        CourseResponseDTO createdCourse = courseService.createCourse(courseRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", createdCourse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isCourseOwner(#id, principal.username)")
    @Operation(summary = "Update entire course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseRequestDTO courseRequest) {

        CourseResponseDTO updatedCourse = courseService.updateCourse(id, courseRequest);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isCourseOwner(#id, principal.username)")
    @Operation(summary = "Partially update course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponseDTO>> patchCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseUpdateDTO courseUpdate) {

        CourseResponseDTO updatedCourse = courseService.patchCourse(id, courseUpdate);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Soft delete course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    @PostMapping("/{courseId}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isCourseOwner(#courseId, principal.username)")
    @Operation(summary = "Publish course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponseDTO>> publishCourse(@PathVariable String courseId) {
        CourseResponseDTO publishedCourse = courseService.publishCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course published successfully", publishedCourse));
    }

    @PostMapping("/{courseId}/unpublish")
    @PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isCourseOwner(#courseId, principal.username)")
    @Operation(summary = "Unpublish course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseResponseDTO>> unpublishCourse(@PathVariable String courseId) {
        CourseResponseDTO unpublishedCourse = courseService.unpublishCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success("Course unpublished successfully", unpublishedCourse));
    }

    @GetMapping("/{courseId}/statistics")
    @PreAuthorize("hasRole('INSTRUCTOR') and @courseService.isCourseOwner(#courseId, principal.username) or hasRole('ADMIN')")
    @Operation(summary = "Get course statistics for instructor dashboard", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CourseStatisticsDTO>> getCourseStatistics(@PathVariable String courseId) {
        CourseStatisticsDTO statistics = courseService.getCourseStatistics(courseId);
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }

    @GetMapping("/instructor/my-courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Get all courses for current instructor", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponseDTO<CourseResponseDTO>> getMyCourses(
            @RequestAttribute String userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CourseResponseDTO> courses = courseService.getCoursesByInstructor(userId, pageable);
        return ResponseEntity.ok(PageResponseDTO.fromPage(courses));
    }
}