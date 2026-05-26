# Hook: SubagentStop — ghi telemetry từng subagent cho $agent-roi.
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try { $obj = $payload | ConvertFrom-Json } catch {
    Write-Output '{"continue": true}'
    exit 0
}

$sessionId = if ($obj.session_id) { [string]$obj.session_id } else { 'unknown' }
$turnId = if ($obj.turn_id) { [string]$obj.turn_id } else { '' }
$agentId = if ($obj.agent_id) { [string]$obj.agent_id } else { 'unknown' }
$agentType = if ($obj.agent_type) { [string]$obj.agent_type } else { 'unknown' }
$model = if ($obj.model) { [string]$obj.model } else { 'unknown' }
$permMode = if ($obj.permission_mode) { [string]$obj.permission_mode } else { 'default' }
$cwd = if ($obj.cwd) { [string]$obj.cwd } else { '' }
$lastMsg = if ($obj.last_assistant_message) { [string]$obj.last_assistant_message } else { '' }
if ($lastMsg.Length -gt 120) { $lastMsg = $lastMsg.Substring(0, 120) }
$lastMsg = ($lastMsg -replace '"', "'") -replace '[,\r\n]', ' '

$timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
$nowMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

$dir = '.codex/subagent-start'
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
$safeAgentId = $agentId -replace '[^A-Za-z0-9_.-]', '_'
$startFile = Join-Path $dir "$safeAgentId.start"

$durationMs = 'N/A'
if (Test-Path $startFile) {
    try {
        $startMs = [Int64](Get-Content -LiteralPath $startFile -TotalCount 1)
        if ($startMs -gt 0 -and $nowMs -ge $startMs) {
            $durationMs = ($nowMs - $startMs).ToString()
        }
        Remove-Item -LiteralPath $startFile -Force -ErrorAction SilentlyContinue
    } catch { }
}

$csvFile = '.codex/subagent-metrics.csv'
if (-not (Test-Path $csvFile)) {
    Set-Content -LiteralPath $csvFile -Value 'timestamp,session_id,turn_id,agent_id,agent_type,model,permission_mode,duration_ms,cwd,last_msg_snippet,note' -Encoding utf8
}

$note = 'tokens=N/A;duration=SubagentStart-SubagentStop;transcript_format=unstable'
$line = "$timestamp,$sessionId,$turnId,$agentId,$agentType,$model,$permMode,$durationMs,$cwd,`"$lastMsg`",$note"
Add-Content -LiteralPath $csvFile -Value $line -Encoding utf8

Write-Output '{"continue": true}'
exit 0
