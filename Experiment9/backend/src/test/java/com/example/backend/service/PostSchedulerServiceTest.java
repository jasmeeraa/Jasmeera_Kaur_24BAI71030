package com.example.backend.service;

import com.example.backend.entity.ScheduledPost;
import com.example.backend.repository.ScheduledPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSchedulerServiceTest {

    @Mock
    private ScheduledPostRepository repository;

    @Mock
    private KafkaProducerService producerService;

    @InjectMocks
    private PostSchedulerService schedulerService;

    private ScheduledPost duePost;

    @BeforeEach
    void setUp() {
        duePost = new ScheduledPost();
        duePost.setId(42L);
        duePost.setContent("A scheduled update");
        duePost.setScheduledTime(LocalDateTime.now().minusMinutes(1));
        duePost.setStatus("PENDING");
    }

    @Test
    void publishesDuePostsAndMarksThemPublishing() {
        when(repository.findByStatusAndScheduledTimeBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(duePost));

        schedulerService.checkAndPublishDuePosts();

        verify(producerService).publishPostEvent(duePost);
        verify(repository).save(duePost);
        org.junit.jupiter.api.Assertions.assertEquals("PUBLISHING", duePost.getStatus());
    }

    @Test
    void doesNothingWhenThereAreNoDuePosts() {
        when(repository.findByStatusAndScheduledTimeBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        schedulerService.checkAndPublishDuePosts();

        verifyNoInteractions(producerService);
        verify(repository, never()).save(any());
    }

    @Test
    void propagatesRepositoryFailuresWithoutPublishing() {
        when(repository.findByStatusAndScheduledTimeBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, schedulerService::checkAndPublishDuePosts);

        verifyNoInteractions(producerService);
        verify(repository, never()).save(any());
    }

    @Test
    void doesNotMarkPostPublishingWhenKafkaPublishFails() {
        when(repository.findByStatusAndScheduledTimeBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(duePost));
        doThrow(new IllegalStateException("kafka unavailable"))
                .when(producerService).publishPostEvent(duePost);

        assertThrows(IllegalStateException.class, schedulerService::checkAndPublishDuePosts);

        org.junit.jupiter.api.Assertions.assertEquals("PENDING", duePost.getStatus());
        verify(repository, never()).save(any());
    }
}
