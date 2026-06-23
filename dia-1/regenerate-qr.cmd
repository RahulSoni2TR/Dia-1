@echo off
setlocal
cd /d "%~dp0"
echo ==========================================================
echo PRODUCT MANAGER: LAUNCHING QR REGENERATION
echo ==========================================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0regenerate-qr.ps1"
echo.
echo Process finished.
pause
