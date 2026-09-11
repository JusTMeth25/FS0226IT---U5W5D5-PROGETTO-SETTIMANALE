@echo off
rem Avvia backend (Spring Boot, porta 8080) e frontend (Vite, porta 5173) in due finestre separate.
setlocal
set "ROOT=%~dp0"

if not exist "%ROOT%BE\env.properties" (
  echo [ERRORE] Manca BE\env.properties. Copia BE\env.properties.example e inserisci le credenziali del DB.
  pause
  exit /b 1
)

start "Camera Oscura - BE" /D "%ROOT%BE" cmd /k "mvnw.cmd spring-boot:run"

if not exist "%ROOT%FEJSX\node_modules" (
  echo Installazione dipendenze frontend...
  pushd "%ROOT%FEJSX"
  call npm install --no-audit --no-fund
  popd
)

rem --open apre il browser su http://localhost:5173 quando Vite e' pronto
start "Camera Oscura - FE" /D "%ROOT%FEJSX" cmd /k "npm run dev -- --open"

echo Backend e frontend avviati in due finestre. Chiudile per fermarli.
endlocal
