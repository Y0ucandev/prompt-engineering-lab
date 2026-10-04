package pl.pollub.genai.lab.koszyk;

import java.math.BigDecimal;
import java.util.List;

/**
 * ZADANIE 1.
 * Oblicza kwotę do zapłaty za koszyk. Zasady rabatów, dostawy i zaokrągleń
 * NIE są tu opisane celowo — są w testach (KoszykTest). Test to specyfikacja.
 *
 * Twoje zadanie: napisać prompt, który pozwoli modelowi zaimplementować tę metodę
 * tak, żeby wszystkie testy przeszły. Kod wklejasz tutaj.
 */
public final class Koszyk {

    private Koszyk() {
    }

    public static BigDecimal total(List<Item> items, String promoCode) {
        throw new UnsupportedOperationException("TODO — zadanie 1");
    }
}
