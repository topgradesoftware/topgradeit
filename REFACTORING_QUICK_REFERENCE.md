# 🚀 Refactoring Quick Reference Guide

## Table of Contents
1. [Common Tasks](#common-tasks)
2. [User Data Operations](#user-data-operations)
3. [Logout Operations](#logout-operations)
4. [User Type Handling](#user-type-handling)
5. [Data Keys Reference](#data-keys-reference)
6. [Migration Checklist](#migration-checklist)

---

## Common Tasks

### Getting Current User Information
```kotlin
// Get user type (type-safe enum)
val userType = UserDataManager.getCurrentUserType()

// Get user name with fallback
val userName = UserDataManager.getCurrentUserName("Guest")

// Get user ID
val userId = UserDataManager.getCurrentUserId()

// Get display name
val displayName = UserDataManager.getUserDisplayName()
```

### Logging Out User
```kotlin
// Simple logout
LogoutManager.performLogout(context, apiService, onComplete, showLoadingCallback)

// Complete logout with dialog
LogoutManager.performCompleteLogout(context, apiService, showLoadingCallback)

// Just navigate after logout
LogoutManager.navigateAfterLogout(context)
```

### Checking Login Status
```kotlin
// Check if user is logged in
val isLoggedIn = UserDataManager.isLoggedIn()
// or
val isLoggedIn = LogoutManager.isLoggedIn()
```

---

## User Data Operations

### Saving Data
```kotlin
// Save any user data
UserDataManager.saveUserData(DataKeys.PARENT_NAME, "John Doe")
UserDataManager.saveUserData(DataKeys.CAMPUS_ID, "123")

// Update profile picture
UserDataManager.updateUserPicture("https://example.com/pic.jpg")
```

### Reading Data
```kotlin
// Read with default value
val name = UserDataManager.getUserData<String>(DataKeys.PARENT_NAME, "Guest")

// Get current user name (handles DEMO case automatically)
val userName = UserDataManager.getCurrentUserName("Default Name")

// Get profile picture
val pictureUrl = UserDataManager.getUserPicture()
```

### Deleting Data
```kotlin
// Delete specific data
UserDataManager.deleteUserData(DataKeys.PARENT_NAME)

// Clear all user data (logout)
UserDataManager.clearAllUserData()
```

### Debugging User Data
```kotlin
// Log all current user data (for debugging)
UserDataManager.logCurrentUserData()
```

---

## Logout Operations

### Simple Logout (from Activity)
```java
// Java
@Override
protected void logout() {
    LogoutManager.INSTANCE.performLogout(
        this,
        API.getAPIService(),
        () -> {
            LogoutManager.INSTANCE.navigateAfterLogout(this);
            return null;
        },
        (Boolean show) -> {
            showLoading(show);
            return null;
        }
    );
}
```

```kotlin
// Kotlin
override fun logout() {
    LogoutManager.performLogout(
        this,
        API.getAPIService(),
        onComplete = { LogoutManager.navigateAfterLogout(this) },
        showLoadingCallback = { show -> showLoading(show) }
    )
}
```

### Logout with Confirmation Dialog
```kotlin
// Shows dialog, then performs logout, then navigates
LogoutManager.performCompleteLogout(
    context = this,
    apiService = API.getAPIService(),
    showLoadingCallback = { show -> showLoading(show) }
)
```

### Custom Logout Flow
```kotlin
// Step 1: Show custom dialog
LogoutManager.showLogoutDialog(context) {
    // Step 2: Clear data
    LogoutManager.clearLoginData(context)
    
    // Step 3: Navigate
    LogoutManager.navigateAfterLogout(context)
}
```

---

## User Type Handling

### Getting User Type
```kotlin
// Get as enum (preferred)
val userType: UserType? = UserDataManager.getCurrentUserType()

// Convert from string
val userType: UserType? = UserType.fromString("PARENT")

// Check validity
val isValid: Boolean = UserType.isValid("STUDENT")
```

### Using User Type
```kotlin
when (userType) {
    UserType.PARENT -> {
        // Parent-specific logic
        val color = userType.getPrimaryColorRes()
        val theme = userType.getThemeIdentifier()
    }
    UserType.STUDENT -> {
        // Student-specific logic
    }
    UserType.TEACHER, UserType.STAFF -> {
        // Staff-specific logic
    }
    null -> {
        // Handle unknown user type
    }
}
```

### Getting User Type Properties
```kotlin
val userType = UserType.PARENT

// Get display name
val displayName = userType.displayName // "Parent Member"

// Get theme identifier
val theme = userType.getThemeIdentifier() // ThemeHelper.THEME_PARENT

// Get primary color resource
val colorRes = userType.getPrimaryColorRes() // R.color.parent_primary

// Get logout API endpoint
val endpoint = userType.getLogoutApiEndpoint() // "logout_parent"
```

---

## Data Keys Reference

### Authentication Keys
```kotlin
DataKeys.IS_LOGIN           // "islogin"
DataKeys.USER_TYPE          // "User_Type"
DataKeys.PASSWORD           // "password"
```

### Parent Keys
```kotlin
DataKeys.PARENT_ID          // "parent_id"
DataKeys.PARENT_NAME        // "full_name"
DataKeys.PARENT_PHONE       // "phone"
DataKeys.PARENT_EMAIL       // "email"
DataKeys.PARENT_PICTURE     // "picture"
```

### Student Keys
```kotlin
DataKeys.STUDENT_ID         // "student_id"
DataKeys.STUDENT_NAME       // "student_name"
DataKeys.STUDENT_PHONE      // "student_phone"
DataKeys.STUDENT_PICTURE    // "student_picture"
DataKeys.STUDENTS           // "students"
```

### Staff Keys
```kotlin
DataKeys.STAFF_ID           // "staff_id"
DataKeys.STAFF_NAME         // "full_name"
DataKeys.STAFF_PHONE        // "staff_phone"
DataKeys.STAFF_PICTURE      // "staff_picture"
DataKeys.STAFF_PASSWORD     // "staff_password"
```

### Campus Keys
```kotlin
DataKeys.CAMPUS_ID          // "campus_id"
DataKeys.CAMPUS_NAME        // "campus_name"
DataKeys.CAMPUS_LIST        // "campus_list"
DataKeys.CURRENT_SESSION    // "current_session"
```

### Dynamic Key Methods
```kotlin
// Get user-type-specific keys
val idKey = DataKeys.getUserIdKey(userType)
val nameKey = DataKeys.getUserNameKey(userType)
val phoneKey = DataKeys.getUserPhoneKey(userType)
val pictureKey = DataKeys.getUserPictureKey(userType)

// Get all keys that should be cleared on logout
val allKeys = DataKeys.getAllUserDataKeys()
```

---

## Migration Checklist

### Migrating from Old Code to New Utilities

#### ✅ Step 1: Replace Hardcoded Strings
```kotlin
// OLD
val name = Paper.book().read("full_name", "")

// NEW
val name = Paper.book().read(DataKeys.PARENT_NAME, "")
// or better:
val name = UserDataManager.getCurrentUserName("Default")
```

#### ✅ Step 2: Replace String User Types with Enum
```kotlin
// OLD
val userType = "PARENT"
if (userType == "PARENT") { ... }

// NEW
val userType = UserType.PARENT
when (userType) {
    UserType.PARENT -> { ... }
}
```

#### ✅ Step 3: Use UserDataManager for Data Operations
```kotlin
// OLD
Paper.init(context)
val name = Paper.book().read("full_name", "")
if ("DEMO".equals(name)) {
    name = "Parent Member"
    Paper.book().write("full_name", name)
}

// NEW
val name = UserDataManager.getCurrentUserName("Parent Member")
```

#### ✅ Step 4: Use LogoutManager for Logout
```kotlin
// OLD
showLoading(true)
val userId = Paper.book().read("parent_id", "")
val campusId = Paper.book().read("campus_id", "")
val body = createRequestBody(userId, campusId)
API.getAPIService().logout_parent(body).enqueue(...)
// ... 50+ lines of code

// NEW
LogoutManager.performLogout(context, apiService, onComplete, showLoadingCallback)
```

#### ✅ Step 5: Remove Paper.init() from Activities
```kotlin
// OLD (in Activity)
override fun onCreate() {
    Paper.init(this) // ❌ Don't do this
}

// NEW (in Application class only)
class TopgradeApplication : Application() {
    override fun onCreate() {
        Paper.init(this) // ✅ Only here
    }
}
```

---

## Best Practices

### ✅ DO's
1. **Use DataKeys constants** instead of hardcoded strings
2. **Use UserType enum** instead of string comparisons
3. **Use UserDataManager** for all data operations
4. **Use LogoutManager** for logout functionality
5. **Initialize PaperDB only once** in Application class
6. **Handle null cases** gracefully with default values
7. **Log operations** for debugging

### ❌ DON'Ts
1. **Don't hardcode data keys** - use DataKeys constants
2. **Don't use string-based user types** - use UserType enum
3. **Don't duplicate logout logic** - use LogoutManager
4. **Don't call Paper.init() in activities** - initialize in Application
5. **Don't forget error handling** - always handle exceptions
6. **Don't ignore null cases** - provide sensible defaults

---

## Common Patterns

### Pattern 1: Dashboard Activity
```java
public class CustomDashboard extends BaseMainDashboard {
    
    @Override
    protected int getLayoutResource() {
        return R.layout.activity_custom_dashboard;
    }
    
    @Override
    protected int getPrimaryColor() {
        return R.color.custom_primary;
    }
    
    @Override
    protected String getUserType() {
        return ThemeHelper.THEME_CUSTOM;
    }
    
    @Override
    protected String getDisplayName() {
        return "Custom Member";
    }
    
    // loadDataAsync() and logout() are inherited!
    // No need to override unless you need custom behavior
}
```

### Pattern 2: Checking User Type Before Action
```kotlin
val userType = UserDataManager.getCurrentUserType()

when (userType) {
    UserType.PARENT -> {
        // Parent can do this
        startActivity(Intent(this, ParentSpecificActivity::class.java))
    }
    UserType.STUDENT -> {
        // Student cannot do this
        Toast.makeText(this, "Students cannot access this feature", Toast.LENGTH_SHORT).show()
    }
    UserType.TEACHER, UserType.STAFF -> {
        // Staff can do this
        startActivity(Intent(this, StaffSpecificActivity::class.java))
    }
    null -> {
        // Unknown user type, redirect to login
        startActivity(Intent(this, SelectRole::class.java))
        finish()
    }
}
```

### Pattern 3: Loading User Profile
```kotlin
// Load all user info at once
fun loadUserProfile() {
    val userType = UserDataManager.getCurrentUserType()
    val userName = UserDataManager.getCurrentUserName("Guest")
    val userId = UserDataManager.getCurrentUserId()
    val campusId = UserDataManager.getCampusId()
    val campusName = UserDataManager.getCampusName()
    val pictureUrl = UserDataManager.getUserPicture()
    
    // Update UI
    tvUserName.text = userName
    tvUserId.text = "ID: $userId"
    tvCampus.text = campusName
    Glide.with(this).load(pictureUrl).into(ivProfilePicture)
}
```

---

## Troubleshooting

### Problem: User name shows "DEMO"
**Solution:** The new code handles this automatically!
```kotlin
// This will never return "DEMO"
val name = UserDataManager.getCurrentUserName("Default")
```

### Problem: Logout not clearing all data
**Solution:** Use LogoutManager which clears everything
```kotlin
LogoutManager.clearLoginData(context)
```

### Problem: User type is null
**Solution:** Always check for null and handle gracefully
```kotlin
val userType = UserDataManager.getCurrentUserType()
if (userType == null) {
    // User not logged in or invalid state
    navigateToLogin()
    return
}
```

### Problem: ClassCastException when reading data
**Solution:** Use type-safe methods
```kotlin
// BAD
val name = Paper.book().read("name") // Type is Any?

// GOOD
val name = UserDataManager.getUserData<String>(DataKeys.PARENT_NAME, "")
```

---

## Examples

### Example 1: Custom Dashboard with Override
```java
public class TeacherDashboard extends BaseMainDashboard {
    
    @Override
    protected void loadDataAsync() {
        // Custom loading logic if needed
        super.loadDataAsync(); // Call parent first
        
        // Then add custom logic
        ConsolidatedANRPreventionHelper.executeInBackground(
            () -> loadTeacherSpecificData(),
            data -> updateUIWithData(data),
            error -> handleError(error)
        );
    }
    
    @Override
    protected void logout() {
        // Custom logout confirmation
        new AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("You have unsaved changes. Still logout?")
            .setPositiveButton("Yes", (d, w) -> super.logout())
            .setNegativeButton("No", null)
            .show();
    }
}
```

### Example 2: Utility Class Using User Data
```kotlin
object NotificationManager {
    
    fun sendNotification(title: String, message: String) {
        val userType = UserDataManager.getCurrentUserType()
        val userId = UserDataManager.getCurrentUserId()
        
        if (userType == null || userId == null) {
            Log.e(TAG, "Cannot send notification: user not logged in")
            return
        }
        
        val endpoint = when (userType) {
            UserType.PARENT -> "/api/parent/notify"
            UserType.STUDENT -> "/api/student/notify"
            UserType.TEACHER, UserType.STAFF -> "/api/staff/notify"
        }
        
        // Send notification
        sendToServer(endpoint, userId, title, message)
    }
}
```

---

## Performance Tips

1. **Cache UserType**: Don't call `getCurrentUserType()` repeatedly in loops
2. **Batch Operations**: Use `clearAllUserData()` instead of deleting keys one by one
3. **Avoid UI Thread**: Use `ConsolidatedANRPreventionHelper` for async operations
4. **Lazy Initialization**: Only load data when needed

---

## Testing

### Unit Testing UserDataManager
```kotlin
@Test
fun testGetCurrentUserName() {
    // Setup
    Paper.book().write(DataKeys.PARENT_NAME, "John Doe")
    Paper.book().write(DataKeys.USER_TYPE, "PARENT")
    
    // Execute
    val name = UserDataManager.getCurrentUserName("Default")
    
    // Verify
    assertEquals("John Doe", name)
}

@Test
fun testHandleDemoName() {
    // Setup
    Paper.book().write(DataKeys.PARENT_NAME, "DEMO")
    Paper.book().write(DataKeys.USER_TYPE, "PARENT")
    
    // Execute
    val name = UserDataManager.getCurrentUserName("Default")
    
    // Verify
    assertNotEquals("DEMO", name)
    assertEquals("Parent Member", name)
}
```

---

**Last Updated:** October 15, 2025  
**Version:** 1.0  
**For:** Topgradeit v5.0.0

