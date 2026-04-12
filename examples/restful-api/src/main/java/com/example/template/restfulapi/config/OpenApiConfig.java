package com.example.template.restfulapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the OpenAPI 3.0 specification metadata for Swagger UI. */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI specification with project metadata.
     *
     * @return configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product API")
                        .version("1.0.0")
                        .description("CRUD API for product management")
                        .contact(new Contact().name("Team").email("team@example.com"))
                        .license(new License().name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
