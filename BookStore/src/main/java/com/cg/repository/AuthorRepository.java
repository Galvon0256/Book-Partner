package com.cg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.cg.entity.Author;

import java.util.List;

/**
 * AuthorRepository — Spring Data REST Repository for Authors.
 *
 * @RepositoryRestResource automatically exposes this as REST endpoints:
 *   GET    /api/authors          → list all authors (paginated)
 *   GET    /api/authors/{id}     → get one author by ID
 *   POST   /api/authors          → create a new author
 *   PUT    /api/authors/{id}     → update an author
 *   PATCH  /api/authors/{id}     → partially update an author
 *
 * The "collectionResourceRel" is what the JSON key is called in the response.
 * The "path" is the URL path segment.
 *
 * We also define TWO custom finder methods here.
 * Spring Data automatically creates the SQL for these — no @Query needed!
 */
@RepositoryRestResource(collectionResourceRel = "authors", path = "authors")
public interface AuthorRepository extends JpaRepository<Author, String> {

    /**
     * Custom Method 1: Find all authors by city.
     *
     * Spring Data reads the method name and creates this SQL automatically:
     *   SELECT * FROM authors WHERE city = ?
     *
     * This is exposed at:
     *   GET /api/authors/search/findByCity?city=Oakland
     */
    List<Author> findByCity(String city);

    /**
     * Custom Method 2: Find all authors by state.
     *
     * Spring Data creates this SQL automatically:
     *   SELECT * FROM authors WHERE state = ?
     *
     * This is exposed at:
     *   GET /api/authors/search/findByState?state=CA
     */
    List<Author> findByState(String state);

}
