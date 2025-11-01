@echo off
echo === Parent Login Fix Test ===
echo.
echo Testing the fix for parent login redirection issue
echo.

REM Check if ADB is available
adb version >nul 2>&1
if errorlevel 1 (
    echo ERROR: ADB not found!
    echo Please ensure Android SDK is installed and ADB is in PATH
    goto :end
)

REM Check if device is connected
adb devices | find "device" >nul
if errorlevel 1 (
    echo ERROR: No device connected!
    echo Please connect your device and enable USB debugging
    goto :end
)

echo 1. Clearing app data to start fresh...
adb shell pm clear topgrade.parent.com.parentseeks
timeout /t 2 /nobreak >nul

echo 2. Clearing logcat...
adb logcat -c

echo 3. Starting app...
adb shell am start -n topgrade.parent.com.parentseeks/topgrade.parent.com.parentseeks.Parent.Activity.SelectRole
timeout /t 3 /nobreak >nul

echo 4. Selecting Parent Login...
adb shell input tap 540 800
timeout /t 2 /nobreak >nul

echo 5. Entering Campus ID...
adb shell input text 2
timeout /t 1 /nobreak >nul

echo 6. Entering Username...
adb shell input text 3075
timeout /t 1 /nobreak >nul

echo 7. Entering Password...
adb shell input text 03047375559
timeout /t 1 /nobreak >nul

echo 8. Checking privacy policy...
adb shell input tap 100 1200
timeout /t 1 /nobreak >nul

echo 9. Clicking Login button...
adb shell input tap 540 1400
timeout /t 8 /nobreak >nul

echo 10. Capturing logs...
echo === LOG OUTPUT ===
adb logcat -d -s LoginScreen:V UserRepository:V Splash:V

echo.
echo 11. Checking current activity...
adb shell dumpsys activity activities | findstr mResumedActivity

echo.
echo === TEST COMPLETE ===
echo Check the log output above for:
echo - LoginScreen: Attempting login with userType: PARENT
echo - UserRepository: User type: PARENT (normalized: Parent)
echo - UserRepository: User data saved successfully. User Type: Parent
echo - Splash: User Type: Parent
echo - Splash: Parent/Student user, going directly to dashboard
echo.
echo If you see "DashBoard" in the current activity, the fix worked!
goto :end

:end
pause 