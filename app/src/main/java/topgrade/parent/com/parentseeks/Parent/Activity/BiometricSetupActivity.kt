package topgrade.parent.com.parentseeks.Parent.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.BiometricManager
import topgrade.parent.com.parentseeks.R
import io.paperdb.Paper

class BiometricSetupActivity : AppCompatActivity() {
    
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricIcon: ImageView
    private lateinit var biometricTitle: TextView
    private lateinit var biometricDescription: TextView
    private lateinit var enableBiometricBtn: Button
    private lateinit var disableBiometricBtn: Button
    private lateinit var backButton: CardView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply anti-flickering flags for fullscreen experience
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this)
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white)
        
        setContentView(R.layout.activity_biometric_setup)
        
        // Apply parent theme
        applyParentTheme()
        
        biometricManager = BiometricManager(this)
        initializeViews()
        setupClickListeners()
        updateBiometricStatus()
    }
    
    private fun applyParentTheme() {
        try {
            // Apply parent theme (dark brown) for biometric setup
            ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
            ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for biometric setup
            ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for biometric setup
            ParentThemeHelper.setFooterVisibility(this, true); // Show footer
            ParentThemeHelper.setHeaderTitle(this, "Biometric Setup");
            
            android.util.Log.d("BiometricSetupActivity", "Parent theme applied successfully")
        } catch (e: Exception) {
            android.util.Log.e("BiometricSetupActivity", "Error applying parent theme", e)
        }
    }
    
    private fun initializeViews() {
        biometricIcon = findViewById(R.id.biometric_icon)
        biometricTitle = findViewById(R.id.biometric_title)
        biometricDescription = findViewById(R.id.biometric_description)
        enableBiometricBtn = findViewById(R.id.enable_biometric_btn)
        disableBiometricBtn = findViewById(R.id.disable_biometric_btn)
        backButton = findViewById(R.id.back_button)
    }
    
    private fun setupClickListeners() {
        enableBiometricBtn.setOnClickListener {
            showBiometricSetupDialog()
        }
        
        disableBiometricBtn.setOnClickListener {
            disableBiometric()
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun updateBiometricStatus() {
        val status = biometricManager.getBiometricStatus()
        
        when (status) {
            BiometricManager.BiometricStatus.AVAILABLE -> {
                biometricIcon.setImageResource(R.drawable.ic_fingerprint)
                biometricTitle.text = "Biometric Authentication Available"
                biometricDescription.text = "You can use fingerprint or face recognition to login securely"
                
                if (biometricManager.isBiometricEnabled()) {
                    enableBiometricBtn.visibility = View.GONE
                    disableBiometricBtn.visibility = View.VISIBLE
                } else {
                    enableBiometricBtn.visibility = View.VISIBLE
                    disableBiometricBtn.visibility = View.GONE
                }
            }
            BiometricManager.BiometricStatus.NO_HARDWARE -> {
                biometricIcon.setImageResource(R.drawable.ic_error)
                biometricTitle.text = "Biometric Hardware Not Available"
                biometricDescription.text = "Your device doesn't support biometric authentication"
                enableBiometricBtn.visibility = View.GONE
                disableBiometricBtn.visibility = View.GONE
            }
            BiometricManager.BiometricStatus.HW_UNAVAILABLE -> {
                biometricIcon.setImageResource(R.drawable.ic_error)
                biometricTitle.text = "Biometric Hardware Unavailable"
                biometricDescription.text = "Biometric hardware is temporarily unavailable"
                enableBiometricBtn.visibility = View.GONE
                disableBiometricBtn.visibility = View.GONE
            }
            BiometricManager.BiometricStatus.NONE_ENROLLED -> {
                biometricIcon.setImageResource(R.drawable.ic_fingerprint)
                biometricTitle.text = "No Biometric Credentials"
                biometricDescription.text = "Please set up fingerprint or face recognition in your device settings"
                enableBiometricBtn.visibility = View.GONE
                disableBiometricBtn.visibility = View.GONE
            }
            BiometricManager.BiometricStatus.UNKNOWN -> {
                biometricIcon.setImageResource(R.drawable.ic_error)
                biometricTitle.text = "Unknown Status"
                biometricDescription.text = "Unable to determine biometric status"
                enableBiometricBtn.visibility = View.GONE
                disableBiometricBtn.visibility = View.GONE
            }
        }
    }
    
    private fun showBiometricSetupDialog() {
        // Get stored credentials from previous login
        val credentials = biometricManager.getStoredCredentials()
        if (credentials != null) {
            // Credentials already stored, just enable biometric
            biometricManager.enableBiometric(credentials.first, credentials.second, credentials.third)
            Toast.makeText(this, "Biometric authentication enabled", Toast.LENGTH_SHORT).show()
            updateBiometricStatus()
        } else {
            // Show dialog to enter credentials
            showCredentialsDialog()
        }
    }
    
    private fun showCredentialsDialog() {
        val dialog = BiometricCredentialsDialog()
        dialog.setOnCredentialsEnteredListener { email, password, campusId ->
            biometricManager.enableBiometric(email, password, campusId)
            Toast.makeText(this, "Biometric authentication enabled", Toast.LENGTH_SHORT).show()
            updateBiometricStatus()
        }
        dialog.show(supportFragmentManager, "BiometricCredentialsDialog")
    }
    
    private fun disableBiometric() {
        biometricManager.disableBiometric()
        Toast.makeText(this, "Biometric authentication disabled", Toast.LENGTH_SHORT).show()
        updateBiometricStatus()
    }
    
    override fun onResume() {
        super.onResume()
        updateBiometricStatus()
    }
} 