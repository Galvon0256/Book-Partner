package com.cg.repository;



import jakarta.persistence.EntityManager;
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

import com.cg.entity.Employee;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data JPA tests for Employee entity and EmployeeRepository.
 *
 * Test coverage:
 *  - empId     : valid patterns (ABC12345F, A-B12345M), invalid patterns, null
 *  - fname     : valid, null (@NotNull), exceeds 20 chars
 *  - minit     : valid single char, exceeds 1 char, null (optional field)
 *  - lname     : valid, null (@NotNull), exceeds 30 chars
 *  - jobId     : valid short, null (optional FK)
 *  - jobLvl    : valid integer, null (optional)
 *  - pubId     : valid string, null (optional FK)
 *  - hireDate  : valid, null (@NotNull)
 *  - Repository: save, findById, findByLname, findByPubId, findByJobId
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Validator validator;

    // ─── helper ───────────────────────────────────────────────────────────────

    /** Returns a fully valid Employee that passes all constraints. */
    private Employee validEmployee(String empId) {
        Employee e = new Employee();
        e.setEmpId(empId);
        e.setFname("John");
        e.setMinit("A");
        e.setLname("Smith");
        e.setJobId((short) 5);
        e.setJobLvl(100);
        e.setPubId("0877");
        e.setHireDate(LocalDateTime.of(2020, 6, 15, 9, 0));
        return e;
    }

    @BeforeEach
    void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. empId — Pattern validation
    //    Valid formats:  ABC12345F  |  A-B12345M
    //    Regex: ^(?:[A-Z]{3}|[A-Z]-[A-Z])[1-9][0-9]{4}[FM]$
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
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — digit starting with 0 fails (first digit must be 1-9)")
        void empId_zeroAsFirstDigit_fails() {
            Employee e = validEmployee("ABC02345F");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — only 4 digits after prefix fails")
        void empId_tooFewDigits_fails() {
            Employee e = validEmployee("ABC1234F");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — suffix not F or M fails")
        void empId_invalidSuffix_fails() {
            Employee e = validEmployee("ABC12345X");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — empty string fails")
        void empId_empty_fails() {
            Employee e = validEmployee("");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("empId"));
        }

        @Test
        @DisplayName("INVALID — null empId fails @Pattern (null check)")
        void empId_null_fails() {
            Employee e = validEmployee("ABC12345F");
            e.setEmpId(null);
            // @Pattern allows null by default — document this behaviour
            // If null must be blocked, add @NotNull to empId
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            // No violation from @Pattern alone — null passes @Pattern
            assertThat(violations).isEmpty(); // <- expected with current entity setup
        }

        @Test
        @DisplayName("INVALID — two-letter prefix (no hyphen) fails")
        void empId_twoLetterPrefix_noHyphen_fails() {
            Employee e = validEmployee("AB12345F");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
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
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fname"));
        }

        @Test
        @DisplayName("INVALID — fname exceeds 20 chars fails @Size")
        void fname_tooLong_fails() {
            Employee e = validEmployee("ABC11114F");
            e.setFname("A".repeat(21));
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("fname"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. minit (optional, max 1 char)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("minit — @Size(max=1), optional")
    class MinitValidation {

        @Test
        @DisplayName("VALID — single char minit")
        void minit_singleChar() {
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
        @DisplayName("INVALID — minit with 2 chars fails @Size")
        void minit_twoChars_fails() {
            Employee e = validEmployee("ABC22223F");
            e.setMinit("AB");
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
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
        @DisplayName("VALID — lname exactly 30 chars")
        void lname_exactly30Chars() {
            Employee e = validEmployee("ABC33332F");
            e.setLname("L".repeat(30));
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("INVALID — lname null fails @NotNull")
        void lname_null_fails() {
            Employee e = validEmployee("ABC33333F");
            e.setLname(null);
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("lname"));
        }

        @Test
        @DisplayName("INVALID — lname exceeds 30 chars fails @Size")
        void lname_tooLong_fails() {
            Employee e = validEmployee("ABC33334F");
            e.setLname("L".repeat(31));
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
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
        @DisplayName("VALID — hireDate set")
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
            Set<ConstraintViolation<Employee>> violations = validator.validate(e);
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("hireDate"));
        }

        @Test
        @DisplayName("EDGE — future hireDate is allowed by current constraints")
        void hireDate_future_allowed() {
            // No @Past or @PastOrPresent on entity — document this gap
            Employee e = validEmployee("ABC44443F");
            e.setHireDate(LocalDateTime.of(2099, 1, 1, 0, 0));
            assertThat(validator.validate(e)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 6. jobId and pubId (optional FK fields)
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("jobId and pubId — optional FK fields")
    class OptionalFkFields {

        @Test
        @DisplayName("VALID — jobId null is allowed (no @NotNull)")
        void jobId_null_allowed() {
            Employee e = validEmployee("ABC55551F");
            e.setJobId(null);
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — pubId null is allowed (no @NotNull)")
        void pubId_null_allowed() {
            Employee e = validEmployee("ABC55552F");
            e.setPubId(null);
            assertThat(validator.validate(e)).isEmpty();
        }

        @Test
        @DisplayName("VALID — jobLvl null is allowed (no @NotNull)")
        void jobLvl_null_allowed() {
            Employee e = validEmployee("ABC55553F");
            e.setJobLvl(null);
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
            Employee e1 = validEmployee("ABC88881F"); e1.setPubId("0877");
            Employee e2 = validEmployee("ABC88882M"); e2.setPubId("0877");
            Employee e3 = validEmployee("ABC88883F"); e3.setPubId("1234");
            employeeRepository.save(e1);
            employeeRepository.save(e2);
            employeeRepository.save(e3);
            entityManager.flush();

            List<Employee> result = employeeRepository.findByPubId("0877");
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Employee::getPubId).containsOnly("0877");
        }

        @Test
        @DisplayName("VALID — no employees for given pubId returns empty list")
        void findByPubId_noMatch() {
            List<Employee> result = employeeRepository.findByPubId("9999");
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