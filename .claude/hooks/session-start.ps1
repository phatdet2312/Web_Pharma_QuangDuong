# session-start.ps1 — Drift + rework alert + memory size check (Windows PowerShell 5.1+).
$ErrorActionPreference = 'Stop'

$projectDir = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { (Get-Location).Path }
$messages = New-Object System.Collections.Generic.List[string]

# 1. Drift check
if (Test-Path (Join-Path $projectDir '.git')) {
    $lastGit = ''
    try { $lastGit = (git -C $projectDir log --format=%h -1 2>$null).Trim() } catch { }
    $logFile = Join-Path $projectDir '.ai-memory/06_evolution_log.md'
    if ($lastGit -and (Test-Path $logFile)) {
        $content = Get-Content $logFile -Raw -ErrorAction SilentlyContinue
        $match = [regex]::Match($content, '\b[a-f0-9]{7,40}\b')
        $lastLogged = if ($match.Success) { $match.Value } else { '' }
        if ($lastLogged -and $lastLogged -ne $lastGit) {
            $messages.Add("DRIFT: HEAD=$lastGit but memory records $lastLogged. Run drift detection.")
        }
    }
}

# 2. Rework alert
$historyFile = Join-Path $projectDir '.claude/edit-history.log'
if (Test-Path $historyFile) {
    $tail = Get-Content -Tail 100 $historyFile -ErrorAction SilentlyContinue
    if ($tail) {
        $fileCounts = @{}
        foreach ($line in $tail) {
            $parts = $line -split '\|', 4
            if ($parts.Count -ge 3) {
                $fp = $parts[2].Trim()
                if ($fileCounts.ContainsKey($fp)) { $fileCounts[$fp]++ } else { $fileCounts[$fp] = 1 }
            }
        }
        $repeated = $fileCounts.GetEnumerator() | Where-Object { $_.Value -gt 3 } | Sort-Object Value -Descending | Select-Object -First 5
        if ($repeated) {
            $detail = ($repeated | ForEach-Object { "$($_.Value)x $($_.Key)" }) -join ', '
            $messages.Add("REWORK: Files edited many times recently: $detail")
        }
    }
}

# 3. Memory size check
foreach ($f in @('.ai-memory/06_evolution_log.md', '.ai-memory/07_learnings.md')) {
    $fullPath = Join-Path $projectDir $f
    if (Test-Path $fullPath) {
        $size = (Get-Item $fullPath).Length
        if ($size -gt 51200) {
            $basename = [System.IO.Path]::GetFileName($fullPath)
            $messages.Add("LARGE: $basename is $size bytes (>50KB). Consider compacting.")
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
