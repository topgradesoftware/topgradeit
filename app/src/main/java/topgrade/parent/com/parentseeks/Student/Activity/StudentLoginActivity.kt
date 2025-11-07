@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Student.Activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
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
import com.google.android.material.textfield.TextInputLayout
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Repository.ConsolidatedUserRepository
import topgrade.parent.com.parentseeks.Parent.Utils.API
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

class StudentLoginActivity : AppCompatActivity() {
    var seleted_campus = ""
    var context: Context? = null
    private var password: String? = null
    private var usernam: String? = null
    var fcm_token = ""
    lateinit var progress_bar: ProgressBar
    private var userType: String = "STUDENT" // Fixed to student
    
    // Dialog management (same as parent login)
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
        val userRepository = ConsolidatedUserRepository(this@StudentLoginActivity, apiService)
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(userRepository) as T
            }
        })[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.student_login_screen)
        
        // Skip ThemeHelper and apply teal theme directly - more aggressive approach
        applyTealThemeDirectly()
        
        context = this@StudentLoginActivity
        Paper.init(this)

        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets()
        
        // Apply teal theme again after window insets setup
        window.decorView.post {
            applyTealThemeDirectly()
        }

        progress_bar = findViewById(R.id.progress_bar_header)
        val privacy_policy_checkbox = findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox)
        val Campus_ID = findViewById<EditText>(R.id.Campus_ID)
        val user_name = findViewById<EditText>(R.id.user_name)
        val login_user = findViewById<CardView>(R.id.login_user)
        val link = findViewById<TextView>(R.id.link)
        val user_enter_password = findViewById<EditText>(R.id.user_enter_password)
        
        // Ensure password starts HIDDEN with crossed eye icon
        ensurePasswordHiddenByDefault()
        
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
            
            // Validation logic
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
            Log.e("StudentLoginActivity", "Biometric setup error: ${getBiometricErrorMessage(e)}")
            // Hide biometric buttons if there's an error
            findViewById<CardView>(R.id.biometric_login_button)?.visibility = View.GONE
            findViewById<CardView>(R.id.biometric_setup_btn)?.visibility = View.GONE
        }
    }

    private fun showUserPickerDialog(userIds: List<String>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Student Account for Biometric Login")
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
                onSuccess = { credentials ->
                    val (email, password, campusId) = credentials
                    findViewById<EditText>(R.id.user_name).setText(email)
                    findViewById<EditText>(R.id.user_enter_password).setText(password)
                    findViewById<EditText>(R.id.Campus_ID).setText(campusId)
                    findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox).isChecked = true
                    performLogin(email, password, campusId)
                },
                onError = { errorMessage ->
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

    private fun performLogin(email: String, password: String, campusId: String) {
        progress_bar.visibility = View.VISIBLE
        
        // Load FCM token in background to avoid StrictMode violations (same as parent login)
        loadFcmTokenInBackground(email, password, campusId)
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
                    if (!isActivityDestroyed) {
                        fcm_token = token
                        Log.d("StudentLoginActivity", "Attempting login with userType: $userType")
                        loginViewModel.login(username, password, campusId, fcm_token, userType)
                    }
                }
            } catch (e: Exception) {
                Log.e("StudentLoginActivity", "Error loading FCM token", e)
                runOnUiThread {
                    if (!isActivityDestroyed) {
                        // Use default token on error
                        fcm_token = "123"
                        Log.d("StudentLoginActivity", "Attempting login with userType: $userType (using default token)")
                        loginViewModel.login(username, password, campusId, fcm_token, userType)
                    }
                }
            }
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

                    Log.d("StudentLoginActivity", "=== LOGIN SUCCESS DEBUG ===")
                    Log.d("StudentLoginActivity", "Login successful - UserType: $userType")
                    Log.d("StudentLoginActivity", "Login state saved to Paper DB")
                    Log.d("StudentLoginActivity", "Paper DB - User_Type: ${Paper.book().read(Constants.User_Type, "NOT_FOUND")}")
                    Log.d("StudentLoginActivity", "Paper DB - is_login: ${Paper.book().read(Constants.is_login, "NOT_FOUND")}")
                    Log.d("StudentLoginActivity", "Paper DB - student_id: ${Paper.book().read("student_id", "NOT_FOUND")}")
                    Log.d("StudentLoginActivity", "Paper DB - campus_id: ${Paper.book().read("campus_id", "NOT_FOUND")}")

                    // Store credentials for biometric login if this was a manual login
                    val isBiometricLogin = intent.getBooleanExtra("biometric_login", false)
                    if (!isBiometricLogin) {
                        // Use the actual login credentials that were used
                        val email = usernam ?: ""
                        val userPassword = password ?: ""
                        val campusId = seleted_campus ?: ""
                        
                        if (email.isNotEmpty() && userPassword.isNotEmpty() && campusId.isNotEmpty()) {
                            try {
                                val biometricManager = AppBiometricManager(this@StudentLoginActivity)
                                // Use email as userId for biometric storage (more reliable than Paper DB IDs)
                                val userId = email
                                biometricManager.enableBiometric(userType, userId, email, userPassword, campusId)
                                
                                // Show biometric setup suggestion dialog after successful login
                                showBiometricSetupSuggestion()
                                
                                Log.d("StudentLoginActivity", "Biometric credentials stored for userType: $userType, userId: $userId")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Continue with login even if biometric setup fails
                            }
                        }
                    }

                    // Navigate to Splash screen
                    startActivity(Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.Splash::class.java)
                        .putExtra("from_login", true)
                    )
                    finish()
                }
                is ConsolidatedUserRepository.LoginResult.Error -> {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    Log.e("StudentLoginActivity", "Login error: ${result.message}")
                }
                is ConsolidatedUserRepository.LoginResult.Loading -> {
                    progress_bar.visibility = View.VISIBLE
                }
            }
        })
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
            // Silently fail - don't show biometric setup suggestion if there's an error
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Reapply teal theme to ensure system status bar (top red area) matches bottom navigation
        applyTealThemeDirectly()
    }

    /**
     * Ensure password field starts HIDDEN (dots) with CROSSED eye icon
     * Material's password_toggle starts with OPEN eye even when password is hidden
     * We need to programmatically toggle it to correct the icon state
     */
    private fun ensurePasswordHiddenByDefault() {
        try {
            val passwordInputLayout = findViewById<TextInputLayout>(R.id.password_input_layout)
                ?: return
            val passwordEditText = passwordInputLayout.editText ?: return

            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)

            passwordInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            passwordInputLayout.setEndIconDrawable(R.drawable.ic_password_visibility_off)
            passwordInputLayout.isEndIconVisible = true
            passwordInputLayout.setEndIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.student_primary))
            )

            passwordInputLayout.setEndIconOnClickListener {
                val isCurrentlyHidden = passwordEditText.transformationMethod is PasswordTransformationMethod
                if (isCurrentlyHidden) {
                    passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    passwordInputLayout.setEndIconDrawable(R.drawable.ic_password_visibility_on)
                } else {
                    passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                    passwordInputLayout.setEndIconDrawable(R.drawable.ic_password_visibility_off)
                }
                passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
            }

            Log.d("StudentLoginActivity", "Password icon forced to CROSSED eye when hidden")
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error setting password visibility", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isActivityDestroyed = true
        
        // Dismiss any open dialogs to prevent window leaks
        currentDialog?.dismiss()
        currentDialog = null
        
        // Shutdown background executor to prevent thread leaks
        backgroundExecutor.shutdown()
    }

    /**
     * Apply teal theme directly - bypass ThemeHelper completely
     */
    private fun applyTealThemeDirectly() {
        try {
            val tealColor = ContextCompat.getColor(this, R.color.student_primary)
            
            Log.d("StudentLoginActivity", "=== APPLYING TEAL THEME DIRECTLY ===")
            Log.d("StudentLoginActivity", "Teal color: #${Integer.toHexString(tealColor)}")
            
            // Set window flags first
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            
            // Apply teal color immediately
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = tealColor
                window.navigationBarColor = tealColor
                Log.d("StudentLoginActivity", "Set status bar and navigation bar to teal")
            }
            
            // Force dark icons
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                var flags = window.decorView.systemUiVisibility
                flags = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                window.decorView.systemUiVisibility = flags
                Log.d("StudentLoginActivity", "Set dark status bar icons")
            }
            
            // Apply multiple times to ensure it sticks
            window.decorView.post { applyTealColorAgain() }
            window.decorView.postDelayed({ applyTealColorAgain() }, 100)
            window.decorView.postDelayed({ applyTealColorAgain() }, 500)
            
            Log.d("StudentLoginActivity", "=== DIRECT TEAL THEME APPLIED ===")
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error applying teal theme directly", e)
        }
    }
    
    private fun applyTealColorAgain() {
        try {
            val tealColor = ContextCompat.getColor(this, R.color.student_primary)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = tealColor
                window.navigationBarColor = tealColor
                Log.d("StudentLoginActivity", "Re-applied teal color")
            }
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error re-applying teal color", e)
        }
    }

    /**
     * Override system status bar with teal color - direct approach that should definitely work
     */
    private fun overrideSystemStatusBarWithTeal() {
        try {
            // Get the exact teal color used in the footer
            val tealColor = ContextCompat.getColor(this, R.color.student_primary)
            
            Log.d("StudentLoginActivity", "=== OVERRIDING SYSTEM STATUS BAR WITH TEAL ===")
            Log.d("StudentLoginActivity", "Teal color: #${Integer.toHexString(tealColor)}")
            
            // Clear any existing flags first
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            
            // Set the flags to draw system bar backgrounds
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            
            // Apply teal color to system status bar (top red area) and navigation bar
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = tealColor
                window.navigationBarColor = tealColor
                Log.d("StudentLoginActivity", "Applied teal color to status bar and navigation bar")
            }
            
            // Force dark status bar icons (white icons on teal background)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                var flags = window.decorView.systemUiVisibility
                // Remove light status bar flag to get dark icons
                flags = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                window.decorView.systemUiVisibility = flags
                Log.d("StudentLoginActivity", "Set dark status bar icons")
            }
            
            // Force dark navigation bar icons (prevent light appearance)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0, // 0 = do NOT use light icons
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
                Log.d("StudentLoginActivity", "Set dark system bar icons via insets controller")
            }
            
            // Apply multiple times with different delays to ensure it sticks
            window.decorView.post {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = tealColor
                    window.navigationBarColor = tealColor
                    Log.d("StudentLoginActivity", "Re-applied teal color (post)")
                }
            }
            
            window.decorView.postDelayed({
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = tealColor
                    window.navigationBarColor = tealColor
                    Log.d("StudentLoginActivity", "Re-applied teal color (50ms delay)")
                }
            }, 50)
            
            window.decorView.postDelayed({
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = tealColor
                    window.navigationBarColor = tealColor
                    Log.d("StudentLoginActivity", "Re-applied teal color (200ms delay)")
                }
            }, 200)
            
            Log.d("StudentLoginActivity", "=== OVERRIDE COMPLETE ===")
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error overriding system status bar with teal", e)
        }
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer won't be hidden behind the navigation bar
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
                Log.e("StudentLoginActivity", "Error in window insets listener: ${e.message}")
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            }
        }
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
            
            Log.d("StudentLoginActivity", "Three dots menu setup completed successfully")
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error setting up three dots menu", e)
        }
    }
    
    /**
     * Show more options popup menu (same as staff main dashboard)
     */
    private fun showMoreOptions(view: View) {
        try {
            Log.d("StudentLoginActivity", "showMoreOptions called")
            
            if (customPopupMenu == null) {
                Log.d("StudentLoginActivity", "Creating new CustomPopupMenu")
                customPopupMenu = CustomPopupMenu(this, view, userType.lowercase())
                customPopupMenu?.setOnMenuItemClickListener { title ->
                    Log.d("StudentLoginActivity", "Menu item clicked: $title")
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
                            Log.w("StudentLoginActivity", "Unknown menu item: $title")
                        }
                    }
                    true
                }
                Log.d("StudentLoginActivity", "CustomPopupMenu created successfully")
            }

            if (customPopupMenu?.isShowing() == true) {
                Log.d("StudentLoginActivity", "Dismissing popup menu")
                customPopupMenu?.dismiss()
            } else {
                Log.d("StudentLoginActivity", "Showing popup menu")
                customPopupMenu?.show()
            }
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error showing popup menu", e)
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
            Log.e("StudentLoginActivity", "Error showing fallback options", e)
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
            Log.e("StudentLoginActivity", "Error sharing application", e)
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
            Log.e("StudentLoginActivity", "Error opening rating", e)
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
            Log.e("StudentLoginActivity", "Error opening change password", e)
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
            Paper.book().delete("student_id")
            Paper.book().delete("campus_id")
            Paper.book().delete("full_name")
            Paper.book().delete("picture")
            Paper.book().delete("phone")
            Paper.book().delete("password")
            Paper.book().delete("campus_name")
            Paper.book().delete("campus_address")
            Paper.book().delete("campus_phone")
            
            // Navigate to role selection
            val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.SelectRole::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("StudentLoginActivity", "Error during logout", e)
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }
}
