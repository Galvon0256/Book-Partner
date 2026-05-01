package com.cg.web;

import com.cg.entity.Job;
import com.cg.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc integration tests for Job READ endpoints only.
 *
 * Rationale: Job is a reference/lookup table. It is never created, updated, or
 * deleted via the API — employees reference it via FK. These tests exist purely
 * as a safety net for the GET surface that Spring Data REST exposes.
 *
 * MySQL CHECK constraints on the jobs table:
 *   jobs_chk_1: min_lvl >= 10
 *   jobs_chk_2: max_lvl <= 250
 * All seeded values satisfy both constraints.
 *
 * Remote DB (and local) already has 14 rows with IDs 1–14.
 * IDs 390–392 are used to avoid clashing with any existing real rows.
 *
 * When findByJobDescIgnoreCase / findByMinLvlLessThanEqual / findByMaxLvlGreaterThanEqual
 * return an empty list, Spring Data REST still includes _embedded.jobs as an empty
 * array — so empty assertions use isEmpty(), not doesNotExist().
 *
 * Covered endpoints:
 *   GET /api/jobs                                          — list all
 *   GET /api/jobs/{id}                                     — get by id (+ 404)
 *   GET /api/jobs/search/findByJobDescIgnoreCase           — search by description
 *   GET /api/jobs/search/findByMinLvlLessThanEqual         — search by min level
 *   GET /api/jobs/search/findByMaxLvlGreaterThanEqual      — search by max level
 *
 * NOTE on param names — JobRepository declares:
 *   findByMinLvlLessThanEqual(@Param("lvl") Integer lvl)
 *   findByMaxLvlGreaterThanEqual(@Param("lvl") Integer lvl)
 * So the query param exposed by Spring Data REST is "lvl", NOT "minLvl"/"maxLvl".
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Job API — GET / Search MockMvc Tests")
class JobMockMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JobRepository jobRepository;

    // IDs 390–392 are free on both local and remote DB (real data only goes up to ID 14,
    // AUTO_INCREMENT=403 means rows were inserted and deleted historically but none exist
    // at these IDs now).
    // Seed values satisfy:
    //   jobs_chk_1: min_lvl >= 10
    //   jobs_chk_2: max_lvl <= 250
    private static final short JOB_ID_1 = 390;  // Senior Editor Test    minLvl=10  maxLvl=250
    private static final short JOB_ID_2 = 391;  // Copy Editor Test      minLvl=30  maxLvl=200
    private static final short JOB_ID_3 = 392;  // Junior Designer Test  minLvl=15  maxLvl=100

    @BeforeEach
    void setUp() {
        if (!jobRepository.existsById(JOB_ID_1)) {
            Job j = new Job();
            j.setJobId(JOB_ID_1);
            j.setJobDesc("Senior Editor Test");
            j.setMinLvl(10);   // exactly at jobs_chk_1 boundary (>= 10) ✓
            j.setMaxLvl(250);  // exactly at jobs_chk_2 boundary (<= 250) ✓
            jobRepository.save(j);
        }
        if (!jobRepository.existsById(JOB_ID_2)) {
            Job j = new Job();
            j.setJobId(JOB_ID_2);
            j.setJobDesc("Copy Editor Test");
            j.setMinLvl(30);   // >= 10 ✓
            j.setMaxLvl(200);  // <= 250 ✓
            jobRepository.save(j);
        }
        if (!jobRepository.existsById(JOB_ID_3)) {
            Job j = new Job();
            j.setJobId(JOB_ID_3);
            j.setJobDesc("Junior Designer Test");
            j.setMinLvl(15);   // >= 10 ✓
            j.setMaxLvl(100);  // <= 250 ✓
            jobRepository.save(j);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. GET /api/jobs  — list all
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/jobs — list all")
    class GetAllJobs {

        @Test
        @DisplayName("200 — list contains all three seeded jobs")
        void getAll_containsAllSeededJobs() throws Exception {
            mockMvc.perform(get("/api/jobs").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isArray())
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            hasItems("Senior Editor Test", "Copy Editor Test", "Junior Designer Test")));
        }

        @Test
        @DisplayName("200 — each job has a self link")
        void getAll_eachJobHasSelfLink() throws Exception {
            mockMvc.perform(get("/api/jobs").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs[0]._links.self.href").exists());
        }

        @Test
        @DisplayName("200 — each job exposes jobDesc, minLvl and maxLvl fields")
        void getAll_jobsHaveExpectedFields() throws Exception {
            mockMvc.perform(get("/api/jobs").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc").isNotEmpty())
                    .andExpect(jsonPath("$._embedded.jobs[*].minLvl").isNotEmpty())
                    .andExpect(jsonPath("$._embedded.jobs[*].maxLvl").isNotEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. GET /api/jobs/{id}  — get by id
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/jobs/{id} — get by id")
    class GetJobById {

        @Test
        @DisplayName("200 — returns correct job fields for existing id")
        void getById_found() throws Exception {
            mockMvc.perform(get("/api/jobs/{id}", JOB_ID_1)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jobDesc").value("Senior Editor Test"))
                    .andExpect(jsonPath("$.minLvl").value(10))
                    .andExpect(jsonPath("$.maxLvl").value(250));
        }

        @Test
        @DisplayName("200 — self link present and contains the job id")
        void getById_hasSelfLink() throws Exception {
            mockMvc.perform(get("/api/jobs/{id}", JOB_ID_1)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links.self.href")
                            .value(containsString("/api/jobs/" + JOB_ID_1)));
        }

        @Test
        @DisplayName("404 — non-existent job id returns Not Found with correct error body")
        // Spring Data REST throws ResourceNotFoundException for unknown IDs.
        // GlobalExceptionHandler catches it → 404 with our standard error shape.
        void getById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/jobs/{id}", 9999)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. GET /api/jobs/search/findByJobDescIgnoreCase
    //    Param name: "jobDesc"  (matches @Param("jobDesc") in JobRepository)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/jobs/search/findByJobDescIgnoreCase")
    class FindByJobDescIgnoreCase {

        @Test
        @DisplayName("200 — exact case match returns the job")
        void findByJobDesc_exactCase() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByJobDescIgnoreCase")
                            .param("jobDesc", "Senior Editor Test")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            hasItem("Senior Editor Test")));
        }

        @Test
        @DisplayName("200 — lowercase input still matches (case-insensitive)")
        void findByJobDesc_allLowercase() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByJobDescIgnoreCase")
                            .param("jobDesc", "senior editor test")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isArray())
                    .andExpect(jsonPath("$._embedded.jobs", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("200 — uppercase input still matches (case-insensitive)")
        void findByJobDesc_allUppercase() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByJobDescIgnoreCase")
                            .param("jobDesc", "COPY EDITOR TEST")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("200 — no match returns empty jobs array")
        // Spring Data REST returns _embedded.jobs as an empty array (not omitted)
        // when the result list is empty — so we assert isEmpty(), not doesNotExist().
        void findByJobDesc_noMatch_emptyResult() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByJobDescIgnoreCase")
                            .param("jobDesc", "Chief Astronaut")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. GET /api/jobs/search/findByMinLvlLessThanEqual
    //    Param name: "lvl" — JobRepository declares @Param("lvl")
    //
    //    Seeded data (all satisfy min_lvl >= 10):
    //      JOB_ID_1  minLvl=10
    //      JOB_ID_2  minLvl=30
    //      JOB_ID_3  minLvl=15
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/jobs/search/findByMinLvlLessThanEqual")
    class FindByMinLvlLessThanEqual {

        @Test
        @DisplayName("200 — returns only jobs where minLvl <= threshold")
        // threshold=10 → only JOB_1 (10) qualifies among seeds;
        // JOB_2 (30) and JOB_3 (15) do NOT
        void findByMinLvl_match_returnsOnlyJobsAtOrBelowThreshold() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMinLvlLessThanEqual")
                            .param("lvl", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isArray())
                    .andExpect(jsonPath("$._embedded.jobs[*].minLvl",
                            everyItem(lessThanOrEqualTo(10))))
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            not(hasItem("Copy Editor Test"))))
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            not(hasItem("Junior Designer Test"))));
        }

        @Test
        @DisplayName("200 — high threshold covers all seeded jobs")
        // threshold=100 → all three seeded jobs qualify (highest seeded minLvl is 30)
        void findByMinLvl_highThreshold_returnsAll() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMinLvlLessThanEqual")
                            .param("lvl", "100")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs",
                            hasSize(greaterThanOrEqualTo(3))));
        }

        @Test
        @DisplayName("200 — threshold below all minLvl values returns empty jobs array")
        // threshold=9 → no job in the entire DB qualifies because
        // jobs_chk_1 enforces min_lvl >= 10 on every row
        void findByMinLvl_belowAll_emptyResult() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMinLvlLessThanEqual")
                            .param("lvl", "9")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isEmpty());
        }

        @Test
        @DisplayName("200 — exact boundary is inclusive (minLvl=10 at threshold=10)")
        // JOB_1 has minLvl=10 exactly — threshold=10 must include it
        void findByMinLvl_exactBoundary_inclusive() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMinLvlLessThanEqual")
                            .param("lvl", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            hasItem("Senior Editor Test")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. GET /api/jobs/search/findByMaxLvlGreaterThanEqual
    //    Param name: "lvl" — JobRepository declares @Param("lvl")
    //
    //    Seeded data (all satisfy max_lvl <= 250):
    //      JOB_ID_1  maxLvl=250
    //      JOB_ID_2  maxLvl=200
    //      JOB_ID_3  maxLvl=100
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/jobs/search/findByMaxLvlGreaterThanEqual")
    class FindByMaxLvlGreaterThanEqual {

        @Test
        @DisplayName("200 — returns only jobs where maxLvl >= threshold")
        // threshold=200 → JOB_1 (250) and JOB_2 (200) qualify; JOB_3 (100) does NOT
        void findByMaxLvl_match_returnsOnlyJobsAtOrAboveThreshold() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMaxLvlGreaterThanEqual")
                            .param("lvl", "200")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isArray())
                    .andExpect(jsonPath("$._embedded.jobs[*].maxLvl",
                            everyItem(greaterThanOrEqualTo(200))))
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            not(hasItem("Junior Designer Test"))));
        }

        @Test
        @DisplayName("200 — low threshold covers all seeded jobs")
        // threshold=50 → all three seeded jobs qualify (lowest seeded maxLvl is 100)
        void findByMaxLvl_lowThreshold_returnsAll() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMaxLvlGreaterThanEqual")
                            .param("lvl", "50")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs",
                            hasSize(greaterThanOrEqualTo(3))));
        }

        @Test
        @DisplayName("200 — threshold above all maxLvl values returns empty jobs array")
        // threshold=251 → no job in the entire DB qualifies because
        // jobs_chk_2 enforces max_lvl <= 250 on every row
        void findByMaxLvl_aboveAll_emptyResult() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMaxLvlGreaterThanEqual")
                            .param("lvl", "251")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs").isEmpty());
        }

        @Test
        @DisplayName("200 — exact boundary is inclusive (maxLvl=250 at threshold=250)")
        // JOB_1 has maxLvl=250 exactly — threshold=250 must include it
        void findByMaxLvl_exactBoundary_inclusive() throws Exception {
            mockMvc.perform(get("/api/jobs/search/findByMaxLvlGreaterThanEqual")
                            .param("lvl", "250")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.jobs[*].jobDesc",
                            hasItem("Senior Editor Test")));
        }
    }
}