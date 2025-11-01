@echo off
echo ========================================
echo Android App Debug Fix Script
echo ========================================
echo.

echo Checking ADB connection...
adb devices
echo.

echo Restarting ADB server...
adb kill-server
timeout /t 2 /nobreak >nul
adb start-server
echo ADB server restarted
echo.

echo Checking device memory...
adb shell dumpsys meminfo | findstr "Total RAM\|Free RAM\|Used RAM"
echo.

echo Checking app memory usage...
adb shell dumpsys meminfo com.topgradesoftware.cmtb
echo.

echo Clearing app data...
adb shell pm clear com.topgradesoftware.cmtb
echo App data cleared
echo.

echo Checking logcat for recent errors...
adb logcat -d | findstr "ERROR\|FATAL\|JDWP\|Broken pipe" | tail -10
echo.

echo Enabling wireless debugging...
adb tcpip 5555
echo Wireless debugging enabled on port 5555
echo.

echo Checking USB debugging status...
adb shell settings get global adb_enabled
echo.

echo ========================================
echo Debug Fix Complete
echo ========================================
echo.
echo Next steps:
echo 1. Clean and rebuild project in Android Studio
echo 2. Restart Android Studio
echo 3. Reinstall the app
echo 4. Test debugging functionality
echo.
pause 