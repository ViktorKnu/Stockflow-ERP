package com.stockflow.supplier;

import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.supplier.dto.SupplierCreateRequest;
import com.stockflow.supplier.dto.SupplierResponse;
import com.stockflow.supplier.dto.SupplierUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void canCreateSupplier() {
        SupplierCreateRequest request = new SupplierCreateRequest(
                "Nordic Supplies AS",
                "orders@nordic.example",
                "+47 22 00 00 00",
                "Oslo"
        );

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setId(1L);
            return supplier;
        });

        SupplierResponse response = supplierService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Nordic Supplies AS");
        assertThat(response.email()).isEqualTo("orders@nordic.example");
    }

    @Test
    void canFindAllSuppliers() {
        when(supplierRepository.findAll()).thenReturn(List.of(supplier()));

        List<SupplierResponse> suppliers = supplierService.findAll();

        assertThat(suppliers).hasSize(1);
        assertThat(suppliers.getFirst().name()).isEqualTo("Nordic Supplies AS");
    }

    @Test
    void canUpdateSupplier() {
        Supplier supplier = supplier();
        SupplierUpdateRequest request = new SupplierUpdateRequest(
                "Nordic Supplies Norge AS",
                "sales@nordic.example",
                "+47 22 00 00 01",
                "Bergen"
        );

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        SupplierResponse response = supplierService.update(1L, request);

        assertThat(response.name()).isEqualTo("Nordic Supplies Norge AS");
        assertThat(response.email()).isEqualTo("sales@nordic.example");
        assertThat(response.address()).isEqualTo("Bergen");
    }

    @Test
    void throwsWhenSupplierDoesNotExist() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    @Test
    void canDeleteSupplier() {
        Supplier supplier = supplier();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        supplierService.delete(1L);

        verify(supplierRepository).delete(supplier);
    }

    private Supplier supplier() {
        return Supplier.builder()
                .id(1L)
                .name("Nordic Supplies AS")
                .email("orders@nordic.example")
                .phone("+47 22 00 00 00")
                .address("Oslo")
                .build();
    }
}
