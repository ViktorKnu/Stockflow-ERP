package com.stockflow.supplier;

import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.supplier.dto.SupplierCreateRequest;
import com.stockflow.supplier.dto.SupplierResponse;
import com.stockflow.supplier.dto.SupplierUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(SupplierMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return SupplierMapper.toResponse(getSupplier(id));
    }

    @Transactional
    public SupplierResponse create(SupplierCreateRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .build();

        return SupplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierUpdateRequest request) {
        Supplier supplier = getSupplier(id);
        supplier.setName(request.name());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        return SupplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = getSupplier(id);
        supplierRepository.delete(supplier);
    }

    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }
}
