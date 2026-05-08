package com.skillforge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean success;
  private String message;
  private T data;
  private Map<String, String> errors;
  private Integer statusCode;
  private String path;
  private LocalDateTime timestamp;

  // Static factory methods
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .statusCode(200)
        .build();
  }

  public static <T> ApiResponse<T> error(String message, Integer statusCode) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .timestamp(LocalDateTime.now())
        .statusCode(statusCode)
        .build();
  }

  public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .errors(errors)
        .timestamp(LocalDateTime.now())
        .statusCode(400)
        .build();
  }
}
