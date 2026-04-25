@echo off
:: ============================================================
::  RunAll.bat  –  Build and Run the Multiplayer Quiz System
::  This script compiles, packages, and starts both server
::  and client in separate windows.
:: ============================================================

echo.
echo  ╔════════════════════════════════════════╗
echo  ║   Multiplayer Quiz  –  Run All Tool    ║
echo  ╚════════════════════════════════════════╝
echo.

:: ── 1. Build the project ──────────────────────────────────────────────────────
echo [Step 1/3] Building project...
call build.bat

if %errorlevel% neq 0 (
    echo.
    echo  ERROR: Build failed. Please check the errors above.
    pause
    exit /b 1
)

echo.
echo [Step 2/3] Starting QuizServer in a new window...
start "Quiz Server" cmd /k "java -jar QuizServer.jar"

echo [Step 3/3] Starting QuizClient in a new window...
timeout /t 2 /nobreak
start "Quiz Client" cmd /k "java -jar QuizClient.jar"

echo.
echo  ✅ All done!
echo     - Server started in "Quiz Server" window
echo     - Client started in "Quiz Client" window
echo.
pause
