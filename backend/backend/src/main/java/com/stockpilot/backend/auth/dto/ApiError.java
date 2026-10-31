package com.stockpilot.backend.auth.dto;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String error, String message) { }
