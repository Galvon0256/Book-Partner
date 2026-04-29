package com.cg.projection;

import com.cg.entity.Discount;
import org.springframework.data.rest.core.config.Projection;
import java.math.BigDecimal;

// What a non-tech user cares about for a discount:
// What type of discount? How much? What quantity range?
// id (surrogate PK) and storId (FK) are internal — not shown
@Projection(name = "discountView", types = { Discount.class })
public interface DiscountProjection {
    String getDiscounttype();   // "Customer Discount" / "Volume Discount"
    Short getLowqty();          // Minimum quantity to qualify
    Short getHighqty();         // Maximum quantity for this tier
    BigDecimal getDiscount();   // "10.50" — percentage discount
}
