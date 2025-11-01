
@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.RelativeLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import io.paperdb.Paper
import org.json.JSONObject
import topgrade.parent.com.parentseeks.Parent.Utils.API
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.HelperRequestQueue
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper
import topgrade.parent.com.parentseeks.Teacher.Utils.Util
import topgrade.parent.com.parentseeks.Teacher.Activity.TeacherLogin
import topgrade.parent.com.parentseeks.Parent.Activity.ParentLoginActivity
import topgrade.parent.com.parentseeks.Student.Activity.StudentLoginActivity
import topgrade.parent.com.parentseeks.Parent.Activity.Splash
import topgrade.parent.com.parentseeks.Parent.Utils.BiometricManager

// import topgrade.parent.com.parentseeks.Parent.Utils.FirebaseDisabler
import topgrade.parent.com.parentseeks.R
import com.android.volley.toolbox.Volley

class SelectRole : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var progressBar: ProgressBar
    
    // Modern App Update Manager
    private var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply anti-flickering flags for fullscreen experience
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this)
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white)
        
        setContentView(R.layout.activity_select_role)

        // Apply Staff theme (Navy Blue) using ThemeHelper like staff login
        ThemeHelper.applySimpleTheme(this, ThemeHelper.THEME_STAFF)

        try {
            context = this@SelectRole
            Paper.init(context)

            progressBar = findViewById(R.id.progress_bar)
            
            // Setup Android respect for system bars
            setupSystemWindowInsets()
            
            // Setup keyboard handling
            setupKeyboardHandling()
            
            // Initialize modern App Update Manager
            appUpdateManager = AppUpdateManagerFactory.create(this)
            
            setupButtons()
            
            // Check for app updates first
            checkForAppUpdates()
            
            if (isConnectionAvailable()) {
                    getServerVersion()
            } else {
                showNoInternetDialog()
            }
            
        } catch (e: Exception) {
            Log.e("SelectRole", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show()
                finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check for app updates when activity resumes
        checkForAppUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            APP_UPDATE_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        Log.d("SelectRole", "Immediate update completed successfully")
                        Toast.makeText(this, "App updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    RESULT_CANCELED -> {
                        Log.d("SelectRole", "Immediate update was cancelled")
                        Toast.makeText(this, "Update cancelled", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Log.d("SelectRole", "Immediate update failed with result code: $resultCode")
                        Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            FLEXIBLE_UPDATE_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        Log.d("SelectRole", "Flexible update completed successfully")
                        Toast.makeText(this, "App updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    RESULT_CANCELED -> {
                        Log.d("SelectRole", "Flexible update was cancelled")
                        // For flexible updates, cancellation is normal - user can continue using the app
                    }
                    else -> {
                        Log.d("SelectRole", "Flexible update failed with result code: $resultCode")
                    }
                }
            }
        }
    }

    companion object {
        private const val APP_UPDATE_REQUEST_CODE = 500
        private const val FLEXIBLE_UPDATE_REQUEST_CODE = 501
    }

    private fun checkForAppUpdates() {
        try {
            appUpdateManager?.let { manager ->
                Log.d("SelectRole", "Checking for app updates...")
                
                manager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    Log.d("SelectRole", "Update availability: ${appUpdateInfo.updateAvailability()}")
                    Log.d("SelectRole", "Available version code: ${appUpdateInfo.availableVersionCode()}")
                    Log.d("SelectRole", "Client version staleness days: ${appUpdateInfo.clientVersionStalenessDays()}")
                    
                    when (appUpdateInfo.updateAvailability()) {
                        UpdateAvailability.UPDATE_AVAILABLE -> {
                            // Check if update is mandatory (stale for more than 7 days)
                            val isMandatory = appUpdateInfo.clientVersionStalenessDays()?.let { days ->
                                days >= 7
                            } ?: false
                            
                            if (isMandatory) {
                                Log.d("SelectRole", "Mandatory update available - starting immediate update")
                                startImmediateUpdate(appUpdateInfo)
                            } else {
                                Log.d("SelectRole", "Optional update available - starting flexible update")
                                startFlexibleUpdate(appUpdateInfo)
                            }
                        }
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                            Log.d("SelectRole", "Developer triggered update in progress")
                            // Resume the update
                            manager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                APP_UPDATE_REQUEST_CODE
                            )
                        }
                        UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                            Log.d("SelectRole", "No updates available")
                        }
                        else -> {
                            Log.d("SelectRole", "Update availability: ${appUpdateInfo.updateAvailability()}")
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("SelectRole", "Failed to check for updates", exception)
                }
            }
        } catch (e: Exception) {
            Log.e("SelectRole", "Error checking for app updates", e)
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo) {
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                this,
                APP_UPDATE_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("SelectRole", "Error starting immediate update", e)
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo) {
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                this,
                FLEXIBLE_UPDATE_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e("SelectRole", "Error starting flexible update", e)
        }
    }

    private fun setupSystemWindowInsets() {
        try {
            // Enable edge-to-edge display
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
            }
            
            // Status bar and navigation bar colors are now handled by ThemeHelper
            
            // Setup window insets listener with safe casting
            try {
                val rootLayout = findViewById<RelativeLayout>(R.id.activity_layout_select_role)
                rootLayout?.let { layout ->
                    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(layout) { view, insets ->
                        val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        
                        // Apply padding to respect system bars
                        view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                        )
                        
                        insets
                    }
                }
            } catch (e: ClassCastException) {
                Log.e("SelectRole", "Error casting root layout to RelativeLayout", e)
                // Try to find as ConstraintLayout instead
                try {
                    val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.activity_layout_select_role)
                    rootLayout?.let { layout ->
                        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(layout) { view, insets ->
                            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                            
                            // Apply padding to respect system bars
                            view.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                systemBars.bottom
                            )
                            
                            insets
                        }
                    }
                } catch (e2: Exception) {
                    Log.e("SelectRole", "Error setting up window insets with ConstraintLayout", e2)
                }
            }
            
            Log.d("SelectRole", "System window insets setup completed")
        } catch (e: Exception) {
            Log.e("SelectRole", "Error setting up system window insets", e)
        }
    }

    private fun setupKeyboardHandling() {
        try {
            // Enable keyboard handling for better UX
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            
            // Setup keyboard visibility listener with safe casting
            try {
                val rootLayout = findViewById<RelativeLayout>(R.id.activity_layout_select_role)
                rootLayout?.let { layout ->
                    layout.viewTreeObserver.addOnGlobalLayoutListener {
                        val heightDiff = layout.rootView.height - layout.height
                        if (heightDiff > 200) { // Keyboard is visible
                            // Adjust layout when keyboard is shown
                            layout.scrollTo(0, layout.height)
                        }
                    }
                }
            } catch (e: ClassCastException) {
                Log.e("SelectRole", "Error casting root layout to RelativeLayout for keyboard handling", e)
                // Try to find as ConstraintLayout instead
                try {
                    val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.activity_layout_select_role)
                    rootLayout?.let { layout ->
                        layout.viewTreeObserver.addOnGlobalLayoutListener {
                            val heightDiff = layout.rootView.height - layout.height
                            if (heightDiff > 200) { // Keyboard is visible
                                // Adjust layout when keyboard is shown
                                layout.scrollTo(0, layout.height)
                            }
                        }
                    }
                } catch (e2: Exception) {
                    Log.e("SelectRole", "Error setting up keyboard handling with ConstraintLayout", e2)
                }
            }
            
            Log.d("SelectRole", "Keyboard handling setup completed")
        } catch (e: Exception) {
            Log.e("SelectRole", "Error setting up keyboard handling", e)
        }
    }

    private fun setupButtons() {
        try {
            findViewById<CardView>(R.id.cv_user_login)?.setOnClickListener { userLoginLink() }
            findViewById<CardView>(R.id.CAmpus_link)?.setOnClickListener { openUrl("https://topgradesoftware.com/index.php?page=campus/login_campus") }
            findViewById<CardView>(R.id.admin_APP)?.setOnClickListener { openUrl("https://topgradesoftware.com/index.php?page=superadmin/login") }
            findViewById<CardView>(R.id.Staff_APP)?.setOnClickListener { startActivity(Intent(context, TeacherLogin::class.java)) }
            findViewById<CardView>(R.id.Parent_APP)?.setOnClickListener { launchLogin("PARENT") }
            findViewById<CardView>(R.id.Student_APP)?.setOnClickListener { launchLogin("STUDENT") }
            
            
            // Additional Options buttons
            findViewById<CardView>(R.id.settings_card)?.setOnClickListener { openAppSettings() }
            findViewById<CardView>(R.id.help_card)?.setOnClickListener { openHelpDocumentation() }
            findViewById<CardView>(R.id.about_card)?.setOnClickListener { showAppInfo() }
            findViewById<CardView>(R.id.feedback_card)?.setOnClickListener { openFeedbackForm() }
            findViewById<CardView>(R.id.share_card)?.setOnClickListener { shareApp() }
            

            

        } catch (e: Exception) {
            Log.e("SelectRole", "Error setting up buttons", e)
        }
    }

    private fun launchLogin(type: String) {
        try {
            // Navigate to appropriate login screen based on type
            Log.d("SelectRole", "Navigating to login screen for type: $type")
            val intent = when (type) {
                "PARENT" -> Intent(this, ParentLoginActivity::class.java)
                "STUDENT" -> Intent(this, StudentLoginActivity::class.java)
                else -> {
                    Log.w("SelectRole", "Unknown login type: $type, defaulting to ParentLoginActivity")
                    Intent(this, ParentLoginActivity::class.java)
                }
            }
            intent.putExtra("type", type)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("SelectRole", "Error launching login for type: $type", e)
            Toast.makeText(this, "Error opening login screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        try {
            val msg = """
                This is the best School Management app.
                Download it here:
                https://play.google.com/store/apps/details?id=${packageName}
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Log.e("SelectRole", "Error sharing app", e)
            Toast.makeText(this, "Error sharing app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.android.chrome")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e2: Exception) {
                Log.e("SelectRole", "Error opening URL: $url", e2)
                Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening URL: $url", e)
            Toast.makeText(this, "Error opening link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun userLoginLink() {
        openUrl("https://topgradesoftware.com/index.php?page=campus/login_user")
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening app settings", e)
            Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openHelpDocumentation() {
        try {
            // Directly open the TopGrade Software help and support page
            openUrl("https://topgradesoftware.com/help_support_form.php")
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening help documentation", e)
            Toast.makeText(this, "Error opening help documentation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askUserInfoAndOpenWhatsApp() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_info, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.et_name)
        val mobileEditText = dialogView.findViewById<EditText>(R.id.et_mobile)
        
        AlertDialog.Builder(this)
            .setTitle("Contact Information")
            .setView(dialogView)
            .setPositiveButton("Continue") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val mobile = mobileEditText.text.toString().trim()
                
                if (name.isNotEmpty() && mobile.isNotEmpty()) {
                    openWhatsAppSupport(name, mobile)
                } else {
                    Toast.makeText(this, "Please fill in both name and mobile number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun askUserInfoAndOpenEmail() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_user_info, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.et_name)
        val mobileEditText = dialogView.findViewById<EditText>(R.id.et_mobile)
        
        AlertDialog.Builder(this)
            .setTitle("Contact Information")
            .setView(dialogView)
            .setPositiveButton("Continue") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val mobile = mobileEditText.text.toString().trim()
                
                if (name.isNotEmpty() && mobile.isNotEmpty()) {
                    openEmailSupport(name, mobile)
                } else {
                    Toast.makeText(this, "Please fill in both name and mobile number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openWhatsAppSupport(name: String, mobile: String) {
        try {
            val phoneNumber = "923006616622"
            val message = """
                Hello Top Grade Software Support Team!
                
                I need help with the ${getString(R.string.app_name)} app.
                
                My Details:
                - Name: $name
                - Mobile: $mobile
                
                Please assist me with my issue.
                
                Thank you!
            """.trimIndent()
            
            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening WhatsApp", e)
            Toast.makeText(this, "Error opening WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openEmailSupport(name: String, mobile: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@topgradesoftware.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Help Request - ${getString(R.string.app_name)}")
            intent.putExtra(Intent.EXTRA_TEXT, """
                Hello Top Grade Software Support Team,
                
                I am using the ${getString(R.string.app_name)} app and I need assistance with the following:
                
                [Please describe your issue here]
                
                My Details:
                - Name: $name
                - Mobile: $mobile
                
                Device Information:
                - App Version: ${packageManager.getPackageInfo(packageName, 0).versionName}
                - Device: ${android.os.Build.MODEL}
                - Android Version: ${android.os.Build.VERSION.RELEASE}
                
                Thank you for your help!
                
                Best regards,
                $name
            """.trimIndent())
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening email support", e)
            Toast.makeText(this, "Error opening email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAppInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.versionCode
            
            val message = """
                Version: $versionName ($versionCode)
                
                Developed by Top Grade Software
                
                Contact Information:
                📱 WhatsApp: +923006616622
                📧 Email: support@topgradesoftware.com
                🌐 Website: https://topgradesoftware.com
                
                For support and inquiries, please contact us through any of the above channels.
            """.trimIndent()
            
            AlertDialog.Builder(this)
                .setTitle("About ${getString(R.string.app_name)}")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: Exception) {
            Log.e("SelectRole", "Error showing app info", e)
            Toast.makeText(this, "Error showing app info", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFeedbackForm() {
        try {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.top.gradeit")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.top.gradeit")))
            }
        } catch (e: Exception) {
            Log.e("SelectRole", "Error opening app rating", e)
            Toast.makeText(this, "Error opening app rating", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isConnectionAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            
            // Check if network has internet capability and is validated
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                             caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                              caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                              caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            
            hasInternet && hasTransport
        } catch (e: Exception) {
            Log.e("SelectRole", "Error checking network connectivity", e)
            false
        }
    }

    private fun getServerVersion() {
        progressBar.visibility = View.VISIBLE
        val params = mapOf("appName" to API.app_version_Name)
        val req = object : JsonObjectRequest(Method.POST, API.app_version, JSONObject(params),
            Response.Listener<JSONObject> { res ->
                progressBar.visibility = View.GONE
                try {
                    val code = res.getJSONObject("status").getString("code")
                    if (code.equals("1000", ignoreCase = true)) {
                        val serverVer = res.getInt("data")
                        val currentVer = packageManager.getPackageInfo(packageName, 0).versionCode
                        if (currentVer < serverVer) showUpdateDialog() else goNext()
                    } else {
                        val message = res.getJSONObject("status").getString("message")
                        Log.w("SelectRole", "Version check failed: $message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        // Continue to login even if version check fails
                        goNext()
                    }
                } catch (e: Exception) {
                    Log.e("SelectRole", "Error parsing version response", e)
                    Toast.makeText(context, "Error checking version", Toast.LENGTH_SHORT).show()
                    // Continue to login even if version check fails
                    goNext()
                }
            },
            Response.ErrorListener { error ->
                progressBar.visibility = View.GONE
                Log.e("SelectRole", "Version check network error", error)
                
                // Provide more specific error messages
                val errorMessage = when {
                    error.networkResponse == null -> "No internet connection. Please check your network."
                    error.networkResponse.statusCode == 404 -> "Version check service not found. Please try again later."
                    error.networkResponse.statusCode >= 500 -> "Server error. Please try again later."
                    else -> "Failed to check version. Please try again."
                }
                
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                
                // Show option to continue without version check
                showVersionCheckFailedDialog()
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Content-Type" to "application/json; charset=utf-8",
                    "User-Agent" to "Topgrade-Software-App/${packageManager.getPackageInfo(packageName, 0).versionName}"
                )
            }
        }

        // Increased timeout and retry attempts for better reliability
        req.retryPolicy = DefaultRetryPolicy(45000, 3, 1.5f) // 45 seconds timeout, 3 retries
        // Use Volley directly instead of HelperRequestQueue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(req)
    }

    private fun showVersionCheckFailedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Version Check Failed")
            .setMessage("Unable to check for app updates. You can continue using the app, but some features may not work properly if an update is available.")
            .setPositiveButton("Continue") { _, _ ->
                goNext()
            }
            .setNegativeButton("Retry") { _, _ ->
                if (isConnectionAvailable()) {
                    getServerVersion()
                } else {
                    showNoInternetDialog()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun goNext() {
        val isLogin = Paper.book().read<Boolean>(Constants.is_login, false) ?: false
        val type = Paper.book().read<String>(Constants.User_Type)

        Log.d("SelectRole", "goNext() - isLogin: $isLogin, type: $type")

        if (isLogin && type != null) {
            val intent = when (type) {
                "Teacher" -> Intent(this, Splash::class.java)
                "PARENT", "STUDENT" -> Intent(this, Splash::class.java)
                else -> {
                    // If type is unknown, clear login state and stay on SelectRole
                    Log.w("SelectRole", "Unknown user type: $type, clearing login state")
                    Paper.book().write(Constants.is_login, false)
                    Paper.book().delete(Constants.User_Type)
                    return
                }
            }
            Log.d("SelectRole", "Navigating to: ${intent.component?.className}")
            startActivity(intent)
            finish()
        } else {
            Log.d("SelectRole", "User not logged in or type is null - staying on SelectRole")
        }
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.app_name))
            .setMessage("You are using an outdated version. Update now?")
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                val pkg = packageName
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
                }
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(context)
            .setTitle("No Internet")
            .setMessage("Please check your connection and try again.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun checkUpdate() {
        appUpdateManager?.let { manager ->
            try {
                manager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        // TODO: Uncomment when AppUpdateType is resolved
                        // if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        //     manager.startUpdateFlowForResult(
                        //         appUpdateInfo,
                        //         AppUpdateType.IMMEDIATE,
                        //         this,
                        //         APP_UPDATE_REQUEST_CODE
                        //     )
                        // }
                        goNext()
                    } else {
                        goNext()
                    }
                }.addOnFailureListener {
                    Log.e("SelectRole", "Error checking for updates", it)
                    goNext()
                }
            } catch (e: Exception) {
                Log.e("SelectRole", "Error in checkUpdate", e)
                goNext()
            }
        } ?: run {
            Log.w("SelectRole", "appUpdateManager not initialized, skipping update check")
            goNext()
        }
    }
    
    private fun toggleFirebase() {
        try {
            Toast.makeText(this, "Firebase toggle feature not available", Toast.LENGTH_SHORT).show()
            Log.d("SelectRole", "Firebase toggle feature disabled")
        } catch (e: Exception) {
            Log.e("SelectRole", "Error in toggleFirebase", e)
            Toast.makeText(this, "Error with Firebase toggle", Toast.LENGTH_SHORT).show()
        }
    }
}
