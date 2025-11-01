@echo off
echo ========================================
echo JDWP Debugging Settings Fix
echo ========================================
echo.
echo This script will help you configure Android Studio
echo for better JDWP debugging stability.
echo.
echo Please follow these steps in Android Studio:
echo.
echo 1. FILE SETTINGS:
echo    - Go to File ^> Settings (or Android Studio ^> Preferences on Mac)
echo    - Navigate to: Build, Execution, Deployment ^> Debugger
echo    - Set "Connection timeout" to: 10000 ms
echo    - Uncheck "Force step over calls"
echo    - Uncheck "Force run to cursor"
echo.
echo 2. COMPILER SETTINGS:
echo    - Go to: Build, Execution, Deployment ^> Compiler
echo    - Set "Build process heap size" to: 4096 MB
echo    - Check "Compile independent modules in parallel"
echo    - Check "Use incremental compilation"
echo.
echo 3. RUN CONFIGURATIONS:
echo    - Go to Run ^> Edit Configurations
echo    - Select your Android App configuration
echo    - In "Deployment Target Options":
echo      - Set "Deploy" to "Default APK"
echo      - Set "Debug type" to "Dual"
echo      - Check "Install Flags" and add: -r -t
echo.
echo 4. CACHE CLEARING:
echo    - Go to File ^> Invalidate Caches and Restart
echo    - Select "Invalidate and Restart"
echo.
echo 5. DEVICE SETTINGS:
echo    - On your device, go to Developer Options
echo    - Enable "USB Debugging (Security Settings)"
echo    - Disable "Verify apps over USB" temporarily
echo.
echo ========================================
echo Configuration completed!
echo ========================================
echo.
echo Additional tips:
echo - Use a high-quality USB cable
echo - Try different USB ports
echo - Keep Android Studio and SDK updated
echo - Close unnecessary applications
echo.
pause
