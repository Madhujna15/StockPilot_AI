package com.stockpilot.backend.auth.dto;

import com.stockpilot.backend.auth.Role;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String fullName,
        String email,
        Role role,
        UUID businessId,
        String businessName) { }
