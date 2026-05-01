package com.cg.handler;

import com.cg.entity.Employee;
import com.cg.exception.DuplicateResourceException;
import com.cg.exception.EmployeeNotFoundException;
import com.cg.exception.InvalidJobIdException;
import com.cg.exception.InvalidJobLevelException;
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
    public void handleBeforeCreateDuplicacy(Employee employee) {
        // Add this check first
        if (employeeRepository.existsById(employee.getEmpId())) {
            throw new DuplicateResourceException(
                "Employee with ID '" + employee.getEmpId() + "' already exists");
        }
        validateJobId(employee);
        validatePubId(employee);
        validateJobLevel(employee);
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
        validateJobLevel(employee);
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    /**
     * If jobId is present on the payload, verify it exists in the jobs table.
     * Throws InvalidJobIdException (→ 400 Bad Request) if not found.
     */
    private void validateJobId(Employee employee) {
        if (employee.getJobId() == null) {
            throw new InvalidJobIdException("Job ID must not be null");
        }
        if (!jobRepository.existsById(employee.getJobId())) {
            throw new InvalidJobIdException("Job not found with id: " + employee.getJobId());
        }
        
        if(employee.getJobId()<=0) {
        	 throw new InvalidJobIdException("Job ID cannot be negative or 0");
        }
        
        
    }

    /**
     * If pubId is present on the payload, verify it exists in the publishers table.
     * Throws InvalidPublisherIdException (→ 400 Bad Request) if not found.
     */
    private void validatePubId(Employee employee) {
        if (employee.getPubId() == null) {
            throw new InvalidPublisherIdException("Publisher ID must not be null");
        }
        
        if(employee.getPubId()==""){
        	throw new InvalidPublisherIdException("Publisher ID should not be empty. Make sure it is a four digit number in string format ");
        	
        }
        if (!publisherRepository.existsById(employee.getPubId())) {
            throw new InvalidPublisherIdException("Publisher not found with id: " + employee.getPubId());
        }
        
       
    }
    
    private void validateJobLevel(Employee employee) {
        if (employee.getJobLvl() == null) return; // jobLvl is optional, skip if not provided

        if (employee.getJobId() == null) return; // jobId null already caught by validateJobId

        jobRepository.findById(employee.getJobId()).ifPresent(job -> {
            if (employee.getJobLvl() < job.getMinLvl() || employee.getJobLvl() > job.getMaxLvl()) {
                throw new InvalidJobLevelException(
                    "Job level " + employee.getJobLvl() +
                    " is out of range for this job. Must be between " +
                    job.getMinLvl() + " and " + job.getMaxLvl()
                );
            }
        });
    }
    

    
}