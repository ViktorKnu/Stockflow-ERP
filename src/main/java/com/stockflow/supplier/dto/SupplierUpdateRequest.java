package com.stockflow.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierUpdateRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @Size(max = 500) String address
) {
}
