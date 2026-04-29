package com.cg.repository;

import com.cg.entity.Store;
import com.cg.projection.StoreProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
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

    List<Store> findByState(String state);
    List<Store> findByCity(String city);

    @Override
    @RestResource(exported = false)
    void deleteById(String id);

    @Override
    @RestResource(exported = false)
    void delete(Store entity);
}
