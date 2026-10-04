package pl.pollub.genai.lab.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Specyfikacja wypożyczalni. NIE ZMIENIAJ TEGO FILEU — zadaniem jest naprawić Library.
 */
class LibraryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 1);
    private Library lib;

    @BeforeEach
    void setUp() {
        lib = new Library();
        lib.addBook(new Book("111", "Czysty kod", "Robert C. Martin"));
        lib.addBook(new Book("222", "Java. Efektywne programowanie", "Joshua Bloch"));
        lib.addBook(new Book("333", "Pragmatyczny programista", "Andrew Hunt"));
        lib.addBook(new Book("444", "Refaktoryzacja", "Martin Fowler"));
        lib.addBook(new Book("555", "Wzorce projektowe", "Erich Gamma"));
        lib.register(new Member("m1", "Ania"));
        lib.register(new Member("m2", "Bartek"));
    }

    // --- katalog ------------------------------------------------------------

    @Test
    @DisplayName("katalog: książka bez ISBN jest odrzucana")
    void bookWithoutIsbn() {
        assertThrows(LibraryException.class, () -> lib.addBook(new Book(" ", "x", "y")));
        assertEquals(5, lib.bookCount());
    }

    @Test
    @DisplayName("rejestracja: duplikat id czytelnika → LibraryException, stary czytelnik zostaje")
    void duplicateMember() {
        assertThrows(LibraryException.class, () -> lib.register(new Member("m1", "Inna Ania")));
        assertEquals(2, lib.memberCount());
    }

    // --- wypożyczanie -------------------------------------------------------

    @Test
    @DisplayName("wypożyczenie: termin zwrotu = dziś + 14 dni")
    void dueDate() {
        Loan loan = lib.borrow("m1", "111", TODAY);
        assertEquals(LocalDate.of(2026, 3, 15), loan.dueOn());
        assertEquals("m1", loan.memberId());
    }

    @Test
    @DisplayName("wypożyczenie: nieznany czytelnik → LibraryException")
    void unknownMember() {
        assertThrows(LibraryException.class, () -> lib.borrow("ghost", "111", TODAY));
    }

    @Test
    @DisplayName("wypożyczenie: nieznana książka → LibraryException (nie NullPointerException)")
    void unknownBook() {
        assertThrows(LibraryException.class, () -> lib.borrow("m1", "999", TODAY));
    }

    @Test
    @DisplayName("wypożyczenie: książka już wypożyczona → LibraryException")
    void alreadyBorrowed() {
        lib.borrow("m1", "111", TODAY);
        assertThrows(LibraryException.class, () -> lib.borrow("m2", "111", TODAY));
    }

    @Test
    @DisplayName("limit: trzecie wypożyczenie przechodzi, czwarte → LibraryException")
    void threeLoansLimit() {
        lib.borrow("m1", "111", TODAY);
        lib.borrow("m1", "222", TODAY);
        lib.borrow("m1", "333", TODAY);
        assertEquals(3, lib.activeLoansOf("m1").size());
        assertThrows(LibraryException.class, () -> lib.borrow("m1", "444", TODAY));
    }

    // --- zwrot i kary -------------------------------------------------------

    @Test
    @DisplayName("zwrot w terminie: kara 0.00, książka znów dostępna")
    void returnOnTime() {
        lib.borrow("m1", "111", TODAY);
        BigDecimal fee = lib.returnBook("111", TODAY.plusDays(14));
        assertEquals(new BigDecimal("0.00"), fee);
        assertTrue(lib.isAvailable("111"));
    }

    @Test
    @DisplayName("zwrot po terminie: 0.50 zł za każdy dzień spóźnienia")
    void lateFee() {
        lib.borrow("m1", "111", TODAY);
        // termin 15.03, zwrot 18.03 → 3 dni × 0.50 = 1.50
        assertEquals(new BigDecimal("1.50"), lib.returnBook("111", TODAY.plusDays(17)));
    }

    @Test
    @DisplayName("zwrot: kara nigdy nie jest ujemna")
    void feeNeverNegative() {
        lib.borrow("m1", "111", TODAY);
        BigDecimal fee = lib.returnBook("111", TODAY.plusDays(2));
        assertTrue(fee.signum() >= 0, "kara ujemna: " + fee);
        assertEquals(new BigDecimal("0.00"), fee);
    }

    @Test
    @DisplayName("zwrot usuwa WŁAŚCIWE wypożyczenie, nie pierwsze z brzegu")
    void returnRemovesCorrectLoan() {
        lib.borrow("m1", "111", TODAY);
        lib.borrow("m2", "222", TODAY);
        lib.returnBook("222", TODAY);
        assertTrue(lib.isAvailable("222"));
        assertFalse(lib.isAvailable("111"), "111 wciąż powinna być wypożyczona");
        assertEquals(1, lib.activeLoansOf("m1").size());
        assertEquals(0, lib.activeLoansOf("m2").size());
    }

    @Test
    @DisplayName("zwrot książki, która nie jest wypożyczona → LibraryException")
    void returnNotBorrowed() {
        assertThrows(LibraryException.class, () -> lib.returnBook("111", TODAY));
    }

    // --- dostępność i wyszukiwanie -----------------------------------------

    @Test
    @DisplayName("dostępne książki: wszystkie minus wypożyczone")
    void availableBooks() {
        lib.borrow("m1", "111", TODAY);
        lib.borrow("m2", "222", TODAY);
        List<Book> dostepne = lib.availableBooks();
        assertEquals(3, dostepne.size());
        assertTrue(dostepne.stream().noneMatch(b -> b.isbn().equals("111")));
        assertTrue(dostepne.stream().noneMatch(b -> b.isbn().equals("222")));
    }

    @Test
    @DisplayName("wyszukiwanie: fragment tytułu, bez względu na wielkość liter")
    void searchByTitle() {
        List<Book> r = lib.search("czysty");
        assertEquals(1, r.size());
        assertEquals("111", r.get(0).isbn());
    }

    @Test
    @DisplayName("wyszukiwanie: fragment nazwiska autora, wielkie litery")
    void searchByAuthor() {
        List<Book> r = lib.search("BLOCH");
        assertEquals(1, r.size());
        assertEquals("222", r.get(0).isbn());
    }

    @Test
    @DisplayName("wyszukiwanie: pusty tekst lub null → pusta lista")
    void searchEmpty() {
        assertTrue(lib.search("").isEmpty());
        assertTrue(lib.search("   ").isEmpty());
        assertTrue(lib.search(null).isEmpty());
    }

    @Test
    @DisplayName("wyszukiwanie: wiele trafień")
    void searchMultipleHits() {
        // „program” pasuje do: „Java. Efektywne programowanie”, „Pragmatyczny programista”
        assertEquals(2, lib.search("program").size());
    }
}
