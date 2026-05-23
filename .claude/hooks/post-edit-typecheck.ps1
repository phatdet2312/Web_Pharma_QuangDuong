# post-edit-typecheck.ps1 — Auto compile/lint after file edits (Windows PowerShell 5.1+).
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$filePath = ''
if ($obj.tool_input) {
    if ($obj.tool_input.file_path) { $filePath = [string]$obj.tool_input.file_path }
    elseif ($obj.tool_input.path)  { $filePath = [string]$obj.tool_input.path }
}

if ([string]::IsNullOrWhiteSpace($filePath)) {
    Write-Output '{"continue": true}'
    exit 0
}

$ext = [System.IO.Path]::GetExtension($filePath).TrimStart('.').ToLower()
$projectDir = if ($env:CLAUDE_PROJECT_DIR) { $env:CLAUDE_PROJECT_DIR } else { (Get-Location).Path }

function Invoke-Check {
    param([string]$cmd, [string]$extName)

    $tmpOut = [System.IO.Path]::GetTempFileName()
    $tmpErr = [System.IO.Path]::GetTempFileName()
    try {
        $proc = Start-Process -FilePath 'cmd.exe' `
            -ArgumentList @('/c', $cmd) `
            -WorkingDirectory $projectDir `
            -NoNewWindow -Wait -PassThru `
            -RedirectStandardOutput $tmpOut `
            -RedirectStandardError $tmpErr
        $rc = $proc.ExitCode
        $lines = @()
        $lines += Get-Content $tmpOut -ErrorAction SilentlyContinue
        $lines += Get-Content $tmpErr -ErrorAction SilentlyContinue
        $tail = ($lines | Select-Object -Last 30) -join "`n"
    } finally {
        Remove-Item $tmpOut -ErrorAction SilentlyContinue
        Remove-Item $tmpErr -ErrorAction SilentlyContinue
    }

    if ($rc -ne 0) {
        $resp = @{
            decision = 'block'
            reason   = "Typecheck/compile FAIL ($extName)"
            hookSpecificOutput = @{
                hookEventName     = 'PostToolUse'
                additionalContext = $tail
            }
        } | ConvertTo-Json -Compress -Depth 5
        Write-Output $resp
        exit 0
    }
}

switch ($ext) {
    'java' {
        if (Test-Path (Join-Path $projectDir 'pom.xml')) { Invoke-Check 'mvn compile -q' 'java' }
        elseif (Test-Path (Join-Path $projectDir 'build.gradle')) { Invoke-Check 'gradle compileJava -q' 'java' }
        elseif (Test-Path (Join-Path $projectDir 'build.gradle.kts')) { Invoke-Check 'gradle compileJava -q' 'java' }
    }
    { $_ -in 'ts','tsx' } {
        if (Test-Path (Join-Path $projectDir 'tsconfig.json')) { Invoke-Check 'npx tsc --noEmit' 'ts' }
    }
    { $_ -in 'js','jsx' } {
        $pkgJson = Join-Path $projectDir 'package.json'
        if ((Test-Path $pkgJson) -and ((Get-Content $pkgJson -Raw) -match '"lint"')) {
            Invoke-Check 'npm run lint --silent' 'js'
        }
    }
    'py' {
        try { $null = Get-Command mypy -ErrorAction Stop
            if ((Test-Path (Join-Path $projectDir 'mypy.ini')) -or (Test-Path (Join-Path $projectDir 'pyproject.toml'))) {
                Invoke-Check "mypy `"$filePath`"" 'py'
            }
        } catch {
            try { $null = Get-Command ruff -ErrorAction Stop
                Invoke-Check "ruff check `"$filePath`"" 'py'
            } catch { }
        }
    }
    'go' {
        if (Test-Path (Join-Path $projectDir 'go.mod')) { Invoke-Check 'go build ./...' 'go' }
    }
    'rs' {
        if (Test-Path (Join-Path $projectDir 'Cargo.toml')) { Invoke-Check 'cargo check' 'rust' }
    }
    'cs' {
        if ((Get-ChildItem (Join-Path $projectDir '*.csproj') -ErrorAction SilentlyContinue) -or
            (Get-ChildItem (Join-Path $projectDir '*.sln') -ErrorAction SilentlyContinue)) {
            Invoke-Check 'dotnet build --no-restore -q' 'cs'
        }
    }
    { $_ -in 'kt','kts' } {
        if (Test-Path (Join-Path $projectDir 'build.gradle')) { Invoke-Check 'gradle compileKotlin -q' 'kt' }
        elseif (Test-Path (Join-Path $projectDir 'build.gradle.kts')) { Invoke-Check 'gradle compileKotlin -q' 'kt' }
    }
    'swift' {
        if (Test-Path (Join-Path $projectDir 'Package.swift')) { Invoke-Check 'swift build' 'swift' }
    }
    'dart' {
        if (Test-Path (Join-Path $projectDir 'pubspec.yaml')) { Invoke-Check "dart analyze `"$filePath`"" 'dart' }
    }
    default {
        $mf = Join-Path $projectDir 'Makefile'
        if (Test-Path $mf) {
            $mfContent = Get-Content $mf -Raw -ErrorAction SilentlyContinue
            if ($mfContent -match '(?m)^check:') { Invoke-Check 'make check' 'makefile' }
            elseif ($mfContent -match '(?m)^lint:') { Invoke-Check 'make lint' 'makefile' }
        }
    }
}

Write-Output '{"continue": true}'
exit 0
