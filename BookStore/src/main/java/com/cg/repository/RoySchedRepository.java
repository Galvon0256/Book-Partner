package com.cg.repository;

import com.cg.entity.RoySched;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * RoySchedRepository — exposes /api/royscheds via Spring Data REST.
 *
 * Spring Data REST automatically generates these endpoints:
 *   GET    /api/royscheds                  → paginated list of all royalty schedules
 *   GET    /api/royscheds/{id}             → single royalty schedule by ID
 *   POST   /api/royscheds                  → create a new royalty schedule (returns 201)
 *   PUT    /api/royscheds/{id}             → update a royalty schedule (returns 200)
 *   GET    /api/royscheds/search/findByTitleTitleId?titleId=BU1032 → schedules for a title
 *
 * No delete operation is exposed — not part of this module.
 * No @Query is used — derived method names only.
 */
@RepositoryRestResource(collectionResourceRel = "royscheds", path = "royscheds")
public interface RoySchedRepository
        extends PagingAndSortingRepository<RoySched, Integer>,
                CrudRepository<RoySched, Integer> {

    /**
     * GET /api/royscheds/search/findByTitleTitleId?titleId=BU1032
     *
     * Returns all royalty tiers for the given title.
     * "TitleTitleId" means: navigate the "title" field → then its "titleId" field.
     * Spring Data JPA handles this traversal automatically.
     *
     * Spring generates SQL: SELECT r.* FROM roysched r JOIN titles t ON r.title_id = t.title_id WHERE t.title_id = ?
     *
     * This is the main endpoint used by the Thymeleaf detail page for a title
     * to show the royalty tiers table.
     */
    List<RoySched> findByTitleTitleId(String titleId);

    /**
     * GET /api/royscheds/search/findByRoyaltyGreaterThanEqual?royalty=12
     *
     * Returns all royalty schedule entries where the royalty percentage
     * is at or above the given value.
     *
     * Useful for filtering high-royalty tiers across all titles.
     *
     * Spring generates SQL: SELECT * FROM roysched WHERE royalty >= ?
     */
    List<RoySched> findByRoyaltyGreaterThanEqual(Integer royalty);

}
