package com.stockflow.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsBySupplierId(Long supplierId);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    @Query("select p from Product p where p.quantity <= p.minimumStock")
    List<Product> findLowStockProducts();
}
