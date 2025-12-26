package ca.gbc.comp3095.eventservice.service;

import ca.gbc.comp3095.eventservice.dto.ResourceDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class ResourceClient {

    private static final Logger logger = LoggerFactory.getLogger(ResourceClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "resourceService";
    
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ResourceClient(RestTemplate restTemplate,
                          @Value("${resource.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findResourcesForEventFallback")
    public List<ResourceDto> findResourcesForEvent(Long eventId) {
        logger.info("Fetching resources for event: {}", eventId);
        URI uri = URI.create(baseUrl + "/api/resources?eventId=" + eventId);
        ResponseEntity<List<ResourceDto>> resp = restTemplate.exchange(
                RequestEntity.get(uri).build(),
                new ParameterizedTypeReference<List<ResourceDto>>() {}
        );
        return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
    }

    /**
     * Fallback method when circuit breaker is open or service fails.
     */
    private List<ResourceDto> findResourcesForEventFallback(Long eventId, Exception e) {
        logger.warn("Circuit breaker fallback triggered for event: {}. Error: {}", eventId, e.getMessage());
        // Return empty list as fallback - could also return cached data
        return Collections.emptyList();
    }
}
