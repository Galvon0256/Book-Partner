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

    @BeforeEach
    void setUp() {
        title1 = new Title();
        title1.setTitleId("BU1032");
        title1.setTitle("The Busy Executive's Database Guide");
        title1.setType("business");
        title1.setPrice(19.99);
        title1.setNotes("An overview of available database systems");
        entityManager.persistAndFlush(title1);

        title2 = new Title();
        title2.setTitleId("PS2091");
        title2.setTitle("Is Anger the Enemy?");
        title2.setType("psychology");
        title2.setPrice(10.95);
        title2.setNotes("Relationship between anger and mental health");
        entityManager.persistAndFlush(title2);
    }

    // ── findByType ──────────────────────────────────────────────

    @Test
    void findByType_returnsMatchingTitles() {
        List<Title> results = titleRepository.findByType("business");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitleId()).isEqualTo("BU1032");
    }

    @Test
    void findByType_returnsEmptyWhenNoMatch() {
        List<Title> results = titleRepository.findByType("cooking");
        assertThat(results).isEmpty();
    }

    // ── findByPublisherPubId ────────────────────────────────────

    @Test
void findByPublisherPubId_returnsMatchingTitles() {
    Publisher publisher = new Publisher();
    publisher.setPubId("1389");
    publisher.setPubName("Algodata Infosystems");
    publisher.setCity("Berkeley");
    publisher.setState("CA");
    publisher.setCountry("USA");
    entityManager.persistAndFlush(publisher);

    Title titleWithPub = new Title();
    titleWithPub.setTitleId("BU2075");
    titleWithPub.setTitle("You Can Combat Computer Stress!");
    titleWithPub.setType("business");
    titleWithPub.setPublisher(publisher);
    entityManager.persistAndFlush(titleWithPub);

    List<Title> results = titleRepository.findByPublisherPubId("1389");
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getTitleId()).isEqualTo("BU2075");
}

    @Test
    void findByPublisherPubId_returnsEmptyForUnknownPubId() {
        List<Title> results = titleRepository.findByPublisherPubId("9999");
        assertThat(results).isEmpty();
    }

    // ── findById ────────────────────────────────────────────────

    @Test
    void findById_returnsTitle_whenExists() {
        Optional<Title> result = titleRepository.findById("BU1032");
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
    void save_persistsTitleWithoutPublisher() {
        Title title = new Title();
        title.setTitleId("TC9999");
        title.setTitle("Test Title");
        title.setType("trad_cook");

        Title saved = titleRepository.save(title);

        assertThat(saved.getTitleId()).isEqualTo("TC9999");
        assertThat(saved.getPublisher()).isNull();
    }

    @Test
void save_persistsTitleWithPublisher() {
    Publisher publisher = new Publisher();
    publisher.setPubId("1622");
    publisher.setPubName("Five Lakes Publishing");
    publisher.setCity("Chicago");
    publisher.setState("IL");
    publisher.setCountry("USA");
    entityManager.persistAndFlush(publisher);

    Title title = new Title();
    title.setTitleId("BU9999");
    title.setTitle("New Business Title");
    title.setType("business");
    title.setPublisher(publisher);

    Title saved = titleRepository.save(title);

    assertThat(saved.getPublisher()).isNotNull();
    assertThat(saved.getPublisher().getPubId()).isEqualTo("1622");
}

    // ── pagination + sorting ────────────────────────────────────

    @Test
    void findAll_returnsPaginatedResults() {
        Page<Title> page = titleRepository.findAll(PageRequest.of(0, 1));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findAll_sortsByTitleAscending() {
        Page<Title> page = titleRepository.findAll(
            PageRequest.of(0, 10, Sort.by("title").ascending())
        );
        List<Title> titles = page.getContent();
        // "Is Anger..." sorts before "The Busy..."
        assertThat(titles.get(0).getTitleId()).isEqualTo("PS2091");
    }
}