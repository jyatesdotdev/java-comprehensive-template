package com.example.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot entry point, not a utility class
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
