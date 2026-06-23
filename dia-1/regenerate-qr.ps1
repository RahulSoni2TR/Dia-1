# regenerate-qr.ps1
#
# INSTRUCTIONS:
# 1. Place this script inside the extracted "ProductManager-Windows" folder on the client's laptop.
# 2. Make sure the main application is running (so the database is active).
# 3. Run this script via regenerate-qr.cmd (or right-click and run with PowerShell).

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JavaExe = Join-Path $ScriptDir "runtime\bin\java.exe"
$Jar = Join-Path $ScriptDir "app\dia-1.jar"
$LauncherScript = Join-Path $ScriptDir "Start-ProductManager.ps1"

function Pause-Script {
    Write-Host ""
    Read-Host "Press ENTER to exit..."
    exit
}

if (!(Test-Path $JavaExe) -or !(Test-Path $Jar)) {
    Write-Host "ERROR: Could not find java runtime or jar files." -ForegroundColor Red
    Write-Host "Please make sure this script is inside the extracted ProductManager-Windows folder." -ForegroundColor Red
    Pause-Script
}

# 1. Check if database port is active
$PortActive = $false
try {
    $tcpConnection = New-Object System.Net.Sockets.TcpClient
    $tcpConnection.Connect("127.0.0.1", 33107)
    $PortActive = $true
    $tcpConnection.Close()
} catch {
    $PortActive = $false
}

if (!$PortActive) {
    Write-Host "ERROR: Database server is not running." -ForegroundColor Red
    Write-Host "Please start the main application (ProductManager.exe) first, then run this script!" -ForegroundColor Red
    Pause-Script
}

# 2. Extract base URL from Start-ProductManager.ps1
$BaseUrl = "http://127.0.0.1:18080" # Default fallback
if (Test-Path $LauncherScript) {
    $Line = Get-Content $LauncherScript | Select-String -Pattern 'app.base-url='
    if ($Line -match 'app\.base-url=([^",\s]+)') {
        $BaseUrl = $Matches[1].Replace("`"", "").Replace("'", "").Replace(",", "").Trim()
    }
}

# Resolve dynamic $AppPort variable if present in BaseUrl
if ($BaseUrl -like '*$AppPort*') {
    $PortFile = Join-Path $ScriptDir "port.txt"
    $AppPort = 18080
    if (Test-Path $PortFile) {
        $Content = (Get-Content $PortFile).Trim()
        if ($Content -as [int] -and [int]$Content -gt 0 -and [int]$Content -lt 65536) {
            $AppPort = [int]$Content
        }
    }
    $BaseUrl = $BaseUrl.Replace('$AppPort', $AppPort)
}

$PersistentRoot = Join-Path $env:USERPROFILE ".productmanager"
$UploadsDir = Join-Path $PersistentRoot "data\uploads"
$QrDir = (Join-Path $UploadsDir "qr_codes").Replace("\", "/") + "/"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "PRODUCT MANAGER: REGENERATING QR CODES" -ForegroundColor Cyan
Write-Host "Using Base URL: $BaseUrl" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan

# 3. Run Java in CLI mode (no web server) to regenerate QR codes
& $JavaExe -jar $Jar `
    "--spring.main.web-application-type=none" `
    "--spring.datasource.url=jdbc:mariadb://127.0.0.1:33107/local?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Kolkata" `
    "--spring.datasource.username=root" `
    "--spring.datasource.password=" `
    "--spring.jpa.hibernate.ddl-auto=none" `
    "--app.base-url=$BaseUrl" `
    "--app.qr-dir=$QrDir" `
    "--app.qr-public-path=/uploads/qr_codes/" `
    "--regenerate-qrs"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "QR Code regeneration completed successfully!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan

Pause-Script
