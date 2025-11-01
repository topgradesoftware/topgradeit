package topgrade.parent.com.parentseeks.Parent.Utils

import android.app.Activity
import android.os.SystemClock
import android.util.Log

/**
 * Screen Tracking Helper
 * Automatically tracks screen views and load times
 * 
 * Usage: Call trackScreenView() in onResume()
 * 
 * @author Topgradeit Team
 * @version 1.0
 * @since 2025-10-15
 */
object ScreenTrackingHelper {
    
    private const val TAG = "ScreenTracking"
    private val screenLoadTimes = mutableMapOf<String, Long>()
    
    /**
     * Track screen view with automatic load time calculation
     */
    @JvmStatic
    fun trackScreenView(activity: Activity) {
        val screenName = activity.javaClass.simpleName
        val screenClass = activity.javaClass.name
        
        // Log screen view
        AnalyticsManager.logScreenView(screenName, screenClass)
        
        // Calculate and log load time if available
        screenLoadTimes[screenName]?.let { startTime ->
            val loadTime = SystemClock.elapsedRealtime() - startTime
            AnalyticsManager.logScreenLoadTime(screenName, loadTime)
            screenLoadTimes.remove(screenName)
        }
    }
    
    /**
     * Start tracking screen load time
     * Call this in onCreate()
     */
    @JvmStatic
    fun startLoadTimeTracking(activity: Activity) {
        val screenName = activity.javaClass.simpleName
        screenLoadTimes[screenName] = SystemClock.elapsedRealtime()
    }
    
    /**
     * Track screen view with custom name
     */
    fun trackScreenView(screenName: String, screenClass: String) {
        AnalyticsManager.logScreenView(screenName, screenClass)
        Log.d(TAG, "Tracked screen: $screenName")
    }
    
    /**
     * Track screen with user context
     */
    fun trackScreenWithContext(activity: Activity, userType: String?, extraData: Map<String, String>? = null) {
        val screenName = activity.javaClass.simpleName
        trackScreenView(activity)
        
        // Log additional context as custom event
        val params = mutableMapOf<String, Any>()
        params["screen_name"] = screenName
        userType?.let { params["user_type"] = it }
        extraData?.forEach { (key, value) -> params[key] = value }
        
        AnalyticsManager.logCustomEvent("screen_view_with_context", params)
    }
}

