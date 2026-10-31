@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Install.ps1" -Mode Uninstall %*
if errorlevel 1 (echo Removal failed. Review the message above.)
pause
