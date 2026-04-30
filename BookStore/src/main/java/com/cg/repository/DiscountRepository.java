package com.cg.repository;

import com.cg.entity.Discount;
import com.cg.projection.DiscountProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// DiscountProjection hides storId (FK)
// Shows discounttype, lowqty, highqty, discount value — all business-meaningful
@RepositoryRestResource(
    collectionResourceRel = "discounts",
    path = "discounts",
    excerptProjection = DiscountProjection.class
)
public interface DiscountRepository extends PagingAndSortingRepository<Discount, String>,
                                            CrudRepository<Discount, String> {

    List<Discount> findByStorId(@Param("storId") String storId);
    List<Discount> findByDiscounttype(@Param("discounttype") String discounttype);

    @Override
    @RestResource(exported = false)
    void deleteById(String id);

    @Override
    @RestResource(exported = false)
    void delete(Discount entity);

    @Override
    @RestResource(exported = false)
    void deleteAllById(Iterable<? extends String> ids);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Discount> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
