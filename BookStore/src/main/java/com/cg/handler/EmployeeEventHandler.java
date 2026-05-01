package com.cg.handler;

import com.cg.entity.Employee;
import com.cg.exception.EmployeeNotFoundException;
import com.cg.exception.InvalidJobIdException;
import com.cg.exception.InvalidPublisherIdException;
import com.cg.repository.EmployeeRepository;
import com.cg.repository.JobRepository;
import com.cg.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * EmployeeEventHandler — Spring Data REST repository event handler.
 *
 * Fires BEFORE Spring Data REST calls save() or create() on the Employee
 * repository, so FK violations are caught and converted to proper 400/404
 * HTTP responses instead of leaking as database errors at commit time.
 *
 * Handles:
 *   @HandleBeforeCreate — fires on POST /api/employees
 *   @HandleBeforeSave   — fires on PUT and PATCH /api/employees/{id}
 *
 * Validations performed:
 *   1. jobId (if provided) must exist in the jobs table      → InvalidJobIdException       (400)
 *   2. pubId (if provided) must exist in the publishers table → InvalidPublisherIdException (400)
 *
 * For PATCH on a missing emp_id, Spring Data REST throws before this handler
 * is reached, but we include an existence check in handleBeforeSave as a
 * safety net in case the flow differs → EmployeeNotFoundException (404).
 */
@Component
@RepositoryEventHandler(Employee.class)
public class EmployeeEventHandler {

    private final JobRepository jobRepository;
    private final PublisherRepository publisherRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeEventHandler(JobRepository jobRepository,
                                PublisherRepository publisherRepository,
                                EmployeeRepository employeeRepository) {
        this.jobRepository = jobRepository;
        this.publisherRepository = publisherRepository;
        this.employeeRepository = employeeRepository;
    }

    // -------------------------------------------------------
    // POST /api/employees
    // Validate FKs before the new record is inserted.
    // -------------------------------------------------------
    @HandleBeforeCreate
    public void handleBeforeCreate(Employee employee) {
        validateJobId(employee);
        validatePubId(employee);
    }

    // -------------------------------------------------------
    // PUT /api/employees/{id}  and  PATCH /api/employees/{id}
    // Validate FKs before the existing record is updated.
    // Also guards against a PATCH reaching a non-existent emp_id.
    // -------------------------------------------------------
    @HandleBeforeSave
    public void handleBeforeSave(Employee employee) {
        // Safety-net: confirm the employee actually exists (covers PATCH on missing ID)
        if (employee.getEmpId() != null &&
                !employeeRepository.existsById(employee.getEmpId())) {
            throw new EmployeeNotFoundException(
                    "Employee not found with id: " + employee.getEmpId());
        }

        validateJobId(employee);
        validatePubId(employee);
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    /**
     * If jobId is present on the payload, verify it exists in the jobs table.
     * Throws InvalidJobIdException (→ 400 Bad Request) if not found.
     */
    private void validateJobId(Employee employee) {
        if (employee.getJobId() != null &&
                !jobRepository.existsById(employee.getJobId())) {
            throw new InvalidJobIdException(
                    "Job not found with id: " + employee.getJobId());
        }
    }

    /**
     * If pubId is present on the payload, verify it exists in the publishers table.
     * Throws InvalidPublisherIdException (→ 400 Bad Request) if not found.
     */
    private void validatePubId(Employee employee) {
        if (employee.getPubId() != null &&
                !publisherRepository.existsById(employee.getPubId())) {
            throw new InvalidPublisherIdException(
                    "Publisher not found with id: " + employee.getPubId());
        }
    }
}