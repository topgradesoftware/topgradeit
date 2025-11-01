# ANR Prevention Helper Consolidation Migration Guide

## Overview
This document provides guidance on migrating from the old scattered ANR Prevention Helper classes to the new consolidated `ConsolidatedANRPreventionHelper` class.

## What Changed

### 1. New Consolidated File
- **File**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/ConsolidatedANRPreventionHelper.kt`
- **Purpose**: Single source of truth for all ANR prevention functionality across the entire application

### 2. Updated Files
- `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/ANRPreventionHelper.kt` - Now uses `ConsolidatedANRPreventionHelper` internally
- `app/src/main/java/topgrade/parent/com/parentseeks/Utils/ANRPreventionHelper.kt` - Now uses `ConsolidatedANRPreventionHelper` internally

## How to Use the New ConsolidatedANRPreventionHelper

### 1. Background Operations
```kotlin
// Old way (Parent ANRPreventionHelper)
val anrHelper = ANRPreventionHelper(context)
anrHelper.executeInBackground(
    operation = { /* heavy operation */ },
    onSuccess = { result -> /* handle success */ },
    onError = { exception -> /* handle error */ }
)

// New way
ConsolidatedANRPreventionHelper.executeInBackground(
    operation = { /* heavy operation */ },
    onComplete = { result -> /* handle success */ },
    onError = { exception -> /* handle error */ }
)
```

### 2. Background Operations with Timeout
```kotlin
// Old way (Utils ANRPreventionHelper)
ANRPreventionHelper.executeInBackgroundWithTimeout(
    operation = { /* heavy operation */ },
    timeoutMs = 5000L,
    onComplete = { result -> /* handle success */ },
    onError = { exception -> /* handle error */ },
    onTimeout = { /* handle timeout */ }
)

// New way
ConsolidatedANRPreventionHelper.executeInBackgroundWithTimeout(
    operation = { /* heavy operation */ },
    timeoutMs = 5000L,
    onComplete = { result -> /* handle success */ },
    onError = { exception -> /* handle error */ },
    onTimeout = { /* handle timeout */ }
)
```

### 3. Coroutine Operations
```kotlin
// Old way (Parent ANRPreventionHelper)
suspend fun loadData(): Data {
    return anrHelper.executeWithTimeout(5000L) {
        // Load data operation
    }
}

// New way
suspend fun loadData(): Data {
    return ConsolidatedANRPreventionHelper.executeWithTimeout(5000L) {
        // Load data operation
    }
}
```

### 4. Safe UI Updates
```kotlin
// Old way (Utils ANRPreventionHelper)
ANRPreventionHelper.safeUIUpdate(activity) {
    // UI update code
}

// New way
ConsolidatedANRPreventionHelper.safeUIUpdate(activity) {
    // UI update code
}
```

### 5. Toast Messages
```kotlin
// Old way (Utils ANRPreventionHelper)
ANRPreventionHelper.showToastSafely(context, "Message")

// New way
ConsolidatedANRPreventionHelper.showToastSafely(context, "Message")
```

### 6. DataStore Operations
```kotlin
// Old way (Parent ANRPreventionHelper)
suspend fun loadUserData(): UserData {
    return anrHelper.loadUserDataSafely()
}

// New way
suspend fun loadUserData(): UserData {
    return ConsolidatedANRPreventionHelper.loadUserDataSafely()
}
```

### 7. Performance Monitoring
```kotlin
// Old way (Utils ANRPreventionHelper)
val result = ANRPreventionHelper.monitorPerformance("operation") {
    // Operation to monitor
}

// New way
val result = ConsolidatedANRPreventionHelper.monitorPerformance("operation") {
    // Operation to monitor
}
```

## Key Features of ConsolidatedANRPreventionHelper

### 1. Comprehensive ANR Prevention
- Background operation execution with timeout
- Safe UI updates with activity lifecycle checks
- Coroutine-based operations with timeout and fallback
- DataStore safe operations
- Performance monitoring
- Toast message handling
- Async initialization
- Thread management utilities

### 2. Enhanced Error Handling
- All operations include comprehensive error handling
- Graceful fallbacks for failed operations
- Detailed logging for debugging
- Safe callback execution

### 3. Performance Optimizations
- Optimized thread pool management
- Memory-efficient operation execution
- Configurable timeout values
- Performance monitoring capabilities

### 4. Backward Compatibility
- Existing wrapper classes maintain API compatibility
- Gradual migration path available
- Deprecation warnings guide developers to new API

## Migration Steps

### Step 1: Update Imports
```kotlin
// Old imports
import topgrade.parent.com.parentseeks.Parent.Utils.ANRPreventionHelper
import topgrade.parent.com.parentseeks.Utils.ANRPreventionHelper

// New import
import topgrade.parent.com.parentseeks.Parent.Utils.ConsolidatedANRPreventionHelper
```

### Step 2: Replace Class Usage
```kotlin
// Old way
val anrHelper = ANRPreventionHelper(context)
anrHelper.executeInBackground(...)

// New way
ConsolidatedANRPreventionHelper.executeInBackground(...)
```

### Step 3: Update Method Calls
- Replace instance method calls with static method calls
- Update parameter names where necessary
- Ensure proper error handling

### Step 4: Test Thoroughly
- Test all ANR prevention scenarios
- Verify timeout handling
- Check error recovery mechanisms
- Validate UI update safety

## Benefits of Consolidation

### 1. Code Maintainability
- Single source of truth for ANR prevention logic
- Easier to maintain and update
- Consistent behavior across the application

### 2. Performance Improvements
- Optimized thread pool management
- Better resource utilization
- Reduced memory overhead

### 3. Enhanced Features
- Comprehensive error handling
- Better timeout management
- Improved logging and debugging
- More robust UI safety checks

### 4. Developer Experience
- Consistent API across the application
- Better documentation and examples
- Easier to understand and use
- Clear migration path

## Troubleshooting

### Common Issues

1. **Import Errors**
   - Ensure you're importing the correct class
   - Check for conflicting imports

2. **Method Signature Changes**
   - Some method signatures have been updated
   - Check parameter names and types

3. **Error Handling**
   - New implementation includes more comprehensive error handling
   - Ensure your error handling code is compatible

4. **Performance Issues**
   - Monitor performance after migration
   - Adjust timeout values if necessary

### Getting Help

If you encounter issues during migration:
1. Check the consolidated class documentation
2. Review the error logs for specific issues
3. Test with the wrapper classes first
4. Gradually migrate to direct usage

## Conclusion

The consolidation of ANR Prevention Helper classes provides a more robust, maintainable, and feature-rich solution for preventing ANR errors in your application. The migration process is straightforward and provides backward compatibility to ensure a smooth transition.

For new code, use `ConsolidatedANRPreventionHelper` directly. For existing code, you can continue using the wrapper classes while gradually migrating to the new consolidated implementation.
