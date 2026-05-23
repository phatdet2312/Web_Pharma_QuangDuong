# Hook: PostToolUse — log mọi file đã sửa vào .codex/edit-history.log
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$toolName = if ($obj.tool_name) { [string]$obj.tool_name } else { 'unknown' }
$filePath = '(patch)'
if ($obj.tool_input) {
    if ($obj.tool_input.file_path)  { $filePath = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)   { $filePath = [string]$obj.tool_input.path }
}
$paths = @()
if ($filePath -ne '(patch)') {
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
if ($paths.Count -eq 0) { $paths += '(patch)' }

$ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
$logFile = '.codex/edit-history.log'
$dir = Split-Path $logFile -Parent
if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
foreach ($p in ($paths | Select-Object -Unique)) {
    Add-Content -Path $logFile -Value "$ts | $toolName | $p" -Encoding utf8
}

Write-Output '{"continue": true}'
exit 0
