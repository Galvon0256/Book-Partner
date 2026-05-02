package com.cg.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.cg.entity.Employee;
import com.cg.entity.Job;
import com.cg.entity.Publisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Data JPA tests for Employee entity and EmployeeRepository.
 *
 * FIX 1: validEmployee() used jobId=6 and pubId="9999", but with
 *         create-drop the jobs and publishers tables are empty — MySQL
 *         rejects every insert with FK constraint violation.
 *         Solution: seed one Job (id=6) and one Publisher (id="9999")
 *         in @BeforeEach before any employee is saved.
 *
 * FIX 2: findByLname_match() saved 2 "Chang" + 1 "Gupta" but asserted
 *         hasSize(3) — corrected to hasSize(2).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private EntityManager entityManager;

    private Validator validator;

    // ─── FK parent records required by validEmployee() ────────────────────────
    // validEmployee() sets jobId=(short)6 and pubId="9999".
    // These parent rows must exist before any employee insert, otherwise
    // MySQL throws: Cannot add or update a child row: FK constraint fails.

    @BeforeEach
    void setUpValidatorAndParentRecords() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Clean child-first to respect FK order, then parent tables.
        employeeRepository.deleteAll();
        entityManager.flush();

        // Seed the Job parent row that validEmployee() references (jobId = 6).
        // deleteAll + re-save keeps each test isolated.
        if (!jobRepository.existsById((short) 6)) {
            Job job = new Job();
            job.setJobId((short) 6);
            job.setJobDesc("Senior Editor");
            job.setMinLvl(10);
            job.setMaxLvl(250);
            jobRepository.save(job);
        }

        // Seed the Publisher parent row that validEmployee() references (pubId = "9999").
        if (!publisherRepository.existsById("9999")) {
            Publisher pub = new Publisher();
            pub.setPubId("9999");
            pub.setPubName("Test Publisher");
            pub.setCity("Test City");
            pub.setState("CA");
            pub.setCountry("USA");
            publisherRepository.save(pub);
        }

        // Also seed extra job ids used in findByJobId tests (7 and 3)
        if (!jobRepository.existsById((short) 7)) {
            Job job7 = new Job();
            job7.setJobId((short) 7);
            job7.setJobDesc("Managing Editor");
            job7.setMinLvl(10);
            job7.setMaxLvl(250);
            jobRepository.save(job7);
        }
        if (!jobRepository.existsById((short) 3)) {
            Job job3 = new Job();
            job3.setJobId((short) 3);
            job3.setJobDesc("Operations Manager");
            job3.setMinLvl(10);
            job3.setMaxLvl(250);
            jobRepository.save(job3);
        }

        // Seed pubIds used in findByPubId tests ("1622", "9952")
        if (!publisherRepository.existsById("1622")) {
            Publisher p = new Publisher();
            p.setPubId("1622");
            p.setPubName("Publisher 1622");
            p.setCity("Oakland");
            p.setState("CA");
            p.setCountry("USA");
            publisherRepository.save(p);
        }
        if (!publisherRepository.existsById("9952")) {
            Publisher p = new Publisher();
            p.setPubId("9952");
            p.setPubName("Publisher 9952");
            p.setCity("Seattle");
            p.setState("WA");
            p.setCountry("USA");
            publisherRepository.save(p);
        }

        entityManager.flush();
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    /** Returns a fully valid Employee that passes all constraints. */
    private Employee validEmployee(String empId) {
        Employee e = new Employee();
        e.setEmpId(empId);
        e.setFname("John");
        e.setMinit("A");
        e.setLname("Smith");
        e.setJobId((short) 6);      // matches seeded Job above
        e.setJobLvl(100);
        e.setPubId("9999");         // matches seeded Publisher above
        e.setHireDate(LocalDateTime.of(2020, 6, 15, 9, 0));
        return e;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. empId — Pattern validation
    // ═══════════════════════════════════════════════════════════════════════════

    
    
    
    
    @Nested
    @DisplayName("empId — @Pattern validation")
    class EmpIdValidation {

        @Test
        @DisplayName("VALID — three-letter prefix format: ABC12345F")
        void empId_threeLetterPrefix_female() {
            Employee e = validEmployee("ABC12345F");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — three-letter prefix format: XYZ98765M")
        void empId_threeLetterPrefix_male() {
            Employee e = validEmployee("XYZ98765M");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — hyphenated prefix format: A-B12345F")
        void empId_hyphenatedPrefix_female() {
            Employee e = validEmployee("A-B12345F");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — hyphenated prefix format: Z-Z99999M")
        void empId_hyphenatedPrefix_male() {
            Employee e = validEmployee("Z-Z99999M");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — lowercase letters in prefix fail")
        void empId_lowercase_fails() {
            Employee e = validEmployee("abc12345F");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — digit starting with 0 fails (first digit must be 1-9)")
        void empId_zeroAsFirstDigit_fails() {
            Employee e = validEmployee("ABC02345F");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — only 4 digits after prefix fails")
        void empId_tooFewDigits_fails() {
            Employee e = validEmployee("ABC1234F");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — suffix not F or M fails")
        void empId_invalidSuffix_fails() {
            Employee e = validEmployee("ABC12345X");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — empty string fails")
        void empId_empty_fails() {
            Employee e = validEmployee("");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — null empId passes @Pattern (null is ignored by @Pattern)")
        void empId_null_fails() {
            Employee e = validEmployee("ABC12345F");
            e.setEmpId(null);
            // @Pattern allows null by default — document this behaviour.
            // Add @NotNull to empId on the entity if null must be blocked.
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — two-letter prefix (no hyphen) fails")
        void empId_twoLetterPrefix_noHyphen_fails() {
            Employee e = validEmployee("AB12345F");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. fname
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("fname — @NotNull @Size(max=20)")
    class FnameValidation {

        @Test
        @DisplayName("VALID — fname within 20 chars")
        void fname_valid() {
            Employee e = validEmployee("ABC11111F");
            e.setFname("Alexander");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — fname exactly 20 chars")
        void fname_exactly20Chars() {
            Employee e = validEmployee("ABC11112F");
            e.setFname("A".repeat(20));
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — fname null fails @NotNull")
        void fname_null_fails() {
            Employee e = validEmployee("ABC11113F");
            e.setFname(null);
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fname"));
        }

        @Test
        @DisplayName("INVALID — fname exceeds 20 chars fails @Size")
        void fname_tooLong_fails() {
            Employee e = validEmployee("ABC11114F");
            e.setFname("A".repeat(21));
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fname"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. minit
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("minit — @Size(max=1), optional")
    class MinitValidation {

        @Test
        @DisplayName("VALID — single char minit passes")
        void minit_valid() {
            Employee e = validEmployee("ABC22221F");
            e.setMinit("B");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — null minit is allowed (optional field)")
        void minit_null_allowed() {
            Employee e = validEmployee("ABC22222F");
            e.setMinit(null);
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — minit exceeds 1 char fails @Size")
        void minit_tooLong_fails() {
            Employee e = validEmployee("ABC22223F");
            e.setMinit("AB");
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("minit"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. lname
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("lname — @NotNull @Size(max=30)")
    class LnameValidation {

        @Test
        @DisplayName("VALID — lname within 30 chars")
        void lname_valid() {
            Employee e = validEmployee("ABC33331F");
            e.setLname("Johnson");
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — lname null fails @NotNull")
        void lname_null_fails() {
            Employee e = validEmployee("ABC33332F");
            e.setLname(null);
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("lname"));
        }

        @Test
        @DisplayName("INVALID — lname exceeds 30 chars fails @Size")
        void lname_tooLong_fails() {
            Employee e = validEmployee("ABC33333F");
            e.setLname("A".repeat(31));
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("lname"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. hireDate
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("hireDate — @NotNull")
    class HireDateValidation {

        @Test
        @DisplayName("VALID — hireDate set passes")
        void hireDate_valid() {
            Employee e = validEmployee("ABC44441F");
            e.setHireDate(LocalDateTime.now());
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — hireDate null fails @NotNull")
        void hireDate_null_fails() {
            Employee e = validEmployee("ABC44442F");
            e.setHireDate(null);
            assertThat(validator.validate(e))
                    .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate"));
        }

        @Test
        @DisplayName("EDGE — future hireDate is allowed by current constraints")
        void hireDate_future_allowed() {
            Employee e = validEmployee("ABC44443F");
            e.setHireDate(LocalDateTime.of(2099, 1, 1, 0, 0));
            assertThat(validator.validate(e)).isEmpty();
        }
    }



    // ═══════════════════════════════════════════════════════════════════════════
    // 7. REPOSITORY CRUD TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EmployeeRepository — CRUD")
    class CrudTests {

        @Test
        @DisplayName("VALID — save and findById returns same employee")
        void save_and_findById() {
            Employee e = validEmployee("ABC66661F");
            employeeRepository.save(e);
            entityManager.flush();
            entityManager.clear();

            Optional<Employee> found = employeeRepository.findById("ABC66661F");
            assertThat(found).isPresent();
            assertThat(found.get().getFname()).isEqualTo("John");
            assertThat(found.get().getLname()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("VALID — findById for non-existent ID returns empty")
        void findById_notFound() {
            Optional<Employee> found = employeeRepository.findById("ZZZ99999F");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("VALID — update fname persists correctly")
        void update_fname() {
            Employee e = validEmployee("ABC66662F");
            employeeRepository.save(e);
            entityManager.flush();
            entityManager.clear();

            Employee fetched = employeeRepository.findById("ABC66662F").orElseThrow();
            fetched.setFname("Jane");
            employeeRepository.save(fetched);
            entityManager.flush();
            entityManager.clear();

            assertThat(employeeRepository.findById("ABC66662F").get().getFname())
                    .isEqualTo("Jane");
        }

        @Test
        @DisplayName("VALID — delete removes employee from DB")
        void delete_employee() {
            Employee e = validEmployee("ABC66663F");
            employeeRepository.save(e);
            entityManager.flush();

            employeeRepository.deleteById("ABC66663F");
            entityManager.flush();

            assertThat(employeeRepository.findById("ABC66663F")).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 8. CUSTOM QUERY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EmployeeRepository — findByLname")
    class FindByLnameTests {

        @Test
        @DisplayName("VALID — returns employees matching lname")
        void findByLname_match() {
            Employee e1 = validEmployee("ABC77771F"); e1.setLname("Chang");
            Employee e2 = validEmployee("ABC77772M"); e2.setLname("Chang");
            Employee e3 = validEmployee("ABC77773F"); e3.setLname("Gupta");
            employeeRepository.save(e1);
            employeeRepository.save(e2);
            employeeRepository.save(e3);
            entityManager.flush();

            List<Employee> result = employeeRepository.findByLname("Chang");
            // FIX: 2 employees have lname "Chang", not 3
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Employee::getLname).containsOnly("Chang");
        }

        @Test
        @DisplayName("VALID — no match returns empty list")
        void findByLname_noMatch() {
            List<Employee> result = employeeRepository.findByLname("Nonexistent");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("EmployeeRepository — findByPubId")
    class FindByPubIdTests {

        @Test
        @DisplayName("VALID — returns employees matching pubId")
        void findByPubId_match() {
            Employee e1 = validEmployee("ABC88881F"); e1.setPubId("1622");
            Employee e2 = validEmployee("ABC88882M"); e2.setPubId("1622");
            Employee e3 = validEmployee("ABC88883F"); e3.setPubId("9952");
            employeeRepository.save(e1);
            employeeRepository.save(e2);
            employeeRepository.save(e3);
            entityManager.flush();

            List<Employee> result = employeeRepository.findByPubId("1622");
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Employee::getPubId).containsOnly("1622");
        }

        @Test
        @DisplayName("VALID — no employees for given pubId returns empty list")
        void findByPubId_noMatch() {
            List<Employee> result = employeeRepository.findByPubId("0736");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("EmployeeRepository — findByJobId")
    class FindByJobIdTests {

        @Test
        @DisplayName("VALID — returns employees matching jobId")
        void findByJobId_match() {
            Employee e1 = validEmployee("ABC99991F"); e1.setJobId((short) 7);
            Employee e2 = validEmployee("ABC99992M"); e2.setJobId((short) 7);
            Employee e3 = validEmployee("ABC99993F"); e3.setJobId((short) 3);
            employeeRepository.save(e1);
            employeeRepository.save(e2);
            employeeRepository.save(e3);
            entityManager.flush();

            List<Employee> result = employeeRepository.findByJobId((short) 7);
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Employee::getJobId).containsOnly((short) 7);
        }

        @Test
        @DisplayName("VALID — no employees for given jobId returns empty list")
        void findByJobId_noMatch() {
            List<Employee> result = employeeRepository.findByJobId((short) 999);
            assertThat(result).isEmpty();
        }
    }
}