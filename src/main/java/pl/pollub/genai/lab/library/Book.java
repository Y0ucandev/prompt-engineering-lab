package pl.pollub.genai.lab.library;

/** Książka w katalogu. ISBN jest identyfikatorem. */
public record Book(String isbn, String title, String author) {
}
