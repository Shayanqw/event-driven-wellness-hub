package ca.gbc.comp3095.goaltrackingservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${wellness.resource.base-url:http://wellness-resource-service:8083}")
    private String wellnessBaseUrl;

    @Bean
    public WebClient wellnessWebClient(WebClient.Builder builder) {
        return builder.baseUrl(wellnessBaseUrl).build();
    }
}
