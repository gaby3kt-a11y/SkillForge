package com.skillforge.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Factory methods
    public static DuplicateResourceException userExists(String email) {
        return new DuplicateResourceException("User", "email", email);
    }

    public static DuplicateResourceException courseTitleExists(String title) {
        return new DuplicateResourceException("Course", "title", title);
    }

    public static DuplicateResourceException enrollmentExists(String userId, String courseId) {
        return new DuplicateResourceException("Enrollment", "userId and courseId",
                String.format("%s-%s", userId, courseId));
    }

    public static DuplicateResourceException reviewExists(String userId, String courseId) {
        return new DuplicateResourceException("Review", "userId and courseId",
                String.format("%s-%s", userId, courseId));
    }
}