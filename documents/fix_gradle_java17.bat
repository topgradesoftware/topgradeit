@echo off
echo === Fixing Gradle Java 17 Compatibility Issue ===
echo.

echo 1. Stopping Gradle daemon...
call gradlew --stop
if errorlevel 1 (
    echo Warning: Could not stop Gradle daemon (might not be running)
)

echo.
echo 2. Cleaning Gradle cache...
call gradlew clean
if errorlevel 1 (
    echo Error: Clean failed
    goto :clean_cache_manually
)

echo.
echo 3. Testing build...
call gradlew build --no-daemon
if errorlevel 1 (
    echo Error: Build failed
    goto :clean_cache_manually
)

echo.
echo === SUCCESS: Gradle Java 17 compatibility fixed! ===
echo You can now build your project normally.
goto :end

:clean_cache_manually
echo.
echo === Manual Cache Clean Required ===
echo Please manually delete the following directories:
echo.
echo 1. Delete Gradle cache:
echo    %USERPROFILE%\.gradle\caches
echo.
echo 2. Delete project build folder:
echo    %CD%\build
echo    %CD%\app\build
echo.
echo 3. Then run: gradlew clean build
echo.
echo 4. If still having issues, try:
echo    - Restart your IDE
echo    - Check JAVA_HOME environment variable
echo    - Ensure you're using Java 17

:end
pause 