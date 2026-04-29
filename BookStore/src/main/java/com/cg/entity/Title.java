package com.cg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Title entity — maps to the "titles" table in the pubs database.
 * Owned by: Tanmay
 *
 * Relationships:
 *   - @ManyToOne Publisher  (many titles belong to one publisher)
 *   - One title has many RoySched rows (mapped on RoySched side)
 *
 * NOTE: Publisher is owned by Kartik. The stub Publisher.java in this
 * project will be replaced by Kartik's real entity after his PR merges.
 */
@Entity
@Table(name = "titles")
public class Title {

    // ── Primary Key ───────────────────────────────────────────────

    @Id
    @Column(name = "title_id", columnDefinition = "CHAR(10)")
    @NotBlank(message = "Title ID is required")
    @Size(max = 10, message = "Title ID must be at most 10 characters")
    private String titleId;

    // ── Columns ───────────────────────────────────────────────────

    @Column(name = "title", columnDefinition = "CHAR(80)", nullable = false)
    @NotBlank(message = "Title name is required")
    @Size(max = 80, message = "Title must be at most 80 characters")
    private String title;

    /*
     * "type" is the book category — e.g. business, mod_cook, psychology, trad_cook, UNDECIDED
     * The pubs schema stores this as CHAR(12), so we keep length = 12.
     */
    @Column(name = "type", columnDefinition = "CHAR(12)")
    @Size(max = 12, message = "Type must be at most 12 characters")
    private String type;

    /*
     * FK to publishers table.
     * fetch = LAZY means Spring will NOT load the publisher unless you ask for it.
     * This avoids slow joins on every title fetch.
     *
     * insertable = false / updatable = false are NOT set here,
     * because we want to be able to set the publisher when creating a title.
     *
     * To link a publisher when creating a title via REST, send the publisher URI:
     *   POST /api/titles
     *   Body: { ..., "publisher": "http://localhost:8080/api/publishers/1389" }
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pub_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Publisher publisher;

    @Column(name = "price")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private Double price;

    @Column(name = "advance")
    @DecimalMin(value = "0.0", message = "Advance cannot be negative")
    private Double advance;

    @Column(name = "royalty")
    @Min(value = 0, message = "Royalty cannot be negative")
    private Integer royalty;

    @Column(name = "ytd_sales")
    @Min(value = 0, message = "YTD sales cannot be negative")
    private Integer ytdSales;

    @Column(name = "notes", columnDefinition = "CHAR(200)")
    @Size(max = 200, message = "Notes must be at most 200 characters")
    private String notes;

    @Column(name = "pubdate")
    private LocalDateTime pubdate;

    // ── Getters ──────────────────────────────────────────────────

    public String getTitleId() {
        return titleId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Double getPrice() {
        return price;
    }

    public Double getAdvance() {
        return advance;
    }

    public Integer getRoyalty() {
        return royalty;
    }

    public Integer getYtdSales() {
        return ytdSales;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getPubdate() {
        return pubdate;
    }

    // ── Setters ──────────────────────────────────────────────────

    public void setTitleId(String titleId) {
        this.titleId = titleId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setAdvance(Double advance) {
        this.advance = advance;
    }

    public void setRoyalty(Integer royalty) {
        this.royalty = royalty;
    }

    public void setYtdSales(Integer ytdSales) {
        this.ytdSales = ytdSales;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPubdate(LocalDateTime pubdate) {
        this.pubdate = pubdate;
    }
}
