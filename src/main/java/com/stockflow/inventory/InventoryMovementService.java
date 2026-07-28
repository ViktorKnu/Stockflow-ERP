package com.stockflow.inventory;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.common.dto.PageResponse;
import com.stockflow.exception.BusinessRuleException;
import com.stockflow.exception.ApiErrorCode;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.inventory.dto.InventoryMovementCreateRequest;
import com.stockflow.inventory.dto.InventoryMovementResponse;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public InventoryMovementResponse create(InventoryMovementCreateRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        return recordMovement(product, request.type(), request.quantity(), request.reason());
    }

    @Transactional
    public InventoryMovementResponse recordMovement(Product product, MovementType type, Integer quantity, String reason) {
        Integer previousQuantity = product.getQuantity();
        Integer newQuantity = calculateNewQuantity(previousQuantity, type, quantity);

        product.setQuantity(newQuantity);

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .type(type)
                .quantity(quantity)
                .reason(reason)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .build();

        InventoryMovement savedMovement = movementRepository.save(movement);
        auditLogService.record(
                AuditAction.INVENTORY_MOVEMENT_CREATED,
                "InventoryMovement",
                savedMovement.getId(),
                "Inventory movement " + type + " for product " + product.getId()
        );

        return InventoryMovementMapper.toResponse(savedMovement);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryMovementResponse> findAll(int page, int size) {
        return PageResponse.from(
                movementRepository.findAll(pageRequest(page, size)),
                InventoryMovementMapper::toResponse
        );
    }

    @Transactional(readOnly = true)
    public InventoryMovementResponse findById(Long id) {
        return InventoryMovementMapper.toResponse(movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory movement not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryMovementResponse> findByProduct(Long productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        return PageResponse.from(
                movementRepository.findByProductId(productId, pageRequest(page, size)),
                InventoryMovementMapper::toResponse
        );
    }

    private Integer calculateNewQuantity(Integer previousQuantity, MovementType type, Integer quantity) {
        if (type != MovementType.ADJUSTMENT && quantity <= 0) {
            throw new BusinessRuleException(
                    ApiErrorCode.INVENTORY_POSITIVE_QUANTITY_REQUIRED,
                    "IN and OUT movements must have quantity greater than zero");
        }

        return switch (type) {
            case IN -> previousQuantity + quantity;
            case OUT -> {
                int updatedQuantity = previousQuantity - quantity;
                if (updatedQuantity < 0) {
                    throw new BusinessRuleException(
                            ApiErrorCode.INSUFFICIENT_STOCK,
                            "Inventory movement cannot make stock negative");
                }
                yield updatedQuantity;
            }
            case ADJUSTMENT -> quantity;
        };
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}
