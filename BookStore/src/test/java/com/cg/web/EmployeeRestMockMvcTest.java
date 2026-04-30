package com.cg.web;

import com.cg.entity.Employee;
import com.cg.entity.Job;
import com.cg.entity.Publisher;
import com.cg.repository.EmployeeRepository;
import com.cg.repository.JobRepository;
import com.cg.repository.PublisherRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc integration tests for the Employee REST API.
 *
 * ── SPRING DATA REST BEHAVIOURS THIS TEST RELIES ON ──────────────────────────
 *
 *  GET   existing  → 200 with HAL+JSON body
 *  GET   missing   → 404 (once EmployeeNotFoundException handler is wired in GlobalExceptionHandler)
 *  POST  valid     → 201 Created
 *  POST  invalid   → 400 (constraint violation caught by GlobalExceptionHandler)
 *  PUT   existing  → 204 No Content  (body is empty — verify change with follow-up GET)
 *  PUT   missing   → 201 Created     (Spring Data REST upserts — it creates the record)
 *  PATCH existing  → 204 No Content  (body is empty — verify change with follow-up GET)
 *  PATCH missing   → 404             (once EmployeeNotFoundException handler is wired)
 *
 * ── FK VALIDATION NOTE ───────────────────────────────────────────────────────
 *  MySQL FK violations only fire at transaction commit — AFTER the HTTP response
 *  has been sent. So invalid-FK tests (jobId / pubId that don't exist) can only
 *  return 400 if you validate BEFORE calling save(), e.g. in a Spring Data REST
 *  @HandleBeforeCreate / @HandleBeforeSave event handler or a service layer that
 *  calls jobRepository.existsById() and publisherRepository.existsById() and
 *  throws InvalidJobIdException / InvalidPublisherIdException before saving.
 *
 * ── SEARCH ENDPOINT NOTE ─────────────────────────────────────────────────────
 *  findByJobId, findByLname, findByPubId all return List<Employee>, not Page<Employee>.
 *  Spring Data REST wraps a List result in { "_embedded": { "employees": [...] } }
 *  with NO "page" metadata block. Empty-result assertions therefore check that the
 *  employees array inside _embedded is empty, not $.page.totalElements.
 *
 * ── REQUIRED CHANGES IN YOUR APP BEFORE ALL TESTS GO GREEN ──────────────────
 *  1. Add EmployeeNotFoundException handler to GlobalExceptionHandler (→ 404).
 *  2. Add InvalidJobIdException handler to GlobalExceptionHandler (→ 400).
 *  3. Add InvalidPublisherIdException handler to GlobalExceptionHandler (→ 400).
 *  4. Add a Spring Data REST event handler (@RepositoryEventHandler on Employee)
 *     that validates jobId and pubId exist before create/save and throws
 *     InvalidJobIdException / InvalidPublisherIdException if not.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Employee API — MockMvc Integration Tests")
class EmployeeRestMockMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private PublisherRepository publisherRepository;

    private static final short  JOB_ID       = 6;
    private static final short  OTHER_JOB_ID = 7;
    private static final String PUB_ID       = "0736";
    private static final String OTHER_PUB_ID = "0877";
    private static final String EMP_ID       = "PMA42628M";

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        if (!jobRepository.existsById(JOB_ID)) {
            Job j = new Job();
            j.setJobId(JOB_ID); j.setJobDesc("Senior Editor");
            j.setMinLvl(10); j.setMaxLvl(250);
            jobRepository.save(j);
        }
        if (!jobRepository.existsById(OTHER_JOB_ID)) {
            Job j = new Job();
            j.setJobId(OTHER_JOB_ID); j.setJobDesc("Managing Editor");
            j.setMinLvl(10); j.setMaxLvl(250);
            jobRepository.save(j);
        }
        if (!publisherRepository.existsById(PUB_ID)) {
            Publisher p = new Publisher();
            p.setPubId(PUB_ID); p.setPubName("New Moon Books");
            p.setCity("Boston"); p.setState("MA"); p.setCountry("USA");
            publisherRepository.save(p);
        }
        if (!publisherRepository.existsById(OTHER_PUB_ID)) {
            Publisher p = new Publisher();
            p.setPubId(OTHER_PUB_ID); p.setPubName("Binnet & Hardley");
            p.setCity("Washington"); p.setState("DC"); p.setCountry("USA");
            publisherRepository.save(p);
        }

        Employee emp = new Employee();
        emp.setEmpId(EMP_ID); emp.setFname("Paolo"); emp.setMinit("M");
        emp.setLname("Accorti"); emp.setJobId(JOB_ID); emp.setJobLvl(35);
        emp.setPubId(PUB_ID); emp.setHireDate(LocalDateTime.of(1992, 8, 27, 0, 0));
        employeeRepository.save(emp);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. GET /api/employees  — list
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/employees — list all")
    class GetAllEmployees {

        @Test
        @DisplayName("200 — list contains the seeded employee")
        void getAll_containsSeededEmployee() throws Exception {
            mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees[*].lname",
                            hasItem("Accorti")));
        }

        @Test
        @DisplayName("200 — each employee exposes a self link for detail navigation")
        void getAll_eachEmployeeHasSelfLink() throws Exception {
            mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(
                            "$._embedded.employees[0]._links.self.href").exists());
        }

        @Test
        @DisplayName("200 — page shows zero elements after all employees deleted")
        void getAll_empty() throws Exception {
            employeeRepository.deleteAll();
            mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // GET /api/employees IS paginated → page block IS present
                    .andExpect(jsonPath("$.page.totalElements").value(0));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. GET /api/employees/{id}  — detail view
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/employees/{id} — employee detail")
    class GetEmployeeById {

        @Test
        @DisplayName("200 — returns all employee fields")
        void getById_found() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fname").value("Paolo"))
                    .andExpect(jsonPath("$.lname").value("Accorti"))
                    .andExpect(jsonPath("$.jobId").value((int) JOB_ID))
                    .andExpect(jsonPath("$.pubId").value(PUB_ID));
        }

        @Test
        @DisplayName("200 — detail response includes self, job and publisher links")
        void getById_hasNavigationLinks() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._links.self.href").exists())
                    .andExpect(jsonPath("$._links.job.href").exists())
                    .andExpect(jsonPath("$._links.publisher.href").exists());
        }

        @Test
        @DisplayName("404 — EmployeeNotFoundException returned for missing emp_id")
        // REQUIRES: GlobalExceptionHandler handles EmployeeNotFoundException → 404.
        // Without that handler the generic Exception handler returns 500 instead.
        void getById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", "ZZZ99999F")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. DETAIL VIEW FLOW — employee → job → publisher
    //    This covers the "click an employee → see details + linked job + linked publisher"
    //    navigation that your frontend performs.
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Detail view flow — list → employee → job → publisher")
    class DetailViewFlow {

        @Test
        @DisplayName("step 1+2: employee detail exposes jobId and pubId needed for next calls")
        void flow_employeeDetailHasJobIdAndPubId() throws Exception {
            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jobId").value((int) JOB_ID))
                    .andExpect(jsonPath("$.pubId").value(PUB_ID));
        }

        @Test
        @DisplayName("step 3: jobId from employee resolves to full job record at /api/jobs/{id}")
        void flow_jobDetailLoadedFromJobId() throws Exception {
            mockMvc.perform(get("/api/jobs/{id}", JOB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // Spring Data REST omits the @Id field from the body by default;
                    // if jobId IS present in your response, keep this line — otherwise remove it.
                    .andExpect(jsonPath("$.jobDesc").value("Senior Editor"))
                    .andExpect(jsonPath("$.minLvl").value(10))
                    .andExpect(jsonPath("$.maxLvl").value(250));
        }

        @Test
        @DisplayName("step 4: pubId from employee resolves to full publisher record at /api/publishers/{id}")
        void flow_publisherDetailLoadedFromPubId() throws Exception {
            mockMvc.perform(get("/api/publishers/{id}", PUB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pubName").value("New Moon Books"))
                    .andExpect(jsonPath("$.city").exists())
                    .andExpect(jsonPath("$.country").exists());
        }

        @Test
        @DisplayName("full round trip: list → detail → job → publisher all return 200")
        void flow_fullRoundTrip() throws Exception {
            // Step 1 — employee list
            mockMvc.perform(get("/api/employees").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees[*].lname",
                            hasItem("Accorti")));

            // Step 2 — employee detail (frontend clicks the employee)
            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Step 3 — job detail (frontend uses jobId from step 2 to call /api/jobs/{jobId})
            mockMvc.perform(get("/api/jobs/{id}", JOB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Step 4 — publisher detail (frontend uses pubId from step 2 to call /api/publishers/{pubId})
            mockMvc.perform(get("/api/publishers/{id}", PUB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. POST /api/employees  — create
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/employees — create employee")
    class CreateEmployee {

        @Test
        @DisplayName("201 — valid payload with existing jobId and pubId")
        void post_validFKs_returns201() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ABC12345F",
                                      "fname":    "Alice",
                                      "minit":    "B",
                                      "lname":    "Smith",
                                      "jobId":    %d,
                                      "jobLvl":   100,
                                      "pubId":    "%s",
                                      "hireDate": "1995-01-15T00:00:00"
                                    }
                                    """.formatted(JOB_ID, PUB_ID)))
                    .andExpect(status().isCreated());

            // Verify persisted via GET
            mockMvc.perform(get("/api/employees/ABC12345F")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fname").value("Alice"))
                    .andExpect(jsonPath("$.lname").value("Smith"));
        }

        @Test
        @DisplayName("201 — null jobId and pubId are both allowed (optional FK fields)")
        void post_nullFKs_returns201() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ABC55555F",
                                      "fname":    "Eve",
                                      "lname":    "Taylor",
                                      "hireDate": "1995-01-15T00:00:00"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("400 — jobId that does not exist in jobs table is rejected before save")
        // REQUIRES: a @HandleBeforeCreate / service layer that calls
        // jobRepository.existsById() and throws InvalidJobIdException.
        // Without that pre-save check, Spring Data REST defers the FK violation
        // to commit time and the test cannot intercept it via MockMvc.
        void post_invalidJobId_returns400() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ABC11111F",
                                      "fname":    "Bad",
                                      "lname":    "JobRef",
                                      "jobId":    9999,
                                      "pubId":    "%s",
                                      "hireDate": "2000-01-01T00:00:00"
                                    }
                                    """.formatted(PUB_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("400 — pubId that does not exist in publishers table is rejected before save")
        // REQUIRES: same pre-save check throwing InvalidPublisherIdException.
        void post_invalidPubId_returns400() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ABC22222F",
                                      "fname":    "Bad",
                                      "lname":    "PubRef",
                                      "jobId":    %d,
                                      "pubId":    "XXXX",
                                      "hireDate": "2000-01-01T00:00:00"
                                    }
                                    """.formatted(JOB_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("4xx — fname missing fails @NotNull constraint")
        void post_missingFname_returns4xx() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ABC66666F",
                                      "lname":    "Taylor",
                                      "hireDate": "1995-01-15T00:00:00"
                                    }
                                    """))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("4xx — hireDate missing fails @NotNull constraint")
        void post_missingHireDate_returns4xx() throws Exception {
            mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":  "ABC77777F",
                                      "fname":  "Frank",
                                      "lname":  "Hill"
                                    }
                                    """))
                    .andExpect(status().is4xxClientError());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. PUT /api/employees/{id}  — full update
    //
    // Spring Data REST PUT behaviour:
    //   Existing ID  → 204 No Content + Location header (not 200 with body)
    //   Missing ID   → 201 Created    (upsert — Spring Data REST creates it)
    //
    // Verify the change after a 204 with a follow-up GET.
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/employees/{id} — full update")
    class UpdateEmployee {

        @Test
        @DisplayName("204 — valid update; follow-up GET confirms change persisted")
        void put_valid_returns204_thenGetConfirms() throws Exception {
            mockMvc.perform(put("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "%s",
                                      "fname":    "PaoloUpdated",
                                      "minit":    "M",
                                      "lname":    "Accorti",
                                      "jobId":    %d,
                                      "jobLvl":   80,
                                      "pubId":    "%s",
                                      "hireDate": "1992-08-27T00:00:00"
                                    }
                                    """.formatted(EMP_ID, JOB_ID, PUB_ID)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fname").value("PaoloUpdated"))
                    .andExpect(jsonPath("$.jobLvl").value(80));
        }

        @Test
        @DisplayName("204 — switching jobId to another valid job persists correctly")
        void put_changeJobId_toAnotherValidJob() throws Exception {
            mockMvc.perform(put("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "%s",
                                      "fname":    "Paolo",
                                      "minit":    "M",
                                      "lname":    "Accorti",
                                      "jobId":    %d,
                                      "jobLvl":   50,
                                      "pubId":    "%s",
                                      "hireDate": "1992-08-27T00:00:00"
                                    }
                                    """.formatted(EMP_ID, OTHER_JOB_ID, PUB_ID)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jobId").value((int) OTHER_JOB_ID));
        }

        @Test
        @DisplayName("204 — switching pubId to another valid publisher persists correctly")
        void put_changePubId_toAnotherValidPublisher() throws Exception {
            mockMvc.perform(put("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "%s",
                                      "fname":    "Paolo",
                                      "minit":    "M",
                                      "lname":    "Accorti",
                                      "jobId":    %d,
                                      "jobLvl":   35,
                                      "pubId":    "%s",
                                      "hireDate": "1992-08-27T00:00:00"
                                    }
                                    """.formatted(EMP_ID, JOB_ID, OTHER_PUB_ID)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pubId").value(OTHER_PUB_ID));
        }

        @Test
        @DisplayName("201 — PUT to non-existent ID creates the resource (Spring Data REST upsert)")
        // NOTE: Spring Data REST upserts by design. To reject missing IDs with 404
        // you would need a custom @RepositoryRestController — not in scope here.
        void put_nonExistentId_createsResource_returns201() throws Exception {
            mockMvc.perform(put("/api/employees/{id}", "ZZZ99999F")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "ZZZ99999F",
                                      "fname":    "Ghost",
                                      "lname":    "User",
                                      "hireDate": "2000-01-01T00:00:00"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("400 — PUT with a jobId that does not exist is rejected before save")
        // REQUIRES: pre-save validation throwing InvalidJobIdException.
        void put_invalidJobId_returns400() throws Exception {
            mockMvc.perform(put("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empId":    "%s",
                                      "fname":    "Paolo",
                                      "lname":    "Accorti",
                                      "jobId":    9999,
                                      "pubId":    "%s",
                                      "hireDate": "1992-08-27T00:00:00"
                                    }
                                    """.formatted(EMP_ID, PUB_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 6. PATCH /api/employees/{id}  — partial update
    //
    // Spring Data REST PATCH behaviour:
    //   Existing ID  → 204 No Content
    //   Missing ID   → 404 (once EmployeeNotFoundException handler is wired)
    //
    // Verify the change after a 204 with a follow-up GET.
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /api/employees/{id} — partial update")
    class PatchEmployee {

        @Test
        @DisplayName("204 — patching fname only; follow-up GET confirms change, lname unchanged")
        void patch_fname_only() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "fname": "PaoloPatch" }
                                    """))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fname").value("PaoloPatch"))
                    .andExpect(jsonPath("$.lname").value("Accorti")); // unchanged
        }

        @Test
        @DisplayName("204 — patching jobId to another valid job; follow-up GET confirms")
        void patch_validJobId() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "jobId": %d }
                                    """.formatted(OTHER_JOB_ID)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.jobId").value((int) OTHER_JOB_ID));
        }

        @Test
        @DisplayName("204 — patching pubId to another valid publisher; follow-up GET confirms")
        void patch_validPubId() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "pubId": "%s" }
                                    """.formatted(OTHER_PUB_ID)))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/employees/{id}", EMP_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pubId").value(OTHER_PUB_ID));
        }

        @Test
        @DisplayName("400 — patching jobId to a non-existent job is rejected before save")
        // REQUIRES: pre-save validation throwing InvalidJobIdException.
        void patch_invalidJobId_returns400() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "jobId": 9999 }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("400 — patching pubId to a non-existent publisher is rejected before save")
        // REQUIRES: pre-save validation throwing InvalidPublisherIdException.
        void patch_invalidPubId_returns400() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", EMP_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "pubId": "XXXX" }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"));
        }

        @Test
        @DisplayName("404 — PATCH on missing emp_id returns Not Found")
        // REQUIRES: GlobalExceptionHandler handles EmployeeNotFoundException → 404.
        // Without that handler the generic Exception handler returns 500 instead.
        void patch_notFound_returns404() throws Exception {
            mockMvc.perform(patch("/api/employees/{id}", "ZZZ99999F")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "fname": "Ghost" }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 7–9. Search endpoints
    //
    // These repositories return List<Employee>, NOT Page<Employee>.
    // Spring Data REST wraps List results as:
    //   { "_embedded": { "employees": [...] } }
    // with NO "page" block.
    //
    // Empty-result assertions therefore check the employees array is empty,
    // NOT $.page.totalElements (which doesn't exist for List return types).
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/employees/search/findByJobId")
    class FindByJobId {

        @Test
        @DisplayName("returns employees matching jobId")
        void findByJobId_match() throws Exception {
            mockMvc.perform(get("/api/employees/search/findByJobId")
                            .param("jobId", String.valueOf(JOB_ID))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees[*].jobId",
                            everyItem(is((int) JOB_ID))));
        }

        @Test
        @DisplayName("returns empty result when no employees have that jobId")
        void findByJobId_noMatch() throws Exception {
            // Search returns List<> → no page block. Check _embedded.employees is absent
            // (Spring Data REST omits _embedded entirely when the list is empty).
            mockMvc.perform(get("/api/employees/search/findByJobId")
                            .param("jobId", "999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // When the list is empty, _embedded is absent from the response
                    .andExpect(jsonPath("$._embedded.employees").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/employees/search/findByLname")
    class FindByLname {

        @Test
        @DisplayName("returns employees matching lname")
        void findByLname_match() throws Exception {
            mockMvc.perform(get("/api/employees/search/findByLname")
                            .param("lname", "Accorti")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees[*].lname",
                            everyItem(is("Accorti"))));
        }

        @Test
        @DisplayName("returns empty result when lname does not match")
        void findByLname_noMatch() throws Exception {
            mockMvc.perform(get("/api/employees/search/findByLname")
                            .param("lname", "Nonexistent")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/employees/search/findByPubId")
    class FindByPubId {

        @Test
        @DisplayName("returns employees matching pubId")
        void findByPubId_match() throws Exception {
            mockMvc.perform(get("/api/employees/search/findByPubId")
                            .param("pubId", PUB_ID)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees[*].pubId",
                            everyItem(is(PUB_ID))));
        }

        @Test
        @DisplayName("returns empty result when pubId does not match")
        void findByPubId_noMatch() throws Exception {
            mockMvc.perform(get("/api/employees/search/findByPubId")
                            .param("pubId", "XXXX")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.employees").isEmpty());
        }
    }
}