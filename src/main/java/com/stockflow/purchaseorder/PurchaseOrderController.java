package com.stockflow.purchaseorder;

import com.stockflow.purchaseorder.dto.PurchaseOrderCreateRequest;
import com.stockflow.purchaseorder.dto.PurchaseOrderItemCreateRequest;
import com.stockflow.purchaseorder.dto.PurchaseOrderResponse;
import com.stockflow.purchaseorder.dto.PurchaseOrderStatusUpdateRequest;
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
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @Operation(summary = "List purchase orders")
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by id")
    public PurchaseOrderResponse findById(@PathVariable Long id) {
        return purchaseOrderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create purchase order")
    public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return purchaseOrderService.create(request);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to purchase order")
    public PurchaseOrderResponse addItem(@PathVariable Long id,
                                         @Valid @RequestBody PurchaseOrderItemCreateRequest request) {
        return purchaseOrderService.addItem(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update purchase order status")
    public PurchaseOrderResponse updateStatus(@PathVariable Long id,
                                              @Valid @RequestBody PurchaseOrderStatusUpdateRequest request) {
        return purchaseOrderService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete purchase order")
    public void delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
    }
}
