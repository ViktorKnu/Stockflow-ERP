package com.stockflow.product;

import com.stockflow.exception.DuplicateResourceException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.product.dto.ProductCreateRequest;
import com.stockflow.product.dto.ProductResponse;
import com.stockflow.product.dto.ProductUpdateRequest;
import com.stockflow.supplier.Supplier;
import com.stockflow.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .description(request.description())
                .category(request.category())
                .quantity(request.quantity())
                .minimumStock(request.minimumStock())
                .price(request.price())
                .supplier(resolveSupplier(request.supplierId()))
                .build();

        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = getProduct(id);
        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateResourceException("Product SKU already exists: " + request.sku());
        }

        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setQuantity(request.quantity());
        product.setMinimumStock(request.minimumStock());
        product.setPrice(request.price());
        product.setSupplier(resolveSupplier(request.supplierId()));

        return ProductMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
    }
}
