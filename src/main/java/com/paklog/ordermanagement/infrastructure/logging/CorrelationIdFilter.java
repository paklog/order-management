package com.paklog.ordermanagement.infrastructure.logging;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ID_KEY = "userId";
    public static final String REQUEST_ID_KEY = "requestId";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract correlation ID
            String correlationId = getCorrelationId(httpRequest);
            String userId = getUserId(httpRequest);
            String requestId = UUID.randomUUID().toString();
            
            // Add to MDC for logging
            MDC.put(CORRELATION_ID_KEY, correlationId);
            MDC.put(REQUEST_ID_KEY, requestId);
            if (userId != null) {
                MDC.put(USER_ID_KEY, userId);
            }
            
            // Add correlation ID to response header
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            logger.debug("Request started - Method: {}, URI: {}, Correlation-ID: {}, User-ID: {}", 
                    httpRequest.getMethod(), 
                    httpRequest.getRequestURI(), 
                    correlationId, 
                    userId);
            
            // Continue with request processing
            chain.doFilter(request, response);
            
            logger.debug("Request completed - Status: {}, Correlation-ID: {}", 
                    httpResponse.getStatus(), 
                    correlationId);
            
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }
    
    @Override
    public void destroy() {
        // No cleanup needed
    }
    
    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
    
    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        // In a real application, this might come from JWT token or session
        return userId;
    }
}