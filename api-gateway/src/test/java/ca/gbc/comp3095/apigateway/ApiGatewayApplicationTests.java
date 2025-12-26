package ca.gbc.comp3095.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.gateway.routes[0].id=test-route",
    "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/**",
    "keycloak.issuer-uri=http://localhost:8090/realms/wellness-hub"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
