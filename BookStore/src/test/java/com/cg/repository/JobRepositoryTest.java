package com.cg.repository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.cg.entity.Job;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Data JPA tests for Job entity and JobRepository.
 *
 * Test coverage:
 *  - jobId       : valid short, null (PK — handled by DB auto/manual)
 *  - jobDesc     : valid, null (@NotNull), blank, exceeds 50 chars
 *  - minLvl      : valid, null (@NotNull)
 *  - maxLvl      : valid, null (@NotNull)
 *  - Repository  : save, findById, findAll, findByMinLvlLessThanEqual,
 *                  findByMaxLvlGreaterThanEqual, findByJobDesc / IgnoreCase
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Validator validator;

    // ─── helper ───────────────────────────────────────────────────────────────

    /** Builds a fully valid Job that passes all constraints. */
    private Job validJob(Short id) {
        Job job = new Job();
        job.setJobId(id);
        job.setJobDesc("Senior Editor");
        job.setMinLvl(50);
        job.setMaxLvl(250);
        return job;
    }

    @BeforeEach
    void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. FIELD-LEVEL VALIDATION (Bean Validation — no DB hit needed)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("jobDesc field validation")
    class JobDescValidation {

        @Test
        @DisplayName("VALID — jobDesc within 50 chars passes")
        void jobDesc_valid() {
            Job job = validJob((short) 1);
            job.setJobDesc("Junior Developer");
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("VALID — jobDesc exactly 50 chars passes")
        void jobDesc_exactly50Chars() {
            Job job = validJob((short) 2);
            job.setJobDesc("A".repeat(50));
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("INVALID — jobDesc null fails @NotNull")
        void jobDesc_null_fails() {
            Job job = validJob((short) 3);
            job.setJobDesc(null);
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("jobDesc")
                            && v.getMessage().equals("Job description cannot be null"));
        }

        @Test
        @DisplayName("INVALID — jobDesc exceeds 50 chars fails @Size")
        void jobDesc_tooLong_fails() {
            Job job = validJob((short) 4);
            job.setJobDesc("A".repeat(51));
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("jobDesc")
                            && v.getMessage().equals("Job description cannot exceed 50 characters"));
        }

        @Test
        @DisplayName("EDGE — blank string (spaces only) passes bean validation but is logically bad")
        void jobDesc_blankString_passesValidation() {
            // @NotNull + @Size don't block blank — document this behaviour
            Job job = validJob((short) 5);
            job.setJobDesc("   ");
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            // No constraint violation — blank passes @NotNull and @Size
            assertThat(violations).isEmpty();
            // If you want to block blank, add @NotBlank to the entity
        }
    }

    @Nested
    @DisplayName("minLvl field validation")
    class MinLvlValidation {

        @Test
        @DisplayName("VALID — minLvl set passes")
        void minLvl_valid() {
            Job job = validJob((short) 10);
            job.setMinLvl(10);
            assertThat(validator.validate(job)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — minLvl null fails @NotNull")
        void minLvl_null_fails() {
            Job job = validJob((short) 11);
            job.setMinLvl(null);
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("minLvl")
                            && v.getMessage().equals("Minimum level is required"));
        }

        @Test
        @DisplayName("EDGE — minLvl zero is allowed by current constraints")
        void minLvl_zero_allowed() {
            Job job = validJob((short) 12);
            job.setMinLvl(0);
            assertThat(validator.validate(job)).isEmpty();
        }

        @Test
        @DisplayName("EDGE — minLvl negative is allowed by current constraints")
        void minLvl_negative_allowed() {
            // No @Min on entity — document this gap if negative is invalid in business logic
            Job job = validJob((short) 13);
            job.setMinLvl(-1);
            assertThat(validator.validate(job)).isEmpty();
        }
    }

    @Nested
    @DisplayName("maxLvl field validation")
    class MaxLvlValidation {

        @Test
        @DisplayName("VALID — maxLvl set passes")
        void maxLvl_valid() {
            Job job = validJob((short) 20);
            job.setMaxLvl(250);
            assertThat(validator.validate(job)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — maxLvl null fails @NotNull")
        void maxLvl_null_fails() {
            Job job = validJob((short) 21);
            job.setMaxLvl(null);
            Set<ConstraintViolation<Job>> violations = validator.validate(job);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("maxLvl")
                            && v.getMessage().equals("Maximum level is required"));
        }

        @Test
        @DisplayName("EDGE — maxLvl less than minLvl is allowed by current constraints")
        void maxLvl_lessThanMinLvl_allowed() {
            // No cross-field validation on entity — document this gap
            Job job = validJob((short) 22);
            job.setMinLvl(200);
            job.setMaxLvl(10); // logically wrong, but no constraint blocks it
            assertThat(validator.validate(job)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. REPOSITORY CRUD TESTS (hits H2 in-memory DB)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("JobRepository — CRUD")
    class CrudTests {

        @Test
        @DisplayName("VALID — save and findById returns same job")
        void save_and_findById() {
            Job job = validJob((short) 100);
            jobRepository.save(job);
            entityManager.flush();
            entityManager.clear();

            Optional<Job> found = jobRepository.findById((short) 100);
            assertThat(found).isPresent();
            assertThat(found.get().getJobDesc()).isEqualTo("Senior Editor");
        }

        @Test
        @DisplayName("VALID — findById for non-existent ID returns empty")
        void findById_notFound() {
            Optional<Job> found = jobRepository.findById((short) 999);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("VALID — findAll returns all saved jobs")
        void findAll_returnsAll() {
            jobRepository.save(validJob((short) 101));
            jobRepository.save(validJob((short) 102));
            entityManager.flush();

            Iterable<Job> all = jobRepository.findAll();
            assertThat(all).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("VALID — update jobDesc persists correctly")
        void update_jobDesc() {
            Job job = validJob((short) 103);
            jobRepository.save(job);
            entityManager.flush();
            entityManager.clear();

            Job fetched = jobRepository.findById((short) 103).orElseThrow();
            fetched.setJobDesc("Updated Desc");
            jobRepository.save(fetched);
            entityManager.flush();
            entityManager.clear();

            assertThat(jobRepository.findById((short) 103).get().getJobDesc())
                    .isEqualTo("Updated Desc");
        }

        @Test
        @DisplayName("VALID — delete removes job from DB")
        void delete_job() {
            Job job = validJob((short) 104);
            jobRepository.save(job);
            entityManager.flush();

            jobRepository.deleteById((short) 104);
            entityManager.flush();

            assertThat(jobRepository.findById((short) 104)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. CUSTOM QUERY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("JobRepository — findByMinLvlLessThanEqual")
    class FindByMinLvlTests {

        @Test
        @DisplayName("VALID — returns jobs with minLvl <= given value")
        void findByMinLvlLessThanEqual_returnsMatch() {
            Job j1 = validJob((short) 200); j1.setMinLvl(30);  jobRepository.save(j1);
            Job j2 = validJob((short) 201); j2.setMinLvl(50);  jobRepository.save(j2);
            Job j3 = validJob((short) 202); j3.setMinLvl(100); jobRepository.save(j3);
            entityManager.flush();

            List<Job> result = jobRepository.findByMinLvlLessThanEqual(50);
            assertThat(result).extracting(Job::getMinLvl)
                    .allMatch(lvl -> lvl <= 50);
            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("VALID — no jobs below threshold returns empty list")
        void findByMinLvlLessThanEqual_noMatch() {
            Job j = validJob((short) 203);
            j.setMinLvl(200); 
            jobRepository.save(j);
            entityManager.flush();

            List<Job> result = jobRepository.findByMinLvlLessThanEqual(9);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("JobRepository — findByMaxLvlGreaterThanEqual")
    class FindByMaxLvlTests {

        @Test
        @DisplayName("VALID — returns jobs with maxLvl >= given value")
        void findByMaxLvlGreaterThanEqual_returnsMatch() {
            Job j1 = validJob((short) 300); j1.setMaxLvl(100); jobRepository.save(j1);
            Job j2 = validJob((short) 301); j2.setMaxLvl(200); jobRepository.save(j2);
            Job j3 = validJob((short) 302); j3.setMaxLvl(50);  jobRepository.save(j3);
            entityManager.flush();

            List<Job> result = jobRepository.findByMaxLvlGreaterThanEqual(100);
            assertThat(result).extracting(Job::getMaxLvl)
                    .allMatch(lvl -> lvl >= 100);
        }

        @Test
        @DisplayName("VALID — no jobs above threshold returns empty list")
        void findByMaxLvlGreaterThanEqual_noMatch() {
            Job j = validJob((short) 303); j.setMaxLvl(50); jobRepository.save(j);
            entityManager.flush();

            List<Job> result = jobRepository.findByMaxLvlGreaterThanEqual(300);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("JobRepository — findByJobDescIgnoreCase")
    class FindByJobDescTests {

        @Test
        @DisplayName("VALID — exact case match returns job")
        void findByJobDescIgnoreCase_exactCase() {
            Job j = validJob((short) 400);
            j.setJobDesc("Copy Editor");
            jobRepository.save(j);
            entityManager.flush();

            List<Job> result = jobRepository.findByJobDescIgnoreCase("Copy Editor");
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getJobDesc()).isEqualTo("Copy Editor");
        }

        @Test
        @DisplayName("VALID — all lowercase input matches")
        void findByJobDescIgnoreCase_allLowercase() {
            Job j = validJob((short) 401);
            j.setJobDesc("Copy Editor");
            jobRepository.save(j);
            entityManager.flush();

            List<Job> result = jobRepository.findByJobDescIgnoreCase("copy editor");
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getJobDesc()).isEqualToIgnoringCase("Copy Editor");
        }

        @Test
        @DisplayName("VALID — all uppercase input matches")
        void findByJobDescIgnoreCase_allUppercase() {
            Job j = validJob((short) 402);
            j.setJobDesc("Senior Designer");
            jobRepository.save(j);
            entityManager.flush();

            List<Job> result = jobRepository.findByJobDescIgnoreCase("SENIOR DESIGNER");
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getJobDesc()).isEqualToIgnoringCase("Senior Designer");
        }

        @Test
        @DisplayName("VALID — no match returns empty list")
        void findByJobDescIgnoreCase_noMatch() {
            List<Job> result = jobRepository.findByJobDescIgnoreCase("Nonexistent Role XYZ");
            assertThat(result).isEmpty();
        }
    }
}