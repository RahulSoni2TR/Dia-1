# backup.ps1
#
# INSTRUCTIONS:
# 1. Place this script inside the extracted "ProductManager-Windows" folder on the client's laptop.
# 2. Make sure your SQL backup files are in the "C:\db_temp" directory.
# 3. Open PowerShell as Administrator, navigate to this folder, and run:
#    powershell -ExecutionPolicy Bypass -File .\backup.ps1

function Pause-Script {
    Write-Host ""
    Read-Host "Press ENTER to exit..."
    exit
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MysqlExe = Join-Path $ScriptDir "mysql\bin\mysql.exe"
$SeedSql = Join-Path $ScriptDir "app\seed.sql"

if (!(Test-Path $MysqlExe)) {
    Write-Host "ERROR: Could not find mysql.exe at $MysqlExe." -ForegroundColor Red
    Write-Host "Please make sure this script is running inside the extracted ProductManager-Windows folder." -ForegroundColor Red
    Pause-Script
}

if (!(Test-Path $SeedSql)) {
    Write-Host "ERROR: Could not find seed.sql at $SeedSql." -ForegroundColor Red
    Write-Host "Please make sure this script is running inside the extracted ProductManager-Windows folder." -ForegroundColor Red
    Pause-Script
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "PRODUCT MANAGER: CLIENT SQL DATABASE RESTORE" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Drop and recreate database
Write-Host "Wiping existing database..." -ForegroundColor Yellow
& $MysqlExe --host=127.0.0.1 --port=33107 --user=root -e "DROP DATABASE IF EXISTS local; CREATE DATABASE local CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Could not connect to the database. Make sure the application (ProductManager.exe) is running!" -ForegroundColor Red
    Pause-Script
}

# 2. Recreate all table structures from seed.sql
Write-Host "Creating empty database table structures from seed.sql..." -ForegroundColor Yellow
Get-Content $SeedSql -Raw | & $MysqlExe --host=127.0.0.1 --port=33107 --user=root local
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to initialize table structures." -ForegroundColor Red
    Pause-Script
}

# 3. Empty all tables to prevent duplicate entries when importing client backup
Write-Host "Clearing default seed records..." -ForegroundColor Yellow
$TruncateSql = @(
    "SET foreign_key_checks = 0;",
    "TRUNCATE TABLE local.user_roles;",
    "TRUNCATE TABLE local.roles;",
    "TRUNCATE TABLE local.users;",
    "TRUNCATE TABLE local.categories;",
    "TRUNCATE TABLE local.products;",
    "TRUNCATE TABLE local.orders;",
    "TRUNCATE TABLE local.rates;",
    "TRUNCATE TABLE local.rate_history;",
    "TRUNCATE TABLE local.enquiry_log;",
    "TRUNCATE TABLE local.sales_log;",
    "TRUNCATE TABLE local.verification_config;",
    "SET foreign_key_checks = 1;"
) -join "`n"

$TruncateSql | & $MysqlExe --host=127.0.0.1 --port=33107 --user=root local
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to clear database tables." -ForegroundColor Red
    Pause-Script
}

# 4. Prepare SQL files list
$SqlFiles = @(
    "local_roles.sql",
    "local_users.sql",
    "local_user_roles.sql",
    "local_categories.sql",
    "local_products.sql",
    "local_orders.sql",
    "local_rates.sql",
    "local_rate_history.sql",
    "local_enquiry_log.sql",
    "local_sales_log.sql",
    "local_verification_config.sql"
)

$SqlStream = [System.Collections.Generic.List[string]]::new()
$SqlStream.Add("SET foreign_key_checks = 0;")

$SuccessCount = 0
$FailCount = 0

# 5. Read and queue each SQL file
foreach ($file in $SqlFiles) {
    $FilePath = "C:\db_temp\$file"
    if (Test-Path $FilePath) {
        Write-Host "Queuing $file for import..." -ForegroundColor Green
        $SqlStream.Add((Get-Content $FilePath -Raw))
        $SuccessCount++
    } else {
        Write-Warning "File $FilePath not found. Skipping."
        $FailCount++
    }
}

$SqlStream.Add("SET foreign_key_checks = 1;")

# 6. Pipe everything to mysql in a single session
if ($SuccessCount -gt 0) {
    Write-Host "Executing database import. Please wait..." -ForegroundColor Yellow
    $JoinedSql = $SqlStream -join "`n"
    $JoinedSql | & $MysqlExe --host=127.0.0.1 --port=33107 --user=root local
    if ($LASTEXITCODE -eq 0) {
        Write-Host "=============================================" -ForegroundColor Cyan
        Write-Host "SUCCESS: Database import completed successfully!" -ForegroundColor Green
        Write-Host "Imported: $SuccessCount tables, Skipped: $FailCount" -ForegroundColor Green
        Write-Host "=============================================" -ForegroundColor Cyan
    } else {
        Write-Host "ERROR: Database import execution failed!" -ForegroundColor Red
    }
} else {
    Write-Host "ERROR: No SQL backup files were found in C:\db_temp!" -ForegroundColor Red
}

Pause-Script
