package com.cg.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * TitleAuthorId — Composite Primary Key for the "titleauthor" table.
 *
 * The titleauthor table has a composite PK made of:
 *   - au_id    (author's ID)
 *   - title_id (title's ID)
 *
 * When a table has composite PK in JPA, we need a separate class
 * that implements Serializable and has equals() + hashCode().
 *
 * This class is used with @IdClass(TitleAuthorId.class) on TitleAuthor entity.
 */
public class TitleAuthorId implements Serializable {

    private String auId;
    private String titleId;

    // Default constructor (required by JPA)
    public TitleAuthorId() {}

    public TitleAuthorId(String auId, String titleId) {
        this.auId = auId;
        this.titleId = titleId;
    }

    // Getters and Setters
    public String getAuId() { return auId; }
    public void setAuId(String auId) { this.auId = auId; }

    public String getTitleId() { return titleId; }
    public void setTitleId(String titleId) { this.titleId = titleId; }

    // equals() and hashCode() are REQUIRED for composite keys to work correctly
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TitleAuthorId)) return false;
        TitleAuthorId that = (TitleAuthorId) o;
        return Objects.equals(auId, that.auId) &&
               Objects.equals(titleId, that.titleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auId, titleId);
    }
}
