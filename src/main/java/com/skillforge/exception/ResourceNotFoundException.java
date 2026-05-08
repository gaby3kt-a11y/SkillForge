package com.skillforge.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

  private final String resourceType;
  private final String resourceId;
  private final String fieldName;
  private final Object fieldValue;

  public ResourceNotFoundException(String message) {
    super(message);
    this.resourceType = null;
    this.resourceId = null;
    this.fieldName = null;
    this.fieldValue = null;
  }

  public ResourceNotFoundException(String resourceType, String resourceId) {
    super(String.format("%s not found with id: %s", resourceType, resourceId));
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.fieldName = "id";
    this.fieldValue = resourceId;
  }

  public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
    super(String.format("%s not found with %s: %s", resourceType, fieldName, fieldValue));
    this.resourceType = resourceType;
    this.resourceId = null;
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
  }

  // Factory methods for common resources
  public static ResourceNotFoundException userNotFound(String userId) {
    return new ResourceNotFoundException("User", "id", userId);
  }

  public static ResourceNotFoundException courseNotFound(String courseId) {
    return new ResourceNotFoundException("Course", "id", courseId);
  }

  public static ResourceNotFoundException enrollmentNotFound(String enrollmentId) {
    return new ResourceNotFoundException("Enrollment", "id", enrollmentId);
  }

  public static ResourceNotFoundException lessonNotFound(String lessonId) {
    return new ResourceNotFoundException("Lesson", "id", lessonId);
  }

  public static ResourceNotFoundException moduleNotFound(String moduleId) {
    return new ResourceNotFoundException("Module", "id", moduleId);
  }

  public static ResourceNotFoundException reviewNotFound(String reviewId) {
    return new ResourceNotFoundException("Review", "id", reviewId);
  }

  public static ResourceNotFoundException paymentNotFound(String paymentId) {
    return new ResourceNotFoundException("Payment", "id", paymentId);
  }

  public static ResourceNotFoundException certificateNotFound(String certificateId) {
    return new ResourceNotFoundException("Certificate", "id", certificateId);
  }
}
