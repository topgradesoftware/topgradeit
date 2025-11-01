# Multi-Profile Biometric Authentication System

## Overview

The enhanced biometric authentication system now supports multiple user profiles (Staff, Student, Parent) with separate storage for each user type. This allows different users to use biometric login without interfering with each other's credentials.

## Key Features

### 🔐 Separate Storage Per User Type
- **Parent Profile**: Stored in `BiometricPrefs_PARENT`
- **Student Profile**: Stored in `BiometricPrefs_STUDENT` 
- **Teacher Profile**: Stored in `BiometricPrefs_TEACHER`

### 👥 Multi-Profile Support
- Users can have biometric credentials for multiple account types
- Profile selection dialog when multiple profiles exist
- Automatic profile detection for single profile scenarios

### 🔒 Enhanced Security
- Encrypted storage using Android Keystore
- Separate encryption keys for each user type
- User authentication required for key access

## Implementation Details

### BiometricManager Class

#### New Methods Added:

```kotlin
// Check biometric status for specific user type
fun isBiometricEnabled(userType: String): Boolean

// Check if any user type has biometric enabled
fun isAnyBiometricEnabled(): Boolean

// Enable biometric for specific user type
fun enableBiometric(userType: String, email: String, password: String, campusId: String, userId: String? = null)

// Disable biometric for specific user type
fun disableBiometric(userType: String)

// Get stored credentials for specific user type
fun getStoredCredentials(userType: String): UserCredentials?

// Get all stored user profiles
fun getAllStoredProfiles(): List<UserProfile>

// Show biometric selection dialog for multiple profiles
fun showBiometricSelectionDialog(activity: FragmentActivity, onProfileSelected: (UserProfile) -> Unit, onCancel: () -> Unit)
```

#### Data Classes:

```kotlin
data class UserCredentials(
    val email: String,
    val password: String,
    val campusId: String,
    val userType: String,
    val userId: String?
)

data class UserProfile(
    val userType: String,
    val email: String,
    val userId: String?,
    val campusId: String
)
```

### User Type Constants

```kotlin
const val USER_TYPE_PARENT = "PARENT"
const val USER_TYPE_STUDENT = "STUDENT"
const val USER_TYPE_TEACHER = "TEACHER"
```

## Usage Examples

### 1. Enabling Biometric for Parent Account

```kotlin
val biometricManager = BiometricManager(context)
biometricManager.enableBiometric(
    userType = BiometricManager.USER_TYPE_PARENT,
    email = "parent@example.com",
    password = "password123",
    campusId = "CAMPUS001"
)
```

### 2. Checking Biometric Status

```kotlin
// Check if parent has biometric enabled
val isParentBiometricEnabled = biometricManager.isBiometricEnabled(BiometricManager.USER_TYPE_PARENT)

// Check if any account has biometric enabled
val isAnyBiometricEnabled = biometricManager.isAnyBiometricEnabled()
```

### 3. Biometric Login with Profile Selection

```kotlin
val biometricManager = BiometricManager(this)

// Get all stored profiles
val profiles = biometricManager.getAllStoredProfiles()

if (profiles.size > 1) {
    // Show profile selection dialog
    biometricManager.showBiometricSelectionDialog(
        activity = this,
        onProfileSelected = { profile ->
            // Handle selected profile
            performBiometricLoginForProfile(profile.userType)
        },
        onCancel = {
            // Handle cancellation
        }
    )
} else if (profiles.size == 1) {
    // Use single profile directly
    performBiometricLoginForProfile(profiles[0].userType)
}
```

### 4. Performing Biometric Authentication

```kotlin
biometricManager.showBiometricPrompt(
    activity = this,
    userType = BiometricManager.USER_TYPE_PARENT,
    onSuccess = { credentials ->
        // Handle successful authentication
        loginWithCredentials(credentials.email, credentials.password, credentials.campusId)
    },
    onError = { errorMessage ->
        // Handle authentication error
        showError(errorMessage)
    },
    onFailed = {
        // Handle authentication failure
        showFailureMessage()
    }
)
```

## Storage Structure

### Encrypted SharedPreferences Files:
- `BiometricPrefs_PARENT` - Parent account credentials
- `BiometricPrefs_STUDENT` - Student account credentials  
- `BiometricPrefs_TEACHER` - Teacher account credentials

### Stored Data Per Profile:
```json
{
  "biometric_enabled": true,
  "user_credentials": "email|password|campusId",
  "user_type": "PARENT",
  "user_id": "parent@example.com"
}
```

## Security Features

### 🔐 Encryption
- Uses Android Keystore for key management
- AES256-GCM encryption for sensitive data
- Separate encryption keys per user type

### 🛡️ Access Control
- User authentication required for key access
- Biometric authentication required for credential retrieval
- Secure deletion of credentials

### 🔒 Data Protection
- Encrypted storage of all sensitive information
- No plain text storage of passwords
- Secure credential clearing

## Migration from Single Profile

The system is backward compatible. Existing single-profile biometric data will continue to work, but new installations will use the multi-profile system.

### Migration Steps:
1. Check for existing biometric data
2. If found, migrate to new multi-profile structure
3. Clear old single-profile data
4. Enable new multi-profile system

## Error Handling

### Common Error Scenarios:
- **No biometric hardware**: Graceful fallback to password login
- **No enrolled biometrics**: Guide user to device settings
- **Authentication failure**: Retry mechanism with user feedback
- **Profile not found**: Clear error message and fallback options

### Error Recovery:
- Automatic retry for temporary failures
- Clear error messages for user guidance
- Fallback to manual login when needed

## Testing

### Test Scenarios:
1. **Single Profile**: Login with one account type
2. **Multiple Profiles**: Login with different account types
3. **Profile Selection**: Test dialog with multiple profiles
4. **Security**: Verify encrypted storage and access control
5. **Error Handling**: Test various error scenarios

### Test Cases:
- ✅ Enable biometric for Parent account
- ✅ Enable biometric for Student account  
- ✅ Enable biometric for Teacher account
- ✅ Login with biometric for each account type
- ✅ Profile selection with multiple accounts
- ✅ Disable biometric for specific account
- ✅ Clear all biometric data
- ✅ Error handling for various scenarios

## Benefits

### 🎯 User Experience
- Faster login for multiple account types
- Clear profile selection interface
- Seamless switching between accounts

### 🔒 Security
- Isolated credential storage per user type
- Enhanced encryption and access control
- Secure credential management

### 🛠️ Maintainability
- Clean separation of concerns
- Extensible architecture for new user types
- Comprehensive error handling

## Future Enhancements

### Potential Improvements:
1. **Profile Management UI**: Allow users to manage multiple profiles
2. **Profile Naming**: Custom names for different accounts
3. **Profile Icons**: Visual indicators for different account types
4. **Auto-login Preferences**: Remember last used profile
5. **Profile Sync**: Sync profiles across devices (if needed)

### Additional User Types:
- Support for additional user roles
- Custom user type definitions
- Role-based access control

## Troubleshooting

### Common Issues:

1. **Biometric not working after update**
   - Clear all biometric data and re-enable
   - Check device biometric settings

2. **Profile selection not showing**
   - Verify multiple profiles exist
   - Check biometric availability

3. **Authentication failures**
   - Verify biometric enrollment
   - Check device compatibility

### Debug Commands:
```kotlin
// Clear all biometric data
biometricManager.clearAllBiometricData()

// Check biometric status
val status = biometricManager.getBiometricStatus()

// List all profiles
val profiles = biometricManager.getAllStoredProfiles()
```

## Conclusion

The multi-profile biometric authentication system provides a secure, user-friendly way to manage multiple account types while maintaining the highest security standards. The system is designed to be extensible, maintainable, and backward compatible. 