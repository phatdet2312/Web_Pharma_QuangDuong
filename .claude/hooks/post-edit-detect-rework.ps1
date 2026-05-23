# post-edit-detect-rework.ps1 — Detect file edited >3 times in 30 min (Windows PowerShell 5.1+).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$toolName = if ($obj.tool_name) { [string]$obj.tool_name } else { '' }
if ($toolName -notmatch '^(Edit|Write|MultiEdit)$') {
    Write-Output '{"continue": true}'
    exit 0
}

$filePath = ''
if ($obj.tool_input) {
    if ($obj.tool_input.file_path)  { $filePath = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)   { $filePath = [string]$obj.tool_input.path }
}

if ([string]::IsNullOrWhiteSpace($filePath) `
    -or $filePath -like '*.ai-memory/*' `
    -or $filePath -like '*.claude/*' `
    -or $filePath -like '*.agents/*' `
    -or $filePath -like '*.log') {
    Write-Output '{"continue": true}'
    exit 0
}

$projectDir = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { (Get-Location).Path }
$historyFile = Join-Path $projectDir '.claude/edit-history.log'
$alertFile   = Join-Path $projectDir '.claude/rework-alerts.log'

if (-not (Test-Path $historyFile)) {
    Write-Output '{"continue": true}'
    exit 0
}

$dir = Split-Path $alertFile -Parent
if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

$threshold = (Get-Date).AddMinutes(-30)
$tail = @()
try { $tail = Get-Content -Tail 200 $historyFile -ErrorAction SilentlyContinue } catch { }

$count = 0
foreach ($line in $tail) {
    $parts = $line -split '\|', 4
    if ($parts.Count -lt 3) { continue }
    $tsStr = $parts[0].Trim()
    $path  = $parts[2].Trim()
    if ($path -ne $filePath) { continue }
    try { $tsDt = [DateTime]::Parse($tsStr) } catch { continue }
    if ($tsDt -gt $threshold) { $count++ }
}

if ($count -gt 3) {
    $now = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Add-Content -Path $alertFile -Value "$now | REWORK_ALERT | $filePath | edited $count times in 30min | hint: consider /reflect" -Encoding utf8
}

Write-Output '{"continue": true}'
exit 0
