package com.stockflow.inventory;

import com.stockflow.inventory.dto.InventoryMovementCreateRequest;
import com.stockflow.inventory.dto.InventoryMovementResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/movements")
@RequiredArgsConstructor
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
    public List<InventoryMovementResponse> findAll() {
        return movementService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory movement by id")
    public InventoryMovementResponse findById(@PathVariable Long id) {
        return movementService.findById(id);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List inventory movements for product")
    public List<InventoryMovementResponse> findByProduct(@PathVariable Long productId) {
        return movementService.findByProduct(productId);
    }
}
