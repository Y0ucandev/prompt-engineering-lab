package pl.pollub.genai.lab.cart;

import java.math.BigDecimal;

/** Pozycja w cartu: nazwa, cena jednostkowa, ilość, kategoria. */
public record Item(String name, BigDecimal unitPrice, int quantity, Category category) {
}
