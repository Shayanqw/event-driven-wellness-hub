package ca.gbc.comp3095.wellnessresourceservice;

import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.WellnessResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Wellness Resource Service using TestContainers for PostgreSQL.
 * Replaces static test configuration with dynamic TestContainers setup.
 */
@SpringBootTest
@Testcontainers
public class WellnessResourceServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("wellnessdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private WellnessResourceRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        // Disable Redis for this test (not needed for basic database tests)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void contextLoadsAndDatabaseIsConnected() {
        // Confirms Spring context loads and services are autowired
        assertThat(repository).isNotNull();
    }

    // Required integration test (Day 4)
    @Test
    void shouldFindAllResourcesWhenDatabaseIsPopulated() {
        // Arrange
        WellnessResource resource = new WellnessResource();
        resource.setTitle("Test Resource");
        resource.setCategory("mindfulness");
        repository.save(resource);

        // Act
        List<WellnessResource> foundResources = repository.findAll();

        // Assert
        assertThat(foundResources).isNotEmpty();
        assertThat(foundResources).extracting(WellnessResource::getTitle).contains("Test Resource");
    }

    @Test
    void shouldCreateAndRetrieveResource() {
        // GIVEN
        WellnessResource resource = new WellnessResource();
        resource.setTitle("New Resource");
        resource.setDescription("Test Description");
        resource.setCategory("fitness");

        // WHEN
        WellnessResource saved = repository.save(resource);
        List<WellnessResource> allResources = repository.findAll();

        // THEN
        assertThat(saved.getId()).isNotNull();
        assertThat(allResources).hasSize(1);
        assertThat(allResources.get(0).getTitle()).isEqualTo("New Resource");
    }

    @Test
    void shouldFindResourcesByCategory() {
        // GIVEN
        WellnessResource resource1 = new WellnessResource();
        resource1.setTitle("Fitness Resource");
        resource1.setCategory("fitness");
        repository.save(resource1);

        WellnessResource resource2 = new WellnessResource();
        resource2.setTitle("Academic Resource");
        resource2.setCategory("academic");
        repository.save(resource2);

        // WHEN
        List<WellnessResource> fitnessResources = repository.findByCategory("fitness");

        // THEN
        assertThat(fitnessResources).hasSize(1);
        assertThat(fitnessResources.get(0).getTitle()).isEqualTo("Fitness Resource");
    }
}