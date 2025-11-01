@echo off
echo Daily Build - Topgrade Software App
echo Started at: %date% %time%
echo.

:: Set project directory
cd /d "%~dp0"

:: Run the auto build script
call auto_build.bat

:: Log the completion
echo Daily build completed at: %date% %time% >> daily_build.log
echo ======================================== >> daily_build.log 