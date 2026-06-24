package com.stockflow.salesorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SalesOrderCreateRequest(
        @NotBlank @Size(max = 160) String customerName,
        @NotBlank @Email @Size(max = 255) String customerEmail
) {
}
