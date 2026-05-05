package com.skillforge.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] args;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String message, String errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    // Common business error codes
    public static final String ENROLLMENT_LIMIT_EXCEEDED = "ENROLLMENT_LIMIT_EXCEEDED";
    public static final String ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String COURSE_NOT_PUBLISHED = "COURSE_NOT_PUBLISHED";
    public static final String SELF_ENROLLMENT = "SELF_ENROLLMENT";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String INVALID_COUPON = "INVALID_COUPON";
    public static final String MAX_RETRY_EXCEEDED = "MAX_RETRY_EXCEEDED";
    public static final String INVALID_OPERATION = "INVALID_OPERATION";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
}