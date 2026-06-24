package com.stockflow.salesorder;

import com.stockflow.salesorder.dto.SalesOrderCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderItemCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderResponse;
import com.stockflow.salesorder.dto.SalesOrderStatusUpdateRequest;
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
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    @Operation(summary = "List sales orders")
    public List<SalesOrderResponse> findAll() {
        return salesOrderService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales order by id")
    public SalesOrderResponse findById(@PathVariable Long id) {
        return salesOrderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create sales order")
    public SalesOrderResponse create(@Valid @RequestBody SalesOrderCreateRequest request) {
        return salesOrderService.create(request);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item to sales order")
    public SalesOrderResponse addItem(@PathVariable Long id,
                                      @Valid @RequestBody SalesOrderItemCreateRequest request) {
        return salesOrderService.addItem(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update sales order status")
    public SalesOrderResponse updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody SalesOrderStatusUpdateRequest request) {
        return salesOrderService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete sales order")
    public void delete(@PathVariable Long id) {
        salesOrderService.delete(id);
    }
}
