# Hook: UserPromptSubmit — quét secret trong prompt user.
# Output: decision=block nếu phát hiện pattern secret (giống scan-secret-in-prompt.sh).
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
    'sk-[A-Za-z0-9_-]{20,}'                                  # OpenAI / Anthropic key
    'ghp_[A-Za-z0-9]{36,}'                                   # GitHub personal token
    'ghs_[A-Za-z0-9]{36,}'                                   # GitHub server token
    'AKIA[0-9A-Z]{16}'                                       # AWS access key
    'AIzaSy[A-Za-z0-9_-]{33}'                                # Google API key
    'xox[baprs]-[A-Za-z0-9-]{10,}'                           # Slack token
    '(password|passwd|pwd|api[_-]?key|secret)\s*[:=]\s*[A-Za-z0-9_/+=-]{8,}'
)

foreach ($pat in $secretPatterns) {
    if ($prompt -imatch $pat) {
        $resp = @{
            decision = 'block'
            reason   = "Prompt có pattern giống secret/credential: $pat. Xóa secret trước khi gửi (Codex sẽ ghi vào history)."
        } | ConvertTo-Json -Compress -Depth 5
        Write-Output $resp
        exit 0
    }
}

Write-Output '{"continue": true}'
exit 0
