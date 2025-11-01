@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Centralized Analytics Manager for Firebase Analytics
 * Tracks user behavior, app events, and performance metrics
 * 
 * Features:
 * - User behavior tracking
 * - Screen view tracking
 * - Event tracking
 * - User properties
 * - E-commerce tracking
 * - Custom events
 * - Performance monitoring
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object AnalyticsManager {
    
    private const val TAG = "AnalyticsManager"
    private lateinit var analytics: FirebaseAnalytics
    private var isInitialized = false
    
    /**
     * Initialize Analytics Manager
     * Call this in Application.onCreate()
     */
    fun initialize(_context: Context) {
        try {
            analytics = Firebase.analytics
            isInitialized = true
            Log.d(TAG, "Analytics initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize analytics", e)
        }
    }
    
    /**
     * Enable/Disable analytics collection
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        if (!isInitialized) return
        analytics.setAnalyticsCollectionEnabled(enabled)
        Log.d(TAG, "Analytics collection ${if (enabled) "enabled" else "disabled"}")
    }
    
    // ==================== SCREEN TRACKING ====================
    
    /**
     * Log screen view
     */
    fun logScreenView(screenName: String, screenClass: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        
        Log.d(TAG, "Screen viewed: $screenName ($screenClass)")
    }
    
    // ==================== USER PROPERTIES ====================
    
    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String?) {
        if (!isInitialized) return
        analytics.setUserProperty(name, value)
        Log.d(TAG, "User property set: $name = $value")
    }
    
    /**
     * Set user ID
     */
    fun setUserId(userId: String?) {
        if (!isInitialized) return
        analytics.setUserId(userId)
        Log.d(TAG, "User ID set: $userId")
    }
    
    /**
     * Set user type (Parent, Student, Teacher)
     */
    fun setUserType(userType: String) {
        setUserProperty("user_type", userType)
    }
    
    /**
     * Set user campus
     */
    fun setUserCampus(campusId: String, campusName: String) {
        setUserProperty("campus_id", campusId)
        setUserProperty("campus_name", campusName)
    }
    
    /**
     * Set user grade/class
     */
    fun setUserGrade(grade: String) {
        setUserProperty("grade", grade)
    }
    
    // ==================== AUTHENTICATION EVENTS ====================
    
    /**
     * Log login event
     */
    fun logLogin(method: String, userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
            param("user_type", userType)
        }
        
        Log.d(TAG, "Login: $method as $userType")
    }
    
    /**
     * Log logout event
     */
    fun logLogout(userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent("logout") {
            param("user_type", userType)
        }
        
        Log.d(TAG, "Logout: $userType")
    }
    
    /**
     * Log signup event
     */
    fun logSignUp(method: String, userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
            param("user_type", userType)
        }
        
        Log.d(TAG, "Sign up: $method as $userType")
    }
    
    // ==================== ENGAGEMENT EVENTS ====================
    
    /**
     * Log dashboard card click
     */
    fun logDashboardCardClick(cardName: String, userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent("dashboard_card_click") {
            param("card_name", cardName)
            param("user_type", userType)
        }
        
        Log.d(TAG, "Dashboard card clicked: $cardName")
    }
    
    /**
     * Log menu item click
     */
    fun logMenuClick(menuItem: String, userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_NAME, menuItem)
            param("user_type", userType)
            param("item_category", "menu")
        }
        
        Log.d(TAG, "Menu clicked: $menuItem")
    }
    
    /**
     * Log button click
     */
    fun logButtonClick(buttonName: String, screenName: String) {
        if (!isInitialized) return
        
        analytics.logEvent("button_click") {
            param("button_name", buttonName)
            param("screen_name", screenName)
        }
        
        Log.d(TAG, "Button clicked: $buttonName on $screenName")
    }
    
    // ==================== ACADEMIC EVENTS ====================
    
    /**
     * Log attendance view
     */
    fun logAttendanceView(studentId: String, period: String) {
        if (!isInitialized) return
        
        analytics.logEvent("attendance_view") {
            param("student_id", studentId)
            param("period", period)
        }
        
        Log.d(TAG, "Attendance viewed: $studentId for $period")
    }
    
    /**
     * Log result/report view
     */
    fun logResultView(studentId: String, examType: String) {
        if (!isInitialized) return
        
        analytics.logEvent("result_view") {
            param("student_id", studentId)
            param("exam_type", examType)
        }
        
        Log.d(TAG, "Result viewed: $studentId for $examType")
    }
    
    /**
     * Log timetable view
     */
    fun logTimetableView(userType: String) {
        if (!isInitialized) return
        
        analytics.logEvent("timetable_view") {
            param("user_type", userType)
        }
        
        Log.d(TAG, "Timetable viewed")
    }
    
    /**
     * Log assignment submission
     */
    fun logAssignmentSubmit(assignmentId: String, subjectName: String) {
        if (!isInitialized) return
        
        analytics.logEvent("assignment_submit") {
            param("assignment_id", assignmentId)
            param("subject", subjectName)
        }
        
        Log.d(TAG, "Assignment submitted: $assignmentId")
    }
    
    /**
     * Log exam marks entry (for teachers)
     */
    fun logExamMarksEntry(examId: String, studentCount: Int) {
        if (!isInitialized) return
        
        analytics.logEvent("exam_marks_entry") {
            param("exam_id", examId)
            param("student_count", studentCount.toLong())
        }
        
        Log.d(TAG, "Exam marks entered for $studentCount students")
    }
    
    // ==================== FINANCIAL EVENTS ====================
    
    /**
     * Log fee payment view
     */
    fun logFeePaymentView(studentId: String, amount: Double) {
        if (!isInitialized) return
        
        analytics.logEvent("fee_payment_view") {
            param("student_id", studentId)
            param(FirebaseAnalytics.Param.VALUE, amount)
            param(FirebaseAnalytics.Param.CURRENCY, "PKR")
        }
        
        Log.d(TAG, "Fee payment viewed: $amount PKR")
    }
    
    /**
     * Log fee challan download
     */
    fun logFeeChallanDownload(studentId: String, challanId: String) {
        if (!isInitialized) return
        
        analytics.logEvent("fee_challan_download") {
            param("student_id", studentId)
            param("challan_id", challanId)
        }
        
        Log.d(TAG, "Fee challan downloaded: $challanId")
    }
    
    /**
     * Log salary view (for teachers)
     */
    fun logSalaryView(staffId: String, month: String) {
        if (!isInitialized) return
        
        analytics.logEvent("salary_view") {
            param("staff_id", staffId)
            param("month", month)
        }
        
        Log.d(TAG, "Salary viewed for $month")
    }
    
    // ==================== COMMUNICATION EVENTS ====================
    
    /**
     * Log diary entry view
     */
    fun logDiaryView(studentId: String) {
        if (!isInitialized) return
        
        analytics.logEvent("diary_view") {
            param("student_id", studentId)
        }
        
        Log.d(TAG, "Diary viewed for student: $studentId")
    }
    
    /**
     * Log diary entry creation
     */
    fun logDiarySend(classId: String, subjectName: String) {
        if (!isInitialized) return
        
        analytics.logEvent("diary_send") {
            param("class_id", classId)
            param("subject", subjectName)
        }
        
        Log.d(TAG, "Diary sent to class: $classId")
    }
    
    /**
     * Log notification received
     */
    fun logNotificationReceived(notificationType: String, title: String) {
        if (!isInitialized) return
        
        analytics.logEvent("notification_received") {
            param("notification_type", notificationType)
            param("title", title)
        }
        
        Log.d(TAG, "Notification received: $title")
    }
    
    /**
     * Log notification opened
     */
    fun logNotificationOpened(notificationType: String, title: String) {
        if (!isInitialized) return
        
        analytics.logEvent("notification_open") {
            param("notification_type", notificationType)
            param("title", title)
        }
        
        Log.d(TAG, "Notification opened: $title")
    }
    
    // ==================== ERROR TRACKING ====================
    
    /**
     * Log error event
     */
    fun logError(errorType: String, errorMessage: String, screenName: String) {
        if (!isInitialized) return
        
        analytics.logEvent("error_occurred") {
            param("error_type", errorType)
            param("error_message", errorMessage)
            param("screen_name", screenName)
        }
        
        Log.d(TAG, "Error logged: $errorType - $errorMessage")
    }
    
    /**
     * Log API error
     */
    fun logApiError(endpoint: String, statusCode: Int, errorMessage: String) {
        if (!isInitialized) return
        
        analytics.logEvent("api_error") {
            param("endpoint", endpoint)
            param("status_code", statusCode.toLong())
            param("error_message", errorMessage)
        }
        
        Log.d(TAG, "API Error: $endpoint returned $statusCode")
    }
    
    // ==================== PERFORMANCE TRACKING ====================
    
    /**
     * Log screen load time
     */
    fun logScreenLoadTime(screenName: String, loadTimeMs: Long) {
        if (!isInitialized) return
        
        analytics.logEvent("screen_load_time") {
            param("screen_name", screenName)
            param("load_time_ms", loadTimeMs)
        }
        
        Log.d(TAG, "Screen load time: $screenName took ${loadTimeMs}ms")
    }
    
    /**
     * Log API response time
     */
    fun logApiResponseTime(endpoint: String, responseTimeMs: Long, success: Boolean) {
        if (!isInitialized) return
        
        analytics.logEvent("api_response_time") {
            param("endpoint", endpoint)
            param("response_time_ms", responseTimeMs)
            param("success", if (success) "true" else "false")
        }
        
        Log.d(TAG, "API response time: $endpoint took ${responseTimeMs}ms")
    }
    
    // ==================== SEARCH EVENTS ====================
    
    /**
     * Log search performed
     */
    fun logSearch(searchTerm: String, category: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
            param("category", category)
        }
        
        Log.d(TAG, "Search: $searchTerm in $category")
    }
    
    // ==================== SHARE EVENTS ====================
    
    /**
     * Log content share
     */
    fun logShare(contentType: String, method: String) {
        if (!isInitialized) return
        
        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.METHOD, method)
        }
        
        Log.d(TAG, "Shared $contentType via $method")
    }
    
    // ==================== CUSTOM EVENTS ====================
    
    /**
     * Log custom event with parameters
     */
    fun logCustomEvent(eventName: String, params: Map<String, Any>) {
        if (!isInitialized) return
        
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Boolean -> param(key, if (value) "true" else "false")
                    else -> param(key, value.toString())
                }
            }
        }
        
        Log.d(TAG, "Custom event: $eventName with ${params.size} params")
    }
    
    /**
     * Log custom event (simple)
     */
    fun logEvent(eventName: String) {
        if (!isInitialized) return
        analytics.logEvent(eventName, null)
        Log.d(TAG, "Event: $eventName")
    }
    
    // ==================== CONVERSION TRACKING ====================
    
    /**
     * Log tutorial begin (onboarding)
     */
    fun logTutorialBegin() {
        if (!isInitialized) return
        analytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null)
        Log.d(TAG, "Tutorial begun")
    }
    
    /**
     * Log tutorial complete
     */
    fun logTutorialComplete() {
        if (!isInitialized) return
        analytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, null)
        Log.d(TAG, "Tutorial completed")
    }
    
    /**
     * Log app open
     */
    fun logAppOpen() {
        if (!isInitialized) return
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        Log.d(TAG, "App opened")
    }
}

