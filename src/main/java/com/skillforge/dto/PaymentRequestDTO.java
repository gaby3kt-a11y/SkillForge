package com.skillforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(STRIPE|PAYPAL|CARD)$", message = "Invalid payment method")
    private String paymentMethod;

    private String paymentToken; // Stripe/PayPal token

    private String couponCode;

    @Builder.Default
    private Boolean savePaymentMethod = false;

    private BillingAddressDTO billingAddress;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BillingAddressDTO {
    private String fullName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}