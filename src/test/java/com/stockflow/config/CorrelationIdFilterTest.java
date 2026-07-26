package com.stockflow.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesValidClientCorrelationIdInRequestResponseAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "client-request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                mdcValueInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME))
                .isEqualTo("client-request_123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo("client-request_123");
        assertThat(mdcValueInsideChain.get()).isEqualTo("client-request_123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void generatesCorrelationIdWhenIncomingValueIsUnsafe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "unsafe value\r\nInjected: true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        String generatedId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
        assertThat(generatedId)
                .isNotBlank()
                .doesNotContain("unsafe", "\r", "\n");
        assertThat(request.getAttribute(CorrelationIdFilter.ATTRIBUTE_NAME)).isEqualTo(generatedId);
    }
}
