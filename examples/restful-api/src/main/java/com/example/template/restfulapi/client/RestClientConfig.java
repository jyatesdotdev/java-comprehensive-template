package com.example.template.restfulapi.client;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Spring configuration for REST client beans.
 *
 * <p>Demonstrates production-ready configuration with timeouts, base URLs,
 * and default headers. In real applications, externalize the base URL
 * to {@code application.yml}.
 */
@Configuration
public class RestClientConfig {

    /**
     * RestTemplate with timeouts via RestTemplateBuilder (preferred over {@code new RestTemplate()}).
     *
     * @param builder the auto-configured builder
     * @return configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * RestClient (Spring 6.1+) — modern fluent alternative to RestTemplate.
     *
     * @param restTemplate the underlying RestTemplate to wrap
     * @return configured RestClient
     */
    @Bean
    public RestClient restClient(RestTemplate restTemplate) {
        return RestClient.create(restTemplate);
    }

    /**
     * WebClient with connection and response timeouts.
     *
     * @return configured WebClient
     */
    @Bean
    public WebClient webClient() {
        var httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
