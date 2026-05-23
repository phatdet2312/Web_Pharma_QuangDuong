# pre-bash-firewall.ps1 — Block dangerous commands (Windows PowerShell 5.1+).
# Works with: PreToolUse (Bash) + PermissionRequest (Bash)
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$hookEvent = if ($obj.hook_event_name) { [string]$obj.hook_event_name } else { 'PreToolUse' }
$cmd = ''
if ($obj.tool_input -and $obj.tool_input.command) {
    if ($obj.tool_input.command -is [array]) {
        $cmd = ($obj.tool_input.command -join ' ')
    } else {
        $cmd = [string]$obj.tool_input.command
    }
}

if ([string]::IsNullOrWhiteSpace($cmd)) {
    Write-Output '{"continue": true}'
    exit 0
}

$denyPatterns = @(
    'rm\s+-rf\s+/',
    'rm\s+-rf\s+\.',
    'git\s+reset\s+--hard',
    'git\s+push\s+.*--force',
    'git\s+clean\s+-fd',
    'drop\s+database',
    'drop\s+table',
    'truncate\s+table',
    'chmod\s+777',
    'curl.*\|\s*bash',
    'wget.*\|\s*bash',
    'Remove-Item\s+.*-Recurse\s+.*-Force\s+/',
    'rd\s+/s\s+/q\s+\\\\'
)

foreach ($pat in $denyPatterns) {
    if ($cmd -imatch $pat) {
        if ($hookEvent -eq 'PermissionRequest') {
            $resp = @{
                hookSpecificOutput = @{
                    hookEventName = 'PermissionRequest'
                    decision = @{
                        behavior = 'deny'
                        message  = "Blocked: command matches dangerous pattern: $pat"
                    }
                }
            } | ConvertTo-Json -Compress -Depth 6
        } else {
            $resp = @{
                hookSpecificOutput = @{
                    hookEventName            = 'PreToolUse'
                    permissionDecision       = 'deny'
                    permissionDecisionReason = "Blocked: command matches dangerous pattern: $pat"
                }
            } | ConvertTo-Json -Compress -Depth 6
        }
        Write-Output $resp
        exit 0
    }
}

Write-Output '{"continue": true}'
exit 0
