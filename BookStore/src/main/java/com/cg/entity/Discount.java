package com.cg.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "discounts")
public class Discount {

    @Id
    @NotBlank(message = "Discount type is required")
    @Size(max = 40, message = "Discount type must be at most 40 characters")
    @Column(name = "discounttype", length = 40)
    private String discounttype;

    // Nullable — global discounts have no specific store
    @Size(max = 4, message = "Store ID must be at most 4 characters")
    @Column(name = "stor_id", length = 4)
    private String storId;

    @Column(name = "lowqty")
    private Short lowqty;

    @Column(name = "highqty")
    private Short highqty;

    @NotNull(message = "Discount value is required")
    @Column(name = "discount", precision = 4, scale = 2)
    private BigDecimal discount;

    public String getDiscounttype() { return discounttype; }
    public void setDiscounttype(String discounttype) { this.discounttype = discounttype; }

    public String getStorId() { return storId; }
    public void setStorId(String storId) { this.storId = storId; }

    public Short getLowqty() { return lowqty; }
    public void setLowqty(Short lowqty) { this.lowqty = lowqty; }

    public Short getHighqty() { return highqty; }
    public void setHighqty(Short highqty) { this.highqty = highqty; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
