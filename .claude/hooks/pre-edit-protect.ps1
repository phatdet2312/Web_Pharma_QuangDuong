# pre-edit-protect.ps1 — Block edits to protected files (Windows PowerShell 5.1+).
# Reads protected patterns from .claudeignore (# protected-edit tag).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$hookEvent = if ($obj.hook_event_name) { [string]$obj.hook_event_name } else { 'PreToolUse' }
$target = ''
if ($obj.tool_input) {
    if ($obj.tool_input.file_path) { $target = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)  { $target = [string]$obj.tool_input.path }
}

if ([string]::IsNullOrWhiteSpace($target)) {
    Write-Output '{"continue": true}'
    exit 0
}

$projectRoot = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { (Get-Location).Path }
$policyFile = Join-Path $projectRoot '.claudeignore'
$protectedFiles = @()

if (Test-Path -LiteralPath $policyFile) {
    foreach ($line in Get-Content -LiteralPath $policyFile) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
        if ($line -match '#\s*(sensitive|protected-edit)\b') {
            $pattern = ($line -split '#', 2)[0].Trim()
            if (-not [string]::IsNullOrWhiteSpace($pattern)) {
                $protectedFiles += $pattern
            }
        }
    }
}

if ($protectedFiles.Count -eq 0) {
    $protectedFiles = @('.env', '.env.*', '.claude/settings.json', '.claude/settings.local.json', '.claudeignore')
}

$normalizedTarget = $target.Replace('\', '/')

foreach ($pf in $protectedFiles) {
    $normalizedPattern = $pf.Replace('\', '/')
    if ($normalizedTarget -like "*$normalizedPattern*") {
        $message = "BLOCKED: File '$pf' is protected. User confirmation required."
        if ($hookEvent -eq 'PermissionRequest') {
            $resp = @{
                hookSpecificOutput = @{
                    hookEventName = 'PermissionRequest'
                    decision = @{
                        behavior = 'deny'
                        message  = $message
                    }
                }
            } | ConvertTo-Json -Compress -Depth 6
        } else {
            $resp = @{
                hookSpecificOutput = @{
                    hookEventName            = 'PreToolUse'
                    permissionDecision       = 'deny'
                    permissionDecisionReason = $message
                }
            } | ConvertTo-Json -Compress -Depth 6
        }
        Write-Output $resp
        exit 0
    }
}

Write-Output '{"continue": true}'
exit 0
