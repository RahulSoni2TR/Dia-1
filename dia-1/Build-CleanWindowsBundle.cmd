@echo off
setlocal
echo ==========================================================
echo PRODUCT MANAGER: CLEAN PRODUCTION BUILD LAUNCHER
echo ==========================================================
echo This script will package a clean, empty distribution zip
echo for deployment on a client's system.
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0packaging\windows\Build-CleanWindowsBundle.ps1"
echo.
echo ==========================================================
echo Build process finished. Press any key to exit.
echo ==========================================================
pause
