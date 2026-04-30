package com.cg.repository;

import com.cg.entity.Author;
import com.cg.entity.Publisher;
import com.cg.entity.Title;
import com.cg.entity.TitleAuthor;
import com.cg.entity.TitleAuthorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TitleAuthorRepository.
 *
 * Insert order must respect FK constraints:
 *   Publisher → Title → Author → TitleAuthor
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TitleAuthorRepositoryTest {

    @Autowired
    private TitleAuthorRepository titleAuthorRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TitleRepository titleRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    // ── Parent entities shared across tests ────────────────────────────────

    private Publisher publisher;
    private Author    author1;
    private Author    author2;
    private Title     title1;
    private Title     title2;

    @BeforeEach
    void setUp() {
        // No delete operations here; rely on transactional rollback between tests.

        // 1. Publisher
        publisher = new Publisher();
        publisher.setPubId("1389");
        publisher.setPubName("Algodata Infosystems");
        publisher.setCity("Berkeley");
        publisher.setState("CA");
        publisher.setCountry("USA");
        publisherRepository.save(publisher);

        // 2. Titles
        title1 = new Title();
        title1.setTitleId("BU1032");
        title1.setTitle("The Busy Executive's Database Guide");
        title1.setType("business");
        title1.setPublisher(publisher);
        title1.setPubdate(LocalDateTime.now());
        titleRepository.save(title1);

        title2 = new Title();
        title2.setTitleId("PS2091");
        title2.setTitle("Is Anger the Enemy?");
        title2.setType("psychology");
        title2.setPublisher(publisher);
        title2.setPubdate(LocalDateTime.now());
        titleRepository.save(title2);

        // 3. Authors
        author1 = new Author();
        author1.setAuId("172-32-1176");
        author1.setAuLname("White");
        author1.setAuFname("Johnson");
        author1.setPhone("415-555-1212");
        author1.setCity("Menlo Park");
        author1.setState("CA");
        author1.setZip("94025");
        author1.setContract(1);
        authorRepository.save(author1);

        author2 = new Author();
        author2.setAuId("213-46-8915");
        author2.setAuLname("Green");
        author2.setAuFname("Marjorie");
        author2.setPhone("415-986-7020");
        author2.setCity("Oakland");
        author2.setState("CA");
        author2.setZip("94609");
        author2.setContract(1);
        authorRepository.save(author2);
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private TitleAuthor buildTitleAuthor(String auId, String titleId, int ord, int royaltyper) {
        TitleAuthor ta = new TitleAuthor();
        ta.setAuId(auId);
        ta.setTitleId(titleId);
        ta.setAuOrd(ord);
        ta.setRoyaltyper(royaltyper);
        return ta;
    }

    // ── save() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save() persists a TitleAuthor with composite PK")
    void save_validTitleAuthor_returnsSavedEntity() {
        TitleAuthor ta = buildTitleAuthor("172-32-1176", "BU1032", 1, 60);

        TitleAuthor saved = titleAuthorRepository.save(ta);

        assertNotNull(saved);
        assertEquals("172-32-1176", saved.getAuId());
        assertEquals("BU1032", saved.getTitleId());
        assertEquals(1, saved.getAuOrd());
        assertEquals(60, saved.getRoyaltyper());
    }

    // ── findById() with composite key ──────────────────────────────────────

    @Test
    @DisplayName("findById() returns entity for an existing composite key")
    void findById_existingCompositeKey_returnsEntity() {
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", "BU1032", 1, 60));

        TitleAuthorId id = new TitleAuthorId("172-32-1176", "BU1032");
        Optional<TitleAuthor> result = titleAuthorRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(60, result.get().getRoyaltyper());
    }

    @Test
    @DisplayName("findById() returns empty Optional for a non-existent composite key")
    void findById_nonExistentKey_returnsEmpty() {
        TitleAuthorId id = new TitleAuthorId("999-99-9999", "BU9999");
        Optional<TitleAuthor> result = titleAuthorRepository.findById(id);
        assertFalse(result.isPresent());
    }

    // ── findAll() with pagination ──────────────────────────────────────────

    @Test
    @DisplayName("findAll(Pageable) returns first page of TitleAuthor records")
    void findAll_withPagination_returnsFirstPage() {
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", "BU1032", 1, 60));
        titleAuthorRepository.save(buildTitleAuthor("213-46-8915", "BU1032", 2, 40));
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", "PS2091", 1, 100));

        Pageable pageable = PageRequest.of(0, 10);
        var page = titleAuthorRepository.findAll(pageable);

        // Should have at least 3 records saved in this test
        assertTrue(page.getContent().size() >= 3);
        assertTrue(page.getTotalElements() >= 3);
    }

    // ── findByAuId() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByAuId() returns all TitleAuthor records for a given author")
    void findByAuId_existingAuthor_returnsCorrectList() {
        // Use unique author ID to avoid interference from other tests
        String uniqueAuthorId = "555-55-5555";
        
        // Create a unique author for this test
        Author testAuthor = new Author();
        testAuthor.setAuId(uniqueAuthorId);
        testAuthor.setAuLname("TestLast");
        testAuthor.setAuFname("TestFirst");
        testAuthor.setPhone("555-555-5555");
        testAuthor.setCity("TestCity");
        testAuthor.setState("TX");
        testAuthor.setZip("77001");
        testAuthor.setContract(1);
        authorRepository.save(testAuthor);
        
        // Save records with unique author ID
        titleAuthorRepository.save(buildTitleAuthor(uniqueAuthorId, "BU1032", 1, 60));
        titleAuthorRepository.save(buildTitleAuthor(uniqueAuthorId, "PS2091", 2, 100));
        titleAuthorRepository.save(buildTitleAuthor("213-46-8915", "BU1032", 1, 40));

        List<TitleAuthor> result = titleAuthorRepository.findByAuId(uniqueAuthorId);

        assertEquals(2, result.size());
        result.forEach(ta -> assertEquals(uniqueAuthorId, ta.getAuId()));
    }

    @Test
    @DisplayName("findByAuId() returns empty list for an author with no titles")
    void findByAuId_noMatch_returnsEmptyList() {
        // Use a different author ID than the ones set up in setUp()
        String testAuthorId = "999-99-9999";
        
        List<TitleAuthor> result = titleAuthorRepository.findByAuId(testAuthorId);

        assertTrue(result.isEmpty());
    }

    // ── findByTitleId() ────────────────────────────────────────────────────

    @Test
    @DisplayName("findByTitleId() returns all TitleAuthor records for a given title")
    void findByTitleId_existingTitle_returnsCorrectList() {
        // Use unique title ID to avoid interference from other tests
        String uniqueTitleId = "TX9999";
        
        // Create a unique title for this test
        Title testTitle = new Title();
        testTitle.setTitleId(uniqueTitleId);
        testTitle.setTitle("Test Title for TitleAuthor");
        testTitle.setType("test");
        testTitle.setPublisher(publisher);
        testTitle.setPubdate(LocalDateTime.now());
        titleRepository.save(testTitle);
        
        // Save records with unique title ID
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", uniqueTitleId, 1, 60));
        titleAuthorRepository.save(buildTitleAuthor("213-46-8915", uniqueTitleId, 2, 40));
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", "PS2091", 1, 100));

        List<TitleAuthor> result = titleAuthorRepository.findByTitleId(uniqueTitleId);

        assertEquals(2, result.size());
        result.forEach(ta -> assertEquals(uniqueTitleId, ta.getTitleId()));
    }

    @Test
    @DisplayName("findByTitleId() returns empty list for a title with no authors")
    void findByTitleId_noMatch_returnsEmptyList() {
        // Use a different title ID that has no TitleAuthor records
        String testTitleId = "BU9999";
        
        List<TitleAuthor> result = titleAuthorRepository.findByTitleId(testTitleId);

        assertTrue(result.isEmpty());
    }

    // ── update ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save() with same composite key updates royaltyper")
    void save_existingCompositeKey_updatesRecord() {
        titleAuthorRepository.save(buildTitleAuthor("172-32-1176", "BU1032", 1, 60));

        TitleAuthor updated = buildTitleAuthor("172-32-1176", "BU1032", 1, 75);
        titleAuthorRepository.save(updated);

        TitleAuthor found = titleAuthorRepository
                .findById(new TitleAuthorId("172-32-1176", "BU1032"))
                .orElseThrow();
        assertEquals(75, found.getRoyaltyper());
    }

    // ── royaltyper edge cases ──────────────────────────────────────────────

    @Test
    @DisplayName("save() allows royaltyper = 0")
    void save_zeroRoyaltyper_succeeds() {
        TitleAuthor ta = buildTitleAuthor("172-32-1176", "BU1032", 1, 0);
        TitleAuthor saved = titleAuthorRepository.save(ta);
        assertEquals(0, saved.getRoyaltyper());
    }

    @Test
    @DisplayName("save() allows royaltyper = 100")
    void save_fullRoyaltyper_succeeds() {
        TitleAuthor ta = buildTitleAuthor("172-32-1176", "BU1032", 1, 100);
        TitleAuthor saved = titleAuthorRepository.save(ta);
        assertEquals(100, saved.getRoyaltyper());
    }
}
