@echo off
REM Script para iniciar todos os servicos em desenvolvimento
REM Uso: scripts\start-all.bat

echo ========================================
echo   Ticket System - Starting All Services
echo ========================================
echo.

REM Definir diretorio raiz
set ROOT_DIR=%~dp0..

echo Starting Backend Services...
echo.

REM Iniciar servicos em janelas separadas
echo Starting Users Service on port 8081...
start "Users Service" cmd /c "cd /d %ROOT_DIR%\services\users && gradle run"

echo Starting Events Service on port 8082...
start "Events Service" cmd /c "cd /d %ROOT_DIR%\services\events && gradle run"

echo Starting Partners Service on port 8083...
start "Partners Service" cmd /c "cd /d %ROOT_DIR%\services\partners && gradle run"

echo Starting Tickets Service on port 8084...
start "Tickets Service" cmd /c "cd /d %ROOT_DIR%\services\tickets && gradle run"

echo Starting Orders Service on port 8085...
start "Orders Service" cmd /c "cd /d %ROOT_DIR%\services\orders && gradle run"

echo Starting Reservations Service on port 8086...
start "Reservations Service" cmd /c "cd /d %ROOT_DIR%\services\reservations && gradle run"

echo.
echo Waiting for services to start (15 seconds)...
timeout /t 15 /nobreak > nul

echo Starting BFF on port 8080...
start "BFF" cmd /c "cd /d %ROOT_DIR%\bff && gradle run"

echo.
echo Waiting for BFF to start (10 seconds)...
timeout /t 10 /nobreak > nul

echo Starting UI on port 3000...
start "UI - Next.js" cmd /c "cd /d %ROOT_DIR%\ui && npm install && npm run dev"

echo.
echo ========================================
echo   All Services Started!
echo ========================================
echo.
echo Services running:
echo   - Users Service:        http://localhost:8081
echo   - Events Service:       http://localhost:8082
echo   - Partners Service:     http://localhost:8083
echo   - Tickets Service:      http://localhost:8084
echo   - Orders Service:       http://localhost:8085
echo   - Reservations Service: http://localhost:8086
echo   - BFF:                  http://localhost:8080
echo   - UI:                   http://localhost:3000
echo.
echo Close this window or press any key to continue...
pause > nul
