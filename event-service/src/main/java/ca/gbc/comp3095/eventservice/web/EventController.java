package ca.gbc.comp3095.eventservice.web;

import ca.gbc.comp3095.eventservice.dto.EventWithResourcesDto;
import ca.gbc.comp3095.eventservice.dto.ResourceDto;
import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.model.Registration;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import ca.gbc.comp3095.eventservice.repository.RegistrationRepository;
import ca.gbc.comp3095.eventservice.service.ResourceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils; // at top

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "API for managing wellness events and registrations")
public class EventController {

    private final EventRepository events;
    private final RegistrationRepository registrations;
    private final ResourceClient resourceClient;

    public EventController(EventRepository events,
                           RegistrationRepository registrations,
                           ResourceClient resourceClient) {
        this.events = events;
        this.registrations = registrations;
        this.resourceClient = resourceClient;
    }

    // GET /events with optional filters: start, end, location
    @Operation(summary = "Get all events", description = "Retrieve all events, optionally filtered by date range and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
        @ApiResponse(responseCode = "400", description = "Invalid date parameters")
    })
    @GetMapping
    public List<Event> getAll(
            @RequestParam(required = false) OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end,
            @RequestParam(required = false) String location) {

        if ((start == null) != (end == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provide both 'start' and 'end' or neither.");
        }

        boolean hasDates = (start != null && end != null);
        boolean hasLoc   = StringUtils.hasText(location); // null-safe, trims whitespace

        if (hasDates && hasLoc) {
            return events.findByStartsAtBetweenAndLocationContainingIgnoreCase(start, end, location);
        }
        if (hasDates) {
            return events.findByStartsAtBetween(start, end);
        }
        if (hasLoc) {
            return events.findByLocationContainingIgnoreCase(location);
        }
        return events.findAll();
    }

    // POST /events
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@RequestBody Event newEvent) {
        return events.save(newEvent);
    }

    // GET /events/{id}
    @GetMapping("/{id}")
    public Event getOne(@PathVariable Long id) {
        return events.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    // PUT /events/{id}
    @PutMapping({"/{id}", "/{id}/"})
    public Event update(@PathVariable Long id, @RequestBody Event incoming) {
        Event existing = events.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (incoming.getTitle() != null) existing.setTitle(incoming.getTitle());
        if (incoming.getStartsAt() != null) existing.setStartsAt(incoming.getStartsAt());
        if (incoming.getLocation() != null) existing.setLocation(incoming.getLocation());

        return events.save(existing);
    }

    // DELETE /events/{id}
    @DeleteMapping({"/{id}", "/{id}/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!events.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        events.deleteById(id);
    }

    // --- Registration endpoints ---

    // POST /events/{id}/registrations
    @PostMapping("/{id}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public Registration register(@PathVariable Long id, @RequestBody Registration payload) {
        Event event = events.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (payload.getAttendeeName() == null || payload.getAttendeeEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and email are required");
        }
        Registration reg = new Registration(event, payload.getAttendeeName(), payload.getAttendeeEmail());
        return registrations.save(reg);
    }

    // GET /events/{id}/registrations
    @GetMapping("/{id}/registrations")
    public List<Registration> registrations(@PathVariable Long id) {
        if (!events.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return registrations.findByEvent_Id(id);
    }

    // --- Resource linking endpoint ---

    // GET /events/{id}/resources  -> event + resources from external service
    @GetMapping("/{id}/resources")
    public EventWithResourcesDto resources(@PathVariable Long id) {
        Event event = events.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        List<ResourceDto> resources = resourceClient.findResourcesForEvent(id);
        return new EventWithResourcesDto(event, resources);
    }
}
