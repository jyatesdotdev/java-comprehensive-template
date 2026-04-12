package com.example.template.testing.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured API testing example using a httpbin container as a test server.
 *
 * <p>Demonstrates: GET/POST requests, JSON path assertions, status code checks,
 * content type validation, and request/response logging.
 *
 * <p>Run with: {@code mvn verify -pl examples/testing -P integration-tests}
 */
@Testcontainers
@DisplayName("REST Assured API Testing")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.AvoidDuplicateLiterals"}) // REST-assured uses fluent assertions
class RestAssuredIT {

    @Container
    static final GenericContainer<?> HTTPBIN = new GenericContainer<>(
            DockerImageName.parse("kennethreitz/httpbin:latest"))
            .withExposedPorts(80)
            .waitingFor(Wait.forHttp("/get").forStatusCode(200));

    @BeforeAll
    static void configureBaseUri() {
        RestAssured.baseURI = "http://" + HTTPBIN.getHost();
        RestAssured.port = HTTPBIN.getFirstMappedPort();
    }

    @AfterAll
    static void resetRestAssured() {
        RestAssured.reset();
    }

    @Test
    @DisplayName("GET request returns 200 and JSON body")
    void getRequest() {
        given()
                .queryParam("foo", "bar")
        .when()
                .get("/get")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("args.foo", equalTo("bar"))
                .body("url", containsString("/get"));
    }

    @Test
    @DisplayName("POST JSON body and validate echo")
    void postJsonBody() {
        String payload = """
                {"name": "Alice", "email": "alice@example.com"}
                """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/post")
        .then()
                .statusCode(200)
                .body("json.name", equalTo("Alice"))
                .body("json.email", equalTo("alice@example.com"));
    }

    @Test
    @DisplayName("Validate response headers")
    void responseHeaders() {
        when()
                .get("/response-headers?X-Custom=hello")
        .then()
                .statusCode(200)
                .header("X-Custom", "hello");
    }

    @Test
    @DisplayName("Status code endpoints")
    void statusCodes() {
        when().get("/status/404").then().statusCode(404);
        when().get("/status/500").then().statusCode(500);
    }

    @Test
    @DisplayName("Request with logging (useful for debugging)")
    void withLogging() {
        given()
                .log().method()
                .log().uri()
        .when()
                .get("/get")
        .then()
                .log().status()
                .statusCode(200);
    }
}
