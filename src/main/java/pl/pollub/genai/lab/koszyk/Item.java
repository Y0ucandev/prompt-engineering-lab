package pl.pollub.genai.lab.koszyk;

import java.math.BigDecimal;

/** Pozycja w koszyku: nazwa, cena jednostkowa, ilość, kategoria. */
public record Item(String name, BigDecimal unitPrice, int quantity, Category category) {
}
