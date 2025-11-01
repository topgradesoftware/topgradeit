@echo off
echo ========================================
echo Gradle Build Optimization Script
echo ========================================

echo.
echo Cleaning Gradle cache and build files...
echo.

REM Stop Gradle daemon
echo Stopping Gradle daemon...
gradle --stop

REM Clean project
echo Cleaning project...
gradle clean

REM Clean Gradle cache (optional - uncomment if needed)
REM echo Cleaning Gradle cache...
REM gradle cleanBuildCache

REM Clean Android build cache
echo Cleaning Android build cache...
if exist "app\build" rmdir /s /q "app\build"
if exist "build" rmdir /s /q "build"
if exist ".gradle" rmdir /s /q ".gradle"

REM Clean Android Studio cache
echo Cleaning Android Studio cache...
if exist "%USERPROFILE%\.android\build-cache" rmdir /s /q "%USERPROFILE%\.android\build-cache"
if exist "%USERPROFILE%\.gradle\caches" rmdir /s /q "%USERPROFILE%\.gradle\caches"

echo.
echo Starting Gradle daemon with optimized settings...
gradle --daemon

echo.
echo ========================================
echo Optimization Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Restart Android Studio
echo 2. Run a clean build: gradle assembleDebug
echo 3. Subsequent builds should be much faster
echo.
pause
