@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.parent.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.net.toUri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.BuildConfig
import topgrade.parent.com.parentseeks.Parent.Activity.AttendanceMenu
import topgrade.parent.com.parentseeks.Parent.Activity.ParentProfile
import topgrade.parent.com.parentseeks.Parent.Activity.SelectRole
import topgrade.parent.com.parentseeks.Parent.Activity.StudentDateSheet
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffProfile
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptor
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel
import topgrade.parent.com.parentseeks.Parent.Utils.API
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.DateSheetBuilder
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper
import topgrade.parent.com.parentseeks.Parent.Utils.UserType
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick

/**
 * Optimized Dashboard Activity
 * Uses DataStore and coroutines to prevent ANRs
 */
class OptimizedDashBoard : AppCompatActivity(), OnMenuCLick {

    companion object {
        private const val TAG = "OptimizedDashBoard"
        private const val GRID_SPAN_COUNT = 3
    }

    // UI Components
    private lateinit var homeRcv: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var moreOption: ImageView

    // Data
    private val homeList = mutableListOf<HomeModel>()
    private lateinit var homeAdapter: HomeAdaptor



    // Coroutine scope for background operations
    private val dashboardScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Check if this dashboard is being used by staff (which should use StaffDashboard instead)
            val resolvedUserType = resolveCurrentUserType()
            if (resolvedUserType == UserType.TEACHER || resolvedUserType == UserType.STAFF) {
                Log.w(TAG, "Staff user detected in OptimizedDashBoard - this should use StaffDashboard instead")
                // Redirect to appropriate staff dashboard
                val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.StaffDashboard::class.java)
                startActivity(intent)
                finish()
                return
            }
            
            setContentView(R.layout.activity_screen_main)
            
            // Apply theme based on user type
            setupWindowInsets()
            applyTheme()
            
            // Initialize UI components first
            initializeViews()
            
            // Initialize data in background to prevent ANR
            initializeDataAsync()
            
            isInitialized = true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing dashboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeViews() {
        try {
            progressBar = findViewById(R.id.progress_bar)
            moreOption = findViewById(R.id.more_option)
            // val backButton = findViewById<ImageView>(R.id.back_button) // Unused
            val headerTitle = findViewById<TextView>(R.id.header_title)
            homeRcv = findViewById(R.id.home_rcv)
            
            if (!::homeRcv.isInitialized) {
                Log.e(TAG, "RecyclerView not found! Check if layout contains home_rcv")
            }
            
            // Set dynamic title from intent
            val dashboardTitleText = intent.getStringExtra("DASHBOARD_TITLE")
            if (!dashboardTitleText.isNullOrEmpty() && headerTitle != null) {
                headerTitle.text = dashboardTitleText
                Log.d(TAG, "Header title set from intent: $dashboardTitleText")
            } else if (headerTitle != null) {
                headerTitle.text = getString(R.string.parent_dashboard)
                Log.d(TAG, "Header title set to default: Parent Dashboard")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }







    private fun initializeDataAsync() {
        // Show progress bar while loading
        if (::progressBar.isInitialized) {
            progressBar.visibility = View.VISIBLE
        }
        
        dashboardScope.launch {
            try {
                // Initialize Paper
                Paper.init(this@OptimizedDashBoard)
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    try {
                        // Initialize home menu items
                        initializeHomeMenuItems()
                        
                        setupRecyclerView()
                        setupClickListeners()
                        
                        // Hide progress bar after successful setup
                        if (::progressBar.isInitialized) {
                            progressBar.visibility = View.GONE
                        }
                        
                        Log.d(TAG, "Data initialization completed successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting up UI components", e)
                        if (::progressBar.isInitialized) {
                            progressBar.visibility = View.GONE
                        }
                        Toast.makeText(this@OptimizedDashBoard, "Error setting up dashboard", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing data", e)
                withContext(Dispatchers.Main) {
                    if (::progressBar.isInitialized) {
                        progressBar.visibility = View.GONE
                    }
                    Toast.makeText(this@OptimizedDashBoard, "Error loading dashboard data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeHomeMenuItems() {
        homeList.clear()
        
        // Get dashboard type from intent
        val dashboardType = intent.getStringExtra("DASHBOARD_TYPE") ?: "default"
        // val userType = intent.getStringExtra("USER_TYPE") ?: "parent" // Unused
        
        when (dashboardType) {
            "staff_leave" -> {
                // Staff Leave Application Dashboard
                homeList.addAll(listOf(
                    HomeModel(1, R.drawable.man, "Staff Profile", "View Profile Details"),
                    HomeModel(2, R.drawable.attendence, "Attendance", "View Attendance"),
                    HomeModel(3, R.drawable.timetablee, "View TimeTable", "Schedule"),
                    HomeModel(4, R.drawable.feedback, "Feedback", "View Feedback"),
                    HomeModel(5, R.drawable.salary, "Salary", "View Salary"),
                    HomeModel(6, R.drawable.progress_report, "Payment History", "View History"),
                    HomeModel(7, R.drawable.schedule, "Assign Task", "View Tasks"),
                    HomeModel(8, R.drawable.children, "Complain Box", "Submit Complaints"),
                    HomeModel(9, R.drawable.share, "Parent Leave Application", "Apply for Leave"),
                    HomeModel(10, R.drawable.rate, "Events/News", "View Events"),
                    HomeModel(11, R.drawable.key, "Change Password", "Update Password"),
                    HomeModel(12, R.drawable.logout, "Logout", "Sign Out")
                ))
            }
            "academics" -> {
                // Child Academics Dashboard - focus on academic features with Leave Application
                homeList.addAll(listOf(
                    HomeModel(1, R.drawable.attendence, "Attendance History", "View attendance"),
                    HomeModel(2, R.drawable.progress_report, "Progress Report", "View reports"),
                    HomeModel(3, R.drawable.feedback, "Teacher Remarks", "View remarks"),
                    HomeModel(4, R.drawable.timetablee, "View TimeTable", "Schedule"),
                    HomeModel(5, R.drawable.schedule, "Date Sheet", "View dates"),
                    HomeModel(6, R.drawable.children, "Child List", "View children"),
                    HomeModel(7, R.drawable.man, "Parent Profile", "View profile"),
                    HomeModel(8, R.drawable.chalan, "Fee Challan", "View challan"),
                    HomeModel(9, R.drawable.share, "Parent Leave Application", "Apply for Leave"),
                    HomeModel(10, R.drawable.rate, "Share App", "Share with friends"),
                    HomeModel(11, R.drawable.key, "Change Password", "Update password"),
                    HomeModel(12, R.drawable.logout, "Logout", "Sign Out")
                ))
            }
            "utilities" -> {
                // Utilities Dashboard - focus on utility features with Leave Application
                homeList.addAll(listOf(
                    HomeModel(1, R.drawable.share, "Share App", "Share with friends"),
                    HomeModel(2, R.drawable.rate, "Rate", "Rate our app"),
                    HomeModel(3, R.drawable.key, "Change Password", "Update password"),
                    HomeModel(4, R.drawable.man, "Parent Profile", "View profile"),
                    HomeModel(5, R.drawable.children, "Child List", "View children"),
                    HomeModel(6, R.drawable.chalan, "Fee Challan", "View challan"),
                    HomeModel(7, R.drawable.attendence, "Attendance History", "View attendance"),
                    HomeModel(8, R.drawable.progress_report, "Progress Report", "View reports"),
                    HomeModel(9, R.drawable.feedback, "Leave Application", "Apply for Leave"),
                    HomeModel(10, R.drawable.timetablee, "View TimeTable", "Schedule"),
                    HomeModel(11, R.drawable.schedule, "Date Sheet", "View dates"),
                    HomeModel(12, R.drawable.logout, "Logout", "Sign Out")
                ))
            }
            else -> {
                // Default Dashboard - all features with Leave Application
                homeList.addAll(listOf(
                    HomeModel(1, R.drawable.man, "Parent Profile", "View Profile"),
                    HomeModel(2, R.drawable.children, "Child List", "View Children"),
                    HomeModel(3, R.drawable.chalan, "Fee Challan", "View Challan"),
                    HomeModel(4, R.drawable.attendence, "Attendance History", "View"),
                    HomeModel(5, R.drawable.progress_report, "Progress Report", "View"),
                    HomeModel(6, R.drawable.feedback, "Teacher Remarks", "View"),
                    HomeModel(7, R.drawable.timetablee, "View TimeTable", "Schedule"),
                    HomeModel(8, R.drawable.schedule, "Date Sheet", "View"),
                    HomeModel(9, R.drawable.share, "Parent Leave Application", "Apply for Leave"),
                    HomeModel(10, R.drawable.rate, "Share App", "Share with friends"),
                    HomeModel(11, R.drawable.key, "Change Password", "Update password"),
                    HomeModel(12, R.drawable.logout, "Logout", "Sign Out")
                ))
            }
        }
        
        Log.d(TAG, "Initialized menu items for dashboard type: $dashboardType with ${homeList.size} items")
    }



    private fun setupRecyclerView() {
        try {
            if (::homeRcv.isInitialized) {
                // Set up 3-column grid layout
                val layoutManager = GridLayoutManager(this@OptimizedDashBoard, GRID_SPAN_COUNT)
                homeRcv.layoutManager = layoutManager
                
                // Set up adapter
                homeAdapter = HomeAdaptor(homeList, this)
                homeRcv.adapter = homeAdapter
                
                // Optimize RecyclerView performance
                homeRcv.setHasFixedSize(true)
                homeRcv.setItemViewCacheSize(20)
                
                Log.d(TAG, "RecyclerView setup completed successfully")
            } else {
                Log.e(TAG, "homeRcv is null - cannot setup RecyclerView")
                Toast.makeText(this, "Error: Could not initialize dashboard", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Setup click listeners
            moreOption.setOnClickListener { showPopupMenu(it) }
            
            // Setup toolbar
            findViewById<Toolbar>(R.id.toolbar)?.apply {
                setTitleTextColor(ContextCompat.getColor(this@OptimizedDashBoard, R.color.white_color))
                title = "Dashboard"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }







    private var customPopupMenu: CustomPopupMenu? = null
    
    private fun showPopupMenu(view: View) {
        try {
            if (customPopupMenu == null) {
                val userType = intent.getStringExtra("USER_TYPE") ?: "parent"
                customPopupMenu = CustomPopupMenu(this, view, userType)
                customPopupMenu?.setOnMenuItemClickListener { title ->
                    when (title) {
                        "Change Login Password" -> {
                            showChangePasswordDialog()
                        }
                        "About App" -> {
                            showAboutDialog()
                        }
                    }
                    true
                }
            }
            
            if (customPopupMenu?.isShowing() == true) {
                customPopupMenu?.dismiss()
            } else {
                customPopupMenu?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing popup menu", e)
        }
    }

    private fun performLogout() {
        try {
            showLoading(true)
            val mApiService = API.getAPIService()
            val parentId = Paper.book().read<String>("parent_id")
            val campusId = Paper.book().read<String>("campus_id")
            
            val postParam = HashMap<String, String>().apply {
                put("parent_parent_id", parentId ?: "")
                put("campus_id", campusId ?: "")
            }
            
            val body = JSONObject(postParam as Map<String, Any>).toString().toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )
            
            mApiService.logout_parent(body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.body() != null) {
                        showLoading(false)
                        Paper.book().write(Constants.is_login, false)
                        val intent = Intent(this@OptimizedDashBoard, SelectRole::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        showLoading(false)
                        showError(response.raw().message)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "Logout failed", t)
                    showLoading(false)
                    showError("Logout failed: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            showError("Error during logout")
            showLoading(false)
        }
    }

    private fun showChangePasswordDialog() {
        // Implement change password dialog
        Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        try {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About Topgrade Software App")
                .setMessage("Version: ${BuildConfig.VERSION_NAME}\n\nA comprehensive school management application for parents, students, and staff.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing about dialog", e)
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Topgrade Software App")
                putExtra(Intent.EXTRA_TEXT, "Check out this amazing school management app!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing app", e)
        }
    }

    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${BuildConfig.APPLICATION_ID}".toUri())
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "Play Store not found, using web browser", e)
            // Fallback to web browser
            val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri())
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Play Store", e)
            showError("Unable to open Play Store")
        }
    }

    /**
     * Example method showing how to use DateSheetBuilder with different parameters
     * This demonstrates the flexibility for different user types and scenarios
     */
    private fun loadDateSheetExample() {
        val dateSheetBuilder = DateSheetBuilder(this)
        
        // Example 1: Load date sheet for current user (from Paper database)
        val currentUserParams = dateSheetBuilder.createParamsFromPaper()
        Log.d(TAG, "Loading date sheet for current user: $currentUserParams")
        
        // Example 2: Load date sheet for a specific staff member
        val staffParams = dateSheetBuilder.createStaffParams(
            staffId = "STAFF123",
            campusId = "CAMPUS456"
        )
        Log.d(TAG, "Loading date sheet for staff: $staffParams")
        
        // Example 3: Load date sheet for a specific student
        val studentParams = dateSheetBuilder.createStudentParams(
            studentId = "STUDENT789",
            campusId = "CAMPUS456",
            parentId = "PARENT101"
        )
        Log.d(TAG, "Loading date sheet for student: $studentParams")
        
        // Example 4: Custom parameters with all three key values
        val customParams = DateSheetBuilder.DateSheetParams(
            staffId = "STAFF123",
            campusId = "CAMPUS456", 
            examSessionId = "EXAM_SESSION_2024",
            studentId = "STUDENT789",
            parentId = "PARENT101",
            studentClassId = "CLASS_10A"
        )
        Log.d(TAG, "Loading date sheet with custom params: $customParams")
        
        // Load exam sessions first, then date sheet
        dateSheetBuilder.loadExamSessions(
            params = customParams,
            progressBar = progressBar,
            callback = object : DateSheetBuilder.DateSheetCallback {
                override fun onExamSessionsLoaded(sessions: List<ExamSession>) {
                    Log.d(TAG, "Exam sessions loaded: ${sessions.size} sessions")
                    
                    // For demo, use the first session
                    if (sessions.isNotEmpty()) {
                        val firstSession = sessions.first()
                        val paramsWithSession = customParams.copy(examSessionId = firstSession.uniqueId)
                        
                        // Now load the date sheet
                        dateSheetBuilder.loadDateSheet(
                            params = paramsWithSession,
                            progressBar = progressBar,
                            callback = this
                        )
                    } else {
                        Toast.makeText(this@OptimizedDashBoard, "No exam sessions available", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onDateSheetLoaded(dateSheetResponse: topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetResponse) {
                    Log.d(TAG, "Date sheet loaded successfully")
                    Toast.makeText(this@OptimizedDashBoard, "Date sheet loaded successfully", Toast.LENGTH_SHORT).show()
                    
                    // Here you can process the date sheet data
                    // For example, navigate to a date sheet display activity
                    // or update UI with the date sheet information
                }
                
                override fun onError(message: String) {
                    Log.e(TAG, "Date sheet error: $message")
                    Toast.makeText(this@OptimizedDashBoard, "Error: $message", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    override fun OnMenuCLick(view: View, title: String) {
        try {
            handleMenuClick(title)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling menu click", e)
        }
    }

    private fun handleMenuClick(title: String) {
        try {
            val resolvedUserType = resolveCurrentUserType()
            when (title) {
                "Parent Profile" -> {
                    // Navigate to parent profile
                    val intent = Intent(this, ParentProfile::class.java)
                    startActivity(intent)
                }
                "Staff Profile" -> {
                    // Navigate to staff profile - check user type to prevent wrong routing
                    val userTypeRaw = intent.getStringExtra("USER_TYPE") ?: Paper.book().read(Constants.User_Type, "parent")
                    Log.d(TAG, "Staff Profile click - USER_TYPE extra: ${intent.getStringExtra("USER_TYPE")}, Paper User_Type: ${Paper.book().read(Constants.User_Type, "not_found")}, Final userType: $userTypeRaw")
                    if (resolvedUserType == UserType.TEACHER || resolvedUserType == UserType.STAFF) {
                        val intent = Intent(this, StaffProfile::class.java)
                        startActivity(intent)
                    } else {
                        Log.w(TAG, "Staff Profile clicked but user type is not staff/teacher: $resolvedUserType")
                        Toast.makeText(this, "Staff profile is only available for staff members", Toast.LENGTH_SHORT).show()
                    }
                }
                "Child List" -> {
                    // Navigate to child list
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ChildList::class.java)
                    startActivity(intent)
                }
                "Fee Challan" -> {
                    // Navigate to fee challan
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.FeeChalan::class.java)
                    startActivity(intent)
                }
                "Salary" -> {
                    // Navigate to staff salary
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.StaffSalary::class.java)
                    startActivity(intent)
                }
                "Payment History" -> {
                    // Navigate to payment history
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.PaymentHistory::class.java)
                    startActivity(intent)
                }
                "Attendance", "Attendance History" -> {
                    val intent = Intent(this, AttendanceMenu::class.java)
                    startActivity(intent)
                }
                "Progress Report" -> {
                    // Navigate to progress report
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.Report::class.java)
                    startActivity(intent)
                }
                "Teacher Remarks", "Feedback" -> {
                    // Navigate to remarks/feedback
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ParentFeedback::class.java)
                    startActivity(intent)
                }
                "View TimeTable" -> {
                    // Navigate to timetable
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ModernStudentTimeTable::class.java)
                    startActivity(intent)
                }
                "Date Sheet" -> {
                    // Navigate to date sheet
                    val intent = Intent(this, StudentDateSheet::class.java)
                    startActivity(intent)
                }
                "Parent Leave Application" -> {
                    // Navigate to parent leave application
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ParentAddApplication::class.java)
                    startActivity(intent)
                }
                "Assign Task" -> {
                    // Navigate to assign task
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu::class.java)
                    startActivity(intent)
                }
                "Complain Box" -> {
                    // Navigate to complain box
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.Complaint.StaffSubmitComplaint::class.java)
                    startActivity(intent)
                }
                "Leave Application" -> {
                    // Navigate to leave application menu
                    val intent = Intent(this, topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffApplicationMenu::class.java)
                    startActivity(intent)
                }
                "Events/News" -> {
                    // Navigate to events/news
                    Toast.makeText(this, "Events/News feature coming soon", Toast.LENGTH_SHORT).show()
                }
                "Share App" -> shareApp()
                "Rate" -> rateApp()
                "Change Password" -> {
                    val passwordIntent = Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.PasswordsChange::class.java)
                    passwordIntent.putExtra("User_TYpe", resolvedUserType.value)
                    startActivity(passwordIntent)
                }
                "Logout" -> performLogout()
                else -> {
                    Log.w(TAG, "Unknown menu item clicked: $title")
                    Toast.makeText(this, "Feature coming soon: $title", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling menu click for: $title", e)
            Toast.makeText(this, "Error opening $title", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(show: Boolean) {
        if (progressBar != null) {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Cancel all coroutines
        dashboardScope.cancel()
    }
    
    /**
     * Apply theme based on user type
     */
    private fun applyTheme() {
        try {
            val userType = resolveCurrentUserType()
            Log.d(TAG, "User Type: ${userType.value}")

            val navigationColorRes = when (userType) {
                UserType.STUDENT -> {
                    ThemeHelper.applyStudentTheme(this)
                    Log.d(TAG, "Student theme applied successfully")
                    R.color.student_primary
                }
                else -> {
                    ParentThemeHelper.applyParentTheme(this, 140)
                    ParentThemeHelper.setHeaderIconVisibility(this, true)
                    ParentThemeHelper.setMoreOptionsVisibility(this, true)
                    ParentThemeHelper.setFooterVisibility(this, true)
                    ParentThemeHelper.setHeaderTitle(this, "Dashboard")
                    Log.d(TAG, "Parent theme applied successfully - UserType: ${userType.value}")
                    R.color.parent_primary
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = ContextCompat.getColor(this, navigationColorRes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = window.decorView
                decor.systemUiVisibility =
                    decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }

            ThemeHelper.applyFooterTheme(this, userType.value)
            findViewById<MaterialCardView>(R.id.footer_container)
                ?.setCardBackgroundColor(ContextCompat.getColor(this, navigationColorRes))

        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme", e)
            try {
                ThemeHelper.applyParentTheme(this)
                Log.d(TAG, "Fallback parent theme applied due to error")
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Error applying fallback theme", fallbackError)
            }
        }
    }

    private fun setupWindowInsets() {
        val root = findViewById<View>(android.R.id.content) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            try {
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val footer = findViewById<View>(R.id.footer_container)
                footer?.layoutParams?.let { params ->
                    val marginParams = params as? ViewGroup.MarginLayoutParams
                    if (marginParams != null) {
                        val bottomMargin = if (systemInsets.bottom > 0) systemInsets.bottom else 0
                        if (marginParams.bottomMargin != bottomMargin) {
                            marginParams.bottomMargin = bottomMargin
                            footer.layoutParams = marginParams
                        }
                    }
                }
                view.setPadding(0, 0, 0, 0)
                WindowInsetsCompat.CONSUMED
            } catch (e: Exception) {
                Log.e(TAG, "Error handling window insets: ${e.message}")
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun resolveCurrentUserType(): UserType {
        val userTypeRaw = intent.getStringExtra("USER_TYPE") ?: Paper.book().read(Constants.User_Type, "")
        return UserType.fromString(userTypeRaw) ?: UserType.PARENT
    }
} 