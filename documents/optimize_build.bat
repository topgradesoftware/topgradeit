@echo off
echo ========================================
echo Topgrade Software App Build Environment Optimizer
echo ========================================

echo.
echo Cleaning Gradle caches...
call gradlew cleanBuildCache
call gradlew clean

echo.
echo Cleaning Android Studio caches...
if exist "%USERPROFILE%\.android\build-cache" (
    rmdir /s /q "%USERPROFILE%\.android\build-cache"
    echo Android build cache cleaned.
)

if exist "%USERPROFILE%\.gradle\caches" (
    echo Gradle caches found. Cleaning old entries...
    forfiles /p "%USERPROFILE%\.gradle\caches" /s /m * /d -30 /c "cmd /c del @path" 2>nul
    echo Old Gradle cache entries cleaned.
)

echo.
echo Optimizing Gradle daemon...
call gradlew --stop
call gradlew --start

echo.
echo ========================================
echo Build environment optimized!
echo ========================================
echo.
echo Next steps:
echo 1. Run fast_build.bat for optimized build
echo 2. Monitor build times
echo 3. Adjust memory settings if needed
echo.
pause
