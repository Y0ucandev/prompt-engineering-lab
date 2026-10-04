#!/usr/bin/env bash
# Liczy zielone testy w jednej klasie testowej. Użycie: ./punkty.sh KoszykTest
set -u
KLASA="${1:-KoszykTest}"
mvn -q test -Dtest="$KLASA" -Dsurefire.failIfNoSpecifiedTests=false >/dev/null 2>&1 || true
PLIK=$(ls target/surefire-reports/TEST-*"$KLASA".xml 2>/dev/null | head -1)
if [ -z "$PLIK" ]; then echo "Brak raportu — czy projekt się kompiluje? Uruchom: mvn test -Dtest=$KLASA"; exit 1; fi
T=$(grep -o 'tests="[0-9]*"' "$PLIK" | head -1 | grep -o '[0-9]*')
F=$(grep -o 'failures="[0-9]*"' "$PLIK" | head -1 | grep -o '[0-9]*')
E=$(grep -o 'errors="[0-9]*"' "$PLIK" | head -1 | grep -o '[0-9]*')
S=$(grep -o 'skipped="[0-9]*"' "$PLIK" | head -1 | grep -o '[0-9]*')
Z=$((T - F - E - S))
echo "$KLASA: $Z / $T zielonych"
