# Hook: PostToolUse - auto compile/lint after file edits.
# Output JSON matches post-edit-typecheck.sh: decision=block + additionalContext on failure.
$ErrorActionPreference = 'Stop'

$payload = [Console]::In.ReadToEnd()
try {
    $obj = $payload | ConvertFrom-Json
} catch {
    Write-Output '{"continue": true}'
    exit 0
}

$filePaths = @()
if ($obj.tool_input) {
    if ($obj.tool_input.file_path) {
        $filePaths += [string]$obj.tool_input.file_path
    } elseif ($obj.tool_input.path) {
        $filePaths += [string]$obj.tool_input.path
    } else {
        $patchText = ''
        if ($obj.tool_input.input) { $patchText = [string]$obj.tool_input.input }
        elseif ($obj.tool_input.patch) { $patchText = [string]$obj.tool_input.patch }
        elseif ($obj.tool_input.command) { $patchText = [string]$obj.tool_input.command }

        if ($patchText) {
            $matches = [regex]::Matches($patchText, '(?m)^\*\*\* (?:Update|Add|Delete) File: (.+)$')
            foreach ($m in $matches) {
                $filePaths += $m.Groups[1].Value.Trim()
            }
        }
    }
}

if ($filePaths.Count -eq 0) {
    Write-Output '{"continue": true}'
    exit 0
}

function Invoke-Check {
    param([string]$cmd, [string]$extName)

    $tmpOut = [System.IO.Path]::GetTempFileName()
    $tmpErr = [System.IO.Path]::GetTempFileName()
    try {
        $proc = Start-Process -FilePath 'cmd.exe' `
            -ArgumentList @('/c', $cmd) `
            -NoNewWindow `
            -Wait `
            -PassThru `
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

$makeChecked = $false
foreach ($filePath in ($filePaths | Select-Object -Unique)) {
    $ext = [System.IO.Path]::GetExtension($filePath).TrimStart('.').ToLower()

    switch ($ext) {
        'java' {
            if (Test-Path 'pom.xml') { Invoke-Check 'mvn compile -q' 'java' }
            elseif (Test-Path 'build.gradle') { Invoke-Check 'gradle compileJava -q' 'java' }
            elseif (Test-Path 'build.gradle.kts') { Invoke-Check 'gradle compileJava -q' 'java' }
        }
        { $_ -in 'ts','tsx' } {
            if (Test-Path 'tsconfig.json') { Invoke-Check 'npx tsc --noEmit' 'ts' }
        }
        { $_ -in 'js','jsx' } {
            if ((Test-Path 'package.json') -and ((Get-Content 'package.json' -Raw) -match '"lint"')) {
                Invoke-Check 'npm run lint --silent' 'js'
            }
        }
        'py' {
            if (Get-Command mypy -ErrorAction SilentlyContinue) {
                if ((Test-Path 'mypy.ini') -or (Test-Path 'pyproject.toml')) {
                    Invoke-Check "mypy `"$filePath`"" 'py'
                }
            } elseif (Get-Command ruff -ErrorAction SilentlyContinue) {
                Invoke-Check "ruff check `"$filePath`"" 'py'
            }
        }
        'go' {
            if (Test-Path 'go.mod') { Invoke-Check 'go build ./...' 'go' }
        }
        'rs' {
            if (Test-Path 'Cargo.toml') { Invoke-Check 'cargo check' 'rust' }
        }
        'cs' {
            if ((Get-ChildItem '*.csproj' -ErrorAction SilentlyContinue) -or (Get-ChildItem '*.sln' -ErrorAction SilentlyContinue)) {
                Invoke-Check 'dotnet build --no-restore -q' 'cs'
            }
        }
        { $_ -in 'kt','kts' } {
            if (Test-Path 'build.gradle') { Invoke-Check 'gradle compileKotlin -q' 'kt' }
            elseif (Test-Path 'build.gradle.kts') { Invoke-Check 'gradle compileKotlin -q' 'kt' }
        }
        'swift' {
            if (Test-Path 'Package.swift') { Invoke-Check 'swift build' 'swift' }
        }
        'dart' {
            if (Test-Path 'pubspec.yaml') { Invoke-Check "dart analyze `"$filePath`"" 'dart' }
        }
        default {
            if (-not $makeChecked -and (Test-Path 'Makefile')) {
                $makeChecked = $true
                $mfContent = Get-Content 'Makefile' -Raw -ErrorAction SilentlyContinue
                if ($mfContent -match '(?m)^check:') {
                    Invoke-Check 'make check' 'makefile'
                } elseif ($mfContent -match '(?m)^lint:') {
                    Invoke-Check 'make lint' 'makefile'
                }
            }
        }
    }
}

Write-Output '{"continue": true}'
exit 0
