# scan-secret-prompt.ps1 — Block prompts containing secrets (Windows PowerShell 5.1+).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$prompt = if ($obj.prompt) { [string]$obj.prompt } else { '' }
if ([string]::IsNullOrWhiteSpace($prompt)) {
    Write-Output '{"continue": true}'
    exit 0
}

$secretPatterns = @(
    'sk-[A-Za-z0-9_-]{20,}',
    'ghp_[A-Za-z0-9]{36,}',
    'ghs_[A-Za-z0-9]{36,}',
    'AKIA[0-9A-Z]{16}',
    'AIzaSy[A-Za-z0-9_-]{33}',
    'xox[baprs]-[A-Za-z0-9-]{10,}',
    '(password|passwd|pwd|api[_-]?key|secret)\s*[:=]\s*[A-Za-z0-9_/+=-]{8,}'
)

foreach ($pat in $secretPatterns) {
    if ($prompt -imatch $pat) {
        $resp = @{
            decision = 'block'
            reason   = "Prompt contains pattern resembling a secret/credential: $pat. Remove before sending."
        } | ConvertTo-Json -Compress -Depth 5
        Write-Output $resp
        exit 0
    }
}

Write-Output '{"continue": true}'
exit 0
