package pl.pollub.genai.lab.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pl.pollub.genai.lab.cart.Category.*;

/**
 * Specyfikacja carta. Każdy test = jedna zasada. Czytaj nazwy i asercje.
 */
class CartTest {

    private static Item item(String name, String price, int qty, Category cat) {
        return new Item(name, new BigDecimal(price), qty, cat);
    }

    private static BigDecimal money(String v) {
        return new BigDecimal(v);
    }

    // --- podstawy ----------------------------------------------------------

    @Test
    @DisplayName("1. pusta lista → 0.00, bez kosztu dostawy")
    void pustyCart() {
        assertEquals(money("0.00"), Cart.total(List.of(), null));
    }

    @Test
    @DisplayName("2. null zamiast listy → IllegalArgumentException")
    void nullList() {
        assertThrows(IllegalArgumentException.class, () -> Cart.total(null, null));
    }

    @Test
    @DisplayName("3. ilość 0 lub ujemna → IllegalArgumentException")
    void invalidQuantity() {
        assertThrows(IllegalArgumentException.class, () -> Cart.total(List.of(item("x", "10.00", 0, OTHER)), null));
        assertThrows(IllegalArgumentException.class, () -> Cart.total(List.of(item("x", "10.00", -2, OTHER)), null));
    }

    @Test
    @DisplayName("4. cena ujemna → IllegalArgumentException")
    void negativePrice() {
        assertThrows(IllegalArgumentException.class, () -> Cart.total(List.of(item("x", "-1.00", 1, OTHER)), null));
    }

    @Test
    @DisplayName("5. suma = cena × ilość, wynik zawsze z dwoma miejscami po przecinku")
    void simpleSum() {
        // 3 × 19.99 = 59.97, poniżej progu darmowej dostawy → +9.99
        assertEquals(money("69.96"), Cart.total(List.of(item("kabel", "19.99", 3, ELECTRONICS)), null));
    }

    @Test
    @DisplayName("6. wynik ma scale 2 nawet dla okrągłych kwot")
    void scaleIsTwo() {
        BigDecimal t = Cart.total(List.of(item("x", "200", 1, OTHER)), null);
        assertEquals(2, t.scale());
        assertEquals(money("200.00"), t);
    }

    // --- dostawa ----------------------------------------------------------

    @Test
    @DisplayName("7. poniżej 150.00 po rabatach: +9.99 dostawy")
    void paidShipping() {
        assertEquals(money("159.98"), Cart.total(List.of(item("x", "149.99", 1, OTHER)), null));
    }

    @Test
    @DisplayName("8. dokładnie 150.00 po rabatach: dostawa darmowa")
    void freeShippingAtThreshold() {
        assertEquals(money("150.00"), Cart.total(List.of(item("x", "150.00", 1, OTHER)), null));
    }

    @Test
    @DisplayName("9. próg dostawy liczy się PO rabatach, nie przed")
    void shippingThresholdAfterDiscounts() {
        // 160.00 − 10% (STUDENT10) = 144.00 < 150 → +9.99 = 153.99
        assertEquals(money("153.99"), Cart.total(List.of(item("x", "160.00", 1, OTHER)), "STUDENT10"));
    }

    // --- rabat ilościowy --------------------------------------------------

    @Test
    @DisplayName("10. 10 lub więcej sztuk tej samej pozycji: 5% rabatu na tę pozycję")
    void quantityDiscount() {
        // 10 × 20.00 = 200.00 − 5% = 190.00, dostawa darmowa
        assertEquals(money("190.00"), Cart.total(List.of(item("x", "20.00", 10, OTHER)), null));
    }

    @Test
    @DisplayName("11. 9 sztuk: brak rabatu ilościowego")
    void noQuantityDiscountBelowTen() {
        // 9 × 20.00 = 180.00, dostawa darmowa
        assertEquals(money("180.00"), Cart.total(List.of(item("x", "20.00", 9, OTHER)), null));
    }

    @Test
    @DisplayName("12. rabat ilościowy obejmuje też książki")
    void quantityDiscountAppliesToBooks() {
        // 10 × 30.00 = 300.00 − 5% = 285.00
        assertEquals(money("285.00"), Cart.total(List.of(item("Java", "30.00", 10, BOOK)), null));
    }

    // --- kody promocyjne --------------------------------------------------

    @Test
    @DisplayName("13. STUDENT10: 10% od sumy (po rabatach ilościowych)")
    void student10() {
        // 300.00 − 10% = 270.00
        assertEquals(money("270.00"), Cart.total(List.of(item("x", "300.00", 1, ELECTRONICS)), "STUDENT10"));
    }

    @Test
    @DisplayName("14. kod jest nieczuły na wielkość liter i spacje wokół")
    void codeIsCaseInsensitive() {
        assertEquals(money("270.00"), Cart.total(List.of(item("x", "300.00", 1, ELECTRONICS)), "  student10 "));
    }

    @Test
    @DisplayName("15. nieznany kod, pusty string, null: brak rabatu, brak wyjątku")
    void unknownCode() {
        List<Item> k = List.of(item("x", "300.00", 1, ELECTRONICS));
        assertEquals(money("300.00"), Cart.total(k, "WAKACJE50"));
        assertEquals(money("300.00"), Cart.total(k, ""));
        assertEquals(money("300.00"), Cart.total(k, null));
    }

    @Test
    @DisplayName("16. WINTER20: 20%, ale tylko gdy suma po rabatach ilościowych ≥ 200.00")
    void winter20Prog() {
        assertEquals(money("160.00"), Cart.total(List.of(item("x", "200.00", 1, OTHER)), "WINTER20"));
        // 199.99 < 200 → kod nie działa, dostawa darmowa (≥150)
        assertEquals(money("199.99"), Cart.total(List.of(item("x", "199.99", 1, OTHER)), "WINTER20"));
    }

    @Test
    @DisplayName("17. książki nigdy nie są objęte rabatem z kodu")
    void booksExcludedFromCode() {
        // książka 100.00 + elektronika 100.00; STUDENT10 obejmuje tylko elektronikę → 100 + 90 = 190.00
        assertEquals(money("190.00"), Cart.total(List.of(
                item("Java", "100.00", 1, BOOK),
                item("mysz", "100.00", 1, ELECTRONICS)), "STUDENT10"));
    }

    @Test
    @DisplayName("18. rabat z kodu jest ograniczony do 100.00")
    void codeDiscountCap() {
        // 2000.00 − 20% = 400 rabatu, ale max 100 → 1900.00
        assertEquals(money("1900.00"), Cart.total(List.of(item("laptop", "2000.00", 1, ELECTRONICS)), "WINTER20"));
    }

    // --- zaokrąglanie -----------------------------------------------------

    @Test
    @DisplayName("19. zaokrąglenie HALF_UP do 2 miejsc dopiero na końcu")
    void roundingAtTheEnd() {
        // 3 × 33.33 = 99.99 − 10% = 89.991 → dostawa +9.99 = 99.981 → 99.98
        assertEquals(money("99.98"), Cart.total(List.of(item("x", "33.33", 3, OTHER)), "STUDENT10"));
    }

    @Test
    @DisplayName("20. wszystko naraz: rabat ilościowy, kod, książka, limit, dostawa")
    void everythingAtOnce() {
        // elektronika: 10 × 50.00 = 500.00 − 5% = 475.00
        // książka:     2 × 40.00 = 80.00 (bez rabatu ilościowego, bez kodu)
        // suma po ilościowym = 555.00 ≥ 200 → WINTER20 działa, ale tylko na 475.00 → 95.00 rabatu (< 100, bez limitu)
        // 475.00 − 95.00 + 80.00 = 460.00, dostawa darmowa
        assertEquals(money("460.00"), Cart.total(List.of(
                item("słuchawki", "50.00", 10, ELECTRONICS),
                item("Czysty kod", "40.00", 2, BOOK)), "winter20"));
    }
}
