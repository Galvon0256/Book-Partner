package com.cg.repository;

import com.cg.entity.Discount;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Discount buildDiscount(String storId) {
        Discount discount = new Discount();
        discount.setStorId(storId);
        discount.setDiscounttype("TD" + UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        discount.setLowqty((short) 1);
        discount.setHighqty((short) 10);
        discount.setDiscount(new BigDecimal("10.50"));
        return discount;
    }

    @Test
    void testFindByStorId_returnsOnlyThatStore() {
        Discount storeDiscount1 = discountRepository.save(buildDiscount("6380"));
        Discount storeDiscount2 = discountRepository.save(buildDiscount("6380"));
        Discount otherStoreDiscount = discountRepository.save(buildDiscount("7066"));
        entityManager.flush();

        List<Discount> result = discountRepository.findByStorId("6380");

        assertTrue(result.stream().anyMatch(d -> d.getDiscounttype().equals(storeDiscount1.getDiscounttype())));
        assertTrue(result.stream().anyMatch(d -> d.getDiscounttype().equals(storeDiscount2.getDiscounttype())));
        assertFalse(result.stream().anyMatch(d -> d.getDiscounttype().equals(otherStoreDiscount.getDiscounttype())));
    }

    @Test
    void testFindByStorId_noMatch_returnsEmpty() {
        List<Discount> result = discountRepository.findByStorId("9999");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByStorId_nullStorId_returnsGlobalDiscounts() {
        Discount globalDiscount = new Discount();
        globalDiscount.setStorId(null);
        globalDiscount.setDiscounttype("TG" + UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        globalDiscount.setDiscount(new BigDecimal("5.00"));
        discountRepository.save(globalDiscount);
        entityManager.flush();

        List<Discount> result = discountRepository.findByStorId(null);

        assertTrue(result.stream().anyMatch(d -> d.getDiscounttype().equals(globalDiscount.getDiscounttype())));
        result.forEach(d -> assertNull(d.getStorId()));
    }

    @Test
    void testFindAll_returnsSavedDiscounts() {
        Discount discount1 = discountRepository.save(buildDiscount("6380"));
        Discount discount2 = discountRepository.save(buildDiscount("7066"));
        entityManager.flush();

        List<Discount> result = (List<Discount>) discountRepository.findAll();

        assertTrue(result.stream().anyMatch(d -> d.getDiscounttype().equals(discount1.getDiscounttype())));
        assertTrue(result.stream().anyMatch(d -> d.getDiscounttype().equals(discount2.getDiscounttype())));
    }

    @Test
    void testFindByDiscounttype_returnsMatching() {
        Discount discount = discountRepository.save(buildDiscount("6380"));
        entityManager.flush();

        List<Discount> result = discountRepository.findByDiscounttype(discount.getDiscounttype());

        assertEquals(1, result.size());
        assertEquals(discount.getDiscounttype(), result.get(0).getDiscounttype());
    }

    @Test
    void testFindByDiscounttype_noMatch_returnsEmpty() {
        List<Discount> result = discountRepository.findByDiscounttype("Nonexistent Type");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindById_returnsSeededCustomerDiscount() {
        Optional<Discount> result = discountRepository.findById("Customer Discount");

        assertTrue(result.isPresent());
        assertEquals("8042", result.get().getStorId());
        assertEquals(0, new BigDecimal("5.00").compareTo(result.get().getDiscount()));
    }
}
