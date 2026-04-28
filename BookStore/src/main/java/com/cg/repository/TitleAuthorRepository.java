package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.cg.entity.TitleAuthor;
import com.cg.entity.TitleAuthorId;

import java.util.List;

/**
 * TitleAuthorRepository — Spring Data REST Repository for TitleAuthor.
 *
 * Automatically exposes:
 *   GET    /api/titleauthors               → list all titleauthor records
 *   GET    /api/titleauthors/{auId}_{titleId}  → get one record
 *   POST   /api/titleauthors              → create a new record
 *   PUT    /api/titleauthors/{id}         → update a record
 *
 * Two custom finder methods defined below.
 */
@RepositoryRestResource(collectionResourceRel = "titleauthors", path = "titleauthors")
public interface TitleAuthorRepository extends JpaRepository<TitleAuthor, TitleAuthorId> {

    /**
     * Custom Method 1: Find all titleauthor records for a specific author.
     *
     * This tells us which titles a particular author has written.
     * Spring Data creates SQL: SELECT * FROM titleauthor WHERE au_id = ?
     *
     * Exposed at:
     *   GET /api/titleauthors/search/findByAuId?auId=172-32-1176
     */
    List<TitleAuthor> findByAuId(String auId);

    /**
     * Custom Method 2: Find all titleauthor records for a specific title.
     *
     * This tells us which authors wrote a particular title.
     * Spring Data creates SQL: SELECT * FROM titleauthor WHERE title_id = ?
     *
     * Exposed at:
     *   GET /api/titleauthors/search/findByTitleId?titleId=PS3333
     */
    List<TitleAuthor> findByTitleId(String titleId);

}
