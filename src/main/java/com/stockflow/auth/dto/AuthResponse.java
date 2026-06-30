package com.stockflow.auth.dto;

import com.stockflow.user.dto.UserResponse;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserResponse user
) {
}
