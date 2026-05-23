# setup.ps1 — Chuyen settings.json sang PowerShell/.ps1 hooks (Windows khong co bash).
# Chay 1 lan duy nhat: .\setup.ps1
# Neu may co bash va van muon doi: .\setup.ps1 -Force
param([switch]$Force)
$ErrorActionPreference = 'Stop'

$scriptDir = $PSScriptRoot
if (-not $scriptDir) { $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
$settingsPath = Join-Path $scriptDir 'settings.json'

if (-not (Test-Path $settingsPath)) {
    Write-Host "[ERROR] Khong tim thay: $settingsPath" -ForegroundColor Red
    exit 1
}

$raw = Get-Content $settingsPath -Raw -Encoding UTF8

if ($raw -notmatch '"command":\s*"bash"') {
    Write-Host "[OK] settings.json da dung PowerShell/.ps1. Khong can doi." -ForegroundColor Green
    exit 0
}

$hasBash = $null -ne (Get-Command bash -ErrorAction SilentlyContinue)
if ($hasBash -and -not $Force) {
    Write-Host "[INFO] bash co san tren may nay -> settings.json hien tai (bash/.sh) da dung." -ForegroundColor Green
    Write-Host "[INFO] Neu van muon doi sang PowerShell, chay: .\.claude\setup.ps1 -Force" -ForegroundColor Yellow
    exit 0
}

Copy-Item $settingsPath "${settingsPath}.backup" -Force
Write-Host "[OK] Backup: ${settingsPath}.backup"

$result = $raw -replace '"command":\s*"bash"', '"command": "powershell"'
$result = $result -replace '"args":\s*\["([^"]+)\.sh"\]', '"args": ["-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "$1.ps1"]'

[System.IO.File]::WriteAllText($settingsPath, $result, (New-Object System.Text.UTF8Encoding $false))

$count = ([regex]::Matches($result, '"command":\s*"powershell"')).Count
Write-Host "[OK] Da chuyen $count hook(s): bash/.sh -> PowerShell/.ps1" -ForegroundColor Green
Write-Host "[OK] settings.json da cap nhat. Claude Code se dung .ps1 hooks." -ForegroundColor Green
