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
public class RoySchedEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    // ── GET /api/royscheds ──────────────────────────────────────

    @Test
    void getRoyscheds_returns200() throws Exception {
        mockMvc.perform(get("/api/royscheds"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.page").exists());
    }

    // ── GET /api/royscheds/{id} ─────────────────────────────────

    @Test
    void getRoySchedById_returns404_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/royscheds/99999"))
                                .andExpect(status().isNotFound());
    }

    // ── POST /api/royscheds ─────────────────────────────────────

    @Test
    void postRoySched_returns201_whenValid() throws Exception {
        String body = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                    "hirange": 1000,
                    "royalty": 10
                }
                """;

        mockMvc.perform(post("/api/royscheds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
        void postRoySched_returns400_whenRoyaltyNegative() throws Exception {
        String body = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                                        "hirange": 1000,
                                        "royalty": -1
                }
                """;

        mockMvc.perform(post("/api/royscheds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    // ── GET /api/royscheds/search/findByTitleTitleId ────────────

    @Test
    void findByTitleTitleId_returns200_withResults() throws Exception {
        mockMvc.perform(get("/api/royscheds/search/findByTitleTitleId")
                        .param("titleId", "BU1032"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void findByTitleTitleId_returns200_withEmptyResult() throws Exception {
        mockMvc.perform(get("/api/royscheds/search/findByTitleTitleId")
                        .param("titleId", "XX9999"))
                .andExpect(status().isOk());
    }

    // ── GET /api/royscheds/search/findByRoyaltyGreaterThanEqual ─

    @Test
    void findByRoyaltyGreaterThanEqual_returns200_withResults() throws Exception {
        mockMvc.perform(get("/api/royscheds/search/findByRoyaltyGreaterThanEqual")
                        .param("royalty", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void findByRoyaltyGreaterThanEqual_returns200_withEmptyResult() throws Exception {
        mockMvc.perform(get("/api/royscheds/search/findByRoyaltyGreaterThanEqual")
                        .param("royalty", "99"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/royscheds/{id} ─────────────────────────────────

    @Test
    void putRoySched_returns200_whenValid() throws Exception {
        // first POST to get an id
        String createBody = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                    "hirange": 2000,
                    "royalty": 8
                }
                """;

        String location = mockMvc.perform(post("/api/royscheds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String putBody = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                    "hirange": 2000,
                    "royalty": 15
                }
                """;

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.royalty").value(15));
    }

    // ── PATCH /api/royscheds/{id} ───────────────────────────────

    @Test
    void patchRoySched_returns200_whenValid() throws Exception {
        String createBody = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                    "hirange": 3000,
                    "royalty": 9
                }
                """;

        String location = mockMvc.perform(post("/api/royscheds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        String patchBody = """
                {
                    "royalty": 20
                }
                """;

        mockMvc.perform(patch(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.royalty").value(20));
    }

    // ── DELETE /api/royscheds/{id} ──────────────────────────────

    @Test
    void deleteRoySched_returns204_whenExists() throws Exception {
        String createBody = """
                {
                    "titleId": "BU1032",
                    "lorange": 0,
                    "hirange": 4000,
                    "royalty": 7
                }
                """;

        String location = mockMvc.perform(post("/api/royscheds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());
    }
}