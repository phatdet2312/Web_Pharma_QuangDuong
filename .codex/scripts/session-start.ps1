# Hook: SessionStart — drift + rework alert + memory size check.
# Output: hookSpecificOutput.additionalContext (Codex chèn vào đầu phiên).
$ErrorActionPreference = 'Stop'

$messages = New-Object System.Collections.Generic.List[string]

# 1. Drift check (chỉ nếu có .git)
if (Test-Path '.git') {
    $lastGit = ''
    try { $lastGit = (git log --format=%h -1 2>$null).Trim() } catch { }
    $logFile = '.ai-memory/06_evolution_log.md'
    if ($lastGit -and (Test-Path $logFile)) {
        $content = Get-Content $logFile -Raw -ErrorAction SilentlyContinue
        $match = [regex]::Match($content, '\b[a-f0-9]{7,40}\b')
        $lastLogged = if ($match.Success) { $match.Value } else { '' }
        if ($lastLogged -and $lastLogged -ne $lastGit) {
            $messages.Add("[WARN] Drift detected: HEAD=$lastGit, memory ghi nhan khac. De xuat chay `$detect-drift.")
        }
    }
}

# 2. Rework alert trong 24h
$alertFile = '.codex/rework-alerts.log'
if (Test-Path $alertFile) {
    $cutoff = (Get-Date).AddHours(-24)
    $recent = Get-Content $alertFile -ErrorAction SilentlyContinue | Where-Object {
        $parts = $_ -split '\|', 2
        if ($parts.Count -lt 1) { return $false }
        try { $tsDt = [DateTime]::Parse($parts[0].Trim()) } catch { return $false }
        $tsDt -gt $cutoff
    }
    if ($recent) {
        $count = @($recent).Count
        $messages.Add("[WARN] Rework alerts trong 24h: $count file bi edit nhieu lan. De xuat chay `$reflect ngam.")
    }
}

# 3. Memory size
foreach ($f in @('.ai-memory/06_evolution_log.md', '.ai-memory/07_learnings.md')) {
    if (Test-Path $f) {
        $size = (Get-Item $f).Length
        if ($size -gt 51200) {
            $messages.Add("[INFO] $f da > 50KB. De xuat chay `$compact-memory de nen log cu.")
        }
    }
}

if ($messages.Count -eq 0) {
    Write-Output '{"continue": true}'
    exit 0
}

$ctx = "Session start checks:`n- " + ($messages -join "`n- ")
$resp = @{
    hookSpecificOutput = @{
        hookEventName     = 'SessionStart'
        additionalContext = $ctx
    }
} | ConvertTo-Json -Compress -Depth 5
Write-Output $resp
exit 0
