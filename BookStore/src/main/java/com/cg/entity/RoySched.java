package com.cg.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.rest.core.annotation.RestResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "roysched")
public class RoySched {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roysched_id")
    private Integer roySchedId;

    // Plain String column — this is what you send in POST body
    @Column(name = "title_id", columnDefinition = "CHAR(10)")
    private String titleId;

    // Relationship for JPA joins only — completely hidden from REST and JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", insertable = false, updatable = false)
    private Title title;

    @Column(name = "lorange")
    @Min(value = 0, message = "Low range cannot be negative")
    private Integer lorange;

    @Column(name = "hirange")
    @Min(value = 0, message = "High range cannot be negative")
    private Integer hirange;

    @Column(name = "royalty")
    @Min(value = 0, message = "Royalty percentage cannot be negative")
    private Integer royalty;

    // ── Getters ──────────────────────────────────────────────────

    public Integer getRoySchedId() { return roySchedId; }

    public Title getTitle() { return title; }
    public Integer getLorange() { return lorange; }
    public Integer getHirange() { return hirange; }
    public Integer getRoyalty() { return royalty; }
    public String getTitleId() { return titleId; }

    // ── Setters ──────────────────────────────────────────────────

    public void setRoySchedId(Integer roySchedId) { this.roySchedId = roySchedId; }

    public void setTitle(Title title) { this.title = title; }
    public void setLorange(Integer lorange) { this.lorange = lorange; }
    public void setHirange(Integer hirange) { this.hirange = hirange; }
    public void setRoyalty(Integer royalty) { this.royalty = royalty; }
    public void setTitleId(String titleId) { this.titleId = titleId; }
}