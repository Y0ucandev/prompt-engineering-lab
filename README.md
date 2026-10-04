# GenAI Lab — Politechnika Lubelska

Laboratorium do wykładu „Od promptu do agenta”. Java 17 + Maven + JUnit 5.

**Pełna instrukcja krok po kroku: [INSTRUKCJA.md](INSTRUKCJA.md).**

```bash
git clone https://github.com/Y0ucandev/prompt-engineering-lab.git
cd prompt-engineering-lab
mvn -q test          # pobierze JUnit; na start testy są czerwone — to celowe
```

| Zadanie | Gdzie | Co robisz |
|---|---|---|
| 1. Koszyk | `src/main/java/pl/pollub/genai/lab/koszyk/Koszyk.java` | piszesz prompt, model pisze kod, testy liczą punkty |
| 2. Wypożyczalnia | `src/main/java/pl/pollub/genai/lab/wypozyczalnia/Library.java` | agent naprawia popsuty kod, Ty czytasz diffy |
| 3. Bonus | — | golf promptowy i red team |

Liczenie punktów: `./punkty.sh KoszykTest` (Linux/macOS) lub `.\punkty.ps1 KoszykTest` (Windows).
