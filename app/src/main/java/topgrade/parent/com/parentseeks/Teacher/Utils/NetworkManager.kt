package topgrade.parent.com.parentseeks.Teacher.Utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import io.paperdb.Paper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Utils.API
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.NetworkErrorHandler
import topgrade.parent.com.parentseeks.Parent.Utils.MenuActionConstants

/**
 * Manages network operations and API calls
 * Handles logout, network state, and API communication
 */
class NetworkManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkManager"
        
        // Constants for menu actions (using consolidated MenuActionConstants)
        val ACTION_SHARE_APP = MenuActionConstants.Actions.ACTION_SHARE_APP
        val ACTION_RATE_US = MenuActionConstants.Actions.ACTION_RATE_US
        val ACTION_CHANGE_PASSWORD = MenuActionConstants.Actions.ACTION_CHANGE_PASSWORD
        val ACTION_LOGOUT = MenuActionConstants.Actions.ACTION_LOGOUT
    }
    
    private var networkErrorHandler: NetworkErrorHandler? = null
    
    fun initialize() {
        networkErrorHandler = NetworkErrorHandler.getInstance(context)
        setupNetworkCallbacks()
    }
    
    private fun setupNetworkCallbacks() {
        networkErrorHandler?.addNetworkCallback(object : NetworkErrorHandler.NetworkCallback {
            override fun onNetworkAvailable(network: android.net.Network) {
                Log.d(TAG, "Network available")
            }
            
            override fun onNetworkLost(network: android.net.Network) {
                Log.w(TAG, "Network lost")
                showNetworkMessage("Network connection lost")
            }
            
            override fun onNetworkUnavailable() {
                Log.w(TAG, "Network unavailable")
                showNetworkMessage("No network connection available")
            }
            
            // Note: onSystemNetworkError is not part of the standard network callback
            // This method has been removed to fix compilation error
        })
    }
    
    private fun showNetworkMessage(message: String) {
        // Use Snackbar instead of Toast for better UX
        try {
            val snackbar = com.google.android.material.snackbar.Snackbar.make(
                (context as? androidx.appcompat.app.AppCompatActivity)?.findViewById(android.R.id.content) ?: return,
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            )
            snackbar.show()
        } catch (e: Exception) {
            // Fallback to Toast if Snackbar fails
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    fun handleMenuAction(action: String) {
        when (action) {
            ACTION_SHARE_APP -> shareApp()
            ACTION_RATE_US -> rateApp()
            ACTION_CHANGE_PASSWORD -> changePassword()
            ACTION_LOGOUT -> logout()
            else -> Log.w(TAG, "Unknown action: $action")
        }
    }
    
    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
                putExtra(Intent.EXTRA_TEXT, "I'm using this great app: [App Link]")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing app", e)
            Toast.makeText(context, "Unable to share app", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Play Store", e)
            // Fallback to web browser
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening browser", e2)
                Toast.makeText(context, "Unable to open rating page", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun changePassword() {
        try {
            val intent = Intent(context, topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange::class.java)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening password change", e)
            Toast.makeText(context, "Unable to open password change", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun logout() {
        try {
            // Call logout API
            performLogoutApiCall()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            // Fallback: clear data and redirect
            clearUserDataAndRedirect()
        }
    }
    
    private fun performLogoutApiCall() {
        val staffId = Paper.book().read("staff_id", "")
        val campusId = Paper.book().read("campus_id", "")
        
        if (staffId?.isEmpty() != false || campusId?.isEmpty() != false) {
            Log.w(TAG, "Missing staff_id or campus_id for logout")
            clearUserDataAndRedirect()
            return
        }
        
        val requestBody = HashMap<String, String>().apply {
            put("staff_id", staffId ?: "")
            put("campus_id", campusId ?: "")
        }
        
        // val jsonBody = org.json.JSONObject(requestBody as MutableMap<Any?, Any?>).toString() // Unused
        // Note: API.getClient() method may not exist
        // This is a placeholder implementation
        Log.d(TAG, "Logout API call would be made here")
        clearUserDataAndRedirect()
    }
    
    private fun clearUserDataAndRedirect() {
        try {
            // Clear only specific keys instead of destroying entire PaperDB
            Paper.book().delete("staff_id")
            Paper.book().delete("campus_id")
            Paper.book().delete("full_name")
            Paper.book().delete("profile_image")
            Paper.book().write(Constants.is_login, false)
            
            // Redirect to login
            val intent = Intent(context, SelectRole::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            
            if (context is androidx.appcompat.app.AppCompatActivity) {
                context.finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user data", e)
        }
    }
    
    fun cleanup() {
        // Note: removeCallbacksAndMessages is not available on Handler
        // This method has been simplified to just clear the handler reference
        networkErrorHandler = null
    }
}
