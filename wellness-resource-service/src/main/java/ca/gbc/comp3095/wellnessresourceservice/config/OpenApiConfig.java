package ca.gbc.comp3095.wellnessresourceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wellnessResourceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wellness Resource Service API")
                        .description("API for managing wellness resources including academic, fitness, and mental health resources")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Wellness Hub Team")
                                .email("wellness@gbc.ca")));
    }
}

