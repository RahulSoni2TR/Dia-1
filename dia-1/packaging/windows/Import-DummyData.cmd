@echo off
setlocal
echo ==========================================================
echo PRODUCT MANAGER: DUMMY TEST DATA IMPORT
echo ==========================================================
echo This script will populate the database with 300+ sample
echo products across all categories and subcategories, complete
echo with estimate snapshots, verification statuses, and orders.
echo.
echo IMPORTANT: Make sure the Product Manager app is currently RUNNING
echo before starting this import (so the database engine is active).
echo.
echo WARNING: This will overwrite/clear existing products, sales,
echo and orders in the database.
echo.
set /p confirm="Are you sure you want to proceed? (Y/N): "
if /i "%confirm%" neq "Y" (
    echo Import cancelled.
    goto :end
)

echo.
echo Importing dummy data...
if not exist "%~dp0mysql\bin\mysql.exe" (
    echo Error: MySQL executable not found at "%~dp0mysql\bin\mysql.exe"
    goto :end
)
if not exist "%~dp0app\dummy_data.sql" (
    echo Error: Dummy SQL file not found at "%~dp0app\dummy_data.sql"
    goto :end
)

"%~dp0mysql\bin\mysql.exe" --host=127.0.0.1 --port=33107 --user=root local < "%~dp0app\dummy_data.sql"
if %ERRORLEVEL% equ 0 (
    echo.
    echo ==========================================================
    echo IMPORT SUCCESSFUL! Please refresh or restart your application.
    echo ==========================================================
) else (
    echo.
    echo ==========================================================
    echo IMPORT FAILED!
    echo Please ensure the Product Manager application is currently RUNNING.
    echo ==========================================================
)

:end
echo.
pause
