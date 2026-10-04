# Liczy zielone testy w jednej klasie testowej. Użycie: .\score.ps1 CartTest
param([string]$TestClass = "CartTest")
mvn -q test "-Dtest=$TestClass" "-Dsurefire.failIfNoSpecifiedTests=false" *> $null
$report = Get-ChildItem "target/surefire-reports/TEST-*$TestClass.xml" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $report) { Write-Host "Brak raportu — czy projekt się kompiluje? Uruchom: mvn test -Dtest=$TestClass"; exit 1 }
[xml]$x = Get-Content $report.FullName
$t = [int]$x.testsuite.tests; $f = [int]$x.testsuite.failures; $e = [int]$x.testsuite.errors; $s = [int]$x.testsuite.skipped
Write-Host "$TestClass`: $($t - $f - $e - $s) / $t zielonych"
