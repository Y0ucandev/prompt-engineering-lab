# GenAI Lab — Politechnika Lubelska

Laboratorium do wykładu „Od promptu do agenta”. Java 17 + Maven + JUnit 5.

**Pełna instrukcja krok po kroku: [INSTRUCTIONS.md](INSTRUCTIONS.md).**

```bash
git clone https://github.com/Y0ucandev/prompt-engineering-lab.git
cd prompt-engineering-lab
mvn -q test          # pobierze JUnit; na start testy są czerwone — to celowe
```

| Zadanie | Gdzie | Co robisz |
|---|---|---|
| 1. Koszyk (`Cart`) | `src/main/java/pl/pollub/genai/lab/cart/Cart.java` | piszesz prompt, model pisze kod, testy liczą punkty |
| 2. Wypożyczalnia (`Library`) | `src/main/java/pl/pollub/genai/lab/library/Library.java` | agent naprawia popsuty kod, Ty czytasz diffy |
| 3. Bonus | — | golf promptowy i red team |

Liczenie punktów: `./score.sh CartTest` (Linux/macOS) lub `.\score.ps1 CartTest` (Windows).
