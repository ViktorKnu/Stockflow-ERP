package com.stockflow.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, Map.of());
    }

    public static ApiError validation(int status, String error, String message, String path,
                                      Map<String, String> validationErrors) {
        return new ApiError(Instant.now(), status, error, message, path, validationErrors);
    }
}
