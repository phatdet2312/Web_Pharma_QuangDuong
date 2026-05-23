# Hook: PreToolUse / PermissionRequest for apply_patch/Edit/Write - protect policy paths.
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$hookEvent = if ($obj.hook_event_name) { [string]$obj.hook_event_name } else { 'PreToolUse' }
$toolName = if ($obj.tool_name) { [string]$obj.tool_name } else { '' }
$target = ''

switch ($toolName) {
    'apply_patch' {
        if ($obj.tool_input.input) { $target = [string]$obj.tool_input.input }
        elseif ($obj.tool_input.patch) { $target = [string]$obj.tool_input.patch }
        elseif ($obj.tool_input.command) { $target = [string]$obj.tool_input.command }
    }
    { $_ -in 'Edit','Write','MultiEdit' } {
        if ($obj.tool_input.file_path) { $target = [string]$obj.tool_input.file_path }
        elseif ($obj.tool_input.path) { $target = [string]$obj.tool_input.path }
    }
    default {
        if ($obj.tool_input.command) { $target = [string]$obj.tool_input.command }
    }
}

if ([string]::IsNullOrWhiteSpace($target)) {
    Write-Output '{"continue": true}'
    exit 0
}

$projectRoot = if ($env:CODEX_PROJECT_ROOT) { $env:CODEX_PROJECT_ROOT } else { (Get-Location).Path }
$policyFile = Join-Path $projectRoot '.codexignore'
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

# Fail-soft fallback if .codexignore is missing/broken. The canonical list remains in .codexignore.
if ($protectedFiles.Count -eq 0) {
    $protectedFiles = @('.env', '.env.*', '.codex/config.toml', '.codex/hooks.json', '.codex/hooks.windows.json', '.codexignore')
}

$normalizedTarget = $target.Replace('\', '/')

foreach ($pf in $protectedFiles) {
    $normalizedPattern = $pf.Replace('\', '/')
    if ($normalizedTarget -like "*$normalizedPattern*") {
        $message = "BLOCKED: File '$pf' is protected. User confirmation is required before editing."
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
