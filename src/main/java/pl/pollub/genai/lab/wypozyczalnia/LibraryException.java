package pl.pollub.genai.lab.wypozyczalnia;

/** Błąd reguł biznesowych wypożyczalni (nie ma książki, limit, duplikat...). */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
