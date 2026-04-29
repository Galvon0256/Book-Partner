package com.cg.repository;

import com.cg.entity.Discount;
import com.cg.projection.DiscountProjection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

// DiscountProjection hides id (surrogate PK) and storId (FK)
// Shows discounttype, lowqty, highqty, discount value — all business-meaningful
@RepositoryRestResource(
    collectionResourceRel = "discounts",
    path = "discounts",
    excerptProjection = DiscountProjection.class
)
public interface DiscountRepository extends PagingAndSortingRepository<Discount, Integer>,
                                            CrudRepository<Discount, Integer> {

    List<Discount> findByStorId(String storId);
    List<Discount> findByDiscounttype(String discounttype);

    @Override
    @RestResource(exported = false)
    void deleteById(Integer id);

    @Override
    @RestResource(exported = false)
    void delete(Discount entity);
}
