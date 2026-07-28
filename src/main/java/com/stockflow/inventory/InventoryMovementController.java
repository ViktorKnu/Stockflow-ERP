package com.stockflow.inventory;

import com.stockflow.inventory.dto.InventoryMovementCreateRequest;
import com.stockflow.inventory.dto.InventoryMovementResponse;
import com.stockflow.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/inventory/movements")
@RequiredArgsConstructor
@Validated
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create inventory movement")
    public InventoryMovementResponse create(@Valid @RequestBody InventoryMovementCreateRequest request) {
        return movementService.create(request);
    }

    @GetMapping
    @Operation(summary = "List inventory movements")
    public PageResponse<InventoryMovementResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return movementService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory movement by id")
    public InventoryMovementResponse findById(@PathVariable Long id) {
        return movementService.findById(id);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List inventory movements for product")
    public PageResponse<InventoryMovementResponse> findByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return movementService.findByProduct(productId, page, size);
    }
}
