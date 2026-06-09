package com.stockflow.inventory;

import com.stockflow.exception.BusinessRuleException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.inventory.dto.InventoryMovementCreateRequest;
import com.stockflow.inventory.dto.InventoryMovementResponse;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

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

        return InventoryMovementMapper.toResponse(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findAll() {
        return movementRepository.findAll().stream()
                .map(InventoryMovementMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryMovementResponse findById(Long id) {
        return InventoryMovementMapper.toResponse(movementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory movement not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(InventoryMovementMapper::toResponse)
                .toList();
    }

    private Integer calculateNewQuantity(Integer previousQuantity, MovementType type, Integer quantity) {
        if (type != MovementType.ADJUSTMENT && quantity <= 0) {
            throw new BusinessRuleException("IN and OUT movements must have quantity greater than zero");
        }

        return switch (type) {
            case IN -> previousQuantity + quantity;
            case OUT -> {
                int updatedQuantity = previousQuantity - quantity;
                if (updatedQuantity < 0) {
                    throw new BusinessRuleException("Inventory movement cannot make stock negative");
                }
                yield updatedQuantity;
            }
            case ADJUSTMENT -> quantity;
        };
    }
}
