# User Repository Consolidation Migration Guide

## Overview
This document provides guidance on migrating from the old scattered User Repository classes to the new consolidated `ConsolidatedUserRepository` class.

## What Changed

### 1. New Consolidated File
- **File**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Repository/ConsolidatedUserRepository.kt`
- **Purpose**: Single source of truth for all user data management functionality across the entire application

### 2. Updated Files
- `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Repository/UserRepository.kt` - Now uses `ConsolidatedUserRepository` internally
- `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Repository/OptimizedUserRepository.kt` - Now uses `ConsolidatedUserRepository` internally

## How to Use the New ConsolidatedUserRepository

### 1. Basic Usage
```kotlin
// Create repository instance
val repository = ConsolidatedUserRepository(context, apiService)

// Set storage mode (optional - defaults to optimized)
repository.setStorageMode(true) // true = optimized (Room), false = legacy (Paper DB)
```

### 2. Login Operations
```kotlin
// Login user
val loginResult = repository.login(
    email = "user@example.com",
    password = "password123",
    campusId = "campus1",
    fcmToken = "fcm_token_here",
    userType = "parent"
)

when (loginResult) {
    is ConsolidatedUserRepository.LoginResult.Success -> {
        // Handle successful login
        val userData = loginResult.loginResponse.data
        Log.d("Login", "Welcome ${userData.fullName}")
    }
    is ConsolidatedUserRepository.LoginResult.Error -> {
        // Handle login error
        Log.e("Login", "Error: ${loginResult.message}")
    }
    is ConsolidatedUserRepository.LoginResult.Loading -> {
        // Handle loading state
        Log.d("Login", "Logging in...")
    }
}
```

### 3. User Data Operations
```kotlin
// Get current user
val userResult = repository.getCurrentUser()
when (userResult) {
    is ConsolidatedUserRepository.Result.Success -> {
        val userData = userResult.data
        // Handle user data (could be UserEntity or LoginData depending on storage mode)
        Log.d("User", "Current user: $userData")
    }
    is ConsolidatedUserRepository.Result.Error -> {
        Log.e("User", "Error: ${userResult.message}")
    }
}

// Get students
val studentsResult = repository.getStudents()
when (studentsResult) {
    is ConsolidatedUserRepository.Result.Success -> {
        val students = studentsResult.data
        // Handle students data (could be List<StudentEntity> or List<SharedStudent>)
        Log.d("Students", "Found ${students.size} students")
    }
    is ConsolidatedUserRepository.Result.Error -> {
        Log.e("Students", "Error: ${studentsResult.message}")
    }
}
```

### 4. Session Operations
```kotlin
// Check if user is logged in
val isLoggedIn = repository.isUserLoggedIn()
Log.d("Session", "User logged in: $isLoggedIn")

// Get current session
val sessionResult = repository.getCurrentSession()
when (sessionResult) {
    is ConsolidatedUserRepository.Result.Success -> {
        val session = sessionResult.data
        // Handle session data (could be SessionEntity or String depending on storage mode)
        Log.d("Session", "Current session: $session")
    }
    is ConsolidatedUserRepository.Result.Error -> {
        Log.e("Session", "Error: ${sessionResult.message}")
    }
}
```

### 5. Data Management Operations
```kotlin
// Update profile
val profileData = mapOf(
    "fullName" to "John Doe",
    "email" to "john.doe@example.com",
    "phone" to "+1234567890"
)

val updateResult = repository.updateProfile(profileData)
when (updateResult) {
    is ConsolidatedUserRepository.Result.Success -> {
        Log.d("Profile", "Profile updated successfully")
    }
    is ConsolidatedUserRepository.Result.Error -> {
        Log.e("Profile", "Error: ${updateResult.message}")
    }
}

// Clear user data (logout)
repository.clearUserData()
Log.d("Logout", "User data cleared")
```

### 6. Migration Operations
```kotlin
// Migrate from Paper DB to optimized database
val migrationResult = repository.migrateFromPaperDB()
if (migrationResult.success) {
    Log.d("Migration", "Migration successful: ${migrationResult.migratedItems.size} items migrated")
} else {
    Log.e("Migration", "Migration failed: ${migrationResult.errorMessage}")
}
```

### 7. Storage Mode Management
```kotlin
// Check current storage mode
val currentMode = repository.getStorageMode()
Log.d("Storage", "Current mode: $currentMode")

// Switch to legacy mode
repository.setStorageMode(false)

// Switch to optimized mode
repository.setStorageMode(true)
```

## Key Features of ConsolidatedUserRepository

### 1. Dual Storage Support
- **Optimized Storage**: Uses Room database for better performance and memory management
- **Legacy Storage**: Uses Paper DB for backward compatibility
- **Configurable**: Can switch between storage modes at runtime

### 2. Comprehensive User Management
- Login functionality with API integration
- User profile management
- Student data management
- Session management
- Data migration capabilities

### 3. Enhanced Error Handling
- All operations include comprehensive error handling
- Graceful fallbacks for failed operations
- Detailed logging for debugging
- Safe callback execution

### 4. Backward Compatibility
- Existing wrapper classes maintain API compatibility
- Gradual migration path available
- Deprecation warnings guide developers to new API

## Migration Steps

### Step 1: Update Imports
```kotlin
// Old imports
import topgrade.parent.com.parentseeks.Parent.Repository.UserRepository
import topgrade.parent.com.parentseeks.Parent.Repository.OptimizedUserRepository

// New import
import topgrade.parent.com.parentseeks.Parent.Repository.ConsolidatedUserRepository
```

### Step 2: Replace Class Usage
```kotlin
// Old way (UserRepository)
val userRepo = UserRepository(apiService)
userRepo.login(...)

// Old way (OptimizedUserRepository)
val optimizedRepo = OptimizedUserRepository(context, apiService)
optimizedRepo.login(...)

// New way
val consolidatedRepo = ConsolidatedUserRepository(context, apiService)
consolidatedRepo.login(...)
```

### Step 3: Update Method Calls
- Replace instance method calls with direct method calls
- Update parameter names where necessary
- Ensure proper error handling
- Handle different return types based on storage mode

### Step 4: Handle Storage Mode
```kotlin
// For legacy compatibility
repository.setStorageMode(false)

// For optimized performance
repository.setStorageMode(true)

// Migrate existing data
repository.migrateFromPaperDB()
```

### Step 5: Test Thoroughly
- Test all user data scenarios
- Verify storage mode switching
- Check data migration
- Validate error handling

## Benefits of Consolidation

### 1. Code Maintainability
- Single source of truth for user data management logic
- Easier to maintain and update
- Consistent behavior across the application

### 2. Performance Improvements
- Optimized database operations
- Better memory management
- Reduced code duplication
- Configurable storage backends

### 3. Enhanced Features
- Comprehensive error handling
- Better data migration capabilities
- Improved logging and debugging
- More robust data management

### 4. Developer Experience
- Consistent API across the application
- Better documentation and examples
- Easier to understand and use
- Clear migration path

## Troubleshooting

### Common Issues

1. **Context Requirements**
   - The consolidated repository requires a Context parameter
   - Ensure you're calling from an Activity or Fragment
   - Use Application context for long-lived operations

2. **Storage Mode Confusion**
   - Different storage modes return different data types
   - Check the storage mode before processing results
   - Use type checking when handling results

3. **Migration Issues**
   - Ensure Paper DB data exists before migration
   - Handle migration failures gracefully
   - Test migration with sample data first

4. **Type Safety**
   - Results can be different types depending on storage mode
   - Use proper type checking and casting
   - Handle unexpected data types gracefully

### Getting Help

If you encounter issues during migration:
1. Check the consolidated class documentation
2. Review the error logs for specific issues
3. Test with the wrapper classes first
4. Gradually migrate to direct usage
5. Verify storage mode settings

## Conclusion

The consolidation of User Repository classes provides a more robust, maintainable, and feature-rich solution for user data management in your application. The migration process is straightforward and provides backward compatibility to ensure a smooth transition.

For new code, use `ConsolidatedUserRepository` directly. For existing code, you can continue using the wrapper classes while gradually migrating to the new consolidated implementation.

The dual storage support allows you to choose the best storage backend for your needs while maintaining compatibility with existing data.
