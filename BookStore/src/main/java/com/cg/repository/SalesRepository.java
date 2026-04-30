package com.cg.repository;

import com.cg.entity.Sales;
import com.cg.entity.SalesId;
import com.cg.projection.SalesProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// SalesProjection hides storId and titleId (internal FK keys)
// Shows ordNum (real order reference), ordDate, qty, payterms
@RepositoryRestResource(
    collectionResourceRel = "sales",
    path = "sales",
    excerptProjection = SalesProjection.class
)
public interface SalesRepository extends PagingAndSortingRepository<Sales, SalesId>,
                                         CrudRepository<Sales, SalesId> {

    List<Sales> findByStorId(@Param("storId") String storId);
    List<Sales> findByTitleId(@Param("titleId") String titleId);

    @Override
    @RestResource(exported = false)
    void deleteById(SalesId id);

    @Override
    @RestResource(exported = false)
    void delete(Sales entity);

    @Override
    @RestResource(exported = false)
    void deleteAllById(Iterable<? extends SalesId> ids);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Sales> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
