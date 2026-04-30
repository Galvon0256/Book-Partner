package com.cg.repository;

import com.cg.entity.Store;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
public class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SalesRepository salesRepository; // or whatever the child repo is

    @Autowired
    private DiscountRepository discountRepository;

    // Helper — builds a valid Store
    private Store buildStore(String storId, String city, String state) {
        Store s = new Store();
        s.setStorId(storId);
        s.setStorName("Store " + storId);
        s.setStorAddress("123 Main St");
        s.setCity(city);
        s.setState(state);
        s.setZip("12345");
        return s;
    }
    

    @BeforeEach
    void cleanUp() {
        salesRepository.deleteAll();
        discountRepository.deleteAll();
        entityManager.flush();
        storeRepository.deleteAll();
        entityManager.flush();
    }


    // Test 1 — save and retrieve, all 6 fields must match
    @Test
    void testSave_thenFindById_allFieldsMatch() {
        Store store = buildStore("6380", "Seattle", "WA");
        storeRepository.save(store);
        entityManager.flush();
        entityManager.clear();

        Optional<Store> result = storeRepository.findById("6380");

        assertTrue(result.isPresent());
        assertEquals("6380", result.get().getStorId());
        assertEquals("Store 6380", result.get().getStorName());
        assertEquals("123 Main St", result.get().getStorAddress());
        assertEquals("Seattle", result.get().getCity());
        assertEquals("WA", result.get().getState());
        assertEquals("12345", result.get().getZip());
    }

    // Test 2 — findByCity returns only matching city
    @Test
    void testFindByCity_returnsOnlyMatchingCity() {
        storeRepository.save(buildStore("1001", "Seattle", "WA"));
        storeRepository.save(buildStore("1002", "Boston", "MA"));
        entityManager.flush();

        List<Store> result = storeRepository.findByCity("Seattle");

        assertEquals(1, result.size());
        assertEquals("Seattle", result.get(0).getCity());
    }

    // Test 3 — findByCity with no match returns empty list, not null
    @Test
    void testFindByCity_noMatch_returnsEmptyList() {
        List<Store> result = storeRepository.findByCity("Tokyo");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // Test 4 — findByState returns only matching state
    @Test
    void testFindByState_returnsOnlyMatchingState() {
        storeRepository.save(buildStore("1001", "Seattle", "WA"));
        storeRepository.save(buildStore("1002", "Los Angeles", "CA"));
        entityManager.flush();

        List<Store> result = storeRepository.findByState("WA");

        assertEquals(1, result.size());
        assertEquals("WA", result.get(0).getState());
    }

    // Test 5 — findByState returns all matching rows (no accidental LIMIT)
    @Test
    void testFindByState_multipleStores_allReturned() {
        storeRepository.save(buildStore("1001", "Seattle", "WA"));
        storeRepository.save(buildStore("1002", "Tacoma", "WA"));
        storeRepository.save(buildStore("1003", "Spokane", "WA"));
        entityManager.flush();

        List<Store> result = storeRepository.findByState("WA");

        assertEquals(3, result.size());
        result.forEach(s -> assertEquals("WA", s.getState()));
    }

    // Test 6 — findAll returns all saved rows
    @Test
    void testFindAll_returnsAllSaved() {
        storeRepository.save(buildStore("1001", "Seattle", "WA"));
        storeRepository.save(buildStore("1002", "Boston", "MA"));
        storeRepository.save(buildStore("1003", "Dallas", "TX"));
        entityManager.flush();

        List<Store> result = (List<Store>) storeRepository.findAll();

        assertEquals(3, result.size());
    }

    // Test 7 — existsById returns true after save
    @Test
    void testExistsById_afterSave_returnsTrue() {
        storeRepository.save(buildStore("6380", "Seattle", "WA"));
        entityManager.flush();

        assertTrue(storeRepository.existsById("6380"));
    }

    // Test 8 — existsById returns false for absent key
    @Test
    void testExistsById_notSaved_returnsFalse() {
        assertFalse(storeRepository.existsById("ZZZZ"));
    }

    // Test 9 — @NotBlank on storId fires at flush
    @Test
    void testSave_nullStorId_throwsException() {
        Store store = new Store();
        store.setStorId(null);
        store.setStorName("Test");
        store.setState("WA");
        store.setZip("12345");

        assertThrows(ConstraintViolationException.class, () -> {
            storeRepository.save(store);
            entityManager.flush();
        });
    }

    // Test 10 — @Size(max=2) on state fires for "WASHINGTON"
    @Test
    void testSave_stateLongerThan2_throwsException() {
        Store store = buildStore("1001", "Seattle", "WASHINGTON");

        assertThrows(ConstraintViolationException.class, () -> {
            storeRepository.save(store);
            entityManager.flush();
        });
    }

    // Test 11 — count returns exact number
    @Test
    void testCount_afterSaves() {
        storeRepository.save(buildStore("1001", "Seattle", "WA"));
        storeRepository.save(buildStore("1002", "Boston", "MA"));
        storeRepository.save(buildStore("1003", "Dallas", "TX"));
        entityManager.flush();

        assertEquals(3L, storeRepository.count());
    }
}
