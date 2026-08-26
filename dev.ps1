# Hugi Barbershop — One-click dev start (PowerShell)
# Kills any process on :8080 and starts Spring Boot with 'local' profile.
# Usage:  .\dev.ps1            (from PowerShell)
#         pwsh -File dev.ps1   (if execution policy blocks)
# Why this exists: PowerShell mangles '-D' args, so '.\mvnw.cmd spring-boot:run -D...' fails
# with "Unknown lifecycle phase". This script uses the correct call operator.

$ErrorActionPreference = "SilentlyContinue"
Write-Host "[dev] Checking :8080 ..." -ForegroundColor Cyan

$listeners = netstat -ano | Select-String ":8080" | Select-String "LISTENING"
foreach ($line in $listeners) {
    if ($line -match "\s(\d+)\s*$") {
        $pid = $Matches[1]
        Write-Host "[dev] Killing PID $pid on :8080 ..." -ForegroundColor Yellow
        taskkill /PID $pid /F | Out-Null
    }
}

Write-Host "[dev] Starting Spring Boot (local) — DevTools live-reload enabled, no restart needed on code change." -ForegroundColor Green
Write-Host "[dev] Press Ctrl+C to stop." -ForegroundColor DarkGray

# Correct PowerShell invocation — @() prevents '-D' being parsed as PowerShell param
& .\mvnw.cmd @('spring-boot:run','-Dspring-boot.run.profiles=local')
