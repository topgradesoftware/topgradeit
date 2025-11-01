Write-Host "========================================" -ForegroundColor Green
Write-Host "Gradle Build Optimization Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host ""
Write-Host "Cleaning Gradle cache and build files..." -ForegroundColor Yellow
Write-Host ""

# Stop Gradle daemon
Write-Host "Stopping Gradle daemon..." -ForegroundColor Cyan
gradle --stop

# Clean project
Write-Host "Cleaning project..." -ForegroundColor Cyan
gradle clean

# Clean Android build cache
Write-Host "Cleaning Android build cache..." -ForegroundColor Cyan
if (Test-Path "app\build") { Remove-Item "app\build" -Recurse -Force }
if (Test-Path "build") { Remove-Item "build" -Recurse -Force }
if (Test-Path ".gradle") { Remove-Item ".gradle" -Recurse -Force }

# Clean Android Studio cache
Write-Host "Cleaning Android Studio cache..." -ForegroundColor Cyan
$androidCache = "$env:USERPROFILE\.android\build-cache"
$gradleCache = "$env:USERPROFILE\.gradle\caches"

if (Test-Path $androidCache) { Remove-Item $androidCache -Recurse -Force }
if (Test-Path $gradleCache) { Remove-Item $gradleCache -Recurse -Force }

Write-Host ""
Write-Host "Starting Gradle daemon with optimized settings..." -ForegroundColor Cyan
gradle --daemon

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Optimization Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart Android Studio" -ForegroundColor White
Write-Host "2. Run a clean build: gradle assembleDebug" -ForegroundColor White
Write-Host "3. Subsequent builds should be much faster" -ForegroundColor White
Write-Host ""
Read-Host "Press Enter to continue"
