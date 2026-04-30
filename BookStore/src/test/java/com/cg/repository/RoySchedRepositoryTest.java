package com.cg.repository;

import com.cg.entity.Publisher;
import com.cg.entity.RoySched;
import com.cg.entity.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoySchedRepositoryTest {

    @Autowired
    private RoySchedRepository roySchedRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Title title;
    private RoySched roySched1;
    private RoySched roySched2;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisher = getOrCreatePublisher(
            "1389",
            "Algodata Infosystems",
            "Berkeley",
            "CA",
            "USA"
        );

        title = new Title();
        title.setTitleId(nextTitleId("RT"));
        title.setTitle("The Busy Executive's Database Guide");
        title.setType("business");
        title.setPubdate(LocalDateTime.now());
        title.setPublisher(publisher);
        entityManager.persistAndFlush(title);

        roySched1 = new RoySched();
        roySched1.setTitleId(title.getTitleId());
        roySched1.setTitle(title);
        roySched1.setLorange(0);
        roySched1.setHirange(5000);
        roySched1.setRoyalty(10);
        entityManager.persistAndFlush(roySched1);

        roySched2 = new RoySched();
        roySched2.setTitleId(title.getTitleId());
        roySched2.setTitle(title);
        roySched2.setLorange(5001);
        roySched2.setHirange(50000);
        roySched2.setRoyalty(12);
        entityManager.persistAndFlush(roySched2);
    }

    // ── findByTitleTitleId ──────────────────────────────────────

    @Test
    void findByTitleTitleId_returnsMatchingEntries() {
        List<RoySched> results = roySchedRepository.findByTitleTitleId(title.getTitleId());
        assertThat(results).hasSize(2);
    }

    @Test
    void findByTitleTitleId_returnsEmptyForUnknownTitleId() {
        List<RoySched> results = roySchedRepository.findByTitleTitleId("XX9999");
        assertThat(results).isEmpty();
    }

    // ── findByRoyaltyGreaterThanEqual ───────────────────────────

    @Test
    void findByRoyaltyGreaterThanEqual_returnsMatchingEntries() {
        List<RoySched> results = roySchedRepository.findByRoyaltyGreaterThanEqual(12);
        assertThat(results)
                .anyMatch(result -> result.getRoySchedId().equals(roySched2.getRoySchedId()));
    }

    @Test
    void findByRoyaltyGreaterThanEqual_includesExactMatch() {
        List<RoySched> results = roySchedRepository.findByRoyaltyGreaterThanEqual(10);
        assertThat(results)
            .anyMatch(result -> result.getRoySchedId().equals(roySched1.getRoySchedId()));
        assertThat(results)
            .anyMatch(result -> result.getRoySchedId().equals(roySched2.getRoySchedId()));
    }

    @Test
    void findByRoyaltyGreaterThanEqual_returnsEmptyWhenNoneQualify() {
        List<RoySched> results = roySchedRepository.findByRoyaltyGreaterThanEqual(99);
        assertThat(results)
            .noneMatch(result -> result.getRoySchedId().equals(roySched1.getRoySchedId()));
        assertThat(results)
            .noneMatch(result -> result.getRoySchedId().equals(roySched2.getRoySchedId()));
    }

    // ── save ────────────────────────────────────────────────────

    @Test
    void save_persistsRoySched() {
        RoySched roySched = new RoySched();
        roySched.setTitleId(title.getTitleId());
        roySched.setTitle(title);
        roySched.setLorange(50001);
        roySched.setHirange(100000);
        roySched.setRoyalty(14);

        RoySched saved = roySchedRepository.save(roySched);

        assertThat(saved.getRoySchedId()).isNotNull();
        assertThat(saved.getRoyalty()).isEqualTo(14);
        assertThat(saved.getTitleId()).isEqualTo(title.getTitleId());
    }

    // ── pagination ─────────────────────────────────────────────

    @Test
    void findAll_returnsPaginatedResults() {
        Page<RoySched> page = roySchedRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(1);
    }

    // ── findById ────────────────────────────────────────────────

    @Test
    void findById_returnsEntry_whenExists() {
        Optional<RoySched> result = roySchedRepository.findById(roySched1.getRoySchedId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitleId()).isEqualTo(title.getTitleId());
    }

    private String nextTitleId(String prefix) {
        int suffix = 1;
        String candidate;
        do {
            candidate = String.format("%s%04d", prefix, suffix);
            suffix++;
        } while (entityManager.find(Title.class, candidate) != null);
        return candidate;
    }

    private Publisher getOrCreatePublisher(
            String pubId,
            String pubName,
            String city,
            String state,
            String country
    ) {
        Publisher existing = entityManager.find(Publisher.class, pubId);
        if (existing != null) {
            return existing;
        }

        Publisher newPublisher = new Publisher();
        newPublisher.setPubId(pubId);
        newPublisher.setPubName(pubName);
        newPublisher.setCity(city);
        newPublisher.setState(state);
        newPublisher.setCountry(country);
        entityManager.persistAndFlush(newPublisher);
        return newPublisher;
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<RoySched> result = roySchedRepository.findById(-999);
        assertThat(result).isEmpty();
    }
}