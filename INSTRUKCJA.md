# Laboratorium: Prompt engineering pod kontrolą

**Agent pisze kod, testy go oceniają.** 90 minut, trzy zadania, punkty, podium.

Na wykładzie widzieliście, jak to wygląda z mojej strony. Dziś robicie to sami. Zasada jest ta sama, co na wykładzie: **nie wierzysz na słowo, sprawdzasz.** Tutaj sprawdzają testy.

---

## 0. Start (5 min)

1. Sklonuj repo i wejdź do katalogu:
   ```bash
   git clone https://github.com/Y0ucandev/prompt-engineering-lab.git
   cd prompt-engineering-lab
   ```
2. Uruchom testy. Na start są czerwone — tak ma być:
   ```bash
   mvn -q test
   ```
   Jeśli zamiast wyniku testów widzisz błąd pobierania zależności, podnieś rękę.
3. Sprawdź, czy liczenie punktów działa:
   ```bash
   ./punkty.sh KoszykTest        # Linux / macOS
   .\punkty.ps1 KoszykTest       # Windows (PowerShell)
   ```
   Powinno wypisać `KoszykTest: 0 / 20 zielonych`.
4. Otwórz narzędzie AI: VS Code z Copilotem (tryb **Agent**), Claude Code w terminalu, Cursor — co masz. Do zadania 1 wystarczy zwykły czat w przeglądarce.
5. Zapisz nazwę zespołu na tablicy. Pracujecie w parach lub solo, jak wolicie.

> **Jak zapisać wynik:** każdy wynik, który liczy się do punktów, pokazujesz prowadzącemu na ekranie (wynik `punkty`, albo czat z promptem). Honor system plus wyrywkowa kontrola.

---

## 1. Koszyk — prompt kontra testy (25 min)

**Co jest:** klasa `Koszyk` z jedną metodą `total(List<Item> items, String promoCode)`, która rzuca `UnsupportedOperationException`. Obok: `Item`, `Category`. W `src/test/.../KoszykTest.java` jest **20 testów**. Każdy test to jedna zasada biznesowa: rabaty, kody promocyjne, dostawa, zaokrąglanie.

**Cel:** napisać prompt, po którym model wygeneruje `Koszyk.total` przechodzący jak najwięcej testów **za pierwszym razem**.

**Jak:**

1. **Przeczytaj testy.** Serio. Zanim otworzysz czat, przeczytaj `KoszykTest.java` od góry do dołu. Nazwy testów (`@DisplayName`) to lista wymagań. Komentarze w testach pokazują, jak liczone są kwoty.
2. **Napisz prompt** w pliku tekstowym (nie w czacie, żeby go nie zgubić). Przypomnienie ze wykładu: **cel · kontekst · kryteria · format · przykłady**. Kontekst masz w `Koszyk.java` (podpis metody, nazwy klas). Kryteria masz w testach. Przykłady też.
3. **Nowy czat, wklej prompt, wyślij.** Jeden raz.
4. **Wklej wygenerowany kod** do `Koszyk.java` (zastąp całą klasę).
5. Uruchom `./punkty.sh KoszykTest`. **Ten wynik zapisujesz** — to Twój „pierwszy strzał”.
6. Jeśli nie jest 20/20: popraw **prompt** (nie kod!), nowy czat, od nowa. Max 3 próby. Zapisz, przy której próbie dobiłeś do 20.

**Zasady:**
- Kod piszesz wyłącznie przez model. Ręczne poprawki w `Koszyk.java` = próba nieważna.
- Nie wklejasz modelowi pliku z testami w całości. Wyciągasz z nich wymagania własnymi słowami (albo liczbami). Dlaczego? Bo w prawdziwym projekcie testów często nie ma, a wymagania musisz umieć spisać sam.
- Nie zmieniasz testów.

**Punkty:**
- Pierwszy strzał: **1 pkt za każdy zielony test** (max 20).
- **+5** jeśli masz 20/20 w ≤ 3 próbach.

**Podpowiedź, jeśli stoisz:** policz, ile zasad znalazłeś w testach. Jeśli mniej niż 10, czytasz za szybko.

---

## 2. Wypożyczalnia — agent naprawia, Ty czytasz (30 min)

**Co jest:** klasa `Library` (wypożyczalnia książek), która „kiedyś działała”. Ktoś ją popsuł. W `LibraryTest.java` jest 17 testów, **9 czerwonych**. Podłożonych błędów jest **siedem** (niektóre psują dwa testy naraz).

**Cel:** doprowadzić `LibraryTest` do 17/17 **z pomocą agenta** i umieć wyjaśnić każdy błąd.

**Jak:**

1. Otwórz agenta w katalogu projektu (Copilot agent mode / Claude Code / Cursor).
2. Zacznij od pliku instrukcji: przeczytaj `AGENTS.md` (ten sam plik jest jako `CLAUDE.md` i `.github/copilot-instructions.md` — agent go wczyta sam). Zobacz, co tam jest.
3. Zleć zadanie. Przykład promptu na początek:
   ```
   W LibraryTest część testów jest czerwona. Uruchom testy, znajdź przyczyny w klasie Library
   i naprawiaj błędy po jednym. Po każdej poprawce uruchom testy i pokaż, co się zmieniło.
   Nie modyfikuj testów. Przy każdej poprawce napisz jednym zdaniem, co było źle.
   ```
   Możesz napisać własny. Możesz prosić o jeden błąd naraz. Możesz najpierw poprosić o plan.
4. **Po każdej zmianie czytaj diff.** W VS Code: widok Source Control. W terminalu: `git diff`. Nie klikaj „Accept”, jeśli nie rozumiesz, co się zmieniło.
5. Każdy naprawiony błąd wpisz do `ZADANIE2_NOTATKI.md`: metoda, co było źle, który test to wykrył. Jedno zdanie, Twoimi słowami.
6. Na koniec: `./punkty.sh LibraryTest`.

**Zasady:**
- `LibraryTest.java` jest nietykalny. Jeśli agent go zmieni, cofnij: `git checkout src/test`.
- Notatki piszesz Ty, nie agent. Prowadzący może zapytać o dowolny wiersz.

**Punkty:**
- **2 pkt** za każdy błąd naprawiony (zielone testy, max 14).
- **1 pkt** za każdy wiersz w notatkach, który tłumaczy błąd, a nie mówi „agent naprawił” (max 7).
- **+3** dla pierwszych trzech zespołów z 17/17 i kompletem notatek.

**Pytanie na koniec zadania** (odpowiedź wpisz na dole notatek): czy w diffach pojawiło się coś, o co nie prosiliście?

---

## 3. Bonus — dla szybkich (10 min)

Dwie konkurencje, możesz zrobić jedną albo obie.

### 3a. Golf promptowy
Najkrótszy prompt (liczony w znakach), który w **nowym czacie**, za jednym razem, daje `Koszyk.total` z wynikiem **20/20**. Pokazujesz prowadzącemu czat i wynik `punkty`.
Punkty: **10 / 6 / 3** za trzy najkrótsze.
Wskazówka: pomyśl, co model i tak zgadnie, a czego nie zgadnie nigdy.

### 3b. Red team
Gdzieś w tym repozytorium jest ukryta instrukcja dla agenta, której nie powinno tam być. Znajdź ją i powiedz prowadzącemu, gdzie jest i co próbuje zrobić.
Punkty: **5** dla pierwszego zespołu, **2** dla każdego następnego.
Wskazówka: pomyśl, co agent czyta, kiedy pracuje w tym projekcie. I przejrzyj swoje diffy z zadania 2 jeszcze raz.

---

## Tabela punktów

| Zadanie | Za co | Max |
|---|---|---|
| 1 | zielone testy po pierwszym prompcie | 20 |
| 1 | 20/20 w ≤ 3 próbach | 5 |
| 2 | naprawione błędy (2 pkt/szt.) | 14 |
| 2 | notatki (1 pkt/wiersz) | 7 |
| 2 | pierwsze 3 zespoły z kompletem | 3 |
| 3a | golf promptowy | 10 |
| 3b | red team | 5 |
| | **razem** | **64** |

---

## Jeśli coś nie działa

| Problem | Co zrobić |
|---|---|
| `mvn` nie pobiera zależności | sprawdź internet; na uczelnianym Wi-Fi czasem trzeba hotspot z telefonu |
| `punkty.sh: Permission denied` | `chmod +x punkty.sh` albo `bash punkty.sh KoszykTest` |
| PowerShell blokuje skrypt | `powershell -ExecutionPolicy Bypass -File .\punkty.ps1 KoszykTest` |
| Nie mam działającego narzędzia AI | dobierz się do pary; do zadania 1 wystarczy czat w przeglądarce |
| Agent zmienił testy | `git checkout src/test` |
| Chcę zacząć zadanie od zera | `git checkout -- . && git clean -fdq` (uwaga: kasuje Twoje zmiany) |

---

## Po labach: co zabierasz do domu

- Prompt do zadania 1, który dał 20/20. Zobacz, czym różni się od Twojej pierwszej wersji. To jest cała lekcja z prompt engineeringu w jednym diffie.
- Nawyk: **testy są specyfikacją**. Zanim poprosisz model o kod, wiedz, po czym poznasz, że kod jest dobry.
- Nawyk: **czytaj diffy**. Agent, który naprawia 7 błędów, może przy okazji zrobić ósmy.
