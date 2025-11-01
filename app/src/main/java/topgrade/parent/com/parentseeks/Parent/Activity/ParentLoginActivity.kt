
package topgrade.parent.com.parentseeks.Parent.Activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.checkbox.MaterialCheckBox
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Repository.ConsolidatedUserRepository
import topgrade.parent.com.parentseeks.Parent.Utils.API
import topgrade.parent.com.parentseeks.Parent.Utils.BiometricManager
import topgrade.parent.com.parentseeks.Parent.Utils.BiometricManager as AppBiometricManager
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.RetrofitClient
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper
import topgrade.parent.com.parentseeks.Parent.ViewModel.LoginViewModel
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu
import android.widget.ImageView
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange
import androidx.appcompat.app.AlertDialog
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ParentLoginActivity : AppCompatActivity() {
    var seleted_campus = ""
    var context: Context? = null
    private var password: String? = null
    private var usernam: String? = null
    var fcm_token = ""
    lateinit var progress_bar: ProgressBar
    private var userType: String = "PARENT" // Fixed to parent only
    
    // Dialog management (same as staff login)
    private var currentDialog: androidx.appcompat.app.AlertDialog? = null
    private var isActivityDestroyed = false
    private var customPopupMenu: CustomPopupMenu? = null
    private var moreOption: ImageView? = null
    private var backButton: ImageView? = null
    
    // Background executor for async operations (replaces deprecated AsyncTask)
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // ViewModel instance
    private val loginViewModel: LoginViewModel by lazy {
        val apiService = RetrofitClient.getClient(API.base_url).create(BaseApiService::class.java)
        val userRepository = ConsolidatedUserRepository(this@ParentLoginActivity, apiService)
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(userRepository) as T
            }
        })[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.parent_login_screen)
        
        // Apply Parent theme (Dark Brown) using ThemeHelper
        ThemeHelper.applySimpleTheme(this, ThemeHelper.THEME_PARENT)

        context = this@ParentLoginActivity
        Paper.init(this)

        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets()

        progress_bar = findViewById(R.id.progress_bar_header)
        val privacy_policy_checkbox = findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox)
        val Campus_ID = findViewById<EditText>(R.id.Campus_ID)
        val user_name = findViewById<EditText>(R.id.user_name)
        val login_user = findViewById<CardView>(R.id.login_user)
        val link = findViewById<TextView>(R.id.link)
        val user_enter_password = findViewById<EditText>(R.id.user_enter_password)
        // Password visibility is handled by TextInputLayout's endIconMode="password_toggle"
        
        link.setOnClickListener {
            val urlString = "https://topgradesoftware.com/privacy-policy.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")
            try {
                context!!.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                intent.setPackage(null)
                context!!.startActivity(intent)
            }
        }
        
        login_user.setOnClickListener {
            usernam = user_name.text.toString()
            password = user_enter_password.text.toString()
            seleted_campus = Campus_ID.text.toString()
            
            // Validation logic (same as staff login)
            if (usernam!!.isEmpty()) {
                Toast.makeText(this, "Enter login Id", Toast.LENGTH_SHORT).show()
            } else if (password!!.isEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            } else if (seleted_campus.isEmpty()) {
                Toast.makeText(this, "Select Campus ID", Toast.LENGTH_SHORT).show()
            } else if (!privacy_policy_checkbox.isChecked) {
                Toast.makeText(this, "Accept Privacy Policy", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(usernam!!, password!!, seleted_campus)
            }
        }

        // Setup biometric login
        setupBiometricLogin()
        
        // Setup three dots menu functionality
        setupThreeDotsMenu()
        
        // Handle biometric login credentials
        val isBiometricLogin = intent.getBooleanExtra("biometric_login", false)
        if (isBiometricLogin) {
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""
            val campusId = intent.getStringExtra("campus_id") ?: ""
            
            // Pre-fill the fields
            user_name.setText(email)
            user_enter_password.setText(password)
            Campus_ID.setText(campusId)
            
            // Check privacy policy checkbox
            privacy_policy_checkbox.isChecked = true
            
            // Auto-login after a short delay
            login_user.postDelayed({
                performLogin(email, password, campusId)
            }, 500)
        }

        observeLoginState()
    }

    private fun setupBiometricLogin() {
        try {
            val biometricManager = AppBiometricManager(this)
            val biometricLoginBtn = findViewById<CardView>(R.id.biometric_login_button)
            val biometricSetupBtn = findViewById<CardView>(R.id.biometric_setup_btn)

            if (biometricManager.isBiometricAvailable()) {
                val savedUserIds = biometricManager.getAllSavedUserIds(userType)
                if (savedUserIds.isNotEmpty()) {
                    biometricLoginBtn?.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            if (savedUserIds.size == 1) {
                                performBiometricLogin(savedUserIds[0])
                            } else {
                                showUserPickerDialog(savedUserIds)
                            }
                        }
                    }
                    biometricSetupBtn?.visibility = View.GONE
                } else {
                    biometricLoginBtn?.visibility = View.GONE
                    biometricSetupBtn?.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            showBiometricSetupDialog()
                        }
                    }
                }
            } else {
                biometricLoginBtn?.visibility = View.GONE
                biometricSetupBtn?.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Log the error but don't crash the app
            e.printStackTrace()
            Log.e("ParentLoginActivity", "Biometric setup error: ${getBiometricErrorMessage(e)}")
            // Hide biometric buttons if there's an error
            findViewById<CardView>(R.id.biometric_login_button)?.visibility = View.GONE
            findViewById<CardView>(R.id.biometric_setup_btn)?.visibility = View.GONE
        }
    }

    private fun showUserPickerDialog(userIds: List<String>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Parent Account for Biometric Login")
            .setItems(userIds.toTypedArray()) { _, which ->
                performBiometricLogin(userIds[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBiometricSetupDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔐 Setup Biometric Login")
            .setMessage("To use biometric login, you need to login with your password first. After successful login, you can enable biometric authentication.")
            .setPositiveButton("Login First") { _, _ ->
                // User will login manually and then can setup biometric
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun performBiometricLogin(userId: String) {
        try {
            val biometricManager = AppBiometricManager(this)
            biometricManager.showBiometricPrompt(
                activity = this,
                userType = userType,
                userId = userId,
                onSuccess = { credentials: Triple<String, String, String> ->
                    val (email, password, campusId) = credentials
                    findViewById<EditText>(R.id.user_name).setText(email)
                    findViewById<EditText>(R.id.user_enter_password).setText(password)
                    findViewById<EditText>(R.id.Campus_ID).setText(campusId)
                    findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox).isChecked = true
                    performLogin(email, password, campusId)
                },
                onError = { errorMessage: String ->
                    val friendlyMessage = getBiometricErrorMessage(errorMessage)
                    Toast.makeText(this, friendlyMessage, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val friendlyMessage = getBiometricErrorMessage(e)
            Toast.makeText(this, friendlyMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun performLogin(username: String, password: String, campusId: String) {
        // Load FCM token in background to avoid StrictMode violation (same as staff logic)
        loadFcmTokenInBackground(username, password, campusId)
    }
    
    /**
     * Load FCM token in background thread to avoid StrictMode violations
     * Uses ExecutorService instead of deprecated AsyncTask
     */
    private fun loadFcmTokenInBackground(username: String, password: String, campusId: String) {
        backgroundExecutor.execute {
            try {
                // Read FCM token in background
                val token = Paper.book().read(Constants.PREFERENCE_EXTRA_REGISTRATION_ID, "123").toString()
                
                // Handle UI updates on main thread
                runOnUiThread {
                    fcm_token = token
                    Log.d("ParentLoginActivity", "Attempting login with userType: $userType")
                    loginViewModel.login(username, password, campusId, fcm_token, userType)
                }
            } catch (e: Exception) {
                Log.e("ParentLoginActivity", "Error loading FCM token", e)
                runOnUiThread {
                    // Use default token on error
                    fcm_token = "123"
                    Log.d("ParentLoginActivity", "Attempting login with userType: $userType (using default token)")
                    loginViewModel.login(username, password, campusId, fcm_token, userType)
                }
            }
        }
    }

    private fun showBiometricSetupSuggestion() {
        try {
            val biometricManager = AppBiometricManager(this)
            if (biometricManager.isBiometricAvailable() && !biometricManager.isBiometricEnabled()) {
                // Dismiss any existing dialog
                currentDialog?.dismiss()
                
                currentDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🔐 Enable Secure Login")
                    .setMessage("Your login credentials have been saved securely. Would you like to enable fingerprint or face recognition for faster, more secure access?")
                    .setPositiveButton("Enable Biometric") { _, _ ->
                        if (!isActivityDestroyed) {
                            startActivity(Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.BiometricSetupActivity::class.java))
                        }
                    }
                    .setNegativeButton("Skip for Now") { dialog, _ ->
                        dialog.dismiss()
                        currentDialog = null
                    }
                    .setCancelable(false)
                    .setOnDismissListener {
                        currentDialog = null
                    }
                    .create()
                
                if (!isActivityDestroyed) {
                    currentDialog?.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    

    private fun observeLoginState() {
        loginViewModel.loginState.observe(this, Observer { result ->
            when (result) {
                is ConsolidatedUserRepository.LoginResult.Success -> {
                    progress_bar.visibility = View.GONE

                    // Save user type and login state
                    Paper.book().write(Constants.User_Type, userType)
                    Paper.book().write(Constants.is_login, true)

                    Log.d("ParentLoginActivity", "=== LOGIN SUCCESS DEBUG ===")
                    Log.d("ParentLoginActivity", "Login successful - UserType: $userType")
                    Log.d("ParentLoginActivity", "Login state saved to Paper DB")
                    Log.d("ParentLoginActivity", "Paper DB - User_Type: ${Paper.book().read(Constants.User_Type, "NOT_FOUND")}")
                    Log.d("ParentLoginActivity", "Paper DB - is_login: ${Paper.book().read(Constants.is_login, "NOT_FOUND")}")
                    Log.d("ParentLoginActivity", "Paper DB - parent_id: ${Paper.book().read("parent_id", "NOT_FOUND")}")
                    Log.d("ParentLoginActivity", "Paper DB - campus_id: ${Paper.book().read("campus_id", "NOT_FOUND")}")
                    Log.d("ParentLoginActivity", "Paper DB - students: ${if (Paper.book().read<Any>("students") != null) "EXISTS" else "NULL"}")

                    // ✅ Store credentials for biometric login if this was a manual login (same as staff logic)
                    val isBiometricLogin = intent.getBooleanExtra("biometric_login", false)
                    if (!isBiometricLogin) {
                        // Use the actual login credentials that were used
                        val email = usernam ?: ""
                        val userPassword = password ?: ""
                        val campusId = seleted_campus ?: ""
                        
                        if (email.isNotEmpty() && userPassword.isNotEmpty() && campusId.isNotEmpty()) {
                            try {
                                val biometricManager = AppBiometricManager(this@ParentLoginActivity)
                                // Use email as userId for biometric storage (more reliable than Paper DB IDs)
                                val userId = email
                                biometricManager.enableBiometric(userType, userId, email, userPassword, campusId)
                                
                                // Show biometric setup suggestion dialog after successful login (same as staff logic)
                                showBiometricSetupSuggestion()
                                
                                Log.d("ParentLoginActivity", "Biometric credentials stored for userType: $userType, userId: $userId")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Continue with login even if biometric setup fails
                            }
                        }
                        
                        progress_bar.visibility = View.GONE
                        startActivity(Intent(this@ParentLoginActivity, Splash::class.java)
                            .putExtra("from_login", true)
                        )
                        finish()
                    } else {
                        // This was a biometric login, navigate directly
                        progress_bar.visibility = View.GONE
                        startActivity(Intent(this@ParentLoginActivity, Splash::class.java)
                            .putExtra("from_login", true)
                        )
                        finish()
                    }
                }
                is ConsolidatedUserRepository.LoginResult.Error -> {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    Log.e("ParentLoginActivity", "Login failed: ${result.message}")
                }
                is ConsolidatedUserRepository.LoginResult.Loading -> {
                    progress_bar.visibility = View.VISIBLE
                }
            }
        })
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This is the modern, international-standard approach for handling system windows
     */
    private fun setupWindowInsets() {
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root_layout)
        
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            try {
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val bottomPadding = if (systemInsets.bottom > 20) systemInsets.bottom else 0
                
                // Only apply bottom padding for navigation bar, no top padding to allow header wave to cover status bar
                view.updatePadding(
                    left = 0,   // no left padding to avoid touch interference
                    top = 0,    // no top padding to allow header wave to cover status bar
                    right = 0,  // no right padding to avoid touch interference
                    bottom = bottomPadding // only bottom padding for navigation bar
                )
                
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            } catch (e: Exception) {
                Log.e("ParentLoginActivity", "Error in window insets listener: ${e.message}")
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            }
        }
    }

    /**
     * Get user-friendly biometric error messages
     */
    private fun getBiometricErrorMessage(error: Any): String {
        val errorString = error.toString().lowercase()
        
        return when {
            errorString.contains("hardware") || errorString.contains("no_hardware") -> 
                "🔧 Biometric hardware not available on this device"
            errorString.contains("enrolled") || errorString.contains("none_enrolled") -> 
                "👆 No biometric data enrolled. Please set up fingerprint or face recognition in device settings"
            errorString.contains("hw_unavailable") || errorString.contains("unavailable") -> 
                "⚠️ Biometric sensor is temporarily unavailable. Please try again later"
            errorString.contains("user_cancel") || errorString.contains("cancel") -> 
                "❌ Biometric authentication cancelled"
            errorString.contains("authentication_failed") || errorString.contains("failed") -> 
                "🔒 Biometric authentication failed. Please try again or use password login"
            errorString.contains("timeout") -> 
                "⏰ Biometric authentication timed out. Please try again"
            errorString.contains("lockout") -> 
                "🔐 Too many failed attempts. Biometric is temporarily locked. Please use password login"
            errorString.contains("no_credentials") || errorString.contains("stored") -> 
                "💾 No biometric credentials found. Please login with password first"
            errorString.contains("security") || errorString.contains("keystore") -> 
                "🛡️ Security error. Please restart the app and try again"
            else -> 
                "🔐 Biometric authentication failed. Please use password login"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isActivityDestroyed = true
        currentDialog?.dismiss()
        currentDialog = null
        
        // Shutdown background executor to prevent thread leaks
        backgroundExecutor.shutdown()
    }
    
    /**
     * Setup three dots menu functionality (same as staff main dashboard)
     */
    private fun setupThreeDotsMenu() {
        try {
            moreOption = findViewById(R.id.more_option)
            backButton = findViewById(R.id.back_button)
            
            // Setup back button
            backButton?.setOnClickListener {
                finish()
            }
            
            // Setup three dots menu
            moreOption?.setOnClickListener { view ->
                showMoreOptions(view)
            }
            
            Log.d("ParentLoginActivity", "Three dots menu setup completed successfully")
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error setting up three dots menu", e)
        }
    }
    
    /**
     * Show more options popup menu (same as staff main dashboard)
     */
    private fun showMoreOptions(view: View) {
        try {
            Log.d("ParentLoginActivity", "showMoreOptions called")
            
            if (customPopupMenu == null) {
                Log.d("ParentLoginActivity", "Creating new CustomPopupMenu")
                customPopupMenu = CustomPopupMenu(this, view, userType.lowercase())
                customPopupMenu?.setOnMenuItemClickListener { title ->
                    Log.d("ParentLoginActivity", "Menu item clicked: $title")
                    when (title) {
                        CustomPopupMenu.MENU_SHARE -> {
                            share()
                        }
                        CustomPopupMenu.MENU_RATE -> {
                            rateUs()
                        }
                        CustomPopupMenu.MENU_CHANGE_PASSWORD -> {
                            changePassword()
                        }
                        CustomPopupMenu.MENU_LOGOUT -> {
                            logout()
                        }
                        else -> {
                            Log.w("ParentLoginActivity", "Unknown menu item: $title")
                        }
                    }
                    true
                }
                Log.d("ParentLoginActivity", "CustomPopupMenu created successfully")
            }

            if (customPopupMenu?.isShowing() == true) {
                Log.d("ParentLoginActivity", "Dismissing popup menu")
                customPopupMenu?.dismiss()
            } else {
                Log.d("ParentLoginActivity", "Showing popup menu")
                customPopupMenu?.show()
            }
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error showing popup menu", e)
            showFallbackOptions()
        }
    }
    
    /**
     * Fallback options if popup menu fails
     */
    private fun showFallbackOptions() {
        try {
            // Show a simple toast first
            Toast.makeText(this, "More options: Share, Rate, Change Password, Logout", Toast.LENGTH_LONG).show()
            
            // Then show a simple dialog with options
            AlertDialog.Builder(this)
                .setTitle("More Options")
                .setItems(arrayOf("Share Application", "Rate Us", "Change Password", "Logout")) { _, which ->
                    when (which) {
                        0 -> share()
                        1 -> rateUs()
                        2 -> changePassword()
                        3 -> logout()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error showing fallback options", e)
            Toast.makeText(this, "More options: Share, Rate, Change Password, Logout", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Share application functionality
     */
    private fun share() {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out this amazing school management app!")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Application"))
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error sharing application", e)
            Toast.makeText(this, "Error sharing application", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Rate us functionality
     */
    private fun rateUs() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to Play Store website
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                }
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error opening rating", e)
            Toast.makeText(this, "Error opening rating", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Change password functionality
     */
    private fun changePassword() {
        try {
            startActivity(Intent(this, PasswordsChange::class.java))
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error opening change password", e)
            Toast.makeText(this, "Error opening change password", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Logout functionality
     */
    private fun logout() {
        try {
            // Clear all stored data
            Paper.book().write(Constants.is_login, false)
            Paper.book().delete(Constants.User_Type)
            Paper.book().delete("parent_id")
            Paper.book().delete("campus_id")
            Paper.book().delete("full_name")
            Paper.book().delete("picture")
            Paper.book().delete("phone")
            Paper.book().delete("password")
            Paper.book().delete("campus_name")
            Paper.book().delete("campus_address")
            Paper.book().delete("campus_phone")
            
            // Navigate to role selection
            val intent = Intent(this, SelectRole::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("ParentLoginActivity", "Error during logout", e)
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }

}
