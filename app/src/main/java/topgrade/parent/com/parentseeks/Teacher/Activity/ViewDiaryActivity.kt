@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Teacher.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import components.searchablespinnerlibrary.SearchableSpinner
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.Parent.Adaptor.DiaryListAdapter
import topgrade.parent.com.parentseeks.Parent.Adaptor.SubjectListAdapter
import topgrade.parent.com.parentseeks.Parent.Model.Diary
import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import java.util.*

class ViewDiaryActivity : AppCompatActivity() {

    private lateinit var context: Context
    private lateinit var selectDate: TextView
    private lateinit var selectClassSpinner: SearchableSpinner
    private lateinit var diaryListAdapter: DiaryListAdapter
    private lateinit var subjectListAdapter: SubjectListAdapter
    private lateinit var childAdapter: ArrayAdapter<String>
    private lateinit var subjectList: MutableList<DiaryEntry>
    private lateinit var subjectEntryList: MutableList<DiaryEntry>
    private lateinit var mainRcv: RecyclerView
    private lateinit var subjectsRcv: RecyclerView
    private lateinit var studentNameList: MutableList<String>
    private lateinit var studentList: MutableList<Any>
    private var selectedChildId: String = ""
    private lateinit var progressBar: ProgressBar
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Apply anti-flickering flags for fullscreen experience
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this)
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white)
        
        setContentView(R.layout.activity_staff_view_diary)
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.navy_blue)
            
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

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets()

        context = this
        subjectEntryList = mutableListOf()
        subjectList = mutableListOf()
        subjectListAdapter = SubjectListAdapter(subjectEntryList, context)
        diaryListAdapter = DiaryListAdapter(subjectList, context)

        mainRcv = findViewById(R.id.main_rcv)
        subjectsRcv = findViewById(R.id.subjects_rcv)

        // Set up the RecyclerView with the adapter
        subjectsRcv.layoutManager = LinearLayoutManager(this)
        subjectsRcv.adapter = subjectListAdapter

        // Set up the RecyclerView with the adapter
        mainRcv.layoutManager = LinearLayoutManager(this)
        mainRcv.adapter = diaryListAdapter

        selectClassSpinner = findViewById(R.id.select_child_spinner)
        progressBar = findViewById(R.id.progress_bar)

        initialization()
        loadStudents()
    }

    private fun initialization() {
        context = this
        subjectList = mutableListOf()
        subjectEntryList = mutableListOf()
        studentNameList = mutableListOf()
        studentList = mutableListOf()
    }

    private fun reloadDiaryData() {
        if (selectedChildId.isEmpty()) {
            Toast.makeText(context, "Please select a student first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Load diary data for the selected student
        loadDiaryMain()
    }

    private fun loadStudents() {
        // For now, using sample data. You can replace this with actual API call
        studentNameList.add("All Students")
        studentNameList.add("Student 1")
        studentNameList.add("Student 2")
        studentNameList.add("Student 3")

        childAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, studentNameList)
        selectClassSpinner.adapter = childAdapter

        selectClassSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedChildId = if (position > 0) "student_$position" else "all"
                reloadDiaryData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadDiaryMain() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        postParam["employee_id"] = selectedChildId
        postParam["date"] = selectedDate
        postParam["date2"] = selectedDate

        progressBar.visibility = View.VISIBLE
        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            JSONObject(postParam as Map<*, *>).toString()
        )

        Constant.mApiService.load_diary(body).enqueue(object : Callback<Diary> {
            override fun onResponse(call: Call<Diary>, response: Response<Diary>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val diary = response.body()
                    if (diary != null && diary.status != null) {
                        if (diary.status.status.code == "1000") {
                            // Data is available and has status code 1000
                            val subjectData = diary.title["0"] // Get the subject data from "0" key
                            if (subjectData != null) {
                                // Convert the DiaryEntry to a HashMap
                                val subjectDataMap = HashMap<String, DiaryEntry>()
                                subjectDataMap["0"] = subjectData

                                // Convert the subjectData HashMap to a List of DiaryEntry
                                subjectList = ArrayList(subjectDataMap.values)
                                mainRcv.adapter = DiaryListAdapter(subjectList, this@ViewDiaryActivity)
                            } else {
                                // No subject data found for the selected child
                                Toast.makeText(context, "No diary data found for the selected student.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Data is not available or has a status code other than 1000
                            val msg = diary.status.status.message ?: "No data available"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Response is not successful, show "no_records_text"
                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Diary>, t: Throwable) {
                t.printStackTrace()
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSubjectsDiary() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        postParam["employee_id"] = selectedChildId
        postParam["date"] = selectedDate
        postParam["date2"] = selectedDate

        progressBar.visibility = View.VISIBLE
        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            JSONObject(postParam as Map<*, *>).toString()
        )

        Constant.mApiService.load_diary(body).enqueue(object : Callback<Diary> {
            override fun onResponse(call: Call<Diary>, response: Response<Diary>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val diary = response.body()
                    if (diary != null && diary.status != null) {
                        if (diary.status.status.code == "1000") {
                            // Data is available and has status code 1000
                            val subjectData = diary.title["0"] // Get the subject data from "0" key
                            if (subjectData != null) {
                                // Convert the DiaryEntry to a HashMap
                                val subjectDataMap = HashMap<String, DiaryEntry>()
                                subjectDataMap["0"] = subjectData

                                // Convert the subjectData HashMap to a List of DiaryEntry
                                subjectEntryList = ArrayList(subjectDataMap.values)
                                subjectsRcv.adapter = SubjectListAdapter(subjectEntryList, this@ViewDiaryActivity)
                            } else {
                                // No subject data found for the selected child
                                Toast.makeText(context, "No diary data found for the selected student.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Data is not available or has a status code other than 1000
                            val msg = diary.status.status.message ?: "No data available"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Response is not successful, show "no_records_text"
                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Diary>, t: Throwable) {
                t.printStackTrace()
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadDiaryMain()
        loadSubjectsDiary()
    }

    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private fun setupWindowInsets() {
        try {
            val rootLayout = findViewById<android.view.View>(android.R.id.content)

            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
                    try {
                        val systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        )

                        // Add bottom margin to root layout to push content above navigation bar
                        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root_layout)
                        if (rootLayout != null) {
                            // Set bottom margin to navigation bar height to ensure content is visible
                            val bottomMargin = if (systemInsets.bottom > 0) systemInsets.bottom else 0
                            val params = rootLayout.layoutParams as android.view.ViewGroup.MarginLayoutParams
                            params.bottomMargin = bottomMargin + 16 // 16dp extra padding
                            rootLayout.layoutParams = params
                        }

                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0)

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        androidx.core.view.WindowInsetsCompat.CONSUMED
                    } catch (e: Exception) {
                        android.util.Log.e("ViewDiaryActivity", "Error in window insets listener: ${e.message}")
                        androidx.core.view.WindowInsetsCompat.CONSUMED
                    }
                }
            } else {
                android.util.Log.e("ViewDiaryActivity", "rootLayout is null - cannot setup window insets")
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewDiaryActivity", "Error setting up window insets: ${e.message}", e)
        }
    }
} 