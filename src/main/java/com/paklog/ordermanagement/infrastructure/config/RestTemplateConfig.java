package com.paklog.ordermanagement.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for RestTemplate used by external service adapters.
 * Configures timeouts and error handling for HTTP clients.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with sensible defaults for external service calls.
     *
     * Timeouts:
     * - Connect timeout: 2 seconds
     * - Read timeout: 3 seconds
     *
     * This ensures that validation checks fail fast if external services are slow or unavailable.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(3))
            .build();
    }
}
