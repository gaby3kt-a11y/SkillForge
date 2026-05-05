package com.skillforge.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Log the authentication error
        log.error("Unauthorized access attempt: {}", authException.getMessage());
        log.error("Request URI: {}", request.getRequestURI());

        // Build detailed error response
        Map<String, Object> errorDetails = buildErrorResponse(request, authException);

        // Set response properties
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write JSON response
        objectMapper.writeValue(response.getOutputStream(), errorDetails);
    }

    private Map<String, Object> buildErrorResponse(HttpServletRequest request,
                                                   AuthenticationException authException) {
        Map<String, Object> errorDetails = new LinkedHashMap<>();

        errorDetails.put("timestamp", LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorDetails.put("message", getCustomErrorMessage(authException));
        errorDetails.put("path", request.getServletPath());
        errorDetails.put("method", request.getMethod());

        // Add request ID for tracking (optional)
        errorDetails.put("requestId", request.getHeader("X-Request-ID"));

        // Add hint for client
        errorDetails.put("hint", "Please provide a valid JWT token in Authorization header");

        return errorDetails;
    }

    private String getCustomErrorMessage(AuthenticationException exception) {
        String message = exception.getMessage();

        if (message == null) {
            return "Authentication failed";
        }

        // Provide user-friendly messages
        if (message.contains("JWT expired")) {
            return "Your session has expired. Please login again.";
        } else if (message.contains("JWT signature")) {
            return "Invalid authentication token.";
        } else if (message.contains("JWT malformed")) {
            return "Malformed authentication token.";
        } else if (message.contains("Bearer token")) {
            return "Missing or invalid Authorization header.";
        }

        return "Authentication required.";
    }
}