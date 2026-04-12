package com.example.template.restfulapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point for the RESTful API example application. */
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot entry point, not a utility class
public class RestApiApplication {

    /**
     * Launches the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
    }
}
