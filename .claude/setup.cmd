@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>nul

echo ============================================
echo   AGENT CLAUDE - Hook Setup (Auto-detect)
echo ============================================
echo.

where bash >nul 2>nul
if !errorlevel! equ 0 (
    echo [OK] bash found on this machine.
    echo [OK] settings.json default ^(bash/.sh^) is correct. Nothing to do.
    goto :done
)

echo [INFO] bash not found. Switching hooks to PowerShell/.ps1 ...
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup.ps1" -Force
if !errorlevel! neq 0 (
    echo [ERROR] setup.ps1 failed.
    goto :done
)

:done
echo.
echo Done.
endlocal
pause
