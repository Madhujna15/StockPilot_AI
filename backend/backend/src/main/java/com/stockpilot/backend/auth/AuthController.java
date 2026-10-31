package com.stockpilot.backend.auth;

import com.stockpilot.backend.auth.dto.AuthResponse;
import com.stockpilot.backend.auth.dto.LoginRequest;
import com.stockpilot.backend.auth.dto.ProfileResponse;
import com.stockpilot.backend.auth.dto.RegisterRequest;
import com.stockpilot.backend.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal AppUser user) {
        return new ProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getBusiness().getId(), user.getBusiness().getName());
    }
}
