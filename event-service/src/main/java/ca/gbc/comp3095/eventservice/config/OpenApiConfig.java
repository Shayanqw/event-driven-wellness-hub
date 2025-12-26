package ca.gbc.comp3095.eventservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Service API")
                        .description("API for managing wellness events and registrations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Wellness Hub Team")
                                .email("events@gbc.ca")));
    }
}

