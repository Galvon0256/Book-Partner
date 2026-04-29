package com.cg.repository;

import com.cg.entity.Title;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * TitleRepository — exposes /api/titles via Spring Data REST.
 *
 * Spring Data REST automatically generates these endpoints:
 *   GET    /api/titles              → paginated list of all titles
 *   GET    /api/titles/{title_id}   → single title by ID
 *   POST   /api/titles              → create a new title (returns 201)
 *   PUT    /api/titles/{title_id}   → update an existing title (returns 200)
 *   GET    /api/titles/search/findByType?type=business   → titles by category
 *   GET    /api/titles/search/findByPublisherPubId?pubId=1389 → titles by publisher
 *
 * Pagination is handled automatically by PagingAndSortingRepository.
 * No need to write a custom findAll — Spring Data REST paginates by default.
 * Page size is set in application.properties: spring.data.rest.default-page-size
 *
 * No @Query is used — Spring Data JPA builds the SQL from the method names.
 * No delete method is exposed — delete is not part of this module.
 */
@RepositoryRestResource(collectionResourceRel = "titles", path = "titles")
public interface TitleRepository
        extends PagingAndSortingRepository<Title, String>,
                CrudRepository<Title, String> {

    /**
     * GET /api/titles/search/findByType?type=business
     *
     * Returns all titles that match the given type (category).
     * Common types in pubs: business, mod_cook, psychology, trad_cook, UNDECIDED
     *
     * Spring generates SQL: SELECT * FROM titles WHERE type = ?
     */
    List<Title> findByType(String type);

    /**
     * GET /api/titles/search/findByPublisherPubId?pubId=1389
     *
     * Returns all titles that belong to the publisher with the given pub_id.
     * "PublisherPubId" means: navigate the "publisher" field → then its "pubId" field.
     * Spring Data JPA understands this association traversal without any @Query.
     *
     * Spring generates SQL: SELECT t.* FROM titles t JOIN publishers p ON t.pub_id = p.pub_id WHERE p.pub_id = ?
     *
     * This is used by Kartik's frontend page to show titles under a publisher.
     */
    List<Title> findByPublisherPubId(String pubId);

}
