package com.skillforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDTO {

  @NotBlank(message = "Course ID is required")
  private String courseId;

  @Pattern(
      regexp = "^(STRIPE|PAYPAL|CARD|FREE)$",
      message = "Payment method must be STRIPE, PAYPAL, CARD, or FREE")
  private String paymentMethod;

  private String promotionCode;

  @Builder.Default private Boolean acceptTerms = false;
}
