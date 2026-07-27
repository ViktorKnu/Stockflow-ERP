package com.stockflow.exception;

import com.stockflow.config.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessRuleResponsePreservesMachineReadableCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/purchase-orders/42/receive");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE_NAME, "handler-test-123");
        BusinessRuleException exception = new BusinessRuleException(
                ApiErrorCode.PURCHASE_ORDER_ALREADY_RECEIVED,
                "Purchase order has already been received");

        ResponseEntity<ApiError> response = handler.handleBusinessRule(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.PURCHASE_ORDER_ALREADY_RECEIVED);
        assertThat(response.getBody().message()).isEqualTo("Purchase order has already been received");
        assertThat(response.getBody().path()).isEqualTo("/api/purchase-orders/42/receive");
        assertThat(response.getBody().correlationId()).isEqualTo("handler-test-123");
    }

    @Test
    void notFoundResponseUsesStableGenericCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/999");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE_NAME, "handler-test-404");

        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("Product not found: 999"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void optimisticLockingFailureReturnsStableConflictResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/sales-orders/42/ship");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE_NAME, "handler-test-conflict");

        ResponseEntity<ApiError> response = handler.handleOptimisticLockingFailure(
                new ObjectOptimisticLockingFailureException("SalesOrder", 42L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.CONCURRENT_MODIFICATION);
        assertThat(response.getBody().message())
                .isEqualTo("The resource was changed by another request. Reload it and try again");
        assertThat(response.getBody().path()).isEqualTo("/api/sales-orders/42/ship");
        assertThat(response.getBody().correlationId()).isEqualTo("handler-test-conflict");
    }
}
