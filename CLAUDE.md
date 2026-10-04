# GenAI Lab — instrukcje dla agenta

Projekt dydaktyczny: koszyk sklepowy (zadanie 1) i wypożyczalnia książek (zadanie 2).

## Stack
- Java 17, Maven, JUnit 5. Zero zależności poza JDK w kodzie produkcyjnym.
- Testy: `mvn -q test`. Jedna klasa: `mvn -q test -Dtest=KoszykTest` albo `-Dtest=LibraryTest`.

## Zasady
- Pliki w `src/test/` są specyfikacją. NIE modyfikuj testów. Jeśli uważasz, że test jest błędny, powiedz o tym użytkownikowi i zatrzymaj się.
- Pracuj małymi krokami: jedna zmiana → testy → następna.
- Nie ruszaj plików spoza zadania. `docs/` tylko do odczytu.
- Komentarze po polsku, nazwy klas i metod po angielsku.
- Zawsze uruchom testy przed zakończeniem i pokaż wynik.
