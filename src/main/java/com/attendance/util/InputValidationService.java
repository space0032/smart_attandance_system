package com.attendance.util;

import com.attendance.dto.StudentDTO;
import com.validator.core.Validator;
import com.validator.result.ValidationResult;
import com.validator.sanitizers.Sanitizer;
import com.validator.sanitizers.Sanitizers;
import com.validator.validators.Validators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for input validation and sanitization using java-validation-sanitizer
 * library.
 * Provides protection against XSS, SQL injection, and ensures data integrity.
 */
@Slf4j
@Service
public class InputValidationService {

    /**
     * Validates and sanitizes student DTO fields.
     * Trims whitespace, removes HTML/XSS, and validates email format.
     *
     * @param dto StudentDTO to validate and sanitize
     * @throws IllegalArgumentException if validation fails
     */
    public void validateAndSanitizeStudent(StudentDTO dto) {
        log.info("=== InputValidationService: Sanitizing student data ===");
        log.info("BEFORE - studentId: {}, firstName: {}, lastName: {}, email: {}",
                dto.getStudentId(), dto.getFirstName(), dto.getLastName(), dto.getEmail());

        // Sanitize all string fields
        dto.setStudentId(sanitizeInput(dto.getStudentId()));
        dto.setFirstName(sanitizeInput(dto.getFirstName()));
        dto.setLastName(sanitizeInput(dto.getLastName()));
        dto.setEmail(sanitizeEmail(dto.getEmail()));
        dto.setDepartment(sanitizeInput(dto.getDepartment()));

        log.info("AFTER  - studentId: {}, firstName: {}, lastName: {}, email: {}",
                dto.getStudentId(), dto.getFirstName(), dto.getLastName(), dto.getEmail());

        // Validate fields using the library
        ValidationResult result = Validator.create()
                .field("studentId", dto.getStudentId())
                .validate(Validators.notBlank())
                .validate(Validators.maxLength(50))
                .field("firstName", dto.getFirstName())
                .validate(Validators.notBlank())
                .validate(Validators.maxLength(100))
                .field("lastName", dto.getLastName())
                .validate(Validators.notBlank())
                .validate(Validators.maxLength(100))
                .field("email", dto.getEmail())
                .validate(Validators.notBlank())
                .validate(Validators.isEmail())
                .field("department", dto.getDepartment())
                .validate(Validators.notBlank())
                .validate(Validators.maxLength(100))
                .execute();

        if (!result.isValid()) {
            String errorMessage = result.getErrors().stream()
                    .findFirst()
                    .map(error -> error.getFieldName() + ": " + error.getMessage())
                    .orElse("Validation failed");
            log.warn("Student validation failed: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.debug("Student DTO validated and sanitized successfully");
    }

    /**
     * Validates and sanitizes username for user creation.
     *
     * @param username The username to validate
     * @return Sanitized username
     * @throws IllegalArgumentException if validation fails
     */
    public String validateAndSanitizeUsername(String username) {
        // Sanitize first
        String sanitized = Sanitizer.create(username)
                .trim()
                .removeHtml()
                .escapeXss()
                .sanitize();

        // Validate
        ValidationResult result = Validator.create()
                .field("username", sanitized)
                .validate(Validators.notBlank())
                .validate(Validators.minLength(3))
                .validate(Validators.maxLength(50))
                .validate(Validators.isAlphanumeric())
                .execute();

        if (!result.isValid()) {
            String errorMessage = result.getErrors().stream()
                    .findFirst()
                    .map(error -> error.getMessage())
                    .orElse("Invalid username");
            log.warn("Username validation failed: {}", errorMessage);
            throw new IllegalArgumentException("Invalid username: " + errorMessage);
        }

        return sanitized;
    }

    /**
     * Validates password strength.
     *
     * @param password The password to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validatePassword(String password) {
        ValidationResult result = Validator.create()
                .field("password", password)
                .validate(Validators.notBlank())
                .validate(Validators.minLength(6))
                .validate(Validators.maxLength(100))
                .execute();

        if (!result.isValid()) {
            String errorMessage = result.getErrors().stream()
                    .findFirst()
                    .map(error -> error.getMessage())
                    .orElse("Invalid password");
            log.warn("Password validation failed");
            throw new IllegalArgumentException("Invalid password: " + errorMessage);
        }
    }

    /**
     * Sanitizes general text input to prevent XSS and remove malicious content.
     *
     * @param input The input string to sanitize
     * @return Sanitized string, or null if input was null
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        return Sanitizer.create(input)
                .trim()
                .removeHtml()
                .escapeXss()
                .stripSqlKeywords()
                .normalizeWhitespace()
                .sanitize();
    }

    /**
     * Sanitizes email input while preserving valid email characters.
     *
     * @param email The email to sanitize
     * @return Sanitized email
     */
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return Sanitizer.create(email)
                .trim()
                .toLowerCase()
                .removeHtml()
                .escapeXss()
                .sanitize();
    }
}
