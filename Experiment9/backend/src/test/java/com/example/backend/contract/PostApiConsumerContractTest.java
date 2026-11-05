package com.example.backend.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
class PostApiConsumerContractTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Pact(consumer = "frontend", provider = "scheduled-post-api")
    public V4Pact getScheduledPosts(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("scheduled posts exist")
                .uponReceiving("a request to list scheduled posts")
                .path("/api/posts")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonArray()
                        .object()
                        .integerType("id", 7)
                        .stringType("content", "Draft post")
                        .stringType("scheduledTime", "2030-01-01T12:00:00")
                        .stringValue("status", "PENDING")
                        .closeObject())
                .toPact()
                .asV4Pact()
                .component1();
    }

    @Pact(consumer = "frontend", provider = "scheduled-post-api")
    public V4Pact schedulePost(PactBuilder builder) {
        return builder.usingLegacyDsl()
                .given("a post can be scheduled")
                .uponReceiving("a request to schedule a post")
                .path("/api/posts/schedule")
                .method("POST")
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .stringType("content", "New post")
                        .stringType("scheduledTime", "2030-01-01T12:00:00"))
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .integerType("id", 8)
                        .stringType("content", "New post")
                        .stringType("scheduledTime", "2030-01-01T12:00:00")
                        .stringValue("status", "PENDING"))
                .toPact()
                .asV4Pact()
                .component1();
    }

    @Test
    @PactTestFor(pactMethod = "getScheduledPosts")
    void frontendCanReadScheduledPosts(MockServer mockServer) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(mockServer.getUrl() + "/api/posts"))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(new ObjectMapper().readTree(
                        "[{\"id\":7,\"content\":\"Draft post\",\"scheduledTime\":\"2030-01-01T12:00:00\",\"status\":\"PENDING\"}]"),
                new ObjectMapper().readTree(response.body()));
    }

    @Test
    @PactTestFor(pactMethod = "schedulePost")
    void frontendCanScheduleAPost(MockServer mockServer) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(mockServer.getUrl() + "/api/posts/schedule"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"content\":\"New post\",\"scheduledTime\":\"2030-01-01T12:00:00\"}"))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(new ObjectMapper().readTree(
                        "{\"id\":8,\"content\":\"New post\",\"scheduledTime\":\"2030-01-01T12:00:00\",\"status\":\"PENDING\"}"),
                new ObjectMapper().readTree(response.body()));
    }
}
