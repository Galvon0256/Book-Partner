package com.cg.repository;

import com.cg.entity.RoySched;
import com.cg.entity.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

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

    @BeforeEach
    void setUp() {
        title = new Title();
        title.setTitleId("BU1032");
        title.setTitle("The Busy Executive's Database Guide");
        title.setType("business");
        entityManager.persistAndFlush(title);

        roySched1 = new RoySched();
        roySched1.setTitleId("BU1032");
        roySched1.setTitle(title);
        roySched1.setLorange(0);
        roySched1.setHirange(5000);
        roySched1.setRoyalty(10);
        entityManager.persistAndFlush(roySched1);

        roySched2 = new RoySched();
        roySched2.setTitleId("BU1032");
        roySched2.setTitle(title);
        roySched2.setLorange(5001);
        roySched2.setHirange(50000);
        roySched2.setRoyalty(12);
        entityManager.persistAndFlush(roySched2);
    }

    // ── findByTitleTitleId ──────────────────────────────────────

    @Test
    void findByTitleTitleId_returnsMatchingEntries() {
        List<RoySched> results = roySchedRepository.findByTitleTitleId("BU1032");
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
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRoyalty()).isEqualTo(12);
    }

    @Test
    void findByRoyaltyGreaterThanEqual_includesExactMatch() {
        List<RoySched> results = roySchedRepository.findByRoyaltyGreaterThanEqual(10);
        assertThat(results).hasSize(2);
    }

    @Test
    void findByRoyaltyGreaterThanEqual_returnsEmptyWhenNoneQualify() {
        List<RoySched> results = roySchedRepository.findByRoyaltyGreaterThanEqual(99);
        assertThat(results).isEmpty();
    }

    // ── save ────────────────────────────────────────────────────

    @Test
    void save_persistsRoySched() {
        RoySched roySched = new RoySched();
        roySched.setTitleId("BU1032");
        roySched.setTitle(title);
        roySched.setLorange(50001);
        roySched.setHirange(100000);
        roySched.setRoyalty(14);

        RoySched saved = roySchedRepository.save(roySched);

        assertThat(saved.getRoySchedId()).isNotNull();
        assertThat(saved.getRoyalty()).isEqualTo(14);
        assertThat(saved.getTitleId()).isEqualTo("BU1032");
    }

    // ── findById ────────────────────────────────────────────────

    @Test
    void findById_returnsEntry_whenExists() {
        Optional<RoySched> result = roySchedRepository.findById(roySched1.getRoySchedId());
        assertThat(result).isPresent();
        assertThat(result.get().getTitleId()).isEqualTo("BU1032");
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<RoySched> result = roySchedRepository.findById(-999);
        assertThat(result).isEmpty();
    }
}