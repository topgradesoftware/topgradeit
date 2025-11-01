# 📊 Analytics Quick Reference

## 🚀 Common Tasks

### Track Screen View
```kotlin
// Automatic in BaseMainDashboard
ScreenTrackingHelper.trackScreenView(this)
```

### Track Button Click
```kotlin
AnalyticsManager.logButtonClick("submit_button", "SubmitScreen")
```

### Track Card Click
```kotlin
AnalyticsManager.logDashboardCardClick("Child Academics", "PARENT")
```

### Track Login
```kotlin
AnalyticsManager.logLogin("email_password", "PARENT")
```

### Track Error
```kotlin
AnalyticsManager.logError("api_error", errorMessage, "ScreenName")
```

---

## 📋 Event Reference

| Category | Method | When to Use |
|----------|--------|-------------|
| **Auth** | `logLogin()` | User logs in |
| **Auth** | `logLogout()` | User logs out |
| **Navigation** | `logDashboardCardClick()` | Card clicked |
| **Navigation** | `logMenuClick()` | Menu item clicked |
| **Academic** | `logAttendanceView()` | View attendance |
| **Academic** | `logResultView()` | View results |
| **Academic** | `logAssignmentSubmit()` | Submit assignment |
| **Financial** | `logFeePaymentView()` | View fee challan |
| **Financial** | `logFeeChallanDownload()` | Download challan |
| **Communication** | `logDiaryView()` | View diary |
| **Communication** | `logDiarySend()` | Send diary entry |
| **Error** | `logError()` | Any error occurs |
| **Error** | `logApiError()` | API call fails |
| **Performance** | `logScreenLoadTime()` | Screen loads |
| **Performance** | `logApiResponseTime()` | API responds |

---

## 🎯 Quick Patterns

### Pattern 1: Track Activity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ScreenTrackingHelper.startLoadTimeTracking(this)
}

override fun onResume() {
    super.onResume()
    ScreenTrackingHelper.trackScreenView(this)
}
```

### Pattern 2: Track API Call
```kotlin
fun loadData() {
    PerformanceTracker.startTracking("load_data_api")
    
    apiService.getData().enqueue(object : Callback<Data> {
        override fun onResponse(call: Call<Data>, response: Response<Data>) {
            PerformanceTracker.stopTracking("load_data_api", "api")
            
            if (response.isSuccessful) {
                // Success
            } else {
                AnalyticsManager.logApiError("/api/data", response.code(), "Error")
            }
        }
        
        override fun onFailure(call: Call<Data>, t: Throwable) {
            PerformanceTracker.stopTracking("load_data_api", "api")
            AnalyticsManager.logApiError("/api/data", 0, t.message ?: "Unknown")
        }
    })
}
```

### Pattern 3: Track User Action
```kotlin
button.setOnClickListener {
    AnalyticsManager.logButtonClick("submit", "FormScreen")
    // Your action
}
```

### Pattern 4: Track Performance
```kotlin
// Method 1: Manual
PerformanceTracker.startTracking("operation_id")
performOperation()
PerformanceTracker.stopTracking("operation_id")

// Method 2: Automatic
val result = trackPerformance("operation_name") {
    performOperation()
}
```

---

## 📊 Event Constants

Use constants from `AnalyticsEvents`:

```kotlin
// Authentication
AnalyticsEvents.Auth.LOGIN
AnalyticsEvents.Auth.LOGOUT

// Dashboard
AnalyticsEvents.Dashboard.CARD_CLICK
AnalyticsEvents.Dashboard.MENU_OPEN

// Academic
AnalyticsEvents.Academic.VIEW_ATTENDANCE
AnalyticsEvents.Academic.VIEW_RESULT

// Financial
AnalyticsEvents.Financial.VIEW_FEE_CHALLAN
AnalyticsEvents.Financial.DOWNLOAD_FEE_CHALLAN

// Communication
AnalyticsEvents.Communication.VIEW_DIARY
AnalyticsEvents.Communication.SEND_DIARY
```

---

## 🔍 Debugging

### View Events in Logcat
```bash
adb logcat -s AnalyticsManager
```

### Enable Firebase DebugView
```bash
adb shell setprop debug.firebase.analytics.app topgrade.parent.com.parentseeks
```

### Test Event
```kotlin
AnalyticsManager.logCustomEvent("test_event", mapOf("test" to "value"))
```

---

## 📈 Firebase Console

**Path to Analytics:**
1. [Firebase Console](https://console.firebase.google.com/)
2. Select Project: Topgradeit
3. Analytics > Dashboard

**Key Reports:**
- **Events** - All tracked events
- **User Properties** - User segments
- **Realtime** - Live activity
- **Retention** - User retention
- **Funnels** - Conversion paths

---

## 💡 Tips

1. **Always track user_type** - Essential for segmentation
2. **Track errors** - Proactive bug detection
3. **Monitor performance** - Identify slow screens
4. **Use constants** - From AnalyticsEvents class
5. **Test in DebugView** - Before production

---

## ⚠️ Privacy

**What to Track:** ✅
- User IDs (anonymized)
- User types
- Feature usage
- Performance metrics

**What NOT to Track:** ❌
- Passwords
- Email addresses
- Phone numbers
- Personal messages

---

**Quick Reference v1.0** | Oct 15, 2025  
See `ANALYTICS_TRACKING_GUIDE.md` for complete documentation

