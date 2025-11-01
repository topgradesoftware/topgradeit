package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import io.paperdb.Paper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession
import topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetResponse
import topgrade.parent.com.parentseeks.Parent.Utils.Constants
import topgrade.parent.com.parentseeks.Parent.Utils.API

/**
 * Flexible DateSheet Builder that can work with different user types and parameters
 * Supports staff ID, campus ID, and exam session ID for different persons
 */
class DateSheetBuilder(private val context: Context) {
    
    companion object {
        private const val TAG = "DateSheetBuilder"
    }
    
    private val apiService: BaseApiService = API.getAPIService()
    
    /**
     * Data class to hold the three key parameters
     */
    data class DateSheetParams(
        val staffId: String? = null,
        val campusId: String? = null,
        val examSessionId: String? = null,
        val studentId: String? = null,
        val parentId: String? = null,
        val studentClassId: String? = null
    )
    
    /**
     * Interface for date sheet callbacks
     */
    interface DateSheetCallback {
        fun onExamSessionsLoaded(sessions: List<ExamSession>)
        fun onDateSheetLoaded(dateSheetResponse: DateSheetResponse)
        fun onError(message: String)
    }
    
    /**
     * Load exam sessions based on the provided parameters
     * This method is flexible and can work with different user types
     */
    fun loadExamSessions(
        params: DateSheetParams,
        progressBar: ProgressBar? = null,
        callback: DateSheetCallback
    ) {
        try {
            // Validate required parameters
            if (params.campusId.isNullOrEmpty()) {
                callback.onError("Campus ID is required")
                return
            }
            
            // Show progress if provided
            progressBar?.visibility = View.VISIBLE
            
            // Build request parameters based on user type
            val postParam = buildExamSessionParams(params)
            
            // Log the request for debugging
            Log.d(TAG, "=== LOADING EXAM SESSIONS ===")
            Log.d(TAG, "Staff ID: ${params.staffId}")
            Log.d(TAG, "Campus ID: ${params.campusId}")
            Log.d(TAG, "Student ID: ${params.studentId}")
            Log.d(TAG, "Parent ID: ${params.parentId}")
            val currentSession = Paper.book().read<String>("current_session")
            Log.d(TAG, "Current Session: $currentSession")
            
            val jsonBody = JSONObject(postParam).toString()
            Log.d(TAG, "Exam Session JSON Body: $jsonBody")
            
            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            
            // Choose the appropriate API endpoint based on user type
            val call = if (params.staffId != null) {
                // For staff/teacher users
                apiService.load_exam_session_teacher(body)
            } else {
                // For parent/student users
                apiService.load_exam_session(body)
            }
            
            call.enqueue(object : Callback<SessionModel> {
                override fun onResponse(call: Call<SessionModel>, response: Response<SessionModel>) {
                    try {
                        progressBar?.visibility = View.GONE
                        
                        if (response.isSuccessful() && response.body() != null) {
                            val sessionModel = response.body()!!
                            if (sessionModel.status?.status == "success" && sessionModel.examSession != null) {
                                Log.d(TAG, "Exam sessions loaded successfully: ${sessionModel.examSession.size} sessions")
                                callback.onExamSessionsLoaded(sessionModel.examSession)
                            } else {
                                val errorMsg = sessionModel.status?.message ?: "No exam sessions available"
                                Log.w(TAG, "No exam sessions: $errorMsg")
                                callback.onError(errorMsg)
                            }
                        } else {
                            val errorMsg = "Failed to load exam sessions: ${response.message()}"
                            Log.e(TAG, errorMsg)
                            callback.onError(errorMsg)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing exam sessions response", e)
                        callback.onError("Error processing exam sessions: ${e.message}")
                    }
                }
                
                override fun onFailure(call: Call<SessionModel>, t: Throwable) {
                    progressBar?.visibility = View.GONE
                    Log.e(TAG, "Failed to load exam sessions", t)
                    callback.onError("Network error: ${t.message}")
                }
            })
            
        } catch (e: Exception) {
            progressBar?.visibility = View.GONE
            Log.e(TAG, "Error in loadExamSessions", e)
            callback.onError("Error loading exam sessions: ${e.message}")
        }
    }
    
    /**
     * Load date sheet based on the provided parameters
     */
    fun loadDateSheet(
        params: DateSheetParams,
        progressBar: ProgressBar? = null,
        callback: DateSheetCallback
    ) {
        try {
            // Validate required parameters
            if (params.campusId.isNullOrEmpty()) {
                callback.onError("Campus ID is required")
                return
            }
            
            if (params.examSessionId.isNullOrEmpty()) {
                callback.onError("Exam session ID is required")
                return
            }
            
            // Show progress if provided
            progressBar?.visibility = View.VISIBLE
            
            // Build request parameters
            val postParam = buildDateSheetParams(params)
            
            // Log the request for debugging
            Log.d(TAG, "=== LOADING DATE SHEET ===")
            Log.d(TAG, "Staff ID: ${params.staffId}")
            Log.d(TAG, "Campus ID: ${params.campusId}")
            Log.d(TAG, "Exam Session ID: ${params.examSessionId}")
            Log.d(TAG, "Student ID: ${params.studentId}")
            Log.d(TAG, "Parent ID: ${params.parentId}")
            Log.d(TAG, "Student Class ID: ${params.studentClassId}")
            
            val jsonBody = JSONObject(postParam).toString()
            Log.d(TAG, "Date Sheet JSON Body: $jsonBody")
            
            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            
            apiService.loadStudentDateSheet(body).enqueue(object : Callback<DateSheetResponse> {
                override fun onResponse(call: Call<DateSheetResponse>, response: Response<DateSheetResponse>) {
                    try {
                        progressBar?.visibility = View.GONE
                        
                        if (response.isSuccessful() && response.body() != null) {
                            val dateSheetResponse = response.body()!!
                            if (dateSheetResponse.status?.status == "success") {
                                Log.d(TAG, "Date sheet loaded successfully")
                                callback.onDateSheetLoaded(dateSheetResponse)
                            } else {
                                val errorMsg = dateSheetResponse.status?.message ?: "No date sheet data available"
                                Log.w(TAG, "No date sheet data: $errorMsg")
                                callback.onError(errorMsg)
                            }
                        } else {
                            val errorMsg = "Failed to load date sheet: ${response.message()}"
                            Log.e(TAG, errorMsg)
                            callback.onError(errorMsg)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing date sheet response", e)
                        callback.onError("Error processing date sheet: ${e.message}")
                    }
                }
                
                override fun onFailure(call: Call<DateSheetResponse>, t: Throwable) {
                    progressBar?.visibility = View.GONE
                    Log.e(TAG, "Failed to load date sheet", t)
                    callback.onError("Network error: ${t.message}")
                }
            })
            
        } catch (e: Exception) {
            progressBar?.visibility = View.GONE
            Log.e(TAG, "Error in loadDateSheet", e)
            callback.onError("Error loading date sheet: ${e.message}")
        }
    }
    
    /**
     * Build exam session parameters based on user type and provided parameters
     */
    private fun buildExamSessionParams(params: DateSheetParams): Map<String, String> {
        val postParam = mutableMapOf<String, String>()
        
        // Always include campus ID
        postParam["campus_id"] = params.campusId!!
        
        // Add current session if available
        val currentSession = Paper.book().read<String>("current_session")
        if (!currentSession.isNullOrEmpty()) {
            postParam["session_id"] = currentSession
        }
        
        // Determine user type and add appropriate parameters
        if (params.staffId != null) {
            // For staff/teacher users
            postParam["employee_id"] = params.staffId
            postParam["parent_id"] = params.campusId // For staff, campus_id is used as parent_id
        } else {
            // For parent/student users
            if (params.studentId != null) {
                postParam["student_id"] = params.studentId
            }
            if (params.parentId != null) {
                postParam["parent_id"] = params.parentId
            } else {
                // Fallback to campus_id if parent_id not provided
                postParam["parent_id"] = params.campusId
            }
        }
        
        return postParam
    }
    
    /**
     * Build date sheet parameters based on user type and provided parameters
     */
    private fun buildDateSheetParams(params: DateSheetParams): Map<String, String> {
        val postParam = mutableMapOf<String, String>()
        
        // Required parameters
        postParam["campus_id"] = params.campusId!!
        postParam["exam_session_id"] = params.examSessionId!!
        
        // Add current session if available
        val currentSession = Paper.book().read<String>("current_session")
        if (!currentSession.isNullOrEmpty()) {
            postParam["session_id"] = currentSession
        }
        
        // Add user-specific parameters
        if (params.studentId != null) {
            postParam["student_id"] = params.studentId
        }
        
        if (params.studentClassId != null) {
            postParam["student_class_id"] = params.studentClassId
        }
        
        if (params.parentId != null) {
            postParam["parent_id"] = params.parentId
        } else {
            // Fallback to campus_id if parent_id not provided
            postParam["parent_id"] = params.campusId
        }
        
        return postParam
    }
    
    /**
     * Create DateSheetParams from Paper database (for current user)
     */
    fun createParamsFromPaper(): DateSheetParams {
        return DateSheetParams(
            staffId = Paper.book().read("employee_id"),
            campusId = Paper.book().read("campus_id"),
            examSessionId = null, // Will be set when user selects exam session
            studentId = Paper.book().read("student_id"),
            parentId = Paper.book().read("parent_id"),
            studentClassId = Paper.book().read("student_class_id")
        )
    }
    
    /**
     * Create DateSheetParams for a specific staff member
     */
    fun createStaffParams(staffId: String, campusId: String): DateSheetParams {
        return DateSheetParams(
            staffId = staffId,
            campusId = campusId,
            examSessionId = null
        )
    }
    
    /**
     * Create DateSheetParams for a specific student
     */
    fun createStudentParams(studentId: String, campusId: String, parentId: String? = null): DateSheetParams {
        return DateSheetParams(
            studentId = studentId,
            campusId = campusId,
            parentId = parentId ?: campusId,
            examSessionId = null
        )
    }
}
