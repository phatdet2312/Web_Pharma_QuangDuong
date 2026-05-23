$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path (Join-Path $ScriptDir '..\..')

& codex -C $ProjectRoot.Path -c 'model_reasoning_effort="xhigh"' @args
