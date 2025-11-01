@echo off
REM Android App Version Update Script for Windows
REM This batch file provides easy access to the version update functionality

echo ========================================
echo Android App Version Manager
echo ========================================
echo.

REM Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python is not installed or not in PATH
    echo Please install Python and try again
    pause
    exit /b 1
)

REM Check if build.gradle exists
if not exist "app\build.gradle" (
    echo Error: app\build.gradle not found
    echo Please run this script from the project root directory
    pause
    exit /b 1
)

:menu
echo Current options:
echo.
echo 1. Show current version
echo 2. Increment patch version (1.0.0 -^> 1.0.1)
echo 3. Increment minor version (1.0.0 -^> 1.1.0)
echo 4. Increment major version (1.0.0 -^> 2.0.0)
echo 5. Set custom version
echo 6. Exit
echo.

set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" (
    python update_version.py --show
    echo.
    pause
    goto menu
)

if "%choice%"=="2" (
    python update_version.py --patch
    echo.
    pause
    goto menu
)

if "%choice%"=="3" (
    python update_version.py --minor
    echo.
    pause
    goto menu
)

if "%choice%"=="4" (
    python update_version.py --major
    echo.
    pause
    goto menu
)

if "%choice%"=="5" (
    set /p custom_version="Enter new version (e.g., 2.0.0): "
    if not "%custom_version%"=="" (
        python update_version.py --version %custom_version%
    ) else (
        echo No version entered. Operation cancelled.
    )
    echo.
    pause
    goto menu
)

if "%choice%"=="6" (
    echo Goodbye!
    exit /b 0
)

echo Invalid choice. Please try again.
echo.
goto menu
