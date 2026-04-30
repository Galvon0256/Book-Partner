package com.cg.endpoint;


import com.cg.entity.Job;
import com.cg.entity.Publisher;
import com.cg.repository.JobRepository;
import com.cg.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeRestMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    // ─────────────────────────────────────────────────────────────
    // SETUP: seed FK parent data
    // ─────────────────────────────────────────────────────────────
    @BeforeEach
    void setup() {

        // Job
        if (!jobRepository.existsById((short) 6)) {
            Job job = new Job();
            job.setJobId((short) 6);
            job.setJobDesc("Editor");
            job.setMinLvl(10);
            job.setMaxLvl(200);
            jobRepository.save(job);
        }

        // Publisher
        if (!publisherRepository.existsById("9999")) {
            Publisher pub = new Publisher();
            pub.setPubId("9999");
            pub.setPubName("Test Pub");
            pub.setCity("X");
            pub.setState("Y");
            pub.setCountry("Z");
            publisherRepository.save(pub);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER JSON
    // ─────────────────────────────────────────────────────────────
    private String validEmployeeJson(String empId) {
        return """
            {
              "empId": "%s",
              "fname": "John",
              "lname": "Smith",
              "jobId": 6,
              "pubId": "9999",
              "hireDate": "2020-06-15T09:00:00"
            }
        """.formatted(empId);
    }

    // ─────────────────────────────────────────────────────────────
    // 1. CREATE EMPLOYEE
    // ─────────────────────────────────────────────────────────────
    @Test
    void createEmployee_shouldWork() throws Exception {

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEmployeeJson("ABC12345F")))
                .andExpect(status().isCreated());
    }

    // ─────────────────────────────────────────────────────────────
    // 2. GET EMPLOYEE + JOB DETAILS
    // ─────────────────────────────────────────────────────────────
    @Test
    void getEmployee_shouldIncludeJobDetails() throws Exception {

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEmployeeJson("ABC12346F")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/employees/ABC12346F"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.job.href").exists());
    }
    // ─────────────────────────────────────────────────────────────
    // 3. INVALID JOB ID → FK FAIL
    // ─────────────────────────────────────────────────────────────
    @Test
    void createEmployee_invalidJobId_shouldFail() throws Exception {

        String json = """
            {
              "empId": "ABC99999F",
              "fname": "John",
              "lname": "Smith",
              "jobId": 999,
              "pubId": "9999",
              "hireDate": "2020-06-15T09:00:00"
            }
        """;

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError());
    }

    // ─────────────────────────────────────────────────────────────
    // 4. INVALID PUB ID → FK FAIL
    // ─────────────────────────────────────────────────────────────
    @Test
    void createEmployee_invalidPubId_shouldFail() throws Exception {

        String json = """
            {
              "empId": "ABC88888F",
              "fname": "John",
              "lname": "Smith",
              "jobId": 6,
              "pubId": "0000",
              "hireDate": "2020-06-15T09:00:00"
            }
        """;

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError());
    }

    // ─────────────────────────────────────────────────────────────
    // 5. SEARCH BY JOB ID
    // ─────────────────────────────────────────────────────────────
    @Test
    void findByJobId_shouldReturnEmployee() throws Exception {

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEmployeeJson("ABC77777F")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/employees/search/findByJobId")
                .param("jobId", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ABC77777F")));
    }

    // ─────────────────────────────────────────────────────────────
    // 6. SEARCH BY PUB ID
    // ─────────────────────────────────────────────────────────────
    @Test
    void findByPubId_shouldReturnEmployee() throws Exception {

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEmployeeJson("ABC66666F")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/employees/search/findByPubId")
                .param("pubId", "9999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ABC66666F")));
    }
}