@echo off
setlocal
cd /d "%~dp0"
echo ==========================================================
echo PRODUCT MANAGER: LAUNCHING DATABASE RESTORE
echo ==========================================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0backup.ps1"
echo.
echo Process finished.
pause
