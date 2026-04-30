package com.cg.repository;

import com.cg.entity.Publisher;
import com.cg.entity.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TitleRepositoryTest {

    @Autowired
    private TitleRepository titleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Title title1;
    private Title title2;
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

        title1 = new Title();
        title1.setTitleId(nextTitleId("TT"));
        title1.setTitle("The Busy Executive's Database Guide");
        title1.setType("business");
        title1.setPrice(19.99);
        title1.setNotes("An overview of available database systems");
        title1.setPubdate(LocalDateTime.now());
        title1.setPublisher(publisher);
        entityManager.persistAndFlush(title1);

        title2 = new Title();
        title2.setTitleId(nextTitleId("TT"));
        title2.setTitle("Is Anger the Enemy?");
        title2.setType("psychology");
        title2.setPrice(10.95);
        title2.setNotes("Relationship between anger and mental health");
        title2.setPubdate(LocalDateTime.now());
        title2.setPublisher(publisher);
        entityManager.persistAndFlush(title2);
    }

    // ── findByType ──────────────────────────────────────────────

    @Test
    void findByType_returnsMatchingTitles() {
        List<Title> results = titleRepository.findByType("business");
        assertThat(results)
                .anyMatch(result -> result.getTitleId().equals(title1.getTitleId()));
    }

    @Test
    void findByType_returnsEmptyWhenNoMatch() {
        List<Title> results = titleRepository.findByType("cooking");
        assertThat(results).isEmpty();
    }

    // ── findByPublisherPubId ────────────────────────────────────

    @Test
    void findByPublisherPubId_returnsMatchingTitles() {
        Publisher localPublisher = getOrCreatePublisher(
                "1389",
                "Algodata Infosystems",
                "Berkeley",
                "CA",
                "USA"
        );

        Title titleWithPub = new Title();
        titleWithPub.setTitleId(nextTitleId("TP"));
        titleWithPub.setTitle("You Can Combat Computer Stress!");
        titleWithPub.setType("business");
        titleWithPub.setPubdate(LocalDateTime.now());
        titleWithPub.setPublisher(localPublisher);
        entityManager.persistAndFlush(titleWithPub);

        List<Title> results = titleRepository.findByPublisherPubId("1389");
        assertThat(results)
            .anyMatch(result -> result.getTitleId().equals(titleWithPub.getTitleId()));
    }

    @Test
    void findByPublisherPubId_returnsEmptyForUnknownPubId() {
        List<Title> results = titleRepository.findByPublisherPubId("9999");
        assertThat(results).isEmpty();
    }

    // ── findById ────────────────────────────────────────────────

    @Test
    void findById_returnsTitle_whenExists() {
        Optional<Title> result = titleRepository.findById(title1.getTitleId());
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo("business");
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<Title> result = titleRepository.findById("XX9999");
        assertThat(result).isEmpty();
    }

    // ── save ────────────────────────────────────────────────────

    @Test
    void save_persistsTitleWithPublisher() {
        Title title = new Title();
        title.setTitleId(nextTitleId("TS"));
        title.setTitle("New Business Title");
        title.setType("business");
        title.setPubdate(LocalDateTime.now());
        title.setPublisher(publisher);

        Title saved = titleRepository.save(title);

        assertThat(saved.getPublisher()).isNotNull();
        assertThat(saved.getPublisher().getPubId()).isEqualTo(publisher.getPubId());
    }

    // ── pagination + sorting ────────────────────────────────────

    @Test
    void findAll_returnsPaginatedResults() {
        Page<Title> page = titleRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findAll_sortsByTitleAscending() {
        Page<Title> page = titleRepository.findAll(
            PageRequest.of(0, 200, Sort.by("title").ascending())
        );
        List<Title> titles = page.getContent();
        // "Is Anger..." sorts before "The Busy..."
        int indexTitle2 = indexOfTitleId(titles, title2.getTitleId());
        int indexTitle1 = indexOfTitleId(titles, title1.getTitleId());
        assertThat(indexTitle2).isGreaterThanOrEqualTo(0);
        assertThat(indexTitle1).isGreaterThanOrEqualTo(0);
        assertThat(indexTitle2).isLessThan(indexTitle1);
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

    private int indexOfTitleId(List<Title> titles, String titleId) {
        for (int i = 0; i < titles.size(); i++) {
            if (titleId.equals(titles.get(i).getTitleId())) {
                return i;
            }
        }
        return -1;
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
}