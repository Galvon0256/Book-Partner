package com.cg.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "titleauthor")
@IdClass(TitleAuthorId.class)   // Tells JPA the composite PK class to use
public class TitleAuthor {

    @Id
    @Column(name = "au_id", length = 11, nullable = false)
    private String auId;

    @Id
    @Column(name = "title_id", length = 10, nullable = false)
    private String titleId;

    @Column(name = "au_ord")
    @Min(value = 1, message = "Author order must be at least 1")
    private Integer auOrd;

    @Column(name = "royaltyper")
    @Min(value = 0, message = "Royalty percentage cannot be negative")
    @Max(value = 100, message = "Royalty percentage cannot exceed 100")
    private Integer royaltyper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "au_id", insertable = false, updatable = false)
    @JsonIgnore
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", insertable = false, updatable = false)
    @JsonIgnore
    private Title title;
    public TitleAuthor() {
        // Required by JPA
    }

    public TitleAuthor(String auId, String titleId, Integer auOrd, Integer royaltyper) {
        this.auId = auId;
        this.titleId = titleId;
        this.auOrd = auOrd;
        this.royaltyper = royaltyper;
    }

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

    public Title getTitle() { return title; }
    public void setTitle(Title title) { this.title = title; }

    

    @Override
public String toString() {
    return "TitleAuthor{" +
            "auId='" + auId + '\'' +
            ", titleId='" + titleId + '\'' +
            ", auOrd=" + auOrd +
            ", royaltyper=" + royaltyper +
            ", titleName='" + (title != null ? title.getTitle() : "null") + '\'' +
            ", titleType='" + (title != null ? title.getType() : "null") + '\'' +
            ", titleRoyalty=" + (title != null ? title.getRoyalty() : "null") +
            '}';
}
}
