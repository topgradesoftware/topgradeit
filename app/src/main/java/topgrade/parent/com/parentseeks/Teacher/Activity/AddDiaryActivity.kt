@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Teacher.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.ImageView
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService
import topgrade.parent.com.parentseeks.Parent.Model.Diary
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListModel
import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant
import java.util.Calendar
import java.util.HashMap

class AddDiaryActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var backIcon: ImageView
    private lateinit var btnPickDate: MaterialButton
    private lateinit var spinnerClass: Spinner
    private lateinit var spinnerSubject: Spinner
    private lateinit var spinnerSection: Spinner
    private lateinit var spinnerStudent: Spinner
    private lateinit var etDescription: EditText
    private lateinit var btnUploadPicture: MaterialButton
    private lateinit var btnAddDiary: MaterialButton

    // Variables for selected data
    private var selectedDate: String = ""
    private var selectedClass: String = ""
    private var selectedSubject: String = ""
    private var selectedSection: String = ""
    private var selectedStudent: String = ""
    private var selectedImageUri: String? = null

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Apply anti-flickering flags for fullscreen experience
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this)
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white)
        
        setContentView(R.layout.activity_add_diary) // Set the layout file
        
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

        // Initialize UI elements by finding their IDs from the layout
        backIcon = findViewById(R.id.back_icon)
        btnPickDate = findViewById(R.id.btnPickDate)
        spinnerClass = findViewById(R.id.spinnerClass)
        spinnerSubject = findViewById(R.id.spinnerSubject)
        spinnerSection = findViewById(R.id.spinnerSection)
        spinnerStudent = findViewById(R.id.spinnerStudent)
        etDescription = findViewById(R.id.etDescription)
        btnUploadPicture = findViewById(R.id.btnUploadPicture)
        btnAddDiary = findViewById(R.id.btnAddDiary)

        // --- ic_arrow_back Setup ---
        backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back button click
        }

        // --- Load Spinner Data ---
        loadClassList()
        loadSubjectList()
        loadSectionList()
        loadStudentList()

        // --- Spinner Selection Listeners ---
        setupSpinnerListeners()

        // --- Button Click Listeners ---

        // Pick Date Button
        btnPickDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Upload Picture Button
        btnUploadPicture.setOnClickListener {
            openGallery()
        }

        // Add Diary Button
        btnAddDiary.setOnClickListener {
            sendDiary()
        }
    }

    /**
     * Shows a DatePickerDialog to allow the user to pick a date.
     */
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
                // You can update a TextView with the selected date here if you add one to the layout
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    /**
     * Sends the diary to the server
     */
    private fun sendDiary() {
        // Validate inputs
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedClass == "Select Class" || selectedClass.isEmpty()) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSubject == "Select Subject" || selectedSubject.isEmpty()) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSection == "Select Section" || selectedSection.isEmpty()) {
            Toast.makeText(this, "Please select a section", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStudent == "Select Student" || selectedStudent.isEmpty()) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
            return
        }

        val description = etDescription.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter diary description", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button and show loading
        btnAddDiary.isEnabled = false
        btnAddDiary.text = "Sending..."

        // Prepare request parameters
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        postParam["date"] = selectedDate
        postParam["class"] = selectedClass
        postParam["subject"] = selectedSubject
        postParam["section"] = selectedSection
        postParam["student"] = selectedStudent
        postParam["description"] = description
        selectedImageUri?.let { uri ->
            postParam["image"] = uri
        }

        val body = JSONObject(postParam as Map<*, *>).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        // Send diary via API
        Constant.mApiService.send_diary(body).enqueue(object : Callback<Diary> {
            override fun onResponse(call: Call<Diary>, response: Response<Diary>) {
                btnAddDiary.isEnabled = true
                btnAddDiary.text = "Add Diary"

                if (response.isSuccessful && response.body() != null) {
                    val diary = response.body()
                    if (diary?.status?.status?.code == "1000") {
                        Toast.makeText(this@AddDiaryActivity, "Diary sent successfully!", Toast.LENGTH_LONG).show()
                        // Clear the form
                        etDescription.text.clear()
                        selectedDate = ""
                        selectedClass = ""
                        selectedSubject = ""
                        selectedSection = ""
                        selectedStudent = ""
                        selectedImageUri = null
                        btnUploadPicture.text = "Upload Picture"
                        btnUploadPicture.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
                        // Optionally finish the activity
                        finish()
                    } else {
                        val errorMessage = diary?.status?.status?.message ?: "Unknown error occurred"
                        Toast.makeText(this@AddDiaryActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@AddDiaryActivity, "Failed to send diary", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Diary>, t: Throwable) {
                btnAddDiary.isEnabled = true
                btnAddDiary.text = "Add Diary"
                Toast.makeText(this@AddDiaryActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Load class list from API
     */
    private fun loadClassList() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        
        val body = JSONObject(postParam as Map<*, *>).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        Constant.mApiService.load_profile(body).enqueue(object : Callback<TeachModel> {
            override fun onResponse(call: Call<TeachModel>, response: Response<TeachModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val teachModel = response.body()!!
                    if (teachModel.status?.code == "1000") {
                        val classList = mutableListOf("Select Class")
                        val uniqueClasses = mutableSetOf<String>()
                        
                        teachModel.teach?.let { teachList ->
                            for (teach in teachList) {
                                teach.className?.let { className ->
                                    if (!uniqueClasses.contains(className)) {
                                        uniqueClasses.add(className)
                                        classList.add(className)
                                    }
                                }
                            }
                        }
                        
                        val adapter = ArrayAdapter(this@AddDiaryActivity, android.R.layout.simple_spinner_item, classList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerClass.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<TeachModel>, t: Throwable) {
                Toast.makeText(this@AddDiaryActivity, "Failed to load classes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Load subject list from API
     */
    private fun loadSubjectList() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        
        val body = JSONObject(postParam as Map<*, *>).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        Constant.mApiService.load_profile(body).enqueue(object : Callback<TeachModel> {
            override fun onResponse(call: Call<TeachModel>, response: Response<TeachModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val teachModel = response.body()!!
                    if (teachModel.status?.code == "1000") {
                        val subjectList = mutableListOf("Select Subject")
                        val uniqueSubjects = mutableSetOf<String>()
                        
                        teachModel.teach?.let { teachList ->
                            for (teach in teachList) {
                                teach.subjectName?.let { subjectName ->
                                    if (!uniqueSubjects.contains(subjectName)) {
                                        uniqueSubjects.add(subjectName)
                                        subjectList.add(subjectName)
                                    }
                                }
                            }
                        }
                        
                        val adapter = ArrayAdapter(this@AddDiaryActivity, android.R.layout.simple_spinner_item, subjectList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerSubject.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<TeachModel>, t: Throwable) {
                Toast.makeText(this@AddDiaryActivity, "Failed to load subjects", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Load section list from API
     */
    private fun loadSectionList() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        
        val body = JSONObject(postParam as Map<*, *>).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        Constant.mApiService.load_profile(body).enqueue(object : Callback<TeachModel> {
            override fun onResponse(call: Call<TeachModel>, response: Response<TeachModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val teachModel = response.body()!!
                    if (teachModel.status?.code == "1000") {
                        val sectionList = mutableListOf("Select Section")
                        val uniqueSections = mutableSetOf<String>()
                        
                        teachModel.teach?.let { teachList ->
                            for (teach in teachList) {
                                teach.sectionName?.let { sectionName ->
                                    if (!uniqueSections.contains(sectionName)) {
                                        uniqueSections.add(sectionName)
                                        sectionList.add(sectionName)
                                    }
                                }
                            }
                        }
                        
                        val adapter = ArrayAdapter(this@AddDiaryActivity, android.R.layout.simple_spinner_item, sectionList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerSection.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<TeachModel>, t: Throwable) {
                Toast.makeText(this@AddDiaryActivity, "Failed to load sections", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Load student list from API
     */
    private fun loadStudentList() {
        val postParam = HashMap<String, String>()
        postParam["staff_id"] = Constant.staff_id
        postParam["parent_id"] = Constant.campus_id
        
        val body = JSONObject(postParam as Map<*, *>).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        Constant.mApiService.load_students(body).enqueue(object : Callback<StudentListModel> {
            override fun onResponse(call: Call<StudentListModel>, response: Response<StudentListModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val studentListModel = response.body()!!
                    if (studentListModel.status?.code == "1000") {
                        val studentList = mutableListOf("Select Student")
                        
                        studentListModel.students?.let { students ->
                            for (student in students) {
                                student.fullName?.let { fullName ->
                                    studentList.add(fullName)
                                }
                            }
                        }
                        
                        val adapter = ArrayAdapter(this@AddDiaryActivity, android.R.layout.simple_spinner_item, studentList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerStudent.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<StudentListModel>, t: Throwable) {
                Toast.makeText(this@AddDiaryActivity, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Setup spinner selection listeners
     */
    private fun setupSpinnerListeners() {
        // Class Spinner
        spinnerClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedClass = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@AddDiaryActivity, "Selected Class: $selectedClass", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Subject Spinner
        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubject = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@AddDiaryActivity, "Selected Subject: $selectedSubject", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Section Spinner
        spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSection = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@AddDiaryActivity, "Selected Section: $selectedSection", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Student Spinner
        spinnerStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStudent = parent?.getItemAtPosition(position).toString()
                Toast.makeText(this@AddDiaryActivity, "Selected Student: $selectedStudent", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Opens gallery to select an image
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri.toString()
                btnUploadPicture.text = "Image Selected ✓"
                btnUploadPicture.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show()
            }
        }
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
                        android.util.Log.e("AddDiaryActivity", "Error in window insets listener: ${e.message}")
                        androidx.core.view.WindowInsetsCompat.CONSUMED
                    }
                }
            } else {
                android.util.Log.e("AddDiaryActivity", "rootLayout is null - cannot setup window insets")
            }
        } catch (e: Exception) {
            android.util.Log.e("AddDiaryActivity", "Error setting up window insets: ${e.message}", e)
        }
    }
} 