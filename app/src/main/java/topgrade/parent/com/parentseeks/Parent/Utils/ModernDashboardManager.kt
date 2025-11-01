@file:Suppress("UNUSED_FUNCTION", "UNUSED_VARIABLE", "unused", "PACKAGE_NAME_MISMATCH", "UNUSED_IMPORT")

package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import topgrade.parent.com.parentseeks.Parent.Model.DashboardConfig
import topgrade.parent.com.parentseeks.Parent.Model.DashboardMenuItem
import topgrade.parent.com.parentseeks.Parent.Model.UserPreferences
import topgrade.parent.com.parentseeks.Parent.Utils.DashboardDataStore
import topgrade.parent.com.parentseeks.Parent.Utils.DashboardMigrationHelper
import topgrade.parent.com.parentseeks.R

/**
 * Modern Dashboard Manager using DataStore
 * Replaces Paper DB with DataStore for better performance and type safety
 */
@Suppress("UNUSED_CLASS", "unused", "UNUSED_PARAMETER")
class ModernDashboardManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ModernDashboardManager"
    }
    
    private val dashboardDataStore = DashboardDataStore(context)
    private val migrationHelper = DashboardMigrationHelper(context)
    @Suppress("UNUSED_VARIABLE")
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Mutable state for menu items
    private val _allMenuItems = mutableListOf<DashboardMenuItem>()
    @Suppress("UNUSED_VARIABLE")
    private val _filteredMenuItems = mutableListOf<DashboardMenuItem>()
    
    // Public flows for reactive UI updates
    val dashboardConfig: StateFlow<DashboardConfig> = dashboardDataStore.dashboardConfig
        .stateIn(scope, SharingStarted.Lazily, DashboardConfig())
    
    val userPreferences: StateFlow<UserPreferences> = dashboardDataStore.userPreferences
        .stateIn(scope, SharingStarted.Lazily, UserPreferences())
    
    val userRole: StateFlow<String> = dashboardDataStore.userRole
        .stateIn(scope, SharingStarted.Lazily, "teacher")
    
    val userPermissions: StateFlow<List<String>> = dashboardDataStore.userPermissions
        .stateIn(scope, SharingStarted.Lazily, emptyList())
    
    val filteredMenuItems: StateFlow<List<DashboardMenuItem>> = combine(
        dashboardConfig,
        userPreferences,
        userRole,
        userPermissions
    ) { config, prefs, role, permissions ->
        filterMenuItems(config, prefs, role, permissions)
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    
    init {
        initializeManager()
    }
    
    /**
     * Initialize the manager and perform migration if needed
     */
    private fun initializeManager() {
        scope.launch {
            try {
                // Check if migration is needed
                val migrationCompleted = migrationHelper.isMigrationCompleted()
                if (!migrationCompleted) {
                    Log.d(TAG, "Migration needed, starting migration...")
                    migrationHelper.migrateToDataStore()
                }
                
                // Initialize default menu items
                initializeDefaultMenuItems()
                
                Log.d(TAG, "ModernDashboardManager initialized successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing ModernDashboardManager", e)
            }
        }
    }
    
    /**
     * Initialize default menu items
     */
    private fun initializeDefaultMenuItems() {
        _allMenuItems.clear()
        
        // Staff Profile
        _allMenuItems.add(DashboardMenuItem("staff_profile", "Staff Profile", 
            R.drawable.man, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffProfile",
            arrayOf("view_profile"), arrayOf("teacher", "admin"), true, true, 1, "profile"))
        
        // Salary
        _allMenuItems.add(DashboardMenuItem("salary", "Salary", 
            R.drawable.salary, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffSalary",
            arrayOf("view_salary"), arrayOf("teacher", "admin"), true, true, 2, "finance"))
        
        // Invoice
        _allMenuItems.add(DashboardMenuItem("invoice", "Invoice", 
            R.drawable.invoice, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffInvoice",
            arrayOf("view_invoice"), arrayOf("teacher", "admin"), true, true, 4, "finance"))
        
        // Ledger
        _allMenuItems.add(DashboardMenuItem("ledger", "Ledger", 
            R.drawable.ledger, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffLedger",
            arrayOf("view_ledger"), arrayOf("teacher", "admin"), true, true, 5, "finance"))
        
        // TimeTable
        _allMenuItems.add(DashboardMenuItem("timetable", "View TimeTable", 
            R.drawable.timetablee, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffTimeTable",
            arrayOf("view_timetable"), arrayOf("teacher", "admin"), true, true, 6, "academic"))
        
        // Assign Task
        _allMenuItems.add(DashboardMenuItem("assign_task", "View Assign Task", 
            R.drawable.assign_task, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu",
            arrayOf("view_tasks"), arrayOf("teacher", "admin"), true, true, 7, "academic"))
        
        // Complain Box
        _allMenuItems.add(DashboardMenuItem("complain_box", "Complain Box", 
            R.drawable.ic_complaints, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffAddComplian",
            arrayOf("submit_complaint"), arrayOf("teacher", "admin"), true, true, 8, "communication"))
        
        // Leave Application
        _allMenuItems.add(DashboardMenuItem("leave_application", "Leave Application", 
            R.drawable.leave_application, "topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu",
            arrayOf("submit_leave"), arrayOf("teacher", "admin"), true, true, 9, "communication"))
        
        // Attendance (for admin/coordinator roles)
        _allMenuItems.add(DashboardMenuItem("attendance", "Attendance", 
            R.drawable.attendence, "topgrade.parent.com.parentseeks.Teacher.Activity.Attendance.StaffAttendanceMenu",
            arrayOf("manage_attendance"), arrayOf("admin", "coordinator"), true, true, 10, "academic"))
        
        // Student List (for admin/coordinator roles)
        _allMenuItems.add(DashboardMenuItem("student_list", "Student List", 
            R.drawable.children, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffStudnetList",
            arrayOf("view_students"), arrayOf("admin", "coordinator"), true, true, 11, "academic"))
        
        // Exam (for admin/coordinator roles)
        _allMenuItems.add(DashboardMenuItem("exam", "Exam", 
            R.drawable.exam, "topgrade.parent.com.parentseeks.Teacher.Exam.ExamManagementDashboard",
            arrayOf("manage_exams"), arrayOf("admin", "coordinator"), true, true, 12, "academic"))
        
        // Feedback
        _allMenuItems.add(DashboardMenuItem("feedback", "Feedback", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.FeedbackMenu",
            arrayOf("view_feedback"), arrayOf("teacher", "admin"), true, true, 13, "communication"))
        
        // Progress Report
        _allMenuItems.add(DashboardMenuItem("progress_report", "Progress Report", 
                            R.drawable.clipboard, "topgrade.parent.com.parentseeks.Teacher.Activites.Activity.StaffProgress",
            arrayOf("view_progress"), arrayOf("teacher", "admin"), true, true, 14, "academic"))
        
        // Send Diary
        _allMenuItems.add(DashboardMenuItem("send_diary", "Send Diary", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.AddDiaryActivity",
            arrayOf("send_diary"), arrayOf("teacher", "admin"), true, true, 15, "communication"))
        
        // View Diary
        _allMenuItems.add(DashboardMenuItem("view_diary", "View Diary", 
            R.drawable.feedback, "topgrade.parent.com.parentseeks.Teacher.Activity.ViewDiaryActivity",
            arrayOf("view_diary"), arrayOf("teacher", "admin"), true, true, 16, "communication"))
        
        // Events/News
        _allMenuItems.add(DashboardMenuItem("events", "Events/News", 
            R.drawable.events, "topgrade.parent.com.parentseeks.Teacher.Activity.StaffEvents",
            arrayOf("view_events"), arrayOf("teacher", "admin"), true, true, 17, "communication"))
    }
    
    /**
     * Filter menu items based on user role, permissions, and preferences
     */
    private fun filterMenuItems(
        @Suppress("UNUSED_PARAMETER") config: DashboardConfig,
        userPrefs: UserPreferences,
        userRole: String,
        userPermissions: List<String>
    ): List<DashboardMenuItem> {
        val filtered = mutableListOf<DashboardMenuItem>()
        
        for (item in _allMenuItems) {
            if (item.isVisible && item.isEnabled) {
                // Check role-based access
                if (item.hasRole(userRole)) {
                    // Check permissions
                    var hasPermission = true
                    if (item.permissions != null) {
                        hasPermission = false
                        for (permission in item.permissions) {
                            if (userPermissions.contains(permission)) {
                                hasPermission = true
                                break
                            }
                        }
                    }
                    
                    if (hasPermission) {
                        // Check if item is hidden by user
                        if (!userPrefs.isHidden(item.id)) {
                            filtered.add(item)
                        }
                    }
                }
            }
        }
        
        // Sort by order and user preferences
        return sortMenuItems(filtered, userPrefs)
    }
    
    /**
     * Sort menu items by custom order and default order
     */
    private fun sortMenuItems(items: List<DashboardMenuItem>, userPrefs: UserPreferences): List<DashboardMenuItem> {
        return items.sortedWith { item1, item2 ->
            // First, check if items are in user's custom order
            val customOrder = userPrefs.customOrder
            if (customOrder != null) {
                val index1 = customOrder.indexOf(item1.id)
                val index2 = customOrder.indexOf(item2.id)
                
                if (index1 != -1 && index2 != -1) {
                    return@sortedWith index1.compareTo(index2)
                } else if (index1 != -1) {
                    return@sortedWith -1 // item1 comes first
                } else if (index2 != -1) {
                    return@sortedWith 1 // item2 comes first
                }
            }
            
            // Then sort by default order
            item1.order.compareTo(item2.order)
        }
    }
    
    /**
     * Get favorite items
     */
    @Suppress("UNUSED_FUNCTION")
    fun getFavoriteItems(): List<DashboardMenuItem> {
        val currentPrefs = userPreferences.value
        return filteredMenuItems.value.filter { item ->
            currentPrefs.isFavorite(item.id)
        }
    }
    
    /**
     * Toggle favorite status for an item
     */
    @Suppress("UNUSED_FUNCTION")
    fun toggleFavorite(itemId: String) {
        scope.launch {
            try {
                dashboardDataStore.toggleFavorite(itemId)
                Log.d(TAG, "Toggled favorite for item: $itemId")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }
    
    /**
     * Hide a menu item
     */
    @Suppress("UNUSED_FUNCTION")
    fun hideItem(itemId: String) {
        scope.launch {
            try {
                dashboardDataStore.toggleHiddenItem(itemId, true)
                Log.d(TAG, "Hidden item: $itemId")
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding item", e)
            }
        }
    }
    
    /**
     * Show a menu item
     */
    @Suppress("UNUSED_FUNCTION")
    fun showItem(itemId: String) {
        scope.launch {
            try {
                dashboardDataStore.toggleHiddenItem(itemId, false)
                Log.d(TAG, "Shown item: $itemId")
            } catch (e: Exception) {
                Log.e(TAG, "Error showing item", e)
            }
        }
    }
    
    /**
     * Update badge count for an item
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateBadge(itemId: String, count: Int) {
        val item = _allMenuItems.find { it.id == itemId }
        item?.let {
            it.badgeCount = count
            Log.d(TAG, "Updated badge count for $itemId: $count")
        }
    }
    
    /**
     * Update badge text for an item
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateBadgeText(itemId: String, text: String) {
        val item = _allMenuItems.find { it.id == itemId }
        item?.let {
            it.badgeText = text
            Log.d(TAG, "Updated badge text for $itemId: $text")
        }
    }
    
    /**
     * Save dashboard configuration
     */
    @Suppress("UNUSED_FUNCTION", "unused")
    fun saveConfiguration(config: DashboardConfig) {
        scope.launch {
            try {
                dashboardDataStore.saveDashboardConfig(config)
                Log.d(TAG, "Dashboard configuration saved")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving dashboard configuration", e)
            }
        }
    }
    
    /**
     * Save user preferences
     */
    @Suppress("UNUSED_FUNCTION")
    fun saveUserPreferences(preferences: UserPreferences) {
        scope.launch {
            try {
                dashboardDataStore.saveUserPreferences(preferences)
                Log.d(TAG, "User preferences saved")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user preferences", e)
            }
        }
    }
    
    /**
     * Update layout preference
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateLayoutPreference(layout: String) {
        scope.launch {
            try {
                dashboardDataStore.updateLayoutPreference(layout)
                Log.d(TAG, "Layout preference updated: $layout")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating layout preference", e)
            }
        }
    }
    
    /**
     * Update theme preference
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateThemePreference(theme: String) {
        scope.launch {
            try {
                dashboardDataStore.updateThemePreference(theme)
                Log.d(TAG, "Theme preference updated: $theme")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating theme preference", e)
            }
        }
    }
    
    /**
     * Update grid columns
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateGridColumns(columns: Int) {
        scope.launch {
            try {
                dashboardDataStore.updateGridColumns(columns)
                Log.d(TAG, "Grid columns updated: $columns")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating grid columns", e)
            }
        }
    }
    
    /**
     * Update custom order
     */
    @Suppress("UNUSED_FUNCTION")
    fun updateCustomOrder(order: List<String>) {
        scope.launch {
            try {
                dashboardDataStore.updateCustomOrder(order)
                Log.d(TAG, "Custom order updated")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating custom order", e)
            }
        }
    }
    
    /**
     * Refresh data from server (placeholder for future implementation)
     */
    @Suppress("UNUSED_FUNCTION")
    fun refreshFromServer() {
        scope.launch {
            try {
                // TODO: Implement server refresh logic
                Log.d(TAG, "Refreshing data from server...")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing from server", e)
            }
        }
    }
    
    /**
     * Get migration status
     */
    @Suppress("UNUSED_FUNCTION")
    suspend fun getMigrationStatus(): DashboardMigrationHelper.MigrationStatus {
        return migrationHelper.getMigrationStatus()
    }
    
    /**
     * Clear all data (for testing/debugging)
     */
    @Suppress("UNUSED_FUNCTION")
    fun clearAllData() {
        scope.launch {
            try {
                dashboardDataStore.clearAllData()
                Log.d(TAG, "All dashboard data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
            }
        }
    }
} 