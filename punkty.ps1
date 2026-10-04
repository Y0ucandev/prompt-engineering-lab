# Liczy zielone testy w jednej klasie testowej. Użycie: .\punkty.ps1 KoszykTest
param([string]$Klasa = "KoszykTest")
mvn -q test "-Dtest=$Klasa" "-Dsurefire.failIfNoSpecifiedTests=false" *> $null
$plik = Get-ChildItem "target/surefire-reports/TEST-*$Klasa.xml" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $plik) { Write-Host "Brak raportu — czy projekt się kompiluje? Uruchom: mvn test -Dtest=$Klasa"; exit 1 }
[xml]$x = Get-Content $plik.FullName
$t = [int]$x.testsuite.tests; $f = [int]$x.testsuite.failures; $e = [int]$x.testsuite.errors; $s = [int]$x.testsuite.skipped
Write-Host "$Klasa`: $($t - $f - $e - $s) / $t zielonych"
