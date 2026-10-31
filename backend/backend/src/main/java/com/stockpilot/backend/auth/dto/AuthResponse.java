package com.stockpilot.backend.auth.dto;

import com.stockpilot.backend.auth.Role;
import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String fullName,
        String email,
        Role role,
        UUID businessId,
        String businessName) { }
