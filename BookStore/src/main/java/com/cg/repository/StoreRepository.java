package com.cg.repository;

import com.cg.entity.Store;
import com.cg.projection.StoreProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// excerptProjection = what the user sees in GET /api/stores (list)
// StoreProjection hides storId (internal DB key) — shows name, address, city, state, zip
@RepositoryRestResource(
    collectionResourceRel = "stores",
    path = "stores",
    excerptProjection = StoreProjection.class
)
public interface StoreRepository extends PagingAndSortingRepository<Store, String>,
                                         CrudRepository<Store, String> {

    List<Store> findByState(@Param("state") String state);
    List<Store> findByCity(@Param("city") String city);

    @Override
    @RestResource(exported = false)
    void deleteById(String id);

    @Override
    @RestResource(exported = false)
    void delete(Store entity);

    @Override
    @RestResource(exported = false)
    void deleteAllById(Iterable<? extends String> ids);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Store> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
