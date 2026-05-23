# Hook: PostToolUse — phát hiện file edit >3 lần trong 30 phút, ghi alert.
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$toolName = if ($obj.tool_name) { [string]$obj.tool_name } else { '' }
if ($toolName -notmatch '^(Edit|Write|MultiEdit|apply_patch)$') {
    Write-Output '{"continue": true}'
    exit 0
}

$filePath = ''
if ($obj.tool_input) {
    if ($obj.tool_input.file_path)  { $filePath = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)   { $filePath = [string]$obj.tool_input.path }
}

$paths = @()
if (-not [string]::IsNullOrWhiteSpace($filePath)) {
    $paths += $filePath
} elseif ($obj.tool_input) {
    $patchText = ''
    if ($obj.tool_input.input) { $patchText = [string]$obj.tool_input.input }
    elseif ($obj.tool_input.patch) { $patchText = [string]$obj.tool_input.patch }
    elseif ($obj.tool_input.command) { $patchText = [string]$obj.tool_input.command }
    if ($patchText) {
        $matches = [regex]::Matches($patchText, '(?m)^\*\*\* (?:Update|Add|Delete) File: (.+)$')
        foreach ($m in $matches) { $paths += $m.Groups[1].Value.Trim() }
    }
}

if ($paths.Count -eq 0) {
    Write-Output '{"continue": true}'
    exit 0
}

$historyFile = '.codex/edit-history.log'
$alertFile   = '.codex/rework-alerts.log'
$dir = Split-Path $historyFile -Parent
if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
if (-not (Test-Path $historyFile)) { New-Item -ItemType File -Path $historyFile | Out-Null }
if (-not (Test-Path $alertFile))   { New-Item -ItemType File -Path $alertFile   | Out-Null }

$threshold = (Get-Date).AddMinutes(-30)
$tail = if ((Get-Item $historyFile).Length -gt 0) { Get-Content -Tail 200 $historyFile } else { @() }

foreach ($filePath in ($paths | Select-Object -Unique)) {
    if ([string]::IsNullOrWhiteSpace($filePath) `
        -or $filePath -like '*.ai-memory/*' `
        -or $filePath -like '*.codex/*' `
        -or $filePath -like '*.agents/*' `
        -or $filePath -like '*.log') {
        continue
    }

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
        Add-Content -Path $alertFile -Value "$now | REWORK_ALERT | $filePath | edited $count times in 30min | hint: consider `$reflect to capture pattern" -Encoding utf8
    }
}

Write-Output '{"continue": true}'
exit 0
