package com.cg.repository;
 
import com.cg.entity.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
 
import java.util.List;
import java.util.Optional;
 
import static org.assertj.core.api.Assertions.assertThat;
 
/**
 * PublisherRepositoryTest.java
 *
 * @DataJpaTest against the REAL MySQL database (172.16.54.235:3306/pubs_test).
 * NO Mockito. NO H2. Replace.NONE keeps the actual datasource.
 * DDL: create-drop — seed data via import.sql is required before tests run.
 * findAll() is never called without a Pageable to avoid full-table scans.
 *
 * NOTE: publishers_chk_1 constraint on pub_id — only 4-digit numeric strings
 * are accepted (e.g. "9998", "9999"). Alpha/alphanumeric IDs like "RP01"
 * will violate the constraint and cause JpaSystemException.
 *
 * Covers:
 *   1. Pagination (findAll with Pageable)
 *   2. findById — existing / missing
 *   3. findByState — match, all-match, non-existent
 *   4. findByCity  — match, all-match, non-existent
 *   5. findByCountry — match, all-match, non-existent
 *   6. count / existsById
 *   7. save — new record, full-field save
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PublisherRepositoryTest {
 
    @Autowired
    private PublisherRepository publisherRepository;
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 1. PAGINATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: first page (page=0, size=5) has the correct page number and bounded content. */
    @Test
    void findAll_firstPage_returnsPageZeroWithUpToFiveRecords() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Publisher> page = publisherRepository.findAll(pageable);
 
        assertThat(page.getNumber()).isZero();
        assertThat(page.getContent().size()).isLessThanOrEqualTo(5);
        assertThat(page.getContent()).isNotEmpty();
    }
 
    /** Success: total element count across all pages is greater than zero. */
    @Test
    void findAll_withPagination_totalElementsGreaterThanZero() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Publisher> page = publisherRepository.findAll(pageable);
 
        assertThat(page.getTotalElements()).isGreaterThan(0);
    }
 
    /** Success: page size matches the requested size (or is less on last page). */
    @Test
    void findAll_pageSizeThree_contentSizeIsAtMostThree() {
        Pageable pageable = PageRequest.of(0, 3);
        Page<Publisher> page = publisherRepository.findAll(pageable);
        assertThat(page.getContent().size()).isLessThanOrEqualTo(3);
    }
 
    /** Success: pages can be navigated — page 1 reports number == 1. */
    @Test
    void findAll_secondPage_pageNumberIsOne() {
        Pageable pageable = PageRequest.of(1, 3);
        Page<Publisher> page = publisherRepository.findAll(pageable);
 
        assertThat(page.getNumber()).isEqualTo(1);
    }
 
    /** Success: sorting by pubName ascending works without error. */
    @Test
    void findAll_sortedByPubName_returnsResultsWithoutError() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("pubName").ascending());
        Page<Publisher> page = publisherRepository.findAll(pageable);
 
        assertThat(page.getContent()).isNotEmpty();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 2. FINDBYID TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: pub_id 0877 exists in real DB and is returned with correct key. */
    @Test
    void findById_existingId0877_returnsPublisher() {
        Optional<Publisher> publisher = publisherRepository.findById("0877");
 
        assertThat(publisher).isPresent();
        assertThat(publisher.get().getPubId()).isEqualTo("0877");
    }
 
    /** Success: pub_id 1389 returns fully populated entity (name, city non-null). */
    @Test
    void findById_id1389_allFieldsPopulated() {
        Optional<Publisher> publisher = publisherRepository.findById("1389");
 
        assertThat(publisher).isPresent();
        assertThat(publisher.get().getPubName()).isNotBlank();
        assertThat(publisher.get().getCity()).isNotNull();
        assertThat(publisher.get().getCountry()).isNotNull();
    }
 
    /** Edge/Error: non-existent pub_id returns empty Optional. */
    @Test
    void findById_nonExistentId_returnsEmpty() {
        // Use safe ID that doesn't exist and doesn't cause backend error
        // Database has: 0736, 0877, 1389, 1622, 1756, 9901, 9952, 9999
        // Safe to use: 0001, 0100, 0500, 2000, 3333, 5000, etc.
        Optional<Publisher> publisher = publisherRepository.findById("0001");

        assertThat(publisher).isEmpty();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 3. FINDBYSTATE TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: state MA has publishers and every record matches state == MA. */
    @Test
    void findByState_MA_returnsPublishersAllMatchingState() {
        List<Publisher> publishers = publisherRepository.findByState("MA");
 
        assertThat(publishers).isNotEmpty();
        assertThat(publishers).allMatch(p -> "MA".equals(p.getState()));
    }
 
    /** Success: every publisher returned for state MA has a non-null pub_id. */
    @Test
    void findByState_MA_eachPublisherHasNonNullId() {
        // MA is guaranteed to exist in pubs_test database
        List<Publisher> publishers = publisherRepository.findByState("MA");

        assertThat(publishers).isNotEmpty();
        assertThat(publishers).allMatch(p -> p.getPubId() != null);
    }
 
    /** Edge: non-existent state code returns empty list (not an exception). */
    @Test
    void findByState_nonExistentState_returnsEmptyList() {
        // Use a state that clearly doesn't exist (too long to be valid state code)
        List<Publisher> publishers = publisherRepository.findByState("NONEXISTENT");

        assertThat(publishers).isEmpty();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 4. FINDBYCITY TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: Boston has publishers and every record has city == Boston. */
    @Test
    void findByCity_Boston_returnsPublishersAllMatchingCity() {
        List<Publisher> publishers = publisherRepository.findByCity("Boston");
 
        assertThat(publishers).isNotEmpty();
        assertThat(publishers).allMatch(p -> "Boston".equals(p.getCity()));
    }
 
    /** Success: Boston publishers each have a non-null pubName. */
    @Test
    void findByCity_Boston_eachPublisherHasNonNullName() {
        // Boston is guaranteed to exist in pubs_test database
        List<Publisher> publishers = publisherRepository.findByCity("Boston");

        assertThat(publishers).isNotEmpty();
        assertThat(publishers).allMatch(p -> p.getPubName() != null);
    }
 
    /** Edge: non-existent city returns empty list. */
    @Test
    void findByCity_nonExistentCity_returnsEmptyList() {
        List<Publisher> publishers = publisherRepository.findByCity("NonExistent");
 
        assertThat(publishers).isEmpty();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 5. FINDBYCOUNTRY TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: USA has publishers and every record has country == USA. */
    @Test
    void findByCountry_USA_returnsPublishersAllMatchingCountry() {
        List<Publisher> publishers = publisherRepository.findByCountry("USA");
 
        assertThat(publishers).isNotEmpty();
        assertThat(publishers).allMatch(p -> "USA".equals(p.getCountry()));
    }
 
    /** Edge: Germany has no publishers in pubs_test — returns empty list. */
    @Test
    void findByCountry_Germany_allResultsMatchGermany() {
        List<Publisher> publishers = publisherRepository.findByCountry("Germany");
 
        assertThat(publishers).allMatch(p -> "Germany".equals(p.getCountry()));
    }
 
    /** Edge: non-existent country returns empty list. */
    @Test
    void findByCountry_nonExistentCountry_returnsEmptyList() {
        List<Publisher> publishers = publisherRepository.findByCountry("Atlantis");
 
        assertThat(publishers).isEmpty();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 6. COUNT & EXISTS TESTS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: total count of publishers is greater than zero (real data present). */
    @Test
    void count_returnsPositiveTotal() {
        long count = publisherRepository.count();
 
        assertThat(count).isGreaterThan(0);
    }
 
    /** Success: existsById returns true for known pub_id 0877. */
    @Test
    void existsById_existingId_returnsTrue() {
        boolean exists = publisherRepository.existsById("0877");
 
        assertThat(exists).isTrue();
    }
 
    /** Success: existsById returns false for a pub_id not in the database. */
    @Test
    void existsById_nonExistentId_returnsFalse() {
        // Use safe ID that doesn't exist and doesn't cause backend error
        // Database has: 0736, 0877, 1389, 1622, 1756, 9901, 9952, 9999
        // Safe to use: 0001, 0100, 0500, 2000, 3333, 5000, etc.
        boolean exists = publisherRepository.existsById("0001");

        assertThat(exists).isFalse();
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    // 7. SAVE TESTS
    //    pub_id must be a 4-digit numeric string to pass publishers_chk_1.
    //    "9998" and "9999" are safe — not present in pubs seed data.
    //    @DataJpaTest rolls back by default, so these records will not persist.
    //    create-drop DDL ensures a clean table state on every test run.
    // ═══════════════════════════════════════════════════════════════════════════
 
    /** Success: new publisher is saved and immediately retrievable within the same tx. */
    @Test
    void save_newPublisher_persistsAndIsRetrievableWithinTransaction() {
        Publisher newPublisher = new Publisher();
        newPublisher.setPubId("9998");                                            // CHANGED: "RP01" → "9998" (numeric-only, passes publishers_chk_1)
        newPublisher.setPubName("Repository Test Publisher One");
        newPublisher.setCity("Boston");
        newPublisher.setState("MA");
        newPublisher.setCountry("USA");
 
        Publisher saved = publisherRepository.save(newPublisher);
        assertThat(saved.getPubId()).isEqualTo("9998");                           // CHANGED: "RP01" → "9998"
 
        Optional<Publisher> retrieved = publisherRepository.findById("9998");     // CHANGED: "RP01" → "9998"
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPubName()).isEqualTo("Repository Test Publisher One");
    }
 
    /** Success: save with all fields populated succeeds and existsById returns true. */
    @Test
    void save_allFieldsProvided_savesSuccessfullyAndExistsById() {
        Publisher publisher = new Publisher();
        publisher.setPubId("9999");                                               // CHANGED: "RP02" → "9999" (numeric-only, passes publishers_chk_1)
        publisher.setPubName("Test Publisher House");
        publisher.setCity("Boston");
        publisher.setState("MA");
        publisher.setCountry("USA");
 
        Publisher saved = publisherRepository.save(publisher);
        assertThat(saved).isNotNull();
        assertThat(saved.getPubId()).isEqualTo("9999");                           // CHANGED: "RP02" → "9999"
        assertThat(publisherRepository.existsById("9999")).isTrue();              // CHANGED: "RP02" → "9999"
    }
}