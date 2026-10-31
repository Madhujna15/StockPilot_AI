package com.stockpilot.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.stockpilot.backend.auth.dto.AuthResponse;
import com.stockpilot.backend.auth.dto.LoginRequest;
import com.stockpilot.backend.auth.dto.RegisterRequest;
import com.stockpilot.backend.business.Business;
import com.stockpilot.backend.business.BusinessRepository;
import com.stockpilot.backend.user.AppUser;
import com.stockpilot.backend.user.AppUserRepository;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
    private final List<Business> savedBusinesses = new ArrayList<>();
    private final List<AppUser> savedUsers = new ArrayList<>();
    private AuthService service;

    @BeforeEach
    void setUp() {
        BusinessRepository businesses = (BusinessRepository) Proxy.newProxyInstance(
                BusinessRepository.class.getClassLoader(), new Class<?>[]{BusinessRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        Business business = (Business) args[0];
                        assignId(business);
                        savedBusinesses.add(business);
                        return business;
                    }
                    return unsupported(method.getName());
                });
        AppUserRepository users = (AppUserRepository) Proxy.newProxyInstance(
                AppUserRepository.class.getClassLoader(), new Class<?>[]{AppUserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByEmailIgnoreCase" -> savedUsers.stream()
                            .anyMatch(user -> user.getEmail().equalsIgnoreCase((String) args[0]));
                    case "save" -> { AppUser user = (AppUser) args[0]; assignId(user); savedUsers.add(user); yield user; }
                    case "findByEmailIgnoreCase" -> savedUsers.stream()
                            .filter(user -> user.getEmail().equalsIgnoreCase((String) args[0])).findFirst();
                    default -> unsupported(method.getName());
                });
        PasswordEncoder encoder = new PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) { return "encoded:" + rawPassword; }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(encode(rawPassword));
            }
        };
        String secret = Base64.getEncoder().encodeToString(
                "stockpilot-test-key-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        JwtService jwtService = new JwtService(secret, 3_600_000L);
        service = new AuthService(users, businesses, encoder, jwtService, 3_600_000L);
    }

    @Test
    void registrationCreatesOwnerAndStoresEncodedPassword() {
        AuthResponse response = service.register(new RegisterRequest(
                " Ramesh Patel ", "Ramesh@Example.com", "correct-horse-9", " Ramesh Store "));

        assertEquals("ramesh@example.com", response.email());
        assertEquals("Ramesh Store", response.businessName());
        assertEquals(Role.OWNER, response.role());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1, savedBusinesses.size());
        assertEquals(1, savedUsers.size());
        assertNotEquals("correct-horse-9", savedUsers.get(0).getPassword());
    }

    @Test
    void duplicateRegistrationDoesNotCreateAnotherBusinessOrUser() {
        RegisterRequest request = new RegisterRequest(
                "Ramesh Patel", "ramesh@example.com", "correct-horse-9", "Ramesh Store");
        service.register(request);

        assertThrows(EmailAlreadyRegisteredException.class, () -> service.register(request));
        assertEquals(1, savedBusinesses.size());
        assertEquals(1, savedUsers.size());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        service.register(new RegisterRequest(
                "Ramesh Patel", "ramesh@example.com", "correct-horse-9", "Ramesh Store"));

        assertThrows(BadCredentialsException.class, () -> service.login(
                new LoginRequest("ramesh@example.com", "wrong-password")));
    }

    private static Object unsupported(String method) {
        throw new UnsupportedOperationException("Unexpected repository method in unit test: " + method);
    }

    private static void assignId(Object entity) {
        try {
            Field id = entity.getClass().getDeclaredField("id");
            id.setAccessible(true);
            id.set(entity, java.util.UUID.randomUUID());
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
