@echo off
REM Hugi Barbershop — One-click dev start (Windows CMD)
REM Kills any process on :8080 and starts Spring Boot with 'local' profile.
REM Usage: double-click dev.bat  OR  .\dev.bat  from CMD

echo [dev] Checking :8080 ...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
  echo [dev] Killing PID %%p on :8080 ...
  taskkill /PID %%p /F >nul 2>&1
)

echo [dev] Starting Spring Boot (local) ...
REM --% not needed in .bat; -D is passed straight to Maven
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
