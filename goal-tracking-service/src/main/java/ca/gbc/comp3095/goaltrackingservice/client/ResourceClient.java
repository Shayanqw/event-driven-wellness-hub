package ca.gbc.comp3095.goaltrackingservice.client;

import ca.gbc.comp3095.goaltrackingservice.dto.ResourceDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class ResourceClient {

    private static final Logger logger = LoggerFactory.getLogger(ResourceClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "resourceService";
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    // timeout values
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    // Use WebClient.Builder injection so Spring Boot can configure it.
    public ResourceClient(WebClient.Builder builder,
                          CircuitBreakerRegistry circuitBreakerRegistry,
                          @org.springframework.beans.factory.annotation.Value("${wellness.resource.base-url:http://wellness-resource-service:8083}") String baseUrl) {
        // When running in docker-compose, use the service name: wellness-resource-service:8083
        // When running locally against AJ's localhost service, you can use http://localhost:8081 (override via property).
        this.webClient = builder.baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
    }

    /**
     * Fetches resources by category with Circuit Breaker protection.
     * Returns empty list on failure (fallback).
     */
    public List<ResourceDTO> getResourcesByCategory(String category) {
        logger.info("Fetching resources for category: {}", category);
        
        try {
            List<ResourceDTO> result = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/resources")
                            .queryParam("category", category)
                            .build())
                    .retrieve()
                    .bodyToFlux(ResourceDTO.class)
                    .transform(CircuitBreakerOperator.of(circuitBreaker))
                    .collectList()
                    .block(REQUEST_TIMEOUT);

            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            logger.warn("Circuit breaker fallback triggered for category: {}. Error: {}", category, e.getMessage());
            return getResourcesByCategoryFallback(category, e);
        }
    }

    /**
     * Fallback method when circuit breaker is open or service fails.
     */
    private List<ResourceDTO> getResourcesByCategoryFallback(String category, Exception e) {
        logger.warn("Circuit breaker fallback triggered for category: {}. Error: {}", category, e.getMessage());
        // Return empty list as fallback - could also return cached data
        return Collections.emptyList();
    }
}
