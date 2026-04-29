package com.cg.repository;

import com.cg.entity.Publisher;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data REST will auto-expose:
 *   GET    /api/publishers           — list all publishers (paginated)
 *   GET    /api/publishers/{pub_id}  — get single publisher
 *   POST   /api/publishers           — create new publisher
 *   PUT    /api/publishers/{pub_id}  — full update
 *   PATCH  /api/publishers/{pub_id}  — partial update
 *   DELETE /api/publishers/{pub_id}  — delete
 *
 * No controller needed. No @Query needed.
 */
@RepositoryRestResource(collectionResourceRel = "publishers", path = "publishers")
public interface PublisherRepository extends PagingAndSortingRepository<Publisher, String>,
                                             CrudRepository<Publisher, String> {

    // Derived query — Spring builds SQL from method name:
    // SELECT * FROM publishers WHERE city = ?
    // Exposed at: GET /api/publishers/search/findByCity?city=Boston
    List<Publisher> findByCity(String city);

    // SELECT * FROM publishers WHERE state = ?
    // Exposed at: GET /api/publishers/search/findByState?state=MA
    List<Publisher> findByState(String state);

    // SELECT * FROM publishers WHERE country = ?
    // Exposed at: GET /api/publishers/search/findByCountry?country=USA
    List<Publisher> findByCountry(String country);
}
