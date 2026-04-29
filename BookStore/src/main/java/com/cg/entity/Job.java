package com.cg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jobs")
public class Job {
	@Id
	@Column(name = "job_id")
	private Short jobId;

	@NotNull(message = "Job description cannot be null")
	@Size(max = 50, message = "Job description cannot exceed 50 characters")
	@Column(name = "job_desc", length = 50, nullable = false)
	private String jobDesc;

	@NotNull(message = "Minimum level is required")
	@Column(name = "min_lvl", nullable = false)
	private Integer minLvl;

	@NotNull(message = "Maximum level is required")
	@Column(name = "max_lvl", nullable = false)
	private Integer maxLvl;

    // ── Getters and Setters ──────────────────────────────────────

    public Short getJobId() {
        return jobId;
    }

    public void setJobId(Short jobId) {
        this.jobId = jobId;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    public Integer getMinLvl() {
        return minLvl;
    }

    public void setMinLvl(Integer minLvl) {
        this.minLvl = minLvl;
    }

    public Integer getMaxLvl() {
        return maxLvl;
    }

    public void setMaxLvl(Integer maxLvl) {
        this.maxLvl = maxLvl;
    }
}