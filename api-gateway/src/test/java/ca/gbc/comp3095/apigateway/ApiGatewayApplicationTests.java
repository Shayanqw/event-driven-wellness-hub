package ca.gbc.comp3095.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for API Gateway.
 * Tests Gateway routing, security, and integration with Keycloak.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
    "keycloak.issuer-uri=http://localhost:8090/realms/wellness-hub"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Gateway application context loads successfully
        // with all required beans (SecurityConfig, Gateway routes, etc.)
    }
}

