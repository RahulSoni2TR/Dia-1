$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildScript = Join-Path $ScriptDir "Build-WindowsBundle.ps1"
Write-Host "Starting Clean Production Build..." -ForegroundColor Cyan
& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $BuildScript -Clean
