# Hook: SubagentStart — ghi mốc bắt đầu subagent để tính ROI.
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

$safeAgentId = $agentId -replace '[^A-Za-z0-9_.-]', '_'
$dir = '.codex/subagent-start'
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

$nowMs = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$startFile = Join-Path $dir "$safeAgentId.start"
Set-Content -LiteralPath $startFile -Value @($nowMs, $sessionId, $turnId, $agentType) -Encoding ascii

Write-Output '{"continue": true}'
exit 0
