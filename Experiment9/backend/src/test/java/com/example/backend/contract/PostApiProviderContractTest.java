package com.example.backend.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.example.backend.entity.ScheduledPost;
import com.example.backend.repository.ScheduledPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDateTime;

@Provider("scheduled-post-api")
@PactFolder("target/pacts")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.admin.fail-fast=false"
        })
class PostApiProviderContractTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ScheduledPostRepository repository;

    @BeforeEach
    void configureProviderTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @State("scheduled posts exist")
    void createScheduledPost() {
        repository.deleteAll();
        repository.save(post("Draft post"));
    }

    @State("a post can be scheduled")
    void clearScheduledPosts() {
        repository.deleteAll();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    private ScheduledPost post(String content) {
        ScheduledPost post = new ScheduledPost();
        post.setContent(content);
        post.setScheduledTime(LocalDateTime.of(2030, 1, 1, 12, 0));
        post.setStatus("PENDING");
        return post;
    }
}
