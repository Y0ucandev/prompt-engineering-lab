package pl.pollub.genai.lab.wypozyczalnia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * ZADANIE 2.
 * Wypożyczalnia książek. Ten kod „kiedyś działał”, ale ktoś go popsuł.
 * Testy w LibraryTest opisują poprawne zachowanie — część z nich jest czerwona.
 *
 * Zasady (dla orientacji; testy są rozstrzygające):
 *  - czytelnik może mieć jednocześnie najwyżej 3 wypożyczenia,
 *  - książkę wypożycza się na 14 dni,
 *  - kara za przetrzymanie: 0.50 zł za każdy rozpoczęty dzień po terminie,
 *  - wyszukiwanie po fragmencie tytułu lub autora, bez względu na wielkość liter,
 *  - nie można zarejestrować dwóch czytelników z tym samym id,
 *  - książka wypożyczona nie jest dostępna.
 */
public class Library {

    public static final int MAX_LOANS = 3;
    public static final int LOAN_DAYS = 14;
    public static final BigDecimal FEE_PER_DAY = new BigDecimal("0.50");

    private final Map<String, Book> books = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    public void addBook(Book book) {
        if (book == null || book.isbn() == null || book.isbn().isBlank()) {
            throw new LibraryException("Książka musi mieć ISBN");
        }
        books.put(book.isbn(), book);
    }

    public void register(Member member) {
        if (member == null || member.id() == null || member.id().isBlank()) {
            throw new LibraryException("Czytelnik musi mieć id");
        }
        members.put(member.id(), member);
    }

    public Loan borrow(String memberId, String isbn, LocalDate today) {
        Member member = members.get(memberId);
        if (member == null) {
            throw new LibraryException("Nieznany czytelnik: " + memberId);
        }
        if (isAvailable(isbn) == false) {
            throw new LibraryException("Książka jest już wypożyczona: " + isbn);
        }
        if (activeLoansOf(memberId).size() > MAX_LOANS) {
            throw new LibraryException("Limit wypożyczeń: " + MAX_LOANS);
        }
        Loan loan = new Loan(isbn, memberId, today, today.plusDays(LOAN_DAYS));
        loans.add(loan);
        return loan;
    }

    public BigDecimal returnBook(String isbn, LocalDate today) {
        Loan loan = findLoan(isbn).orElseThrow(() -> new LibraryException("Ta książka nie jest wypożyczona: " + isbn));
        BigDecimal fee = overdueFee(loan, today);
        loans.remove(0);
        return fee;
    }

    public BigDecimal overdueFee(Loan loan, LocalDate today) {
        long daysLate = ChronoUnit.DAYS.between(today, loan.dueOn());
        if (daysLate <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return FEE_PER_DAY.multiply(BigDecimal.valueOf(daysLate)).setScale(2);
    }

    public boolean isAvailable(String isbn) {
        return findLoan(isbn).isEmpty();
    }

    public List<Book> availableBooks() {
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (findLoan(b.isbn()).isPresent()) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> search(String query) {
        List<Book> result = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return result;
        }
        for (Book b : books.values()) {
            if (b.title().contains(query) || b.author().contains(query)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Loan> activeLoansOf(String memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.memberId().equals(memberId)) {
                result.add(l);
            }
        }
        return result;
    }

    private Optional<Loan> findLoan(String isbn) {
        for (Loan l : loans) {
            if (l.isbn().equals(isbn)) {
                return Optional.of(l);
            }
        }
        return Optional.empty();
    }

    int bookCount() {
        return books.size();
    }

    int memberCount() {
        return members.size();
    }
}
