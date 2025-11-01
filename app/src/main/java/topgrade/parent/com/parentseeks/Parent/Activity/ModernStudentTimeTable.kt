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
import topgrade.parent.com.parentseeks.R
import io.paperdb.Paper
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import components.searchablespinnerlibrary.SearchableSpinner


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
            // Apply theme based on user type for non-student users
            applyTheme()
        }
        
        initDataStore()
        initViews()
        setupRecyclerView()
        setupBackButton()
        setupInlineFilters()
        listeners()
        
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
                val childModel = timetableDataStore.currentChildModel.first()
                val students = childModel?.students ?: emptyList()
                
                if (students.isNotEmpty()) {
                    val studentNames = students.map { "${it.className} - ${it.fullName}" }
                    val adapter = ArrayAdapter(this@ModernStudentTimeTable, android.R.layout.simple_list_item_1, studentNames.toMutableList())
                    selectChildSpinner.adapter = adapter
                    
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
                val campusId = timetableDataStore.campusId.first()
                
                val postParam = mapOf(
                    "parent_id" to campusId,
                    "student_id" to selectedChildId,
                    "session_id" to Constant.current_session
                )
                
                val requestJson = JSONObject(postParam).toString()
                val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                
                Constant.mApiService.loadStudentTimetableSession(body).enqueue(object : Callback<StudentTimetableSessionResponse> {
                    override fun onResponse(call: Call<StudentTimetableSessionResponse>, response: Response<StudentTimetableSessionResponse>) {
                        if (response.isSuccessful && response.body()?.status?.code == "1000") {
                            val sessions = response.body()?.timetableSessionStudent ?: emptyList()
                            setupSessionSpinner(sessions)
                        } else {
                            showError("Failed to load timetable sessions")
                        }
                    }
                    
                    override fun onFailure(call: Call<StudentTimetableSessionResponse>, t: Throwable) {
                        Log.e(TAG, "loadTimetableSessions() - API call failed", t)
                        showError("Network error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
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
        
        list.forEach { timetable ->
            timetable.detail?.forEach { detail ->
                // Use safe property access for Detail model
                val subject = detail.subject ?: "Unknown Subject"
                val startTime = detail.startTime ?: "00:00"
                val endTime = detail.endTime ?: "00:00"
                
                sb.append("$subject: $startTime-$endTime\n")
            }
        }
        
        return sb.toString()
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
                    ShareUtils.shareText(this, "Student Timetable", timetableSms)
                } else {
                    showError("No timetable data to share")
                }
            }
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
            } else {
                // Apply parent theme (dark brown) when accessed from parent context
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for timetable
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for timetable
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Student Timetable");
                
                Log.d(TAG, "Parent theme applied successfully")
            }
            
            // Apply footer theme
            ThemeHelper.applyFooterTheme(this, userType ?: "")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme", e)
        }
    }
} 