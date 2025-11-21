@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Parent.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.Parent.Adaptor.StudentTimetableAdaptor
import topgrade.parent.com.parentseeks.Parent.Model.ChildModel
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableResponse
import topgrade.parent.com.parentseeks.Parent.Model.timetable.StudentTimetableSessionResponse
import topgrade.parent.com.parentseeks.Parent.Model.timetable.Timetable
import topgrade.parent.com.parentseeks.Parent.Model.timetable.TimetableSessionStudent
import topgrade.parent.com.parentseeks.Parent.Model.timetable.Detail
import topgrade.parent.com.parentseeks.Parent.Utils.ShareUtils
import topgrade.parent.com.parentseeks.Parent.Utils.TimetableDataStore
import topgrade.parent.com.parentseeks.Parent.Utils.TimetableMigrationHelper
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import topgrade.parent.com.parentseeks.R
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import topgrade.parent.com.parentseeks.Teacher.Utils.Util
import components.searchablespinnerlibrary.SearchableSpinner
import android.widget.PopupMenu
import android.widget.ListView
import android.view.Gravity
import android.widget.PopupWindow
import android.view.ViewGroup


/**
 * Modern Student Timetable Activity using TimetableDataStore
 * Replaces Paper DB with modern DataStore implementation
 */
class ModernStudentTimeTable : AppCompatActivity(), View.OnClickListener {
    
    companion object {
        private const val TAG = "ModernStudentTimeTable"
    }
    
    // UI Components
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalRecords: TextView
    private lateinit var sentTimetableInSms: Button
    private lateinit var timetableRcv: RecyclerView
    private lateinit var selectChildSpinner: SearchableSpinner
    private lateinit var selectExamSession: SearchableSpinner
    
    // DataStore
    private lateinit var timetableDataStore: TimetableDataStore
    private lateinit var migrationHelper: TimetableMigrationHelper
    
    // Data
    private var student: SharedStudent? = null
    private var list: kotlin.collections.List<Timetable> = emptyList()
    private var timetableSms = ""
    private var selectedChildId = ""
    private var selectedStudentClassId = ""
    private var selectedChildName = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_student_timetable)
        
        // Apply student theme (teal) for student user type
        val userType = Paper.book().read(Constants.User_Type, "")
        if ("STUDENT" == userType) {
            // Apply student theme using StudentThemeHelper
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.applyStudentTheme(this, 100)
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderIconVisibility(this, false)
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setMoreOptionsVisibility(this, false)
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setFooterVisibility(this, true)
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderTitle(this, "Time Table")
        } else {
            // Configure status bar for dark brown background with white icons
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Set transparent status bar to allow header wave to cover it
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = ContextCompat.getColor(this, R.color.dark_brown)
                
                // For Android M and above, ensure white status bar icons on dark background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    var flags = window.decorView.systemUiVisibility
                    // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                    flags = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    window.decorView.systemUiVisibility = flags
                }
            }

            // Configure status bar and navigation bar icons for Android R and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }
            
            // For parent theme, don't use ParentThemeHelper as it overwrites navigation bar color
            // System bars are already configured above to match child list
            Log.d(TAG, "Parent theme - system bars already configured in onCreate()")
        }
        
        // Setup window insets
        setupWindowInsets()
        
        // Initialize Paper database
        Paper.init(this)
        
        initDataStore()
        initViews()
        setupRecyclerView()
        setupBackButton()
        setupInlineFilters()
        listeners()
        
        // Initialize header title
        findViewById<TextView>(R.id.header_title)?.text = getString(R.string.timetable)
        
        Log.d(TAG, "onCreate() - Modern Student Timetable initialized")
    }
    
    private fun initDataStore() {
        timetableDataStore = TimetableDataStore(this)
        migrationHelper = TimetableMigrationHelper(this)
    }
    
    private fun initViews() {
        selectChildSpinner = findViewById(R.id.selectChildSpinner)
        selectExamSession = findViewById(R.id.selectExamSession)
        tvTotalRecords = findViewById(R.id.total_records)
        sentTimetableInSms = findViewById(R.id.send_timetable_In_sms)
        timetableRcv = findViewById(R.id.rv_student_timetable)
        progressBar = findViewById(R.id.content_progress_bar)
    }
    
    private fun setupRecyclerView() {
        timetableRcv.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "setupRecyclerView() - RecyclerView initialized with LinearLayoutManager")
    }
    
    
    
    private fun updateStudentInfo() {
        student?.let { student ->
            Log.d(TAG, "updateStudentInfo() - Updated UI for student: ${student.fullName}")
        } ?: run {
            Log.d(TAG, "updateStudentInfo() - No student selected")
        }
    }
    
    private fun setupBackButton() {
        (findViewById(R.id.back_button) as ImageView).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun setupInlineFilters() {
        // Setup child spinner
        selectChildSpinner.setTitle("Select Child")
        // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
        
        // Setup exam session spinner
        selectExamSession.setTitle("Select Timetable Session")
        // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
        
        // Load students and setup adapter
        loadStudentsAndSetupAdapter()
    }
    
    private fun loadStudentsAndSetupAdapter() {
        lifecycleScope.launch {
            try {
                // Try to load from TimetableDataStore first
                var childModel = timetableDataStore.currentChildModel.first()
                var students = childModel?.students ?: emptyList()
                
                // If no students found in DataStore, try Paper DB (for parent users)
                if (students.isEmpty()) {
                    Log.d(TAG, "No students in DataStore, trying Paper DB...")
                    try {
                        val paperStudents = Paper.book().read<List<SharedStudent>>("students")
                        if (paperStudents != null && paperStudents.isNotEmpty()) {
                            students = paperStudents
                            Log.d(TAG, "Loaded ${students.size} students from Paper DB")
                            
                            // Convert Paper DB students to ChildModel format and save to DataStore for future use
                            if (students.isNotEmpty()) {
                                childModel = ChildModel().apply {
                                    setStudents(students)
                                }
                                timetableDataStore.saveCurrentChildModel(childModel)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading students from Paper DB", e)
                    }
                } else {
                    Log.d(TAG, "Loaded ${students.size} students from DataStore")
                }
                
                if (students.isNotEmpty()) {
                    val studentNames = students.map { "${it.className} - ${it.fullName}" }
                    val adapter = ArrayAdapter(this@ModernStudentTimeTable, android.R.layout.simple_list_item_1, studentNames.toMutableList())
                    selectChildSpinner.adapter = adapter
                    
                    // Auto-select first student if only one exists
                    if (students.size == 1) {
                        student = students[0]
                        selectedChildId = student?.uniqueId ?: ""
                        selectedStudentClassId = student?.studentClassId ?: ""
                        selectedChildName = student?.fullName ?: ""
                        updateStudentInfo()
                        loadTimetableSessions()
                    }
                    
                    // Setup selection listener
                    selectChildSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            student = students[position]
                            selectedChildId = student?.uniqueId ?: ""
                            selectedStudentClassId = student?.studentClassId ?: ""
                            selectedChildName = student?.fullName ?: ""
                            updateStudentInfo()
                            loadTimetableSessions()
                        }
                        
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                } else {
                    Log.e(TAG, "No students found in either DataStore or Paper DB")
                    showError("No students found. Please contact your school administrator.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadStudentsAndSetupAdapter() - Error loading students", e)
                showError("Error loading students: ${e.message}")
            }
        }
    }
    
    private fun loadTimetableSessions() {
        if (selectedChildId.isEmpty()) {
            showError("Please select a student first")
            return
        }
        
        lifecycleScope.launch {
            try {
                // Try to get campusId from DataStore first, then fallback to Constant or Paper DB
                var campusId = timetableDataStore.campusId.first()
                
                if (campusId.isEmpty()) {
                    // Fallback to Constant.campus_id
                    campusId = Constant.campus_id ?: ""
                    Log.d(TAG, "CampusId from DataStore empty, using Constant.campus_id: $campusId")
                }
                
                if (campusId.isEmpty()) {
                    // Final fallback to Paper DB
                    campusId = Paper.book().read<String>("campus_id") ?: ""
                    Log.d(TAG, "CampusId from Constant empty, using Paper DB: $campusId")
                }
                
                if (campusId.isEmpty()) {
                    showError("Campus ID not available. Please login again.")
                    Log.e(TAG, "loadTimetableSessions() - Campus ID is empty")
                    return@launch
                }
                
                val postParam = mapOf(
                    "parent_id" to campusId,
                    "student_id" to selectedChildId,
                    "session_id" to Constant.current_session
                )
                
                Log.d(TAG, "loadTimetableSessions() - Request parameters:")
                Log.d(TAG, "  parent_id: $campusId")
                Log.d(TAG, "  student_id: $selectedChildId")
                Log.d(TAG, "  session_id: ${Constant.current_session}")
                
                val requestJson = JSONObject(postParam).toString()
                val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                
                progressBar.visibility = View.VISIBLE
                
                Constant.mApiService.loadStudentTimetableSession(body).enqueue(object : Callback<StudentTimetableSessionResponse> {
                    override fun onResponse(call: Call<StudentTimetableSessionResponse>, response: Response<StudentTimetableSessionResponse>) {
                        progressBar.visibility = View.GONE
                        
                        Log.d(TAG, "loadTimetableSessions() - Response code: ${response.code()}")
                        Log.d(TAG, "loadTimetableSessions() - Response successful: ${response.isSuccessful}")
                        
                        if (response.isSuccessful && response.body() != null) {
                            val statusCode = response.body()?.status?.code
                            val statusMessage = response.body()?.status?.message
                            
                            Log.d(TAG, "loadTimetableSessions() - Status code: $statusCode")
                            Log.d(TAG, "loadTimetableSessions() - Status message: $statusMessage")
                            
                            if (statusCode == "1000") {
                                val sessions = response.body()?.timetableSessionStudent ?: emptyList()
                                Log.d(TAG, "loadTimetableSessions() - Loaded ${sessions.size} sessions")
                                setupSessionSpinner(sessions)
                            } else {
                                val errorMsg = statusMessage ?: "Failed to load timetable sessions"
                                Log.e(TAG, "loadTimetableSessions() - API error: $errorMsg")
                                showError(errorMsg)
                            }
                        } else {
                            Log.e(TAG, "loadTimetableSessions() - Response not successful or body is null")
                            showError("Failed to load timetable sessions. Please try again.")
                        }
                    }
                    
                    override fun onFailure(call: Call<StudentTimetableSessionResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.e(TAG, "loadTimetableSessions() - API call failed", t)
                        showError("Network error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "loadTimetableSessions() - Error", e)
                showError("Error loading sessions: ${e.message}")
            }
        }
    }
    
    private fun setupSessionSpinner(sessions: List<TimetableSessionStudent>) {
        if (sessions.isNotEmpty()) {
            val sessionNames = sessions.map { it.fullName ?: "Unknown Session" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sessionNames.toMutableList())
            selectExamSession.adapter = adapter
            
            selectExamSession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedSession = sessions[position]
                    loadTimetable(selectedSession.uniqueId ?: "")
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            
            // Auto-select first session if only one available
            if (sessions.size == 1) {
                selectExamSession.setSelection(0)
            }
        } else {
            showError("No timetable sessions found")
        }
    }
    
    
    
    
    private fun loadTimetable(sessionId: String) {
        Log.d(TAG, "=== TIMETABLE LOADING STARTED ===")
        Log.d(TAG, "Session ID: $sessionId")
        Log.d(TAG, "Student: ${student?.fullName ?: "NULL"}")
        Log.d(TAG, "Selected Child ID: $selectedChildId")
        
        if (selectedChildId.isEmpty() || student == null) {
            showError("Please select a student first")
            return
        }
        
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                
                val studentId = if (selectedChildId.isNotEmpty()) selectedChildId else student?.uniqueId ?: ""
                val parentIdParam = student?.parentId ?: ""
                
                val postParam = mapOf(
                    "student_id" to studentId,
                    "parent_id" to parentIdParam,
                    "timetable_session_id" to sessionId
                )
                
                // Log request parameters
                Log.d(TAG, "Request Parameters:")
                Log.d(TAG, "- student_id: $studentId")
                Log.d(TAG, "- parent_id: $parentIdParam")
                Log.d(TAG, "- timetable_session_id: $sessionId")
                
                val requestJson = JSONObject(postParam).toString()
                Log.d(TAG, "Request JSON: $requestJson")
                val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                
                Constant.mApiService.loadStudentTimetable(body).enqueue(object : Callback<StudentTimetableResponse> {
                    override fun onResponse(call: Call<StudentTimetableResponse>, response: Response<StudentTimetableResponse>) {
                        progressBar.visibility = View.GONE
                        
                        if (response.isSuccessful && response.body()?.status?.code == "1000") {
                            list = response.body()?.timetable ?: emptyList()
                            if (list.isNotEmpty()) {
                                updateTimetableUI()
                                sentTimetableInSms.visibility = View.VISIBLE
                                timetableSms = generateTimetableSms()
                                Log.d(TAG, "loadTimetable() - Successfully loaded ${list.size} timetable records")
                            } else {
                                showError("No timetable data available")
                            }
                        } else {
                            val errorMessage = response.body()?.status?.message ?: "Failed to load timetable data"
                            showError(errorMessage)
                            Log.e(TAG, "loadTimetable() - API error: $errorMessage")
                        }
                    }
                    
                    override fun onFailure(call: Call<StudentTimetableResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.e(TAG, "loadTimetable() - API call failed", t)
                        showError("Network error: ${t.message}")
                    }
                })
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "loadTimetable() - Error loading timetable", e)
                showError("Error loading timetable: ${e.message}")
            }
        }
    }
    
    private fun updateTimetableUI() {
        tvTotalRecords.text = "Total Records: ${list.size}"
        
        // Extract Detail objects from Timetable objects
        val detailList: List<Detail> = list.flatMap { timetable ->
            timetable.detail ?: emptyList()
        }
        
        // Setup adapter with detail data
        val adapter = StudentTimetableAdaptor(detailList, this@ModernStudentTimeTable)
        timetableRcv.adapter = adapter
        
        // Generate SMS text
        timetableSms = generateTimetableSms()
        
        Log.d(TAG, "updateTimetableUI() - Timetable updated with ${list.size} records, ${detailList.size} details")
    }
    
    private fun generateTimetableSms(): String {
        val sb = StringBuilder()
        sb.append("Timetable for ${student?.fullName ?: "Student"}:\n\n")
        
        // Extract all details and sort by start time (same as adapter)
        val allDetails = mutableListOf<Detail>()
        list.forEach { timetable ->
            timetable.detail?.let { details ->
                allDetails.addAll(details)
            }
        }
        
        // Sort by start time in ascending order (same as adapter)
        val sortedDetails = allDetails.sortedBy { detail ->
            detail.startTime ?: "00:00"
        }
        
        // Generate SMS in sorted order
        sortedDetails.forEach { detail ->
            // Use safe property access for Detail model
            val subject = detail.subject ?: "Unknown Subject"
            val startTime = formatTimeWithoutSeconds(detail.startTime ?: "00:00")
            val endTime = formatTimeWithoutSeconds(detail.endTime ?: "00:00")
            val teacher = detail.staff ?: "N/A"
            
            sb.append("$subject: $startTime-$endTime ($teacher)\n")
        }
        
        return sb.toString()
    }
    
    /**
     * Format time string to remove seconds (e.g., "07:45:00" -> "07:45")
     */
    private fun formatTimeWithoutSeconds(time: String): String {
        return if (time.contains(":") && time.split(":").size > 2) {
            // Remove seconds if present (format: HH:MM:SS -> HH:MM)
            time.substring(0, time.lastIndexOf(":"))
        } else {
            time
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "showError() - $message")
    }
    
    private fun listeners() {
        // SMS button click listener
        sentTimetableInSms.setOnClickListener(this)
    }
    
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.send_timetable_In_sms -> {
                if (timetableSms.isNotEmpty()) {
                    showShareMenu(v)
                } else {
                    showError("No timetable data to share")
                }
            }
        }
    }
    
    /**
     * Show share menu with multiple options - same as teacher feedback
     * Uses PopupWindow to ensure it opens above the button and doesn't get hidden by navigation bar
     */
    private fun showShareMenu(view: View) {
        val menuItems = listOf(
            "Send via WhatsApp",
            "Send via WhatsApp Business",
            "Send via Local SMS",
            "Share via Other Apps"
        )
        
        val listView = ListView(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, menuItems)
        listView.adapter = adapter
        
        val popupWindow = PopupWindow(
            listView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        
        // Set background and elevation for better appearance
        popupWindow.setBackgroundDrawable(getDrawable(android.R.drawable.dialog_holo_light_frame))
        popupWindow.elevation = 8f
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val title = menuItems[position]
            
            // Copy timetable logic - read phone from PaperDB each time (same as feedback)
            when (title) {
                "Send via WhatsApp" -> {
                    val phone = Paper.book().read<String>("phone") ?: ""
                    Util.shareToWhatsAppWithNumber(this, timetableSms, phone, "com.whatsapp")
                }
                "Send via WhatsApp Business" -> {
                    val phone_business = Paper.book().read<String>("phone") ?: ""
                    Util.shareToWhatsAppWithNumber(this, timetableSms, phone_business, "com.whatsapp.w4b")
                }
                "Send via Local SMS" -> {
                    val phone_sms = Paper.book().read<String>("phone") ?: ""
                    Util.showSmsIntent(this, timetableSms, phone_sms)
                }
                "Share via Other Apps" -> {
                    val phone_other = Paper.book().read<String>("phone") ?: ""
                    Util.shareWithPhoneNumber(this, timetableSms, phone_other)
                }
            }
            popupWindow.dismiss()
        }
        
        // Post to ensure view is laid out before measuring
        view.post {
            // Measure popup to get its height
            listView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val popupHeight = listView.measuredHeight
            
            // Get button location in window
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val buttonX = location[0]
            val buttonY = location[1]
            
            // Convert 16dp to pixels for spacing
            val spacingPx = (16 * resources.displayMetrics.density).toInt()
            
            // Calculate y position to show above button
            val yPosition = buttonY - popupHeight - spacingPx
            
            // Show above the button using showAtLocation with window coordinates
            popupWindow.showAtLocation(
                window.decorView.rootView,
                Gravity.NO_GRAVITY,
                buttonX,
                yPosition
            )
        }
    }
    
    /**
     * Apply theme based on user type
     */
    private fun applyTheme() {
        try {
            val userType = Paper.book().read(Constants.User_Type, "")
            Log.d(TAG, "User Type: $userType")
            
            if (userType == "STUDENT") {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this)
                
                // Apply system bars theme (status bar and navigation bar)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = getColor(R.color.student_primary)
                    window.navigationBarColor = getColor(R.color.student_primary)
                }
                
                // Force dark navigation bar icons (prevent light appearance)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        0, // 0 = do NOT use light icons
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    )
                }
                
                Log.d(TAG, "Student theme applied")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme", e)
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * Uses margin approach like child list - footer pushed above navigation bar,
     * navigation bar's dark_brown color creates transparent/blended appearance
     */
    private fun setupWindowInsets() {
        try {
            val rootLayout = findViewById<View>(android.R.id.content)
            
            rootLayout?.let { root ->
                ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                    try {
                        val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                        findViewById<View>(R.id.footer_container)?.let { footerContainer ->
                            val bottomMargin = if (systemInsets.bottom > 0) systemInsets.bottom else 0
                            val params = footerContainer.layoutParams as? android.view.ViewGroup.MarginLayoutParams
                            params?.let {
                                it.bottomMargin = bottomMargin
                                footerContainer.layoutParams = it
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0)
                        WindowInsetsCompat.CONSUMED
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in window insets listener: ${e.message}")
                        WindowInsetsCompat.CONSUMED
                    }
                }
            } ?: Log.e(TAG, "rootLayout is null - cannot setup window insets")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up window insets: ${e.message}", e)
        }
    }
} 