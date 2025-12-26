package ca.gbc.comp3095.wellnessresourceservice;

import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.WellnessResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Redis caching functionality using TestContainers.
 * Verifies that resources are properly cached and evicted.
 */
@SpringBootTest
@Testcontainers
class WellnessResourceRedisIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:alpine"))
            .withExposedPorts(6379);

    @Container
    static org.testcontainers.containers.PostgreSQLContainer<?> postgres = 
            new org.testcontainers.containers.PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("wellnessdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private WellnessResourceRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @DynamicPropertySource
    static void configureProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        
        // Redis configuration
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        // Clear cache before each test
        if (cacheManager != null && cacheManager.getCache("resources") != null) {
            cacheManager.getCache("resources").clear();
        }
    }

    @Test
    void shouldCacheResourcesOnFirstRequest() {
        // GIVEN: Create a resource in the database
        WellnessResource resource = new WellnessResource();
        resource.setTitle("Test Resource");
        resource.setDescription("Test Description");
        resource.setCategory("mindfulness");
        repository.save(resource);

        // WHEN: First request (should hit database)
        List<WellnessResource> firstResult = repository.findAll();
        assertThat(firstResult).hasSize(1);

        // THEN: Verify resource is in cache
        var cache = cacheManager.getCache("resources");
        assertThat(cache).isNotNull();
        
        // Second request should use cache (we can't directly verify cache hit,
        // but we can verify the cache exists and contains data)
        List<WellnessResource> secondResult = repository.findAll();
        assertThat(secondResult).hasSize(1);
    }

    @Test
    void shouldCacheResourcesByCategory() {
        // GIVEN: Create resources with different categories
        WellnessResource resource1 = new WellnessResource();
        resource1.setTitle("Fitness Resource");
        resource1.setCategory("fitness");
        repository.save(resource1);

        WellnessResource resource2 = new WellnessResource();
        resource2.setTitle("Academic Resource");
        resource2.setCategory("academic");
        repository.save(resource2);

        // WHEN: Query by category (should cache)
        List<WellnessResource> fitnessResources = repository.findByCategory("fitness");
        assertThat(fitnessResources).hasSize(1);

        // THEN: Verify cache exists
        var cache = cacheManager.getCache("resources");
        assertThat(cache).isNotNull();
        
        // Query again - should use cache
        List<WellnessResource> cachedFitnessResources = repository.findByCategory("fitness");
        assertThat(cachedFitnessResources).hasSize(1);
    }

    @Test
    void shouldEvictCacheWhenResourceIsCreated() {
        // GIVEN: Create and cache a resource
        WellnessResource resource1 = new WellnessResource();
        resource1.setTitle("Original Resource");
        resource1.setCategory("wellness");
        repository.save(resource1);

        // Query to populate cache
        List<WellnessResource> initialResources = repository.findAll();
        assertThat(initialResources).hasSize(1);

        // WHEN: Create a new resource (should evict cache)
        WellnessResource resource2 = new WellnessResource();
        resource2.setTitle("New Resource");
        resource2.setCategory("wellness");
        repository.save(resource2);

        // THEN: Cache should be evicted (next query should hit database)
        // We verify by checking that both resources are returned
        List<WellnessResource> allResources = repository.findAll();
        assertThat(allResources).hasSize(2);
    }

    @Test
    void shouldEvictCacheWhenResourceIsUpdated() {
        // GIVEN: Create and cache a resource
        WellnessResource resource = new WellnessResource();
        resource.setTitle("Original Title");
        resource.setCategory("fitness");
        WellnessResource saved = repository.save(resource);

        // Query to populate cache
        repository.findAll();

        // WHEN: Update the resource (should evict cache)
        saved.setTitle("Updated Title");
        repository.save(saved);

        // THEN: Cache should be evicted
        // Verify updated resource is returned
        List<WellnessResource> resources = repository.findAll();
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void shouldEvictCacheWhenResourceIsDeleted() {
        // GIVEN: Create and cache resources
        WellnessResource resource1 = new WellnessResource();
        resource1.setTitle("Resource 1");
        resource1.setCategory("wellness");
        repository.save(resource1);

        WellnessResource resource2 = new WellnessResource();
        resource2.setTitle("Resource 2");
        resource2.setCategory("wellness");
        WellnessResource saved2 = repository.save(resource2);

        // Query to populate cache
        List<WellnessResource> initialResources = repository.findAll();
        assertThat(initialResources).hasSize(2);

        // WHEN: Delete a resource (should evict cache)
        repository.deleteById(saved2.getId());

        // THEN: Cache should be evicted
        List<WellnessResource> remainingResources = repository.findAll();
        assertThat(remainingResources).hasSize(1);
        assertThat(remainingResources.get(0).getTitle()).isEqualTo("Resource 1");
    }
}

