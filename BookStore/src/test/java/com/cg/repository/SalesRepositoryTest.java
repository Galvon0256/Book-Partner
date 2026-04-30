package com.cg.repository;

import com.cg.entity.Sales;
import com.cg.entity.SalesId;
import com.cg.entity.Store;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
public class SalesRepositoryTest {

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DiscountRepository discountRepository;

    // Sales requires Store FK — persist stores first
    @BeforeEach
    void cleanUp() {
        salesRepository.deleteAll();
        entityManager.flush();
        discountRepository.deleteAll();   // ← add this before storeRepository.deleteAll()
        entityManager.flush();
        storeRepository.deleteAll();
        entityManager.flush();

        // re-insert stores
        Store s1 = new Store();
        s1.setStorId("6380"); s1.setStorName("Store One");
        s1.setCity("Seattle"); s1.setState("WA"); s1.setZip("98101");
        storeRepository.save(s1);

        Store s2 = new Store();
        s2.setStorId("7066"); s2.setStorName("Store Two");
        s2.setCity("Boston"); s2.setState("MA"); s2.setZip("02101");
        storeRepository.save(s2);

        entityManager.flush();
    }


    private Sales buildSales(String storId, String ordNum, String titleId) {
        Sales sale = new Sales();
        sale.setStorId(storId);
        sale.setOrdNum(ordNum);
        sale.setTitleId(titleId);
        sale.setOrdDate(LocalDateTime.now());
        sale.setQty((short) 5);
        sale.setPayterms("Net 30");
        return sale;
    }

    // Test 1 — findById with wrong composite key returns Optional.empty()
    @Test
    void testFindById_wrongKey_returnsEmpty() {
        Optional<Sales> result = salesRepository.findById(new SalesId("XXXX", "0000", "XX000"));

        assertTrue(result.isEmpty());
    }

    // Test 2 — findByStorId returns only sales for that store, not other stores
    @Test
    void testFindByIdStorId_returnsOnlyThatStore() {
        salesRepository.save(buildSales("6380", "ORD001", "BU1032"));
        salesRepository.save(buildSales("6380", "ORD002", "BU1033"));
        salesRepository.save(buildSales("6380", "ORD003", "BU1034"));
        salesRepository.save(buildSales("7066", "ORD004", "BU1035"));
        entityManager.flush();

        List<Sales> result = salesRepository.findByStorId("6380");

        assertEquals(3, result.size());
        result.forEach(s -> assertEquals("6380", s.getStorId()));
    }

    // Test 3 — findByStorId with no matching rows returns empty list, not null
    @Test
    void testFindByIdStorId_noSales_returnsEmpty() {
        List<Sales> result = salesRepository.findByStorId("9999");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    
    // findByTitleId returns only sales with that title
    @Test
    void testFindByTitleId_returnsMatchingSales() {
        salesRepository.save(buildSales("6380", "ORD001", "BU1032"));
        salesRepository.save(buildSales("6380", "ORD002", "BU1032"));
        salesRepository.save(buildSales("6380", "ORD003", "PS2091")); // different title
        entityManager.flush();
 
        List<Sales> result = salesRepository.findByTitleId("BU1032");
 
        assertEquals(2, result.size());
        result.forEach(s -> assertEquals("BU1032", s.getTitleId()));
    }
 
    // findByTitleId with no match returns empty list, not null
    @Test
    void testFindByTitleId_noMatch_returnsEmpty() {
        List<Sales> result = salesRepository.findByTitleId("ZZZZZZ");
 
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
