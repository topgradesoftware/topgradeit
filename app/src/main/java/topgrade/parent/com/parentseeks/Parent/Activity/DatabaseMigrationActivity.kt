package topgrade.parent.com.parentseeks.Parent.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import topgrade.parent.com.parentseeks.Parent.Utils.DatabaseMigrationManager
import topgrade.parent.com.parentseeks.Parent.Utils.MemoryLeakDetector
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper
import topgrade.parent.com.parentseeks.R

class DatabaseMigrationActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DatabaseMigrationActivity"
    }
    
    private lateinit var migrationManager: DatabaseMigrationManager
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var detailsTextView: TextView
    private lateinit var startMigrationButton: Button
    private lateinit var verifyMigrationButton: Button
    private lateinit var resetMigrationButton: Button
    private lateinit var closeButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register activity for memory leak detection
        MemoryLeakDetector.registerActivity(this)
        
        setContentView(R.layout.activity_database_migration)
        
        // Apply parent theme
        applyParentTheme()
        
        // Initialize views
        initializeViews()
        
        // Initialize migration manager
        migrationManager = DatabaseMigrationManager(this)
        
        // Setup button click listeners
        setupButtonListeners()
        
        // Check migration status
        checkMigrationStatus()
    }
    
    private fun applyParentTheme() {
        try {
            // Apply parent theme (dark brown) for database migration
            ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
            ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for migration
            ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for migration
            ParentThemeHelper.setFooterVisibility(this, true); // Show footer
            ParentThemeHelper.setHeaderTitle(this, "Database Migration");
            
            Log.d(TAG, "Parent theme applied successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying parent theme", e)
        }
    }
    
    private fun initializeViews() {
        progressBar = findViewById(R.id.progress_bar)
        statusTextView = findViewById(R.id.status_text_view)
        detailsTextView = findViewById(R.id.details_text_view)
        startMigrationButton = findViewById(R.id.start_migration_button)
        verifyMigrationButton = findViewById(R.id.verify_migration_button)
        resetMigrationButton = findViewById(R.id.reset_migration_button)
        closeButton = findViewById(R.id.close_button)
    }
    
    private fun setupButtonListeners() {
        startMigrationButton.setOnClickListener {
            startMigration()
        }
        
        verifyMigrationButton.setOnClickListener {
            verifyMigration()
        }
        
        resetMigrationButton.setOnClickListener {
            resetMigration()
        }
        
        closeButton.setOnClickListener {
            finish()
        }
    }
    
    private fun checkMigrationStatus() {
        lifecycleScope.launch {
            try {
                val needsMigration = migrationManager.isMigrationNeeded()
                val stats = migrationManager.getMigrationStats()
                
                updateStatus(
                    if (needsMigration) "Migration Required" else "Migration Completed",
                    stats
                )
                
                startMigrationButton.isEnabled = needsMigration
                verifyMigrationButton.isEnabled = !needsMigration
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking migration status", e)
                updateStatus("Error", "Failed to check migration status: ${e.message}")
            }
        }
    }
    
    private fun startMigration() {
        startMigrationButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        
        updateStatus("Starting Migration", "Preparing to migrate data from Paper DB to Room database...")
        
        lifecycleScope.launch {
            try {
                val result = migrationManager.performMigration(object : DatabaseMigrationManager.MigrationProgressCallback {
                    override fun onProgressUpdate(progress: Int, message: String) {
                        runOnUiThread {
                            progressBar.progress = progress
                            updateStatus("Migrating...", message)
                        }
                    }
                    
                    override fun onMigrationCompleted(success: Boolean, message: String) {
                        runOnUiThread {
                            if (success) {
                                progressBar.progress = 100
                                updateStatus("Migration Completed", message)
                                startMigrationButton.isEnabled = false
                                verifyMigrationButton.isEnabled = true
                            } else {
                                updateStatus("Migration Failed", message)
                                startMigrationButton.isEnabled = true
                            }
                        }
                    }
                    
                    override fun onMigrationError(error: String) {
                        runOnUiThread {
                            updateStatus("Migration Error", error)
                            startMigrationButton.isEnabled = true
                        }
                    }
                })
                
                // Log migration result
                Log.d(TAG, "Migration completed: ${result.success}")
                Log.d(TAG, "Migrated items: ${result.migratedItems.size}")
                Log.d(TAG, "Errors: ${result.errors.size}")
                
                if (result.success) {
                    updateDetails("Migration Summary:\n" +
                            "✅ Successfully migrated ${result.migratedItems.size} items\n" +
                            "❌ ${result.errors.size} errors encountered\n" +
                            "📊 Progress: ${result.progress}%")
                } else {
                    updateDetails("Migration failed: ${result.errorMessage}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during migration", e)
                updateStatus("Migration Error", "Failed to perform migration: ${e.message}")
                startMigrationButton.isEnabled = true
            }
        }
    }
    
    private fun verifyMigration() {
        verifyMigrationButton.isEnabled = false
        updateStatus("Verifying Migration", "Checking migration integrity...")
        
        lifecycleScope.launch {
            try {
                val verificationResult = migrationManager.verifyMigration()
                
                val verificationMessage = StringBuilder()
                verificationMessage.append("Migration Verification Results:\n\n")
                verificationMessage.append("User Data: ${if (verificationResult.userDataMatch) "✅" else "❌"}\n")
                verificationMessage.append("Students Data: ${if (verificationResult.studentsDataMatch) "✅" else "❌"}\n")
                verificationMessage.append("Session Data: ${if (verificationResult.sessionDataMatch) "✅" else "❌"}\n")
                verificationMessage.append("\nOverall: ${if (verificationResult.allDataMatch) "✅ PASSED" else "❌ FAILED"}")
                
                if (verificationResult.errorMessage != null) {
                    verificationMessage.append("\n\nError: ${verificationResult.errorMessage}")
                }
                
                updateStatus(
                    if (verificationResult.allDataMatch) "Verification Passed" else "Verification Failed",
                    verificationMessage.toString()
                )
                
                verifyMigrationButton.isEnabled = true
                
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying migration", e)
                updateStatus("Verification Error", "Failed to verify migration: ${e.message}")
                verifyMigrationButton.isEnabled = true
            }
        }
    }
    
    private fun resetMigration() {
        resetMigrationButton.isEnabled = false
        updateStatus("Resetting Migration", "Clearing migration status...")
        
        lifecycleScope.launch {
            try {
                migrationManager.resetMigrationStatus()
                updateStatus("Migration Reset", "Migration status has been reset. You can now perform migration again.")
                startMigrationButton.isEnabled = true
                verifyMigrationButton.isEnabled = false
                resetMigrationButton.isEnabled = true
                
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting migration", e)
                updateStatus("Reset Error", "Failed to reset migration: ${e.message}")
                resetMigrationButton.isEnabled = true
            }
        }
    }
    
    private fun updateStatus(title: String, message: String) {
        statusTextView.text = title
        detailsTextView.text = message
        Log.d(TAG, "Status Update: $title - $message")
    }
    
    private fun updateDetails(details: String) {
        detailsTextView.text = details
        Log.d(TAG, "Details Update: $details")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unregister activity from memory leak detection
        MemoryLeakDetector.unregisterActivity(this)
        
        // Check for memory leaks before destroying
        MemoryLeakDetector.checkMemoryLeaks(this)
    }
} 