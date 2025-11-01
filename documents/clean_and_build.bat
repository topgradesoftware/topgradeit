@echo off
echo Cleaning project...
gradlew clean

echo.
echo Building debug version...
gradlew assembleDebug

echo.
echo Build completed!
pause 