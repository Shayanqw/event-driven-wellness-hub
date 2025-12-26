package ca.gbc.comp3095.wellnessresourceservice.controller;

import ca.gbc.comp3095.wellnessresourceservice.model.WellnessResource;
import ca.gbc.comp3095.wellnessresourceservice.repository.WellnessResourceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable; // Required for Caching
import org.springframework.cache.annotation.CacheEvict; // Required for Cache Eviction

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Wellness Resources", description = "API for managing wellness resources")
public class WellnessResourceController {

    @Autowired
    private WellnessResourceRepository repository;

    // READ: Caching the resource list (Day 3)
    @Operation(summary = "Get all wellness resources", description = "Retrieve all wellness resources, optionally filtered by eventId or category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved resources")
    })
    @Cacheable("resources")
    @GetMapping
    public List<WellnessResource> getAllResources(@RequestParam(required = false) Long eventId,
                                                   @RequestParam(required = false) String category) {
        System.out.println("--- FETCHING ALL RESOURCES FROM DATABASE (Slow) ---");
        if (category != null && !category.isEmpty()) {
            return repository.findByCategory(category);
        }
        // If eventId is provided, return all resources (can be filtered later if needed)
        return repository.findAll();
    }

    // READ: Caching filtered resources (Day 3)
    @Operation(summary = "Get resources by category", description = "Retrieve wellness resources filtered by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved resources")
    })
    @Cacheable(value = "resources", key = "#category")
    @GetMapping("/category/{category}")
    public List<WellnessResource> getResourcesByCategory(@PathVariable String category) {
        System.out.println("--- FETCHING RESOURCES BY CATEGORY FROM DATABASE (Slow) ---");
        return repository.findByCategory(category);
    }

    // WRITE: Evicts the cache when a new resource is created (Day 3)
    @Operation(summary = "Create a new wellness resource", description = "Create a new wellness resource (requires STAFF role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resource created successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - STAFF role required")
    })
    @CacheEvict(value = "resources", allEntries = true)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WellnessResource createResource(@RequestBody WellnessResource resource) {
        return repository.save(resource);
    }

    // WRITE: Evicts the cache when a resource is updated (Day 3)
    @CacheEvict(value = "resources", allEntries = true)
    @PutMapping("/{id}")
    public WellnessResource updateResource(@PathVariable Long id, @RequestBody WellnessResource resource) {
        resource.setId(id);
        return repository.save(resource);
    }

    // WRITE: Evicts the cache when a resource is deleted (Day 3)
    @CacheEvict(value = "resources", allEntries = true)
    @DeleteMapping("/{id}")
    public void deleteResource(@PathVariable Long id) {
        repository.deleteById(id);
    }
}