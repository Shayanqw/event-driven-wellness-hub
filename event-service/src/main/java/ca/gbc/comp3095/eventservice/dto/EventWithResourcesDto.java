package ca.gbc.comp3095.eventservice.dto;

import ca.gbc.comp3095.eventservice.model.Event;

import java.util.List;

public class EventWithResourcesDto {
    private Event event;
    private List<ResourceDto> resources;

    public EventWithResourcesDto() {}
    public EventWithResourcesDto(Event event, List<ResourceDto> resources) {
        this.event = event;
        this.resources = resources;
    }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public List<ResourceDto> getResources() { return resources; }
    public void setResources(List<ResourceDto> resources) { this.resources = resources; }
}
