package com.cg.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.cg.entity.Employee;

import java.util.List;

/*
 * Spring Data REST will auto-expose these endpoints:
 *
 *   GET  /api/employees                                           → paginated list
 *   GET  /api/employees/{emp_id}                                  → single employee
 *   POST /api/employees                                           → create employee
 *   PUT  /api/employees/{emp_id}                                  → update employee
 *
 *   GET  /api/employees/search/findByLname?lname=Chang            → search by last name
 *   GET  /api/employees/search/findByPubId?pubId=0877             → employees at a publisher
 *   GET  /api/employees/search/findByJobId?jobId=10               → employees in particular job
 *

 */

@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
public interface EmployeeRepository
        extends PagingAndSortingRepository<Employee, String>,
                CrudRepository<Employee, String> {

    // GET /api/employees/search/findByLname?lname=Chang
    // Derived query — no @Query needed — Spring Data builds it automatically
    List<Employee> findByLname(@Param("lname") String lname);

    // GET /api/employees/search/findByPubId?pubId=0877
    // Works on the plain pubId String column (temporary until Kartik merges)
    List<Employee> findByPubId(@Param("pubId") String pubId);
    
 // GET /api/employees/search/findByJobId?jobId=5
    List<Employee> findByJobId(@Param("jobId") Short jobId);
}