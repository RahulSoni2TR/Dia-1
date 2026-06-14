param(
    [switch]$NoUi,
    [int]$SmokeTestSeconds = 0
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$RuntimeJava = Join-Path $Root "runtime\bin\java.exe"
$JavaExe = if (Test-Path $RuntimeJava) { $RuntimeJava } else { "java" }
$MysqlBin = Join-Path $Root "mysql\bin"
$Mysqld = Join-Path $MysqlBin "mysqld.exe"
$Mysql = Join-Path $MysqlBin "mysql.exe"
$MysqlAdmin = Join-Path $MysqlBin "mysqladmin.exe"
$Jar = Join-Path $Root "app\dia-1.jar"
$SeedSql = Join-Path $Root "app\seed.sql"
$PersistentRoot = Join-Path $env:USERPROFILE ".productmanager"
$DataDir = Join-Path $PersistentRoot "data\mysql"
$UploadsDir = Join-Path $PersistentRoot "data\uploads"
$QrDir = (Join-Path $UploadsDir "qr_codes").Replace("\", "/") + "/"
$BackupDir = Join-Path $PersistentRoot "data\backups"
$LogsDir = Join-Path $PersistentRoot "logs"
$MysqlPort = 33107
$AppPort = 18080
$AppUrl = "http://127.0.0.1:$AppPort"

function Ensure-Directory($Path) {
    if (!(Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

# Migrate data from old application folder if it exists
$OldData = Join-Path $Root "data"
$OldLogs = Join-Path $Root "logs"

if (Test-Path $OldData) {
    if (!(Test-Path $PersistentRoot)) {
        Ensure-Directory $PersistentRoot
    }
    $NewData = Join-Path $PersistentRoot "data"
    if (!(Test-Path $NewData)) {
        try {
            Write-Host "Migrating existing database and upload files to persistent user profile location..."
            Copy-Item -Path $OldData -Destination $NewData -Recurse -Force
            Write-Host "Migration complete."
        } catch {
            Write-Warning "Could not copy data to persistent location: $_. Please ensure no other instances of the app are running."
        }
    }
    if (Test-Path $NewData) {
        try {
            Rename-Item -Path $OldData -NewName "data_migrated_backup" -Force
            Write-Host "Renamed old data folder to data_migrated_backup."
        } catch {
            Write-Warning "Could not rename old data folder (it may be locked by a running process): $_"
        }
    }
}
if (Test-Path $OldLogs) {
    if (!(Test-Path $PersistentRoot)) {
        Ensure-Directory $PersistentRoot
    }
    $NewLogs = Join-Path $PersistentRoot "logs"
    if (!(Test-Path $NewLogs)) {
        try {
            Write-Host "Migrating logs..."
            Copy-Item -Path $OldLogs -Destination $NewLogs -Recurse -Force
        } catch {
            Write-Warning "Could not copy logs: $_"
        }
    }
    if (Test-Path $NewLogs) {
        try {
            Rename-Item -Path $OldLogs -NewName "logs_migrated_backup" -Force
        } catch {
            Write-Warning "Could not rename old logs folder: $_"
        }
    }
}

function Wait-ForMysql {
    for ($i = 0; $i -lt 60; $i++) {
        & $MysqlAdmin --host=127.0.0.1 --port=$MysqlPort --user=root ping 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            return
        }
        Start-Sleep -Seconds 1
    }
    throw "MySQL did not start on port $MysqlPort."
}

function Wait-ForApp {
    for ($i = 0; $i -lt 90; $i++) {
        try {
            Invoke-WebRequest -Uri $AppUrl -UseBasicParsing -TimeoutSec 2 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "Product Manager did not start on port $AppPort."
}

function Find-Edge {
    $candidates = @(
        "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe",
        "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
        "$env:LocalAppData\Microsoft\Edge\Application\msedge.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }
    return $null
}

function Start-AppProcess($FilePath, $Arguments, $OutLog, $ErrLog) {
    $argString = ($Arguments | ForEach-Object { ConvertTo-QuotedArgument $_ }) -join " "
    $params = @{
        FilePath = $FilePath
        ArgumentList = $argString
        NoNewWindow = $true
        PassThru = $true
    }
    if ($OutLog) {
        $params.RedirectStandardOutput = $OutLog
    }
    if ($ErrLog) {
        $params.RedirectStandardError = $ErrLog
    }
    return Start-Process @params
}

function ConvertTo-QuotedArgument($Argument) {
    $value = [string]$Argument
    if ($value -notmatch '[\s"]') {
        return $value
    }
    return '"' + ($value -replace '"', '\"') + '"'
}

Ensure-Directory $DataDir
Ensure-Directory $UploadsDir
Ensure-Directory $QrDir
Ensure-Directory $BackupDir
Ensure-Directory $LogsDir

$MysqlLog = Join-Path $LogsDir "mysql.log"
$MysqlErrLog = Join-Path $LogsDir "mysql-error.log"
$AppLog = Join-Path $LogsDir "app.log"
$AppErrLog = Join-Path $LogsDir "app-error.log"

if (!(Test-Path $Mysqld)) {
    throw "Bundled MySQL was not found at $Mysqld."
}
if (!(Test-Path $Jar)) {
    throw "Backend jar was not found at $Jar."
}

$InitializedMarker = Join-Path $DataDir ".initialized"
if (!(Test-Path $InitializedMarker)) {
    & $Mysqld --initialize-insecure --basedir="$Root\mysql" --datadir="$DataDir" --console
    if ($LASTEXITCODE -ne 0) {
        throw "Could not initialize bundled MySQL data folder."
    }
    New-Item -ItemType File -Path $InitializedMarker | Out-Null
}

$MysqlArgs = @(
    "--no-defaults",
    "--standalone",
    "--console",
    "--basedir=$Root\mysql",
    "--datadir=$DataDir",
    "--port=$MysqlPort",
    "--bind-address=127.0.0.1",
    "--skip-log-bin",
    "--character-set-server=utf8mb4",
    "--collation-server=utf8mb4_0900_ai_ci"
)
$MysqlProcess = Start-AppProcess $Mysqld $MysqlArgs $MysqlLog $MysqlErrLog

try {
    Wait-ForMysql
    & $Mysql --host=127.0.0.1 --port=$MysqlPort --user=root --execute="CREATE DATABASE IF NOT EXISTS local CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
    if ($LASTEXITCODE -ne 0) {
        throw "Could not create local database."
    }

    $SeedMarker = Join-Path $DataDir ".seed-imported"
    if ((Test-Path $SeedSql) -and !(Test-Path $SeedMarker)) {
        Get-Content $SeedSql | & $Mysql --host=127.0.0.1 --port=$MysqlPort --user=root local
        if ($LASTEXITCODE -eq 0) {
            New-Item -ItemType File -Path $SeedMarker | Out-Null
        }
    }

    $UploadsUri = ($UploadsDir -replace "\\", "/")
    if (!$UploadsUri.EndsWith("/")) {
        $UploadsUri = "$UploadsUri/"
    }
    $BackendArgs = @(
        "-jar", $Jar,
        "--server.address=0.0.0.0",
        "--server.port=$AppPort",
        "--spring.datasource.url=jdbc:mysql://127.0.0.1:$MysqlPort/local?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Kolkata",
        "--spring.datasource.username=root",
        "--spring.datasource.password=",
        "--spring.jpa.hibernate.ddl-auto=none",
        "--spring.web.resources.static-locations=classpath:/static/,file:/$UploadsUri",
        "--app.server.host=192.168.1.50",
        "--app.server.port=$AppPort",
        "--app.base-url=http://192.168.1.50:$AppPort",
        "--app.qr-dir=$QrDir",
        "--app.qr-public-path=/uploads/qr_codes/",
        "--app.uploads.path=file:/$UploadsUri",
        "--save.uploads.path=$UploadsDir",
        "--delete.baseUploadDir.path=$UploadsDir",
        "--app.backup.dir=$BackupDir",
        "--spring.cache.type=simple"
    )
    $BackendProcess = Start-AppProcess $JavaExe $BackendArgs $AppLog $AppErrLog

    Wait-ForApp

    if ($NoUi) {
        Write-Host "Product Manager started at $AppUrl"
        if ($SmokeTestSeconds -gt 0) {
            Start-Sleep -Seconds $SmokeTestSeconds
        }
        return
    }

    $Edge = Find-Edge
    if ($Edge) {
        $EdgeProfile = Join-Path $Root "data\edge-profile"
        Ensure-Directory $EdgeProfile
        $FaviconCache = Join-Path $EdgeProfile "Default\Favicons"
        $FaviconJournal = Join-Path $EdgeProfile "Default\Favicons-journal"
        if (Test-Path $FaviconCache) { Remove-Item $FaviconCache -Force -ErrorAction SilentlyContinue }
        if (Test-Path $FaviconJournal) { Remove-Item $FaviconJournal -Force -ErrorAction SilentlyContinue }
        $EdgeProcess = Start-Process -FilePath $Edge -ArgumentList @("--app=$AppUrl", "--user-data-dir=$EdgeProfile") -PassThru
        Start-Sleep -Seconds 3
        if ($EdgeProcess.HasExited) {
            # Edge exited too quickly (likely delegated to an existing background instance)
            Write-Host "Product Manager is running at $AppUrl"
            Write-Host "Press ENTER in this window to stop the application..." -ForegroundColor Yellow
            Read-Host
        } else {
            # Edge is running as a dedicated process, wait for it to close
            Wait-Process -Id $EdgeProcess.Id
        }
    } else {
        Start-Process $AppUrl
        Write-Host "Product Manager is running at $AppUrl"
        Write-Host "Close this window to stop the app."
        Read-Host
    }
} finally {
    if ($BackendProcess -and !$BackendProcess.HasExited) {
        Stop-Process -Id $BackendProcess.Id -Force
    }
    & $MysqlAdmin --host=127.0.0.1 --port=$MysqlPort --user=root shutdown 2>$null | Out-Null
    if ($MysqlProcess -and !$MysqlProcess.HasExited) {
        Stop-Process -Id $MysqlProcess.Id -Force
    }
}
