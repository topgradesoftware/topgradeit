@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Teacher.Activity

import android.content.ActivityNotFoundException
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import io.paperdb.Paper
import org.json.JSONException
import org.json.JSONObject
import topgrade.parent.com.parentseeks.Parent.Activity.Splash
import topgrade.parent.com.parentseeks.Parent.Utils.API
import topgrade.parent.com.parentseeks.Parent.Utils.BiometricManager as AppBiometricManager
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.WindowCompat
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu
import android.widget.ImageView
import topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange
import topgrade.parent.com.parentseeks.Parent.Utils.UserType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TeacherLogin : AppCompatActivity() {
    var seleted_campus = ""
    lateinit var progress_bar:ProgressBar
    private var password: String? = null
    private var usernam: String? = null
    var fcm_token = ""
    private lateinit var biometricLoginButton: CardView
    private var isActivityDestroyed = false
    private var currentDialog: androidx.appcompat.app.AlertDialog? = null
    private var userType: String = UserType.TEACHER.value // Dynamic user type support
    private var customPopupMenu: CustomPopupMenu? = null
    private var moreOption: ImageView? = null
    private var backButton: ImageView? = null
    
    // Background executor for async operations (replaces deprecated AsyncTask)
    private val backgroundExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_staff_login)
        
        // Configure status bar for navy blue background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navy_blue)

            // For Android M and above, ensure white status bar icons on dark background
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val flags = window.decorView.systemUiVisibility
                // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                window.decorView.systemUiVisibility = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                window.insetsController?.setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } catch (e: Exception) {
                Log.e("TeacherLogin", "Error setting system bars appearance", e)
            }
        }
        
        // Apply Staff theme (Navy Blue) using ThemeHelper
        ThemeHelper.applySimpleTheme(this, ThemeHelper.THEME_STAFF)
        
        Paper.init(this)
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets()
        
        progress_bar=findViewById<ProgressBar>(R.id.progress_bar)
        val privacy_policy_checkbox=findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox)
        val Campus_ID=findViewById<EditText>(R.id.Campus_ID)
        val user_name=findViewById<EditText>(R.id.user_name)
        val login_user=findViewById<CardView>(R.id.login_user)
        val link=findViewById<TextView>(R.id.link)
        val user_enter_password=findViewById<EditText>(R.id.user_enter_password)
        
        // Ensure password starts HIDDEN with crossed eye icon
        ensurePasswordHiddenByDefault()
        
        biometricLoginButton = findViewById(R.id.biometric_login_button)
        link.setOnClickListener {
            val urlString = "https://topgradeit.com/privacy-policy.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.android.chrome")
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                intent.setPackage(null)
                startActivity(intent)
            }
        }
        login_user.setOnClickListener {
            usernam = user_name?.text?.toString() ?: ""
            password = user_enter_password?.text?.toString() ?: ""
            seleted_campus = Campus_ID?.text?.toString() ?: ""
            if (usernam.isNullOrEmpty()) {
                Toast.makeText(this, "Enter login Id", Toast.LENGTH_SHORT).show()
            } else if (password.isNullOrEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            } else if (seleted_campus.isEmpty()) {
                Toast.makeText(this, "Select Campus ID", Toast.LENGTH_SHORT).show()
            } else if (!privacy_policy_checkbox.isChecked) {
                Toast.makeText(this, "Accept Privacy Policy", Toast.LENGTH_SHORT).show()
            } else {
                // Load FCM token in background to avoid StrictMode violation
                loadFcmTokenInBackground(usernam ?: "", password ?: "", seleted_campus)
            }
        }
        
        // Setup biometric login (same as parent login)
        setupBiometricLogin()
        
        // Setup three dots menu functionality
        setupThreeDotsMenu()
        
        // Handle biometric login credentials (same as parent login)
        val isBiometricLogin = intent.getBooleanExtra("biometric_login", false)
        if (isBiometricLogin) {
            val email = intent.getStringExtra("email") ?: ""
            val password = intent.getStringExtra("password") ?: ""
            val campusId = intent.getStringExtra("campus_id") ?: ""
            
            // Pre-fill the fields
            user_name?.setText(email)
            user_enter_password?.setText(password)
            Campus_ID?.setText(campusId)
            
            // Check privacy policy checkbox
            privacy_policy_checkbox.isChecked = true
            
            // Auto-login after a short delay
            login_user.postDelayed({
                login_api_hint(email, password, campusId)
            }, 500)
        }
    }

    private fun login_api_hint(name: String, password: String, campus_id: String) {
        progress_bar!!.visibility = View.VISIBLE
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest: StringRequest = object : StringRequest(Request.Method.POST,
                API.staff_login, Response.Listener<String> { response ->
            Log.e("Response", response)
            try {
                val obj = JSONObject(response)
                val status = obj.getJSONObject("status")
                if (status.getString("code") == "1000") {
                    try {
                        val jsonArray = obj.getJSONArray("data")
                        val data = jsonArray.getJSONObject(0)
                        val parentModel = Gson().fromJson(data.toString(), StaffModel::class.java)

                        // Enhanced logging to debug the staff name issue
                        Log.d("TeacherLogin", "API Response - Raw data object: ${data.toString()}")
                        Log.d("TeacherLogin", "API Response - Staff model full_name: '${parentModel.full_name}'")
                        Log.d("TeacherLogin", "API Response - Staff model full_name length: ${parentModel.full_name?.length ?: "null"}")
                        Log.d("TeacherLogin", "API Response - Staff model full_name is empty: ${parentModel.full_name?.isEmpty() ?: "null"}")

                        Paper.book().write("Staff_Model", parentModel)
                        if (parentModel.full_name != null) {
                            if (!parentModel.full_name.isEmpty()) {
                                Paper.book().write("full_name", parentModel.full_name)
                                Log.d("TeacherLogin", "Stored full_name in Paper DB: '${parentModel.full_name}'")
                            } else {
                                Paper.book().write("full_name", "")
                                Log.d("TeacherLogin", "Stored empty full_name in Paper DB (was empty)")
                            }
                        } else {
                            Paper.book().write("full_name", "")
                            Log.d("TeacherLogin", "Stored empty full_name in Paper DB (was null)")
                        }
                        if (parentModel.picture != null) {
                            if (!parentModel.picture.isEmpty()) {
                                Paper.book().write("picture", parentModel.picture)
                            } else {
                                Paper.book().write("picture", "")
                            }
                        } else {
                            Paper.book().write("picture", "")
                        }
                        if (parentModel.phone != null) {
                            if (!parentModel.phone.isEmpty()) {
                                Paper.book().write("phone", parentModel.phone)
                            } else {
                                Paper.book().write("phone", "")
                            }
                        } else {
                            Paper.book().write("phone", "")
                        }
                        val staff_id = data.getString("unique_id")
                        val campus = obj.getJSONObject("campus")
                        
                        // Debug: Log the entire campus object to see its structure
                        Log.d("TeacherLogin", "Campus object: ${campus.toString()}")
                        
                        val campus_id = campus.getString("unique_id") // Get campus ID from campus object
                        val campus_name = campus.getString("full_name")
                        val campus_address = campus.getString("address")
                        val campus_phone = campus.getString("phone")
                        
                        // Debug: Log all campus-related values
                        Log.d("TeacherLogin", "Staff ID: $staff_id")
                        Log.d("TeacherLogin", "Campus ID: $campus_id")
                        Log.d("TeacherLogin", "Campus Name: $campus_name")
                        Log.d("TeacherLogin", "Campus Address: $campus_address")
                        Log.d("TeacherLogin", "Campus Phone: $campus_phone")
                        Paper.book().write("staff_id", staff_id)
                        Paper.book().write("campus_id", campus_id)
                        Constant.staff_id = staff_id
                        Constant.campus_id = campus_id
                        Paper.book().write("password", password)
                        Paper.book().write(Constants.is_login, true)
                        Paper.book().write(Constants.User_Type, "Teacher")
                        Paper.book().write("campus_name", campus_name)
                        Paper.book().write("campus_address", campus_address)
                        Paper.book().write("campus_phone", campus_phone)
                        Paper.book().write("staff_password", password)
                        val campus_session = obj.getJSONObject("campus_session")
                        val current_session = campus_session.getString("unique_id")
                        Paper.book().write("current_session", current_session)
                        Constant.current_session = current_session
                        
                        // ✅ Store credentials for biometric login if this was a manual login (same as parent login)
                        val isBiometricLogin = intent.getBooleanExtra("biometric_login", false)
                        if (!isBiometricLogin) {
                            // Use the actual login credentials that were used
                            val email = usernam ?: ""
                            val userPassword = password ?: ""
                            val campusId = seleted_campus ?: ""
                            
                            if (email.isNotEmpty() && userPassword.isNotEmpty() && campusId.isNotEmpty()) {
                                try {
                                    val biometricManager = AppBiometricManager(this@TeacherLogin)
                                    biometricManager.enableBiometric(userType, email, email, userPassword, campusId)
                                    
                                    // Show biometric setup suggestion dialog after successful login
                                    showBiometricSetupSuggestion()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Continue with login even if biometric setup fails
                                }
                            }
                        }
                        
                        progress_bar!!.visibility = View.GONE
                        startActivity(Intent(this, Splash::class.java)
                            .putExtra("from_login", true)
                        )
                        finish()
                    } catch (error: Exception) {
                        error.printStackTrace()
                        progress_bar!!.visibility = View.GONE
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progress_bar!!.visibility = View.GONE
                    val T1 = status.getString("message")
                    Toast.makeText(this, T1, Toast.LENGTH_SHORT).show()
                }
            } catch (e1: JSONException) {
                e1.printStackTrace()
                Toast.makeText(this, e1.message, Toast.LENGTH_SHORT).show()
                progress_bar!!.visibility = View.GONE
            }
        }, Response.ErrorListener { error: com.android.volley.VolleyError ->
            progress_bar!!.visibility = View.GONE
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val postParam = HashMap<String?, String?>()
                postParam["login_email"] = name
                postParam["login_pass"] = password
                postParam["login_id"] = campus_id // CAMPUS ID
                postParam["fcm_token"] = fcm_token // fcm_token
                postParam["operation"] = "login"
                
                // Debug logging
                Log.d("StaffLogin", "Sending login request with:")
                Log.d("StaffLogin", "Email: $name")
                Log.d("StaffLogin", "Campus ID: $campus_id")
                Log.d("StaffLogin", "FCM Token: $fcm_token")
                Log.d("StaffLogin", "Request Body: ${JSONObject(postParam as Map<*, *>).toString()}")
                
                return JSONObject(postParam as Map<*, *>).toString().toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header_parameter = HashMap<String, String>()
                header_parameter["Content-Type"] = "application/json"
                return header_parameter
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                90000, // 90 seconds timeout - increased for better data loading without retry dialogs
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(jsonObjectRequest)
    }

    private fun Send_OTP(name: String, password: String, campus_id: String) {
        progress_bar!!.visibility = View.VISIBLE
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest: StringRequest = object : StringRequest(Request.Method.POST,
                API.staff_login, Response.Listener<String> { response ->
            Log.e("Response", response)
            try {
                val obj = JSONObject(response)
                val status = obj.getJSONObject("status")
                if (status.getString("code") == "1000") {
                    try {
                        progress_bar!!.visibility = View.GONE
                        val otp = obj.getString("otp")
                        val phone = obj.getString("phone")
                        startActivity(Intent(this, OTPActivity::class.java)
                                .putExtra("otp", otp).putExtra("phone", phone).putExtra("login_email", name).putExtra("login_pass", password).putExtra("login_id", campus_id)
                        )
                    } catch (error: Exception) {
                        error.printStackTrace()
                        progress_bar!!.visibility = View.GONE
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progress_bar!!.visibility = View.GONE
                    val T1 = status.getString("message")
                    Toast.makeText(this, T1, Toast.LENGTH_SHORT).show()
                }
            } catch (e1: JSONException) {
                e1.printStackTrace()
                Toast.makeText(this, e1.message, Toast.LENGTH_SHORT).show()
                progress_bar!!.visibility = View.GONE
            }
        }, Response.ErrorListener { error: com.android.volley.VolleyError ->
            progress_bar!!.visibility = View.GONE
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        }) {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val postParam = HashMap<String?, String?>()
                postParam["login_email"] = name
                postParam["login_pass"] = password
                postParam["login_id"] = campus_id // CAMPUS ID
                postParam["operation"] = "send_otp"
                return JSONObject(postParam as Map<*, *>).toString().toByteArray()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val header_parameter = HashMap<String, String>()
                header_parameter["Content-Type"] = "application/json"
                return header_parameter
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                90000, // 90 seconds timeout - increased for better data loading without retry dialogs
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(jsonObjectRequest)
    }


    
    private fun setupBiometricLogin() {
        try {
            val biometricManager = AppBiometricManager(this)
            val biometricLoginBtn = findViewById<androidx.cardview.widget.CardView>(R.id.biometric_login_button)
            val biometricSetupBtn = findViewById<androidx.cardview.widget.CardView>(R.id.biometric_setup_btn)

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
            Log.e("TeacherLogin", "Biometric setup error: ${getBiometricErrorMessage(e)}")
            // Hide biometric buttons if there's an error
            findViewById<androidx.cardview.widget.CardView>(R.id.biometric_login_button)?.visibility = View.GONE
            findViewById<androidx.cardview.widget.CardView>(R.id.biometric_setup_btn)?.visibility = View.GONE
        }
    }

    private fun showUserPickerDialog(userIds: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Select ${userType.replaceFirstChar { it.uppercase() }} Account for Biometric Login")
            .setItems(userIds.toTypedArray()) { _, which ->
                performBiometricLogin(userIds[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
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
    
    private fun showBiometricSetupDialog() {
        // Dismiss any existing dialog
        currentDialog?.dismiss()
        
        currentDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔐 Setup Biometric Login")
            .setMessage("To use biometric login, you need to login with your password first. After successful login, you can enable biometric authentication.")
            .setPositiveButton("Login First") { _, _ ->
                // User will login manually and then can setup biometric
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                currentDialog = null
            }
            .setCancelable(true)
            .setOnDismissListener {
                currentDialog = null
            }
            .create()
        
        if (!isActivityDestroyed) {
            currentDialog?.show()
        }
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
                    findViewById<EditText>(R.id.user_name)?.setText(email)
                    findViewById<EditText>(R.id.user_enter_password)?.setText(password)
                    findViewById<EditText>(R.id.Campus_ID)?.setText(campusId)
                    findViewById<MaterialCheckBox>(R.id.privacy_policy_checkbox)?.isChecked = true
                    login_api_hint(email, password, campusId)
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

    override fun onResume() {
        super.onResume()
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
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy_blue))
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

            Log.d("TeacherLogin", "Password icon forced to CROSSED eye when hidden")
        } catch (e: Exception) {
            Log.e("TeacherLogin", "Error setting password visibility", e)
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
                        login_api_hint(username, password, campusId)
                    }
                }
            } catch (e: Exception) {
                Log.e("TeacherLogin", "Error loading FCM token", e)
                runOnUiThread {
                    if (!isActivityDestroyed) {
                        // Use default token on error
                        fcm_token = "123"
                        login_api_hint(username, password, campusId)
                    }
                }
            }
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
                Log.e("TeacherLogin", "Error in window insets listener: ${e.message}")
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
            
            Log.d("TeacherLogin", "Three dots menu setup completed successfully")
        } catch (e: Exception) {
            Log.e("TeacherLogin", "Error setting up three dots menu", e)
        }
    }
    
    /**
     * Show more options popup menu (same as staff main dashboard)
     */
    private fun showMoreOptions(view: View) {
        try {
            Log.d("TeacherLogin", "showMoreOptions called")
            
            if (customPopupMenu == null) {
                Log.d("TeacherLogin", "Creating new CustomPopupMenu")
                customPopupMenu = CustomPopupMenu(this, view, userType)
                customPopupMenu?.setOnMenuItemClickListener { title ->
                    Log.d("TeacherLogin", "Menu item clicked: $title")
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
                            Log.w("TeacherLogin", "Unknown menu item: $title")
                        }
                    }
                    true
                }
                Log.d("TeacherLogin", "CustomPopupMenu created successfully")
            }

            if (customPopupMenu?.isShowing() == true) {
                Log.d("TeacherLogin", "Dismissing popup menu")
                customPopupMenu?.dismiss()
            } else {
                Log.d("TeacherLogin", "Showing popup menu")
                customPopupMenu?.show()
            }
        } catch (e: Exception) {
            Log.e("TeacherLogin", "Error showing popup menu", e)
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
            Log.e("TeacherLogin", "Error showing fallback options", e)
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
            Log.e("TeacherLogin", "Error sharing application", e)
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
            Log.e("TeacherLogin", "Error opening rating", e)
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
            Log.e("TeacherLogin", "Error opening change password", e)
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
            Paper.book().delete("staff_id")
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
            Log.e("TeacherLogin", "Error during logout", e)
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show()
        }
    }
}