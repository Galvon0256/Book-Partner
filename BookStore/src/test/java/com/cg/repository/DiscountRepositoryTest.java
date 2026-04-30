package com.cg.repository;

import com.cg.entity.Discount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Discount buildDiscount(String storId, String type) {
        Discount d = new Discount();
        d.setStorId(storId);
        d.setDiscounttype(type);
        d.setLowqty((short) 1);
        d.setHighqty((short) 10);
        d.setDiscount(new BigDecimal("10.50"));
        return d;
    }

    // Test 1 — findByStorId returns only that store's discounts
    @Test
    void testFindByStorId_returnsOnlyThatStore() {
        discountRepository.save(buildDiscount("6380", "Customer Discount"));
        discountRepository.save(buildDiscount("6380", "Volume Discount"));
        discountRepository.save(buildDiscount("7066", "Other Discount"));
        entityManager.flush();

        List<Discount> result = discountRepository.findByStorId("6380");

        assertEquals(2, result.size());
        result.forEach(d -> assertEquals("6380", d.getStorId()));
    }

    // Test 2 — findByStorId with no match returns empty list, not null
    @Test
    void testFindByStorId_noMatch_returnsEmpty() {
        List<Discount> result = discountRepository.findByStorId("9999");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // Test 3 — global discounts (storId = null) are retrievable by findByStorId(null)
    @Test
    void testFindByStorId_nullStorId_returnsGlobalDiscount() {
        Discount globalDiscount = new Discount();
        globalDiscount.setStorId(null); // global — no specific store
        globalDiscount.setDiscounttype("Global Discount");
        globalDiscount.setDiscount(new BigDecimal("5.00"));
        discountRepository.save(globalDiscount);
        entityManager.flush();

        List<Discount> result = discountRepository.findByStorId(null);

        assertEquals(1, result.size());
        assertNull(result.get(0).getStorId());
    }

    // Test 4 — findAll returns all saved discounts
    @Test
    void testFindAll() {
        discountRepository.save(buildDiscount("6380", "Type A"));
        discountRepository.save(buildDiscount("6380", "Type B"));
        discountRepository.save(buildDiscount("7066", "Type C"));
        entityManager.flush();

        List<Discount> result = (List<Discount>) discountRepository.findAll();

        assertEquals(3, result.size());
    }

    @Test
    void testFindByDiscounttype_returnsMatching() {
        discountRepository.save(buildDiscount("6380", "Customer Discount"));
        discountRepository.save(buildDiscount("7066", "Customer Discount"));
        discountRepository.save(buildDiscount("6380", "Volume Discount")); // different type
        entityManager.flush();
 
        List<Discount> result = discountRepository.findByDiscounttype("Customer Discount");
 
        assertEquals(2, result.size());
        result.forEach(d -> assertEquals("Customer Discount", d.getDiscounttype()));
    }
 
    // findByDiscounttype with no match returns empty list, not null
    @Test
    void testFindByDiscounttype_noMatch_returnsEmpty() {
        List<Discount> result = discountRepository.findByDiscounttype("Nonexistent Type");
 
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
