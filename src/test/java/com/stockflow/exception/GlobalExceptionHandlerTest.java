package com.stockflow.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessRuleResponsePreservesMachineReadableCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/purchase-orders/42/receive");
        BusinessRuleException exception = new BusinessRuleException(
                ApiErrorCode.PURCHASE_ORDER_ALREADY_RECEIVED,
                "Purchase order has already been received");

        ResponseEntity<ApiError> response = handler.handleBusinessRule(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.PURCHASE_ORDER_ALREADY_RECEIVED);
        assertThat(response.getBody().message()).isEqualTo("Purchase order has already been received");
        assertThat(response.getBody().path()).isEqualTo("/api/purchase-orders/42/receive");
    }

    @Test
    void notFoundResponseUsesStableGenericCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/999");

        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("Product not found: 999"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ApiErrorCode.RESOURCE_NOT_FOUND);
    }
}
