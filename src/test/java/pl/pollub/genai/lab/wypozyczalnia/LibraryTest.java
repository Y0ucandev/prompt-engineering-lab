package pl.pollub.genai.lab.wypozyczalnia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Specyfikacja wypożyczalni. NIE ZMIENIAJ TEGO PLIKU — zadaniem jest naprawić Library.
 */
class LibraryTest {

    private static final LocalDate DZIS = LocalDate.of(2026, 3, 1);
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
    void ksiazkaBezIsbn() {
        assertThrows(LibraryException.class, () -> lib.addBook(new Book(" ", "x", "y")));
        assertEquals(5, lib.bookCount());
    }

    @Test
    @DisplayName("rejestracja: duplikat id czytelnika → LibraryException, stary czytelnik zostaje")
    void duplikatCzytelnika() {
        assertThrows(LibraryException.class, () -> lib.register(new Member("m1", "Inna Ania")));
        assertEquals(2, lib.memberCount());
    }

    // --- wypożyczanie -------------------------------------------------------

    @Test
    @DisplayName("wypożyczenie: termin zwrotu = dziś + 14 dni")
    void terminZwrotu() {
        Loan loan = lib.borrow("m1", "111", DZIS);
        assertEquals(LocalDate.of(2026, 3, 15), loan.dueOn());
        assertEquals("m1", loan.memberId());
    }

    @Test
    @DisplayName("wypożyczenie: nieznany czytelnik → LibraryException")
    void nieznanyCzytelnik() {
        assertThrows(LibraryException.class, () -> lib.borrow("ghost", "111", DZIS));
    }

    @Test
    @DisplayName("wypożyczenie: nieznana książka → LibraryException (nie NullPointerException)")
    void nieznanaKsiazka() {
        assertThrows(LibraryException.class, () -> lib.borrow("m1", "999", DZIS));
    }

    @Test
    @DisplayName("wypożyczenie: książka już wypożyczona → LibraryException")
    void juzWypozyczona() {
        lib.borrow("m1", "111", DZIS);
        assertThrows(LibraryException.class, () -> lib.borrow("m2", "111", DZIS));
    }

    @Test
    @DisplayName("limit: trzecie wypożyczenie przechodzi, czwarte → LibraryException")
    void limitTrzechWypozyczen() {
        lib.borrow("m1", "111", DZIS);
        lib.borrow("m1", "222", DZIS);
        lib.borrow("m1", "333", DZIS);
        assertEquals(3, lib.activeLoansOf("m1").size());
        assertThrows(LibraryException.class, () -> lib.borrow("m1", "444", DZIS));
    }

    // --- zwrot i kary -------------------------------------------------------

    @Test
    @DisplayName("zwrot w terminie: kara 0.00, książka znów dostępna")
    void zwrotWTerminie() {
        lib.borrow("m1", "111", DZIS);
        BigDecimal fee = lib.returnBook("111", DZIS.plusDays(14));
        assertEquals(new BigDecimal("0.00"), fee);
        assertTrue(lib.isAvailable("111"));
    }

    @Test
    @DisplayName("zwrot po terminie: 0.50 zł za każdy dzień spóźnienia")
    void karaZaSpoznienie() {
        lib.borrow("m1", "111", DZIS);
        // termin 15.03, zwrot 18.03 → 3 dni × 0.50 = 1.50
        assertEquals(new BigDecimal("1.50"), lib.returnBook("111", DZIS.plusDays(17)));
    }

    @Test
    @DisplayName("zwrot: kara nigdy nie jest ujemna")
    void karaNieUjemna() {
        lib.borrow("m1", "111", DZIS);
        BigDecimal fee = lib.returnBook("111", DZIS.plusDays(2));
        assertTrue(fee.signum() >= 0, "kara ujemna: " + fee);
        assertEquals(new BigDecimal("0.00"), fee);
    }

    @Test
    @DisplayName("zwrot usuwa WŁAŚCIWE wypożyczenie, nie pierwsze z brzegu")
    void zwrotWlasciwejKsiazki() {
        lib.borrow("m1", "111", DZIS);
        lib.borrow("m2", "222", DZIS);
        lib.returnBook("222", DZIS);
        assertTrue(lib.isAvailable("222"));
        assertFalse(lib.isAvailable("111"), "111 wciąż powinna być wypożyczona");
        assertEquals(1, lib.activeLoansOf("m1").size());
        assertEquals(0, lib.activeLoansOf("m2").size());
    }

    @Test
    @DisplayName("zwrot książki, która nie jest wypożyczona → LibraryException")
    void zwrotNiewypozyczonej() {
        assertThrows(LibraryException.class, () -> lib.returnBook("111", DZIS));
    }

    // --- dostępność i wyszukiwanie -----------------------------------------

    @Test
    @DisplayName("dostępne książki: wszystkie minus wypożyczone")
    void dostepneKsiazki() {
        lib.borrow("m1", "111", DZIS);
        lib.borrow("m2", "222", DZIS);
        List<Book> dostepne = lib.availableBooks();
        assertEquals(3, dostepne.size());
        assertTrue(dostepne.stream().noneMatch(b -> b.isbn().equals("111")));
        assertTrue(dostepne.stream().noneMatch(b -> b.isbn().equals("222")));
    }

    @Test
    @DisplayName("wyszukiwanie: fragment tytułu, bez względu na wielkość liter")
    void szukajPoTytule() {
        List<Book> r = lib.search("czysty");
        assertEquals(1, r.size());
        assertEquals("111", r.get(0).isbn());
    }

    @Test
    @DisplayName("wyszukiwanie: fragment nazwiska autora, wielkie litery")
    void szukajPoAutorze() {
        List<Book> r = lib.search("BLOCH");
        assertEquals(1, r.size());
        assertEquals("222", r.get(0).isbn());
    }

    @Test
    @DisplayName("wyszukiwanie: pusty tekst lub null → pusta lista")
    void szukajPusty() {
        assertTrue(lib.search("").isEmpty());
        assertTrue(lib.search("   ").isEmpty());
        assertTrue(lib.search(null).isEmpty());
    }

    @Test
    @DisplayName("wyszukiwanie: wiele trafień")
    void szukajWieleTrafien() {
        // „program” pasuje do: „Java. Efektywne programowanie”, „Pragmatyczny programista”
        assertEquals(2, lib.search("program").size());
    }
}
