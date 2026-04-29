package com.cg.repository;



import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.cg.entity.Job;

import java.util.List;

/*
 * Spring Data REST will auto-expose these endpoints:
 *
 *   GET  /api/jobs                                        → paginated list of all jobs
 *   GET  /api/jobs/{job_id}                               → single job by ID
 *   POST /api/jobs                                        → create new job
 *   PUT  /api/jobs/{job_id}                               → update job
 *
 *   GET  /api/jobs/search/findByMinLvlLessThanEqual?lvl=50  → jobs with min_lvl ≤ value
 *   GET  /api/jobs/search/findByMaxLvlGreaterThanEqual?lvl=100 → jobs with max_lvl ≥ value
 *   GET  /api/jobs/search/findByJobDescIgnoreCase?jobDesc=desc → jobs with desc
 */

@RepositoryRestResource(collectionResourceRel = "jobs", path = "jobs")
public interface JobRepository
        extends PagingAndSortingRepository<Job, Short>,
                CrudRepository<Job, Short> {

    // GET /api/jobs/search/findByMinLvlLessThanEqual?lvl=50
    // Returns all jobs whose minimum level is at or below the given value
	List<Job> findByMinLvlLessThanEqual(@Param("lvl") Integer lvl);
	List<Job> findByMaxLvlGreaterThanEqual(@Param("lvl") Integer lvl);

	// GET /api/jobs/search/findByJobDescIgnoreCase?jobDesc=editor
	List<Job> findByJobDescIgnoreCase(@Param("jobDesc") String jobDesc);
}