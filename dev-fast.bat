@echo off
REM Fast dev start — skips npm install/tailwind rebuild and uses offline Maven
REM Use after first `npm run dev` has built once. Reloads in ~2s via DevTools.
echo [dev:fast] Checking :8080 ...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
  echo [dev:fast] Killing PID %%p ...
  taskkill /PID %%p /F >nul 2>&1
)
echo [dev:fast] Starting (offline, skip frontend) ...
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local -o -DskipTests -Dfrontend-maven-plugin.skip=true
