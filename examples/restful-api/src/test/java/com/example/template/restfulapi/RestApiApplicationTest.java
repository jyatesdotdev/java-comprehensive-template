package com.example.template.restfulapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/** Smoke test that the Docker-built REST example application starts a Spring context. */
@SpringBootTest
class RestApiApplicationTest {

  @Autowired private ApplicationContext context;

  @Test
  @DisplayName("application context loads")
  void contextLoads() {
    assertThat(context.getBean(RestApiApplication.class)).isNotNull();
  }
}
