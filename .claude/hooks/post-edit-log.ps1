# post-edit-log.ps1 — Log every edited file (Windows PowerShell 5.1+).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$toolName = if ($obj.tool_name) { [string]$obj.tool_name } else { 'unknown' }
$filePath = 'unknown'
if ($obj.tool_input) {
    if ($obj.tool_input.file_path)  { $filePath = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)   { $filePath = [string]$obj.tool_input.path }
}

$ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'

$projectRoot = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { (Get-Location).Path }
$logDir = Join-Path $projectRoot '.claude'
$logFile = Join-Path $logDir 'edit-history.log'

if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

Add-Content -Path $logFile -Value "$ts | $toolName | $filePath" -Encoding utf8

Write-Output '{"continue": true}'
exit 0
