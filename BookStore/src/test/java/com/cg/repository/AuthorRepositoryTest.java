package com.cg.repository;

import com.cg.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer tests for AuthorRepository.
 *
 * - Uses real MySQL pubs_test database (NOT H2).
 * - @AutoConfigureTestDatabase(replace = NONE) tells Spring NOT to swap
 *   the real DataSource with an embedded one.
 * - @ActiveProfiles("test") loads application-test.properties which
 *   points to pubs_test schema.
 * - ddl-auto=create-drop in test properties ensures tables are created
 *   fresh before tests and dropped after.
 * - @BeforeEach clears the table so every test starts clean.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("AuthorRepository – Repository Layer Tests (MySQL pubs_test)")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    // -------------------------------------------------------
    // Common test data
    // -------------------------------------------------------
    private Author author1;
    private Author author2;
    private Author author3;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();

        author1 = new Author(
                "172-32-1176", "White", "Johnson", "408 496-7223",
                "10932 Bigge Rd", "Menlo Park", "CA", "94025", 1);

        author2 = new Author(
                "213-46-8915", "Green", "Marjorie", "415 986-7020",
                "309 63rd St", "Oakland", "CA", "94618", 1);

        author3 = new Author(
                "238-95-7766", "Carson", "Cheryl", "415 548-7723",
                "589 Darwin Ln", "Berkeley", "CA", "94705", 1);

        authorRepository.saveAll(List.of(author1, author2, author3));
    }

    // -------------------------------------------------------
    // 1. Save / Create
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should persist a new Author and return it with correct fields")
    void testSaveAuthor() {
        Author newAuthor = new Author(
                "999-88-7777", "Smith", "John", "123 456-7890",
                "100 Main St", "San Francisco", "CA", "94102", 0);

        Author saved = authorRepository.save(newAuthor);

        assertThat(saved).isNotNull();
        assertThat(saved.getAuId()).isEqualTo("999-88-7777");
        assertThat(saved.getAuLname()).isEqualTo("Smith");
        assertThat(saved.getAuFname()).isEqualTo("John");
        assertThat(saved.getPhone()).isEqualTo("123 456-7890");
        assertThat(saved.getCity()).isEqualTo("San Francisco");
        assertThat(saved.getState()).isEqualTo("CA");
        assertThat(saved.getZip()).isEqualTo("94102");
        assertThat(saved.getContract()).isEqualTo(0);
    }

    // -------------------------------------------------------
    // 2. FindById
    // -------------------------------------------------------

    @Test
    @DisplayName("findById() – should return Author when ID exists")
    void testFindByIdExists() {
        Optional<Author> result = authorRepository.findById("172-32-1176");

        assertThat(result).isPresent();
        assertThat(result.get().getAuLname()).isEqualTo("White");
        assertThat(result.get().getAuFname()).isEqualTo("Johnson");
        assertThat(result.get().getCity()).isEqualTo("Menlo Park");
    }

    @Test
    @DisplayName("findById() – should return empty Optional when ID does not exist")
    void testFindByIdNotFound() {
        Optional<Author> result = authorRepository.findById("000-00-0000");

        assertThat(result).isNotPresent();
    }

    // -------------------------------------------------------
    // 3. FindAll
    // -------------------------------------------------------

    @Test
    @DisplayName("findAll() – should return all saved authors")
    void testFindAll() {
        List<Author> all = authorRepository.findAll();

        assertThat(all).hasSize(3);
    }

    // -------------------------------------------------------
    // 4. findByCity
    // -------------------------------------------------------

    @Test
    @DisplayName("findByCity() – should return authors matching the given city")
    void testFindByCityFound() {
        List<Author> result = authorRepository.findByCity("Oakland");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuLname()).isEqualTo("Green");
    }

    @Test
    @DisplayName("findByCity() – should return empty list when city does not match")
    void testFindByCityNotFound() {
        List<Author> result = authorRepository.findByCity("Denver");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCity() – should return multiple authors from the same city")
    void testFindByCityMultipleResults() {
        Author extra = new Author(
                "111-22-3333", "Adams", "Alice", "415 111-2222",
                "100 Lake Shore Dr", "Oakland", "CA", "94601", 1);
        authorRepository.save(extra);

        List<Author> result = authorRepository.findByCity("Oakland");

        assertThat(result).hasSize(2);
    }

    // -------------------------------------------------------
    // 5. findByState
    // -------------------------------------------------------

    @Test
    @DisplayName("findByState() – should return all authors in a given state")
    void testFindByStateFound() {
        List<Author> result = authorRepository.findByState("CA");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Author::getState).containsOnly("CA");
    }

    @Test
    @DisplayName("findByState() – should return empty list when no authors in state")
    void testFindByStateNotFound() {
        List<Author> result = authorRepository.findByState("TX");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByState() – should correctly filter between two states")
    void testFindByStateFilteredCorrectly() {
        Author txAuthor = new Author(
                "444-55-6666", "Brown", "Bob", "512 000-1111",
                "789 Elm St", "Austin", "TX", "78701", 0);
        authorRepository.save(txAuthor);

        List<Author> caAuthors = authorRepository.findByState("CA");
        List<Author> txAuthors = authorRepository.findByState("TX");

        assertThat(caAuthors).hasSize(3);
        assertThat(txAuthors).hasSize(1);
        assertThat(txAuthors.get(0).getAuLname()).isEqualTo("Brown");
    }

    // -------------------------------------------------------
    // 6. Update
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should update existing Author's city and phone")
    void testUpdateAuthor() {
        Author existing = authorRepository.findById("172-32-1176").orElseThrow();
        existing.setCity("San Jose");
        existing.setPhone("408 000-9999");

        Author updated = authorRepository.save(existing);

        assertThat(updated.getCity()).isEqualTo("San Jose");
        assertThat(updated.getPhone()).isEqualTo("408 000-9999");
        assertThat(updated.getAuId()).isEqualTo("172-32-1176");
    }

    @Test
    @DisplayName("save() – should update contract value of existing Author")
    void testUpdateContract() {
        Author existing = authorRepository.findById("172-32-1176").orElseThrow();
        existing.setContract(0);

        Author updated = authorRepository.save(existing);

        assertThat(updated.getContract()).isEqualTo(0);
    }

    // -------------------------------------------------------
    // 7. Delete
    // -------------------------------------------------------

    @Test
    @DisplayName("deleteById() – should remove an Author from the database")
    void testDeleteById() {
        authorRepository.deleteById("213-46-8915");

        Optional<Author> result = authorRepository.findById("213-46-8915");
        assertThat(result).isNotPresent();

        assertThat(authorRepository.findAll()).hasSize(2);
    }

    // -------------------------------------------------------
    // 8. Count
    // -------------------------------------------------------

    @Test
    @DisplayName("count() – should return correct number of authors")
    void testCount() {
        assertThat(authorRepository.count()).isEqualTo(3);
    }

    // -------------------------------------------------------
    // 9. ExistsById
    // -------------------------------------------------------

    @Test
    @DisplayName("existsById() – should return true for an existing Author")
    void testExistsByIdTrue() {
        assertThat(authorRepository.existsById("172-32-1176")).isTrue();
    }

    @Test
    @DisplayName("existsById() – should return false for a non-existing Author")
    void testExistsByIdFalse() {
        assertThat(authorRepository.existsById("000-00-0000")).isFalse();
    }

    // -------------------------------------------------------
    // 10. Null optional fields
    // -------------------------------------------------------

    @Test
    @DisplayName("save() – should persist Author with null optional fields")
    void testSaveAuthorWithNullOptionalFields() {
        Author minimal = new Author(
                "777-66-5555", "Doe", "Jane", "000 000-0000",
                null, null, null, null, 0);

        Author saved = authorRepository.save(minimal);

        assertThat(saved.getAuId()).isEqualTo("777-66-5555");
        assertThat(saved.getAddress()).isNull();
        assertThat(saved.getCity()).isNull();
        assertThat(saved.getState()).isNull();
        assertThat(saved.getZip()).isNull();
    }
}