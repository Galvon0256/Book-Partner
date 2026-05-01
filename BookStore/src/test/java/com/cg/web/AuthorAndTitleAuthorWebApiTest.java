package com.cg.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorAndTitleAuthorWebApiTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Author GET collection ────────────────────────────────────────────────

    @Test
    void getAuthors_returns200() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getAuthors_halJson_withEmbeddedArray() throws Exception {
        mockMvc.perform(get("/api/authors").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getAuthors_pagination_respectsPageSize() throws Exception {
        mockMvc.perform(get("/api/authors").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(5)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalElements").isNumber());
    }

    @Test
    void getAuthors_returnsSearchLink() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.search").exists());
    }

    @Test
    void getAuthors_returnsProfileLink() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.profile").exists());
    }

    // ── Author GET single ────────────────────────────────────────────────────

    @Test
    void getAuthorById_existingId_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/authors/213-46-8915"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auId", is("213-46-8915")))
                .andExpect(jsonPath("$.auLname").exists())
                .andExpect(jsonPath("$.auFname").exists())
                .andExpect(jsonPath("$.phone").exists())
                .andExpect(jsonPath("$.contract").exists());
    }

    @Test
    void getAuthorById_existingId_returnsSelfLink() throws Exception {
        mockMvc.perform(get("/api/authors/213-46-8915"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.self.href").isNotEmpty());
    }

    @Test
    void getAuthorById_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/api/authors/000-00-0000"))
                .andExpect(status().isNotFound());
    }

    // ── Author POST ──────────────────────────────────────────────────────────

    @Test
    void postAuthor_validPayload_returns201WithLocation() throws Exception {
        String json = """
                {
                    "auId": "998-11-2001",
                    "auLname": "TestLname",
                    "auFname": "TestFname",
                    "phone": "415 000-0001",
                    "address": "1 Test St",
                    "city": "TestCity",
                    "state": "CA",
                    "zip": "94000",
                    "contract": 1
                }
                """;

        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void postAuthor_afterCreate_resourceIsRetrievable() throws Exception {
        String json = """
                {
                    "auId": "998-11-2002",
                    "auLname": "RetrieveLname",
                    "auFname": "RetrieveFname",
                    "phone": "415 000-0002",
                    "city": "Oakland",
                    "state": "CA",
                    "zip": "94601",
                    "contract": 0
                }
                """;

        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/authors/998-11-2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auLname", is("RetrieveLname")))
                .andExpect(jsonPath("$.auFname", is("RetrieveFname")))
                .andExpect(jsonPath("$.city", is("Oakland")));
    }

    // ── Author PUT ───────────────────────────────────────────────────────────

    @Test
    void putAuthor_existingId_returns204() throws Exception {
        String json = """
                {
                    "auId": "213-46-8915",
                    "auLname": "Green",
                    "auFname": "Marjorie",
                    "phone": "415 986-7020",
                    "address": "309 63rd St",
                    "city": "Oakland",
                    "state": "CA",
                    "zip": "94618",
                    "contract": 1
                }
                """;

        mockMvc.perform(put("/api/authors/213-46-8915")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());
    }

    @Test
    void putAuthor_existingId_updatedFieldReflectedOnGet() throws Exception {
        String json = """
                {
                    "auId": "238-95-7766",
                    "auLname": "Carson",
                    "auFname": "Cheryl",
                    "phone": "415 548-7723",
                    "address": "589 Darwin Ln",
                    "city": "San Francisco",
                    "state": "CA",
                    "zip": "94705",
                    "contract": 1
                }
                """;

        mockMvc.perform(put("/api/authors/238-95-7766")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/authors/238-95-7766"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("San Francisco")));
    }

    // ── Author PATCH ─────────────────────────────────────────────────────────

    @Test
    void patchAuthor_existingId_partialUpdateSucceeds() throws Exception {
        String patchJson = """
                {
                    "city": "Berkeley"
                }
                """;

        mockMvc.perform(patch("/api/authors/238-95-7766")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/authors/238-95-7766"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Berkeley")))
                .andExpect(jsonPath("$.auLname").exists())
                .andExpect(jsonPath("$.state", is("CA")));
    }

    @Test
    void patchAuthor_nonExistentId_returns404() throws Exception {
        String patchJson = """
                {
                    "city": "NoWhere"
                }
                """;

        mockMvc.perform(patch("/api/authors/000-00-9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());
    }

    // ── Author Search ────────────────────────────────────────────────────────

    @Test
    void searchAuthorsByCity_existingCity_returnsResults() throws Exception {
        mockMvc.perform(get("/api/authors/search/findByCity").param("city", "Oakland"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray());
    }

    @Test
    void searchAuthorsByCity_nonExistentCity_returns200() throws Exception {
        mockMvc.perform(get("/api/authors/search/findByCity").param("city", "NonExistentCity"))
                .andExpect(status().isOk());
    }

    @Test
    void searchAuthorsByState_existingState_returnsResults() throws Exception {
        mockMvc.perform(get("/api/authors/search/findByState").param("state", "CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.authors").isArray());
    }

    // ── TitleAuthor GET collection ───────────────────────────────────────────

    @Test
    void getTitleAuthors_returns200() throws Exception {
        mockMvc.perform(get("/api/titleauthors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getTitleAuthors_halJson_withEmbeddedArray() throws Exception {
        mockMvc.perform(get("/api/titleauthors").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors").isArray())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getTitleAuthors_pagination_respectsPageSize() throws Exception {
        mockMvc.perform(get("/api/titleauthors").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(5)))
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.totalElements").isNumber());
    }

    @Test
    void getTitleAuthors_returnsSearchLink() throws Exception {
        mockMvc.perform(get("/api/titleauthors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.search").exists());
    }

    @Test
    void getTitleAuthors_returnsProfileLink() throws Exception {
        mockMvc.perform(get("/api/titleauthors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.profile").exists());
    }

    @Test
    void getTitleAuthorById_existingId_returnsSelfLink() throws Exception {
        mockMvc.perform(get("/api/titleauthors/search/findByAuId").param("auId", "172-32-1176"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.self.href").isNotEmpty());
    }

    // ── TitleAuthor POST ─────────────────────────────────────────────────────

    @Test
    void postTitleAuthor_validPayload_returns201WithLocation() throws Exception {
        String json = """
                {
                    "auId": "213-46-8915",
                    "titleId": "PC8888",
                    "auOrd": 1,
                    "royaltyper": 40
                }
                """;

        mockMvc.perform(post("/api/titleauthors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void postTitleAuthor_afterCreate_resourceIsRetrievable() throws Exception {
        String json = """
                {
                    "auId": "238-95-7766",
                    "titleId": "PC8888",
                    "auOrd": 2,
                    "royaltyper": 30
                }
                """;

        mockMvc.perform(post("/api/titleauthors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/titleauthors/search/findByAuId").param("auId", "238-95-7766"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors[0].auId", is("238-95-7766")))
                .andExpect(jsonPath("$._embedded.titleauthors[0].titleId", is("PC8888")))
                .andExpect(jsonPath("$._embedded.titleauthors[0].royaltyper", is(30)));
    }

    // ── TitleAuthor PUT ──────────────────────────────────────────────────────

    @Test
    void putTitleAuthor_existingId_returns204() throws Exception {
        String json = """
                {
                    "auId": "172-32-1176",
                    "titleId": "BU1032",
                    "auOrd": 1,
                    "royaltyper": 60
                }
                """;

        mockMvc.perform(post("/api/titleauthors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/titleauthors/search/findByTitleId").param("titleId", "BU1032"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors[0].royaltyper", is(60)));
    }

    @Test
    void putTitleAuthor_existingId_updatedFieldReflectedOnGet() throws Exception {
        String json = """
                {
                    "auId": "172-32-1176",
                    "titleId": "BU1032",
                    "auOrd": 1,
                    "royaltyper": 75
                }
                """;

        mockMvc.perform(post("/api/titleauthors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/titleauthors/search/findByAuId").param("auId", "172-32-1176"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors").isArray());
    }

    // ── TitleAuthor PATCH ────────────────────────────────────────────────────

    @Test
    void patchTitleAuthor_existingId_partialUpdateSucceeds() throws Exception {
        String patchJson = """
                {
                    "royaltyper": 50
                }
                """;

        mockMvc.perform(post("/api/titleauthors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "auId": "213-46-8915",
                            "titleId": "BU1032",
                            "auOrd": 2,
                            "royaltyper": 99
                        }
                        """))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/titleauthors/search/findByAuId").param("auId", "213-46-8915"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors[0].auId", is("213-46-8915")));
    }

    @Test
    void searchTitleAuthorByAuId_nonExistentId_returns200() throws Exception {
        mockMvc.perform(get("/api/titleauthors/search/findByAuId").param("auId", "000-00-0000"))
                .andExpect(status().isOk());
    }

    @Test
    void searchTitleAuthorByTitleId_existingId_returnsMatchingArray() throws Exception {
        mockMvc.perform(get("/api/titleauthors/search/findByTitleId").param("titleId", "BU1032"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.titleauthors").isArray())
                .andExpect(jsonPath("$._embedded.titleauthors[0].titleId", is("BU1032")));
    }

    @Test
    void searchTitleAuthorByTitleId_nonExistentId_returns200() throws Exception {
        mockMvc.perform(get("/api/titleauthors/search/findByTitleId").param("titleId", "XXXXXX"))
                .andExpect(status().isOk());
    }
}