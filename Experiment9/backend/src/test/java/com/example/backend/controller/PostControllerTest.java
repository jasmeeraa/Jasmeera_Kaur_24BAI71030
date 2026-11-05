package com.example.backend.controller;

import com.example.backend.entity.ScheduledPost;
import com.example.backend.repository.ScheduledPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduledPostRepository repository;

    @Test
    void getsScheduledPosts() throws Exception {
        ScheduledPost post = scheduledPost(7L, "Draft post", "PENDING");
        when(repository.findAll()).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(7)))
                .andExpect(jsonPath("$[0].content", is("Draft post")))
                .andExpect(jsonPath("$[0].scheduledTime", is("2030-01-01T12:00:00")))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    void schedulesPostAndSetsPendingStatus() throws Exception {
        when(repository.save(any(ScheduledPost.class))).thenAnswer(invocation -> {
            ScheduledPost post = invocation.getArgument(0);
            post.setId(8L);
            return post;
        });

        mockMvc.perform(post("/api/posts/schedule")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "New post",
                                  "scheduledTime": "2030-01-01T12:00:00",
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(8)))
                .andExpect(jsonPath("$.content", is("New post")))
                .andExpect(jsonPath("$.scheduledTime", is("2030-01-01T12:00:00")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(repository).save(argThat(post -> "PENDING".equals(post.getStatus())));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/posts/schedule")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\":"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }

    @Test
    void returnsMethodNotAllowedForUnsupportedUpdateRoute() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/posts")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    private ScheduledPost scheduledPost(Long id, String content, String status) {
        ScheduledPost post = new ScheduledPost();
        post.setId(id);
        post.setContent(content);
        post.setScheduledTime(LocalDateTime.of(2030, 1, 1, 12, 0));
        post.setStatus(status);
        return post;
    }
}
