# Hook: Stop — ghi telemetry turn chính vào .codex/agent-metrics.csv.
# Codex 0.133 đã có SubagentStart/SubagentStop. Telemetry từng subagent
# được ghi bởi log-subagent-start/stop vào .codex/subagent-metrics.csv.
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try { $obj = $payload | ConvertFrom-Json } catch {
    Write-Output '{"continue": true}'
    exit 0
}

$sessionId = if ($obj.session_id) { [string]$obj.session_id } else { 'unknown' }
$turnId = if ($obj.turn_id) { [string]$obj.turn_id } else { '' }
$model = if ($obj.model) { [string]$obj.model } else { 'unknown' }
$permMode = if ($obj.permission_mode) { [string]$obj.permission_mode } else { 'default' }
$cwd = if ($obj.cwd) { [string]$obj.cwd } else { '' }
$lastMsg = if ($obj.last_assistant_message) { [string]$obj.last_assistant_message } else { '' }
if ($lastMsg.Length -gt 80) { $lastMsg = $lastMsg.Substring(0, 80) }
$lastMsg = ($lastMsg -replace '"', "'") -replace '[,\r\n]', ' '

$ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
$nowEpoch = [int][double]::Parse((Get-Date -UFormat %s))

$dir = '.codex'
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
$lastStopFile = Join-Path $dir '.last_stop_epoch'
$csvFile = Join-Path $dir 'agent-metrics.csv'

$durationApproxMs = 'N/A'
if (Test-Path $lastStopFile) {
    try {
        $lastEpoch = [int](Get-Content $lastStopFile -ErrorAction SilentlyContinue)
        if ($lastEpoch -gt 0) {
            $delta = $nowEpoch - $lastEpoch
            if ($delta -lt 3600) {
                $durationApproxMs = ($delta * 1000).ToString()
            }
        }
    } catch { }
}
Set-Content -LiteralPath $lastStopFile -Value $nowEpoch -Encoding ascii

if (-not (Test-Path $csvFile)) {
    Set-Content -LiteralPath $csvFile -Value 'timestamp,session_id,turn_id,model,permission_mode,duration_approx_ms,cwd,last_msg_snippet,note' -Encoding utf8
}

$note = 'tokens=N/A;scope=main-turn;duration=approx'
$line = "$ts,$sessionId,$turnId,$model,$permMode,$durationApproxMs,$cwd,`"$lastMsg`",$note"
Add-Content -LiteralPath $csvFile -Value $line -Encoding utf8

Write-Output '{"continue": true}'
exit 0
