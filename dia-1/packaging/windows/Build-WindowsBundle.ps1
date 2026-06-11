$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$BundleRoot = Join-Path $RepoRoot "dist\ProductManager-Windows"
$MysqlSource = "C:\Program Files\MySQL\MySQL Server 8.0"
$UploadsSource = "C:\Users\RAHUL\Downloads\uploads"
$StaticDir = Join-Path $RepoRoot "src\main\resources\static"
$FrontendDist = Join-Path $RepoRoot "frontend\dist"

function Ensure-Directory($Path) {
    if (!(Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

if (!(Test-Path $MysqlSource)) {
    throw "MySQL Server was not found at $MysqlSource. Install MySQL Server 8.0 or update MysqlSource in this script."
}

Push-Location (Join-Path $RepoRoot "frontend")
& cmd.exe /c npm run build
if ($LASTEXITCODE -ne 0 -and !(Test-Path (Join-Path $FrontendDist "index.html"))) {
    throw "Frontend build failed."
} elseif ($LASTEXITCODE -ne 0) {
    Write-Warning "Frontend build was blocked in the bundle script; using existing frontend\dist output."
}
Pop-Location

Ensure-Directory $StaticDir
Copy-Item (Join-Path $FrontendDist "index.html") (Join-Path $StaticDir "index.html") -Force
Ensure-Directory (Join-Path $StaticDir "assets")
Copy-Item (Join-Path $FrontendDist "assets\*") (Join-Path $StaticDir "assets") -Recurse -Force

Push-Location $RepoRoot
& .\mvnw.cmd -DskipTests package
if ($LASTEXITCODE -ne 0) {
    throw "Backend build failed."
}
Pop-Location

if (Test-Path $BundleRoot) {
    # Delete build artifacts app and launcher, but keep data, logs, mysql, and runtime to avoid file locks
    foreach ($dir in @("app", "launcher")) {
        $subDir = Join-Path $BundleRoot $dir
        if (Test-Path $subDir) {
            Remove-Item $subDir -Recurse -Force
        }
    }
} else {
    Ensure-Directory $BundleRoot
    Ensure-Directory (Join-Path $BundleRoot "data")
    Ensure-Directory (Join-Path $BundleRoot "logs")
}
Ensure-Directory (Join-Path $BundleRoot "app")
Ensure-Directory (Join-Path $BundleRoot "launcher")

Copy-Item (Join-Path $PSScriptRoot "ProductManager.cmd") (Join-Path $BundleRoot "ProductManager.cmd") -Force
Copy-Item (Join-Path $PSScriptRoot "launcher\Start-ProductManager.ps1") (Join-Path $BundleRoot "launcher\Start-ProductManager.ps1") -Force
Copy-Item (Join-Path $RepoRoot "target\your-project-name-0.0.1-SNAPSHOT.jar") (Join-Path $BundleRoot "app\dia-1.jar") -Force

$RuntimeDir = Join-Path $BundleRoot "runtime"
if (!(Test-Path $RuntimeDir)) {
    Write-Host "Creating Java runtime using jlink..."
    $JavaCommand = Get-Command java
    $JavaHome = Split-Path -Parent (Split-Path -Parent $JavaCommand.Source)
    $JmodsPath = Join-Path $JavaHome "jmods"
    & jlink --module-path $JmodsPath --add-modules ALL-MODULE-PATH --output $RuntimeDir --strip-debug --no-header-files --no-man-pages
    if ($LASTEXITCODE -ne 0) {
        throw "Java runtime creation failed."
    }
} else {
    Write-Host "Java runtime already exists. Skipping recreation."
}

$MysqlDest = Join-Path $BundleRoot "mysql"
if (!(Test-Path $MysqlDest)) {
    Write-Host "Copying MySQL Server binaries from source..."
    Copy-Item $MysqlSource $MysqlDest -Recurse -Force
} else {
    Write-Host "MySQL Server binaries already exist. Skipping copying."
}

if (Test-Path $UploadsSource) {
    Copy-Item $UploadsSource (Join-Path $BundleRoot "data\uploads") -Recurse -Force
}

$DumpFile = Join-Path $BundleRoot "app\seed.sql"
$DumpExe = Join-Path $MysqlSource "bin\mysqldump.exe"
& $DumpExe --host=127.0.0.1 --port=3306 --user=root --password=new_password --databases local --routines --triggers --events --single-transaction --result-file="$DumpFile"
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Could not export the current MySQL database. The bundle was created without seed data."
    if (Test-Path $DumpFile) {
        Remove-Item $DumpFile -Force
    }
}

Get-ChildItem -Path $BundleRoot -Exclude "data", "logs" | Compress-Archive -DestinationPath (Join-Path $RepoRoot "dist\ProductManager-Windows.zip") -Force

Write-Host "Bundle created:"
Write-Host $BundleRoot
Write-Host (Join-Path $RepoRoot "dist\ProductManager-Windows.zip")
