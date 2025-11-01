# App Launch Fix - Role Selection Screen

## Issue
The app was launching directly to the parent login instead of showing the role selection screen with options for Admin, Campus, Super Admin, and Staff login.

## Root Cause
The `Splash` activity was checking for stored login data and automatically navigating to the appropriate dashboard if the user was already logged in, bypassing the role selection screen.

## Problem Location
**File**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/Splash.java`

**Method**: `loadLoginDataInBackground()`

The method was checking:
```java
final boolean isLoggedIn = Paper.book().read(Constants.is_login, false);
final String userType = Paper.book().read(Constants.User_Type, "");

if (!isLoggedIn || userType.isEmpty()) {
    navigateToRoleSelection();
} else {
    loadAdditionalDataInBackground(userType); // This loads dashboard directly
}
```

## Solution Applied

### 1. Clear Stored Login Data
Added a method to clear all stored login data:
```java
private void clearStoredLoginData() {
    try {
        Paper.book().delete(Constants.is_login);
        Paper.book().delete(Constants.User_Type);
        Paper.book().delete("parent_id");
        Paper.book().delete("campus_id");
        Paper.book().delete("students");
        Paper.book().delete("campus_name");
        Log.d("Splash", "Cleared stored login data");
    } catch (Exception e) {
        Log.e("Splash", "Error clearing stored login data", e);
    }
}
```

### 2. Force Role Selection
Modified `loadLoginDataInBackground()` to always show the role selection screen:
```java
private void loadLoginDataInBackground() {
    executor.execute(new Runnable() {
        @Override
        public void run() {
            try {
                // Clear stored login data to force role selection
                clearStoredLoginData();
                
                // Always navigate to role selection
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isActivityDestroyed) return;
                        Log.d("Splash", "Navigating to SelectRole (forced)");
                        navigateToRoleSelection();
                    }
                });
            } catch (Exception e) {
                Log.e("Splash", "Error loading login data", e);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isActivityDestroyed) {
                            navigateToRoleSelection();
                        }
                    }
                });
            }
        }
    });
}
```

## Role Selection Screen Options
The `SelectRole` activity now properly displays:

1. **Admin Login** (Red card) - For administrative access
2. **Campus Login** (Green card) - For campus-specific access  
3. **Super Admin** (Blue card) - For super administrator access
4. **Staff Login** (Navy blue card) - For staff/teacher access

## Expected Behavior
Now when the app launches:
1. Splash screen appears briefly
2. Role selection screen shows with 4 login options
3. User can choose their appropriate login type
4. App navigates to the selected login screen

## Files Modified
- `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/Splash.java`

## Testing
To verify the fix:
1. Launch the app
2. Should see role selection screen with 4 colored cards
3. Each card should navigate to appropriate login screen
4. No more direct navigation to parent login

## Notes
- The fix clears all stored login data on app launch
- This ensures users always see the role selection screen
- Users will need to log in again after this change
- The role selection screen uses the navy blue color scheme as preferred
