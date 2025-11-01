@echo off
echo 🚀 Starting Fast Build...
echo.

REM Set optimized environment variables
set GRADLE_OPTS=-Xmx8192m -XX:MaxMetaspaceSize=2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=100
set ANDROID_GRADLE_OPTS=-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.caching=true

REM Clean build cache if needed
if "%1"=="clean" (
    echo 🧹 Cleaning build cache...
    gradlew clean
    echo.
)

REM Build with optimizations
echo ⚡ Building with optimizations...
gradlew assembleDebug --parallel --max-workers=8 --build-cache --configuration-cache --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Build completed successfully!
    echo 📱 APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ❌ Build failed! Check the error messages above.
)

echo.
echo 🏁 Fast build finished!
pause
