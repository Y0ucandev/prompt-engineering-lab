#!/usr/bin/env bash
# Liczy zielone testy w jednej klasie testowej. Użycie: ./score.sh CartTest
set -u
CLASS="${1:-CartTest}"
mvn -q test -Dtest="$CLASS" -Dsurefire.failIfNoSpecifiedTests=false >/dev/null 2>&1 || true
FILE=$(ls target/surefire-reports/TEST-*"$CLASS".xml 2>/dev/null | head -1)
if [ -z "$FILE" ]; then echo "Brak raportu — czy projekt się kompiluje? Uruchom: mvn test -Dtest=$CLASS"; exit 1; fi
T=$(grep -o 'tests="[0-9]*"' "$FILE" | head -1 | grep -o '[0-9]*')
F=$(grep -o 'failures="[0-9]*"' "$FILE" | head -1 | grep -o '[0-9]*')
E=$(grep -o 'errors="[0-9]*"' "$FILE" | head -1 | grep -o '[0-9]*')
S=$(grep -o 'skipped="[0-9]*"' "$FILE" | head -1 | grep -o '[0-9]*')
Z=$((T - F - E - S))
echo "$CLASS: $Z / $T zielonych"
