package com.cg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Table(name = "employee")
public class Employee {

	@Id
	@Column(name = "emp_id", length = 10)
	@Pattern(
		    regexp = "^(?:[A-Z]{3}|[A-Z]-[A-Z])[1-9][0-9]{4}[FM]$",
		    message = "empId must be like ABC12345F or A-B12345M"
		)
	private String empId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "fname", length = 20, nullable = false)
    private String fname;

    @Size(max = 1)
    @Column(name = "minit", length = 1)
    private String minit;
    
    @NotBlank
    @Size(max = 30)
    @Column(name = "lname", length = 30, nullable = false)
    private String lname;

 // raw FK field (used for insert/update)
    @NotNull
    @Column(name = "job_id")
    private Short jobId;
    // relationship (used for fetch only)
    

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", insertable = false, updatable = false)
    private Job job;

    
    @Column(name = "job_lvl")
    private Integer jobLvl;
    
    @NotBlank
    @Column(name = "pub_id", columnDefinition = "char(4)")
    private String pubId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id",insertable = false, updatable = false)
    private Publisher publisher;
 

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDateTime hireDate;

    // ── Getters and Setters ──────────────────────────────────────

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }



    public String getMinit() {
		return minit;
	}

	public void setMinit(String minit) {
		this.minit = minit;
	}

	public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }
    
    

    public Short getJobId() {
		return jobId;
	}

	public void setJobId(Short jobId) {
		this.jobId = jobId;
	}

	public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Integer getJobLvl() {
        return jobLvl;
    }

    public void setJobLvl(Integer jobLvl) {
        this.jobLvl = jobLvl;
    }

    public String getPubId() {
        return pubId;
    }

    public void setPubId(String pubId) {
        this.pubId = pubId;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
}