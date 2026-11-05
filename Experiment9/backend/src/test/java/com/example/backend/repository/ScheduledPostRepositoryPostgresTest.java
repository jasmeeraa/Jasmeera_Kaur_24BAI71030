package com.example.backend.repository;

import com.example.backend.entity.ScheduledPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ScheduledPostRepositoryPostgresTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ScheduledPostRepository repository;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Test
    void savesAndQueriesDuePostsInPostgres() {
        LocalDateTime dueTime = LocalDateTime.now().minusMinutes(1);
        ScheduledPost post = new ScheduledPost();
        post.setContent("Post stored in PostgreSQL");
        post.setScheduledTime(dueTime);
        post.setStatus("PENDING");

        ScheduledPost savedPost = repository.saveAndFlush(post);
        List<ScheduledPost> duePosts =
                repository.findByStatusAndScheduledTimeBefore("PENDING", LocalDateTime.now());

        assertThat(savedPost.getId()).isNotNull();
        assertThat(duePosts).extracting(ScheduledPost::getId).contains(savedPost.getId());
        assertThat(repository.findById(savedPost.getId()))
                .get()
                .extracting(ScheduledPost::getContent)
                .isEqualTo("Post stored in PostgreSQL");
    }
}
