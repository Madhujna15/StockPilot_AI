package com.stockpilot.backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stockpilot.backend.auth.dto.RegisterRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegisterRequestValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() { factory.close(); }

    @Test
    void acceptsWellFormedRegistration() {
        RegisterRequest request = new RegisterRequest(
                "Ramesh Patel", "ramesh@example.com", "correct-horse-9", "Ramesh Store");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsShortPasswordAndMalformedEmail() {
        RegisterRequest request = new RegisterRequest("Ramesh Patel", "not-an-email", "short", "Ramesh Store");

        assertFalse(validator.validate(request).isEmpty());
    }
}
