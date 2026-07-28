package com.stockflow.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    Page<InventoryMovement> findByProductId(Long productId, Pageable pageable);
}
