@echo off
echo ========================================
echo Topgrade Software App - Clear Login Data Script
echo ========================================
echo.
echo This script will clear the app's stored login data
echo and force the app to show the login screen on next launch.
echo.
echo Options:
echo 1. Clear app data (recommended)
echo 2. Uninstall and reinstall app
echo 3. Exit
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Clearing app data...
    adb shell pm clear topgrade.parent.com.parentseeks
    echo.
    echo App data cleared successfully!
    echo The app will now show the login screen on next launch.
    echo.
    pause
) else if "%choice%"=="2" (
    echo.
    echo Uninstalling app...
    adb uninstall topgrade.parent.com.parentseeks
    echo.
    echo App uninstalled. Please reinstall the app.
    echo.
    pause
) else if "%choice%"=="3" (
    echo.
    echo Exiting...
    exit /b 0
) else (
    echo.
    echo Invalid choice. Please run the script again.
    echo.
    pause
)
