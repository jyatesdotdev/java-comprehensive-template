package com.example.template.restfulapi.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.template.restfulapi.exception.ClientException;
import com.example.template.restfulapi.exception.ResourceNotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.CloseResource", "unchecked"})
class ProductClientExceptionTest {

    private static final UUID TEST_ID = UUID.randomUUID();

    @Test
    void jaxRsGetShouldThrowResourceNotFoundExceptionOn404() {
        Client jaxRsClient = mockJaxRsClient(404);
        ProductJaxRsClient client = new ProductJaxRsClient("http://localhost");
        ReflectionTestUtils.setField(client, "client", jaxRsClient);

        assertThatThrownBy(() -> client.get(TEST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void jaxRsDeleteShouldThrowClientExceptionOnFailure() {
        Client jaxRsClient = mockJaxRsClient(500);
        ProductJaxRsClient client = new ProductJaxRsClient("http://localhost");
        ReflectionTestUtils.setField(client, "client", jaxRsClient);

        assertThatThrownBy(() -> client.delete(TEST_ID))
                .isInstanceOf(ClientException.class);
    }

    @Test
    void restClientGetShouldThrowResourceNotFoundExceptionOn404() {
        RestClient mockRestClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec mockUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec mockResponseSpec = mock(RestClient.ResponseSpec.class);

        when(mockRestClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(UUID.class))).thenReturn(mockUriSpec);
        when(mockUriSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            java.util.function.Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
            if (predicate.test(HttpStatusCode.valueOf(404))) {
                RestClient.ResponseSpec.ErrorHandler handler = invocation.getArgument(1);
                handler.handle(null, null);
            }
            return mockResponseSpec;
        });

        ProductRestTemplateClient client = new ProductRestTemplateClient("http://localhost");
        ReflectionTestUtils.setField(client, "restClient", mockRestClient);

        assertThatThrownBy(() -> client.getWithRestClient(TEST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void webClientGetReactiveShouldErrorWithResourceNotFoundExceptionOn404() {
        ExchangeFunction exchangeFunction = request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        ProductWebClientExample client = new ProductWebClientExample("http://localhost");
        ReflectionTestUtils.setField(client, "webClient", webClient);

        assertThatThrownBy(() -> client.getReactive(TEST_ID).block())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Client mockJaxRsClient(int statusCode) {
        Client mockClient = mock(Client.class);
        WebTarget mockTarget = mock(WebTarget.class);
        Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
        Response mockResponse = mock(Response.class);

        when(mockClient.target(anyString())).thenReturn(mockTarget);
        when(mockTarget.path(anyString())).thenReturn(mockTarget);
        when(mockTarget.resolveTemplate(anyString(), any())).thenReturn(mockTarget);
        when(mockTarget.request(anyString())).thenReturn(mockBuilder);
        when(mockTarget.request()).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResponse);
        when(mockBuilder.delete()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(statusCode);

        return mockClient;
    }
}
