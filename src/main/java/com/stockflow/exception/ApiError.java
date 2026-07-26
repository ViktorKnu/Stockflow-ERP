package com.stockflow.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        ApiErrorCode code,
        String message,
        String path,
        String correlationId,
        Map<String, String> validationErrors
) {
    public static ApiError of(int status, String error, ApiErrorCode code, String message, String path,
                              String correlationId) {
        return new ApiError(Instant.now(), status, error, code, message, path, correlationId, Map.of());
    }

    public static ApiError validation(int status, String error, ApiErrorCode code, String message, String path,
                                      String correlationId,
                                      Map<String, String> validationErrors) {
        return new ApiError(
                Instant.now(), status, error, code, message, path, correlationId, validationErrors);
    }
}
