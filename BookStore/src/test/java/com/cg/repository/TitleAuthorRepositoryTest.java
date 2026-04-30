package com.cg.repository;

import com.cg.entity.Author;
import com.cg.entity.TitleAuthor;
import com.cg.entity.TitleAuthorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer tests for TitleAuthorRepository.
 *
 * - Uses real MySQL pubs_test database (NOT H2).
 * - TitleAuthor has a composite PK (auId + titleId) via @IdClass.
 * - Author rows are saved first because auId is a FK in titleauthor table.
 * - @BeforeEach clears both tables so every test starts from a known state.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("TitleAuthorRepository – Repository Layer Tests (MySQL pubs_test)")
class TitleAuthorRepositoryTest {

    @Autowired
    private TitleAuthorRepository titleAuthorRepository;

    @Autowired
    private AuthorRepository authorRepository;

    // -------------------------------------------------------
    // Common test data
    // -------------------------------------------------------
    private TitleAuthor ta1;
    private TitleAuthor ta2;
    private TitleAuthor ta3;

    @BeforeEach
    void setUp() {
        // Clear child table first (FK constraint)
        titleAuthorRepository.deleteAll();
        authorRepository.deleteAll();

        // Parent authors
        Author a1 = new Author("172-32-1176", "White", "Johnson", "408 496-7223",
                "10932 Bigge Rd", "Menlo Park", "CA", "94025", 1);
        Author a2 = new Author("213-46-8915", "Green", "Marjorie", "415 986-7020",
                "309 63rd St", "Oakland", "CA", "94618", 1);
        authorRepository.saveAll(List.of(a1, a2));

        // TitleAuthor records (composite key: auId + titleId)
        ta1 = new TitleAuthor("172-32-1176", "BU1032", 1, 60);
        ta2 = new TitleAuthor("172-32-1176", "BU2075", 1, 100);
        ta3 = new TitleAuthor("213-46-8915", "BU1032", 2, 40);
        titleAuthorRepository.saveAll(List.of(ta1, ta2, ta3));
    }

    // -------------------------------------------------------
    // 1. Save / Create
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should persist a new TitleAuthor with composite key")
    void testSaveTitleAuthor() {
        TitleAuthor newTa = new TitleAuthor("213-46-8915", "TC7777", 1, 80);

        TitleAuthor saved = titleAuthorRepository.save(newTa);

        assertThat(saved).isNotNull();
        assertThat(saved.getAuId()).isEqualTo("213-46-8915");
        assertThat(saved.getTitleId()).isEqualTo("TC7777");
        assertThat(saved.getAuOrd()).isEqualTo(1);
        assertThat(saved.getRoyaltyper()).isEqualTo(80);
    }

    // -------------------------------------------------------
    // 2. FindById (composite key)
    // -------------------------------------------------------

    @Test
    @DisplayName("findById() – should return TitleAuthor when composite key exists")
    void testFindByIdExists() {
        TitleAuthorId id = new TitleAuthorId("172-32-1176", "BU1032");

        Optional<TitleAuthor> result = titleAuthorRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getAuOrd()).isEqualTo(1);
        assertThat(result.get().getRoyaltyper()).isEqualTo(60);
    }

    @Test
    @DisplayName("findById() – should return empty Optional when composite key not found")
    void testFindByIdNotFound() {
        TitleAuthorId id = new TitleAuthorId("999-99-9999", "XX9999");

        Optional<TitleAuthor> result = titleAuthorRepository.findById(id);

        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("findById() – should not return row when titleId part of key is wrong")
    void testFindByIdPartialKeyMismatch() {
        TitleAuthorId id = new TitleAuthorId("172-32-1176", "WRONG1");

        Optional<TitleAuthor> result = titleAuthorRepository.findById(id);

        assertThat(result).isNotPresent();
    }

    // -------------------------------------------------------
    // 3. FindAll
    // -------------------------------------------------------

    @Test
    @DisplayName("findAll() – should return all persisted TitleAuthor records")
    void testFindAll() {
        List<TitleAuthor> all = titleAuthorRepository.findAll();

        assertThat(all).hasSize(3);
    }

    // -------------------------------------------------------
    // 4. findByAuId
    // -------------------------------------------------------

    @Test
    @DisplayName("findByAuId() – should return all TitleAuthors for a given author")
    void testFindByAuIdFound() {
        List<TitleAuthor> result = titleAuthorRepository.findByAuId("172-32-1176");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TitleAuthor::getAuId).containsOnly("172-32-1176");
    }

    @Test
    @DisplayName("findByAuId() – should return single record when author has one title")
    void testFindByAuIdSingleResult() {
        List<TitleAuthor> result = titleAuthorRepository.findByAuId("213-46-8915");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitleId()).isEqualTo("BU1032");
    }

    @Test
    @DisplayName("findByAuId() – should return empty list when auId does not exist")
    void testFindByAuIdNotFound() {
        List<TitleAuthor> result = titleAuthorRepository.findByAuId("000-00-0000");

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------
    // 5. findByTitleId
    // -------------------------------------------------------

    @Test
    @DisplayName("findByTitleId() – should return all authors for a given title")
    void testFindByTitleIdFound() {
        List<TitleAuthor> result = titleAuthorRepository.findByTitleId("BU1032");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TitleAuthor::getTitleId).containsOnly("BU1032");
    }

    @Test
    @DisplayName("findByTitleId() – should return single record when title has one author")
    void testFindByTitleIdSingleResult() {
        List<TitleAuthor> result = titleAuthorRepository.findByTitleId("BU2075");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuId()).isEqualTo("172-32-1176");
    }

    @Test
    @DisplayName("findByTitleId() – should return empty list when titleId does not exist")
    void testFindByTitleIdNotFound() {
        List<TitleAuthor> result = titleAuthorRepository.findByTitleId("ZZZZZZ");

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------
    // 6. Update
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should update royaltyper of an existing TitleAuthor")
    void testUpdateRoyaltyper() {
        TitleAuthorId id = new TitleAuthorId("172-32-1176", "BU1032");
        TitleAuthor existing = titleAuthorRepository.findById(id).orElseThrow();
        existing.setRoyaltyper(75);

        TitleAuthor updated = titleAuthorRepository.save(existing);

        assertThat(updated.getRoyaltyper()).isEqualTo(75);
        assertThat(updated.getAuId()).isEqualTo("172-32-1176");
        assertThat(updated.getTitleId()).isEqualTo("BU1032");
    }

    @Test
    @DisplayName("save() – should update auOrd of an existing TitleAuthor")
    void testUpdateAuOrd() {
        TitleAuthorId id = new TitleAuthorId("213-46-8915", "BU1032");
        TitleAuthor existing = titleAuthorRepository.findById(id).orElseThrow();
        existing.setAuOrd(5);

        TitleAuthor updated = titleAuthorRepository.save(existing);

        assertThat(updated.getAuOrd()).isEqualTo(5);
    }

    // -------------------------------------------------------
    // 7. Delete
    // -------------------------------------------------------

    @Test
    @DisplayName("deleteById() – should remove a TitleAuthor by composite key")
    void testDeleteById() {
        TitleAuthorId id = new TitleAuthorId("172-32-1176", "BU1032");

        titleAuthorRepository.deleteById(id);

        assertThat(titleAuthorRepository.findById(id)).isNotPresent();
        assertThat(titleAuthorRepository.findAll()).hasSize(2);
    }

    // -------------------------------------------------------
    // 8. Count
    // -------------------------------------------------------

    @Test
    @DisplayName("count() – should return correct total number of TitleAuthor records")
    void testCount() {
        assertThat(titleAuthorRepository.count()).isEqualTo(3);
    }

    // -------------------------------------------------------
    // 9. ExistsById
    // -------------------------------------------------------

    @Test
    @DisplayName("existsById() – should return true for existing composite key")
    void testExistsByIdTrue() {
        TitleAuthorId id = new TitleAuthorId("172-32-1176", "BU2075");

        assertThat(titleAuthorRepository.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("existsById() – should return false for non-existing composite key")
    void testExistsByIdFalse() {
        TitleAuthorId id = new TitleAuthorId("999-00-0000", "NOTHERE");

        assertThat(titleAuthorRepository.existsById(id)).isFalse();
    }

    // -------------------------------------------------------
    // 10. Royaltyper boundary values
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should persist TitleAuthor with royaltyper = 0 (lower boundary)")
    void testSaveWithZeroRoyalty() {
        TitleAuthor ta = new TitleAuthor("213-46-8915", "MC2222", 2, 0);

        TitleAuthor saved = titleAuthorRepository.save(ta);

        assertThat(saved.getRoyaltyper()).isEqualTo(0);
    }

    @Test
    @DisplayName("save() – should persist TitleAuthor with royaltyper = 100 (upper boundary)")
    void testSaveWithFullRoyalty() {
        TitleAuthor ta = new TitleAuthor("213-46-8915", "MC3333", 1, 100);

        TitleAuthor saved = titleAuthorRepository.save(ta);

        assertThat(saved.getRoyaltyper()).isEqualTo(100);
    }
}