package com.cg.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * TitleAuthor entity — maps to the "titleauthor" table in the "pubs" database.
 *
 * This is a JOIN/bridge table between authors and titles.
 * It stores:
 *   - Which author wrote which title
 *   - The author's order (if multiple authors for one title)
 *   - The royalty percentage for that author
 *
 * The Primary Key is COMPOSITE: (au_id + title_id) together = unique row
 */
@Entity
@Table(name = "titleauthor")
@IdClass(TitleAuthorId.class)   // Tells JPA the composite PK class to use
public class TitleAuthor {

    // -------------------------------------------------------
    // Composite Primary Key — Part 1: Author ID
    // -------------------------------------------------------
    @Id
    @Column(name = "au_id", length = 11, nullable = false)
    private String auId;

    // -------------------------------------------------------
    // Composite Primary Key — Part 2: Title ID
    // -------------------------------------------------------
    @Id
    @Column(name = "title_id", length = 10, nullable = false)
    private String titleId;

    // -------------------------------------------------------
    // Author order — e.g., 1 = first author, 2 = second author
    // -------------------------------------------------------
    @Column(name = "au_ord")
    @Min(value = 1, message = "Author order must be at least 1")
    private Integer auOrd;

    // -------------------------------------------------------
    // Royalty percentage for this author for this title
    // -------------------------------------------------------
    @Column(name = "royaltyper")
    @Min(value = 0, message = "Royalty percentage cannot be negative")
    @Max(value = 100, message = "Royalty percentage cannot exceed 100")
    private Integer royaltyper;

    // -------------------------------------------------------
    // Relationship — Many TitleAuthor records belong to one Author
    // @JoinColumn links "au_id" in this table to "au_id" in authors table
    // insertable=false, updatable=false because au_id is already an @Id field
    // -------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "au_id", insertable = false, updatable = false)
    private Author author;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------
    public TitleAuthor() {
        // Required by JPA
    }

    public TitleAuthor(String auId, String titleId, Integer auOrd, Integer royaltyper) {
        this.auId = auId;
        this.titleId = titleId;
        this.auOrd = auOrd;
        this.royaltyper = royaltyper;
    }

    // -------------------------------------------------------
    // Getters and Setters (no Lombok)
    // -------------------------------------------------------
    public String getAuId() { return auId; }
    public void setAuId(String auId) { this.auId = auId; }

    public String getTitleId() { return titleId; }
    public void setTitleId(String titleId) { this.titleId = titleId; }

    public Integer getAuOrd() { return auOrd; }
    public void setAuOrd(Integer auOrd) { this.auOrd = auOrd; }

    public Integer getRoyaltyper() { return royaltyper; }
    public void setRoyaltyper(Integer royaltyper) { this.royaltyper = royaltyper; }

    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }

    @Override
    public String toString() {
        return "TitleAuthor{" +
                "auId='" + auId + '\'' +
                ", titleId='" + titleId + '\'' +
                ", auOrd=" + auOrd +
                ", royaltyper=" + royaltyper +
                '}';
    }
}
