Write-Host "========================================" -ForegroundColor Green
Write-Host "Android Build Optimization Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host ""
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
./gradlew clean

Write-Host ""
Write-Host "Clearing Gradle caches..." -ForegroundColor Yellow
$gradleCachePath = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $gradleCachePath) {
    Remove-Item -Path $gradleCachePath -Recurse -Force
    Write-Host "Gradle caches cleared." -ForegroundColor Green
}

Write-Host ""
Write-Host "Clearing Android build cache..." -ForegroundColor Yellow
$androidCachePath = "$env:USERPROFILE\.android\build-cache"
if (Test-Path $androidCachePath) {
    Remove-Item -Path $androidCachePath -Recurse -Force
    Write-Host "Android build cache cleared." -ForegroundColor Green
}

Write-Host ""
Write-Host "Clearing temporary files..." -ForegroundColor Yellow
if (Test-Path "build") { Remove-Item -Path "build" -Recurse -Force }
if (Test-Path "app\build") { Remove-Item -Path "app\build" -Recurse -Force }
if (Test-Path ".gradle") { Remove-Item -Path ".gradle" -Recurse -Force }

Write-Host ""
Write-Host "Starting optimized build..." -ForegroundColor Yellow
./gradlew assembleDebug --parallel --max-workers=12 --daemon

Write-Host ""
Write-Host "Build optimization complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
