package com.cg.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TitleEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    // ── GET /api/titles ─────────────────────────────────────────

    @Test
    void gettitles_returns200() throws Exception {
        mockMvc.perform(get("/api/titles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page").exists());
    }

    // ── GET /api/titles/{id} ────────────────────────────────────

    @Test
    void getTitleById_returns200_whenExists() throws Exception {
        mockMvc.perform(get("/api/titles/BU1032"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void getTitleById_returns404_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/titles/XX9999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/titles ────────────────────────────────────────

    @Test
    void postTitle_returns201_whenValid() throws Exception {
        String body = """
                {
                    "titleId": "TE0001",
                    "title": "Test Endpoint Title",
                    "type": "business",
                    "publisher": "/api/publishers/1389",
                    "price": 12.99,
                    "advance": 0.0,
                    "royalty": 10,
                    "ytdSales": 0,
                    "notes": "Endpoint test title",
                    "pubdate": "2024-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post("/api/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }

    @Test
    void postTitle_returns400_whenTitleNameMissing() throws Exception {
        String body = """
                {
                    "titleId": "TE0002",
                    "publisher": "/api/publishers/1389",
                    "price": 12.99,
                    "advance": 0.0,
                    "royalty": 10,
                    "ytdSales": 0,
                    "notes": "Endpoint test title",
                    "pubdate": "2024-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post("/api/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    // ── GET /api/titles/search/findByType ───────────────────────

    @Test
    void findByType_returns200_withResults() throws Exception {
        mockMvc.perform(get("/api/titles/search/findByType")
                        .param("type", "business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void findByType_returns200_withEmptyResult() throws Exception {
        mockMvc.perform(get("/api/titles/search/findByType")
                        .param("type", "cooking"))
                .andExpect(status().isOk());
    }

    // ── GET /api/titles/search/findByPublisherPubId ─────────────

    @Test
    void findByPublisherPubId_returns200_withResults() throws Exception {
        mockMvc.perform(get("/api/titles/search/findByPublisherPubId")
                        .param("pubId", "1389"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void findByPublisherPubId_returns200_withEmptyResult() throws Exception {
        mockMvc.perform(get("/api/titles/search/findByPublisherPubId")
                        .param("pubId", "9999"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/titles/{id} ────────────────────────────────────

    @Test
    void putTitle_returns200_whenValid() throws Exception {
        String body = """
                {
                    "titleId": "BU1032",
                    "title": "Updated Title Name",
                    "type": "business",
                    "publisher": "/api/publishers/1389",
                    "price": 19.99,
                    "advance": 0.0,
                    "royalty": 10,
                    "ytdSales": 0,
                    "notes": "Updated via endpoint",
                    "pubdate": "2024-01-01T00:00:00"
                }
                """;

        mockMvc.perform(put("/api/titles/BU1032")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
            .andExpect(status().is2xxSuccessful());
    }

    // ── PATCH /api/titles/{id} ──────────────────────────────────

    @Test
    void patchTitle_returns200_whenValid() throws Exception {
        String body = """
                {
                    "type": "psychology"
                }
                """;

        mockMvc.perform(patch("/api/titles/BU1032")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/titles/BU1032"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("psychology"));
    }

    // ── DELETE /api/titles/{id} ─────────────────────────────────

    @Test
    void deleteTitle_returns204_whenExists() throws Exception {
        // first create one to delete
        String body = """
                {
                    "titleId": "DE0001",
                    "title": "Title To Delete",
                    "type": "business",
                    "publisher": "/api/publishers/1389",
                    "price": 9.99,
                    "advance": 0.0,
                    "royalty": 5,
                    "ytdSales": 0,
                    "notes": "Delete me",
                    "pubdate": "2024-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post("/api/titles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/titles/DE0001"))
                .andExpect(status().isNoContent());
    }
}