package pl.pollub.genai.lab.wypozyczalnia;

import java.time.LocalDate;

/** Aktywne wypożyczenie. */
public record Loan(String isbn, String memberId, LocalDate borrowedOn, LocalDate dueOn) {
}
