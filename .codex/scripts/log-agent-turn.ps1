# Hook: Stop — ghi telemetry cuối turn vào .codex/agent-metrics.csv
# GIỚI HẠN Codex 2026: Stop event không expose tokens/duration/agent_name.
# Workaround duration: tính delta giữa 2 lần Stop liên tiếp (approximation).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try { $obj = $payload | ConvertFrom-Json } catch {
    Write-Output '{"continue": true}'
    exit 0
}

$sessionId = if ($obj.session_id) { [string]$obj.session_id } else { 'unknown' }
$turnId    = if ($obj.turn_id)    { [string]$obj.turn_id }    else { '' }
$model     = if ($obj.model)      { [string]$obj.model }      else { 'unknown' }
$permMode  = if ($obj.permission_mode) { [string]$obj.permission_mode } else { 'default' }
$cwd       = if ($obj.cwd) { [string]$obj.cwd } else { '' }
$lastMsg   = if ($obj.last_assistant_message) { [string]$obj.last_assistant_message } else { '' }
if ($lastMsg.Length -gt 80) { $lastMsg = $lastMsg.Substring(0, 80) }
$lastMsg = ($lastMsg -replace '"', "'") -replace '[,\r\n]', ' '

$ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
$nowEpoch = [int][double]::Parse((Get-Date -UFormat %s))

$dir = '.codex'
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
$lastStopFile = Join-Path $dir '.last_stop_epoch'
$csvFile      = Join-Path $dir 'agent-metrics.csv'

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
Set-Content -Path $lastStopFile -Value $nowEpoch -Encoding ascii

if (-not (Test-Path $csvFile)) {
    Set-Content -Path $csvFile -Value 'timestamp,session_id,turn_id,model,permission_mode,duration_approx_ms,cwd,last_msg_snippet,note' -Encoding utf8
}

$note = 'tokens=N/A;agent_name=N/A;duration=approx'
$line = "$ts,$sessionId,$turnId,$model,$permMode,$durationApproxMs,$cwd,`"$lastMsg`",$note"
Add-Content -Path $csvFile -Value $line -Encoding utf8

Write-Output '{"continue": true}'
exit 0
