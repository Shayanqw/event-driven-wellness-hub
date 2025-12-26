package ca.gbc.comp3095.eventservice;

import ca.gbc.comp3095.eventservice.model.Event;
import ca.gbc.comp3095.eventservice.repository.EventRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerIT {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("eventdb")
            .withUsername("event_user")
            .withPassword("password");

    static WireMockServer wiremock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        wiremock = new WireMockServer(9099);
        wiremock.start();
        configureFor("localhost", 9099);
        r.add("resource.base-url", () -> "http://localhost:9099");
    }

    @AfterAll
    static void stopWiremock() {
        if (wiremock != null && wiremock.isRunning()) {
            wiremock.stop();
            wiremock = null;
        }
    }

    @LocalServerPort int port;

    @Autowired TestRestTemplate rest;
    @Autowired EventRepository events;

    @Test
    void crud_and_resources_and_registration_flow() {
        // create event
        Event e = new Event("Mile Run");
        e.setStartsAt(OffsetDateTime.parse("2026-01-10T10:00:00Z"));
        e.setLocation("Toronto");
        e = events.save(e);

        // --- mock external resources (no text blocks) ---
        String stubJson =
                "[{\"id\":101,\"name\":\"Water Station\",\"type\":\"facility\",\"info\":\"near start\"}," +
                        " {\"id\":102,\"name\":\"Physio\",\"type\":\"service\",\"info\":\"finish line\"}]";

        wiremock.stubFor(get(urlPathEqualTo("/resources"))
                .withQueryParam("eventId", equalTo(String.valueOf(e.getId())))
                .willReturn(okJson(stubJson)));

        // call resources endpoint
        ResponseEntity<String> res = rest.getForEntity(
                "http://localhost:" + port + "/events/" + e.getId() + "/resources",
                String.class);
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
        Assertions.assertTrue(res.getBody().contains("Water Station"));

        // register attendee (no text blocks)
        String payload = "{\"attendeeName\":\"Alice\",\"attendeeEmail\":\"alice@example.com\"}";
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> regRes = rest.postForEntity(
                "http://localhost:" + port + "/events/" + e.getId() + "/registrations",
                new HttpEntity<>(payload, h),
                String.class);
        Assertions.assertEquals(HttpStatus.CREATED, regRes.getStatusCode());

        // list registrations
        ResponseEntity<String> listRes = rest.getForEntity(
                "http://localhost:" + port + "/events/" + e.getId() + "/registrations",
                String.class);
        Assertions.assertEquals(HttpStatus.OK, listRes.getStatusCode());
        Assertions.assertTrue(listRes.getBody().contains("alice@example.com"));
    }
}
