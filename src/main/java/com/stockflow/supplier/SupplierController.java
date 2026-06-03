package com.stockflow.supplier;

import com.stockflow.supplier.dto.SupplierCreateRequest;
import com.stockflow.supplier.dto.SupplierResponse;
import com.stockflow.supplier.dto.SupplierUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "List all suppliers")
    public List<SupplierResponse> findAll() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by id")
    public SupplierResponse findById(@PathVariable Long id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create supplier")
    public SupplierResponse create(@Valid @RequestBody SupplierCreateRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete supplier")
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }
}
