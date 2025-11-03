package com.paklog.ordermanagement.infrastructure.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

/**
 * Configuration for Resilience4j circuit breakers, retries, and other resilience patterns.
 * Provides fault tolerance for external service calls.
 */
@Configuration
public class ResilienceConfig {

    /**
     * Circuit breaker registry with custom configuration for external services.
     *
     * Circuit Breaker Parameters:
     * - Failure Rate Threshold: 50% - Opens circuit if 50% of calls fail
     * - Slow Call Rate Threshold: 50% - Opens if 50% of calls are slow
     * - Slow Call Duration: 3 seconds - Calls taking longer are considered slow
     * - Sliding Window Size: 10 calls - Evaluates last 10 calls
     * - Minimum Calls: 5 - Requires 5 calls before calculating failure rate
     * - Wait Duration in Open State: 30 seconds - Circuit stays open for 30s before attempting recovery
     * - Permitted Calls in Half-Open: 3 - Allows 3 test calls when recovering
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .slowCallRateThreshold(50.0f)
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(Exception.class)
            .build();

        return CircuitBreakerRegistry.of(defaultConfig);
    }

    /**
     * Retry registry with custom configuration for transient failures.
     *
     * Retry Parameters:
     * - Max Attempts: 3 - Retries up to 3 times
     * - Wait Duration: 1 second - Waits 1s between retries
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(Exception.class)
            .build();

        return RetryRegistry.of(defaultConfig);
    }
}
