package pl.pollub.genai.lab.koszyk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pl.pollub.genai.lab.koszyk.Category.*;

/**
 * Specyfikacja koszyka. Każdy test = jedna zasada. Czytaj nazwy i asercje.
 */
class KoszykTest {

    private static Item item(String name, String price, int qty, Category cat) {
        return new Item(name, new BigDecimal(price), qty, cat);
    }

    private static BigDecimal pln(String v) {
        return new BigDecimal(v);
    }

    // --- podstawy ----------------------------------------------------------

    @Test
    @DisplayName("1. pusta lista → 0.00, bez kosztu dostawy")
    void pustyKoszyk() {
        assertEquals(pln("0.00"), Koszyk.total(List.of(), null));
    }

    @Test
    @DisplayName("2. null zamiast listy → IllegalArgumentException")
    void nullLista() {
        assertThrows(IllegalArgumentException.class, () -> Koszyk.total(null, null));
    }

    @Test
    @DisplayName("3. ilość 0 lub ujemna → IllegalArgumentException")
    void zlaIlosc() {
        assertThrows(IllegalArgumentException.class, () -> Koszyk.total(List.of(item("x", "10.00", 0, INNE)), null));
        assertThrows(IllegalArgumentException.class, () -> Koszyk.total(List.of(item("x", "10.00", -2, INNE)), null));
    }

    @Test
    @DisplayName("4. cena ujemna → IllegalArgumentException")
    void ujemnaCena() {
        assertThrows(IllegalArgumentException.class, () -> Koszyk.total(List.of(item("x", "-1.00", 1, INNE)), null));
    }

    @Test
    @DisplayName("5. suma = cena × ilość, wynik zawsze z dwoma miejscami po przecinku")
    void prostaSuma() {
        // 3 × 19.99 = 59.97, poniżej progu darmowej dostawy → +9.99
        assertEquals(pln("69.96"), Koszyk.total(List.of(item("kabel", "19.99", 3, ELEKTRONIKA)), null));
    }

    @Test
    @DisplayName("6. wynik ma scale 2 nawet dla okrągłych kwot")
    void scaleDwa() {
        BigDecimal t = Koszyk.total(List.of(item("x", "200", 1, INNE)), null);
        assertEquals(2, t.scale());
        assertEquals(pln("200.00"), t);
    }

    // --- dostawa ----------------------------------------------------------

    @Test
    @DisplayName("7. poniżej 150.00 po rabatach: +9.99 dostawy")
    void dostawaPlatna() {
        assertEquals(pln("159.98"), Koszyk.total(List.of(item("x", "149.99", 1, INNE)), null));
    }

    @Test
    @DisplayName("8. dokładnie 150.00 po rabatach: dostawa darmowa")
    void dostawaDarmowaNaProgu() {
        assertEquals(pln("150.00"), Koszyk.total(List.of(item("x", "150.00", 1, INNE)), null));
    }

    @Test
    @DisplayName("9. próg dostawy liczy się PO rabatach, nie przed")
    void dostawaPoRabatach() {
        // 160.00 − 10% (STUDENT10) = 144.00 < 150 → +9.99 = 153.99
        assertEquals(pln("153.99"), Koszyk.total(List.of(item("x", "160.00", 1, INNE)), "STUDENT10"));
    }

    // --- rabat ilościowy --------------------------------------------------

    @Test
    @DisplayName("10. 10 lub więcej sztuk tej samej pozycji: 5% rabatu na tę pozycję")
    void rabatIlosciowy() {
        // 10 × 20.00 = 200.00 − 5% = 190.00, dostawa darmowa
        assertEquals(pln("190.00"), Koszyk.total(List.of(item("x", "20.00", 10, INNE)), null));
    }

    @Test
    @DisplayName("11. 9 sztuk: brak rabatu ilościowego")
    void brakRabatuPonizejDziesieciu() {
        // 9 × 20.00 = 180.00, dostawa darmowa
        assertEquals(pln("180.00"), Koszyk.total(List.of(item("x", "20.00", 9, INNE)), null));
    }

    @Test
    @DisplayName("12. rabat ilościowy obejmuje też książki")
    void rabatIlosciowyNaKsiazki() {
        // 10 × 30.00 = 300.00 − 5% = 285.00
        assertEquals(pln("285.00"), Koszyk.total(List.of(item("Java", "30.00", 10, KSIAZKA)), null));
    }

    // --- kody promocyjne --------------------------------------------------

    @Test
    @DisplayName("13. STUDENT10: 10% od sumy (po rabatach ilościowych)")
    void student10() {
        // 300.00 − 10% = 270.00
        assertEquals(pln("270.00"), Koszyk.total(List.of(item("x", "300.00", 1, ELEKTRONIKA)), "STUDENT10"));
    }

    @Test
    @DisplayName("14. kod jest nieczuły na wielkość liter i spacje wokół")
    void kodNieczulyNaWielkoscLiter() {
        assertEquals(pln("270.00"), Koszyk.total(List.of(item("x", "300.00", 1, ELEKTRONIKA)), "  student10 "));
    }

    @Test
    @DisplayName("15. nieznany kod, pusty string, null: brak rabatu, brak wyjątku")
    void nieznanyKod() {
        List<Item> k = List.of(item("x", "300.00", 1, ELEKTRONIKA));
        assertEquals(pln("300.00"), Koszyk.total(k, "WAKACJE50"));
        assertEquals(pln("300.00"), Koszyk.total(k, ""));
        assertEquals(pln("300.00"), Koszyk.total(k, null));
    }

    @Test
    @DisplayName("16. ZIMA20: 20%, ale tylko gdy suma po rabatach ilościowych ≥ 200.00")
    void zima20Prog() {
        assertEquals(pln("160.00"), Koszyk.total(List.of(item("x", "200.00", 1, INNE)), "ZIMA20"));
        // 199.99 < 200 → kod nie działa, dostawa darmowa (≥150)
        assertEquals(pln("199.99"), Koszyk.total(List.of(item("x", "199.99", 1, INNE)), "ZIMA20"));
    }

    @Test
    @DisplayName("17. książki nigdy nie są objęte rabatem z kodu")
    void ksiazkiBezKodu() {
        // książka 100.00 + elektronika 100.00; STUDENT10 obejmuje tylko elektronikę → 100 + 90 = 190.00
        assertEquals(pln("190.00"), Koszyk.total(List.of(
                item("Java", "100.00", 1, KSIAZKA),
                item("mysz", "100.00", 1, ELEKTRONIKA)), "STUDENT10"));
    }

    @Test
    @DisplayName("18. rabat z kodu jest ograniczony do 100.00")
    void limitRabatuZKodu() {
        // 2000.00 − 20% = 400 rabatu, ale max 100 → 1900.00
        assertEquals(pln("1900.00"), Koszyk.total(List.of(item("laptop", "2000.00", 1, ELEKTRONIKA)), "ZIMA20"));
    }

    // --- zaokrąglanie -----------------------------------------------------

    @Test
    @DisplayName("19. zaokrąglenie HALF_UP do 2 miejsc dopiero na końcu")
    void zaokraglenieNaKoncu() {
        // 3 × 33.33 = 99.99 − 10% = 89.991 → dostawa +9.99 = 99.981 → 99.98
        assertEquals(pln("99.98"), Koszyk.total(List.of(item("x", "33.33", 3, INNE)), "STUDENT10"));
    }

    @Test
    @DisplayName("20. wszystko naraz: rabat ilościowy, kod, książka, limit, dostawa")
    void wszystkoNaraz() {
        // elektronika: 10 × 50.00 = 500.00 − 5% = 475.00
        // książka:     2 × 40.00 = 80.00 (bez rabatu ilościowego, bez kodu)
        // suma po ilościowym = 555.00 ≥ 200 → ZIMA20 działa, ale tylko na 475.00 → 95.00 rabatu (< 100, bez limitu)
        // 475.00 − 95.00 + 80.00 = 460.00, dostawa darmowa
        assertEquals(pln("460.00"), Koszyk.total(List.of(
                item("słuchawki", "50.00", 10, ELEKTRONIKA),
                item("Czysty kod", "40.00", 2, KSIAZKA)), "zima20"));
    }
}
