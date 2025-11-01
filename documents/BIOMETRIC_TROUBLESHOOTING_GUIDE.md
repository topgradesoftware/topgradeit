# Biometric Storage Troubleshooting Guide

## The Problem

Multiple users (Parent, Student, Teacher) using the same mobile device have their biometric data mixed up or overwritten because the data is being saved globally on the device.

## Root Cause Analysis

### Previous Implementation Issues:
1. **Global Storage**: All biometric data was stored in a single encrypted preference file
2. **No User Isolation**: Different users shared the same storage space
3. **Data Overwrite**: When User B enabled biometric, it would overwrite User A's data
4. **No User Identification**: System couldn't distinguish between different users

### Current Solution:
1. **User-Specific Storage**: Each user gets their own encrypted preference file
2. **Unique Storage Keys**: Format: `userType_userId` (e.g., `PARENT_parent123@email.com`)
3. **Complete Isolation**: No cross-user data access
4. **File System Scanning**: Scans all preference files to find all stored profiles

## How to Test the Fix

### 1. Use the Test Activity

I've created a test activity to verify the fix:

```kotlin
// Add this to your AndroidManifest.xml
<activity android:name=".Parent.Activity.BiometricTestActivity" />
```

### 2. Test Steps:

1. **Launch the test activity**
2. **Enable Parent Biometric** - Click "Enable Parent Biometric"
3. **Enable Student Biometric** - Click "Enable Student Biometric"  
4. **Enable Teacher Biometric** - Click "Enable Teacher Biometric"
5. **Check Status** - Click "Refresh Status"

### 3. Expected Results:

You should see:
```
Stored Profiles:
• PARENT - parent123@email.com
• STUDENT - student456@email.com
• TEACHER - teacher789@email.com
```

## Manual Testing Without Test Activity

### 1. Test Parent Account:
```kotlin
val biometricManager = BiometricManager(context)

// Enable biometric for parent
biometricManager.enableBiometric(
    userType = BiometricManager.USER_TYPE_PARENT,
    email = "parent@email.com",
    password = "parent123",
    campusId = "CAMPUS001",
    userId = "parent@email.com"
)

// Check if enabled
val isEnabled = biometricManager.isBiometricEnabled(
    BiometricManager.USER_TYPE_PARENT, 
    "parent@email.com"
)
println("Parent biometric enabled: $isEnabled")
```

### 2. Test Student Account:
```kotlin
// Enable biometric for student
biometricManager.enableBiometric(
    userType = BiometricManager.USER_TYPE_STUDENT,
    email = "student@email.com",
    password = "student123",
    campusId = "CAMPUS001",
    userId = "student@email.com"
)

// Check if enabled
val isEnabled = biometricManager.isBiometricEnabled(
    BiometricManager.USER_TYPE_STUDENT, 
    "student@email.com"
)
println("Student biometric enabled: $isEnabled")
```

### 3. Verify Both Exist:
```kotlin
val profiles = biometricManager.getAllStoredProfiles()
println("Total profiles: ${profiles.size}")
profiles.forEach { profile ->
    println("${profile.userType} - ${profile.email}")
}
```

## Debug Commands

### 1. List All Profiles:
```kotlin
val debugInfo = biometricManager.debugListAllProfiles()
println(debugInfo)
```

### 2. Check Specific User:
```kotlin
val parentEnabled = biometricManager.isBiometricEnabled("PARENT", "parent@email.com")
val studentEnabled = biometricManager.isBiometricEnabled("STUDENT", "student@email.com")
println("Parent: $parentEnabled, Student: $studentEnabled")
```

### 3. Clear All Data:
```kotlin
biometricManager.clearAllBiometricData()
```

## Storage File Locations

### Encrypted Preference Files:
- Location: `/data/data/your.package.name/shared_prefs/`
- Files: `BiometricPrefs_PARENT_*.xml`, `BiometricPrefs_STUDENT_*.xml`, etc.

### Example Files:
```
BiometricPrefs_PARENT_parent123@email.com.xml
BiometricPrefs_STUDENT_student456@email.com.xml
BiometricPrefs_TEACHER_teacher789@email.com.xml
```

## Common Issues and Solutions

### Issue 1: Still seeing global storage
**Solution**: Make sure you're using the updated BiometricManager with user-specific methods

### Issue 2: Profiles not showing up
**Solution**: Check if the file scanning is working:
```kotlin
val profiles = biometricManager.getAllStoredProfiles()
if (profiles.isEmpty()) {
    println("No profiles found - checking file system...")
    // Add debug logging
}
```

### Issue 3: Biometric not working for specific user
**Solution**: Verify the user ID is correct:
```kotlin
val credentials = biometricManager.getStoredCredentials(userType, userId)
if (credentials == null) {
    println("No credentials found for $userType with ID $userId")
}
```

## Verification Steps

### Step 1: Clear All Data
```kotlin
biometricManager.clearAllBiometricData()
```

### Step 2: Enable Multiple Users
```kotlin
// Enable parent
biometricManager.enableBiometric("PARENT", "parent@email.com", "pass1", "CAMPUS1", "parent@email.com")

// Enable student  
biometricManager.enableBiometric("STUDENT", "student@email.com", "pass2", "CAMPUS1", "student@email.com")

// Enable teacher
biometricManager.enableBiometric("TEACHER", "teacher@email.com", "pass3", "CAMPUS1", "teacher@email.com")
```

### Step 3: Verify All Exist
```kotlin
val profiles = biometricManager.getAllStoredProfiles()
assert(profiles.size == 3) // Should have 3 profiles
```

### Step 4: Test Individual Access
```kotlin
val parentCreds = biometricManager.getStoredCredentials("PARENT", "parent@email.com")
val studentCreds = biometricManager.getStoredCredentials("STUDENT", "student@email.com")
val teacherCreds = biometricManager.getStoredCredentials("TEACHER", "teacher@email.com")

assert(parentCreds != null)
assert(studentCreds != null) 
assert(teacherCreds != null)
```

## Integration with LoginScreen

### Updated LoginScreen Code:
```kotlin
// In LoginScreen.kt - observeLoginState()
if (!isBiometricLogin) {
    val email = usernam ?: ""
    val userPassword = password ?: ""
    val campusId = seleted_campus ?: ""
    
    if (email.isNotEmpty() && userPassword.isNotEmpty() && campusId.isNotEmpty()) {
        val biometricManager = BiometricManager(this@LoginScreen)
        biometricManager.enableBiometric(userType, email, userPassword, campusId, email)
        
        showBiometricSetupSuggestion()
    }
}
```

### Updated BiometricSetupActivity:
```kotlin
// In BiometricSetupActivity.kt
currentUserEmail = intent.getStringExtra("user_email") ?: ""

// Check status with user email
if (biometricManager.isBiometricEnabled(currentUserType, currentUserEmail)) {
    // Show disable button
} else {
    // Show enable button
}
```

## Final Verification

After implementing the fix, test this scenario:

1. **User A (Parent)** logs in and enables biometric
2. **User B (Student)** logs in and enables biometric  
3. **User C (Teacher)** logs in and enables biometric
4. **All three users** should be able to use biometric login independently
5. **No data conflicts** should occur

## Expected Behavior

### ✅ **Correct Behavior:**
- Each user has their own biometric storage
- No interference between users
- All users can enable/disable biometric independently
- Profile selection shows all available users

### ❌ **Incorrect Behavior:**
- Only one user can have biometric enabled
- Enabling biometric for User B overwrites User A's data
- Profile selection doesn't work
- Biometric data is shared between users

## If Still Having Issues

1. **Check the logs** for any error messages
2. **Verify file permissions** for shared preferences
3. **Test with the test activity** to isolate the issue
4. **Clear all data** and test from scratch
5. **Check if the device supports biometric** authentication

## Contact Support

If you're still experiencing issues after following this guide, please provide:
1. Device model and Android version
2. Logcat output during testing
3. Steps to reproduce the issue
4. Screenshots of the test activity results 