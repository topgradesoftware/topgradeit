package topgrade.parent.com.parentseeks.Teacher.Diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import components.searchablespinnerlibrary.SearchableSpinner;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class DiarySubmitSection extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DiarySubmitSection";

    // UI Components
    private SearchableSpinner classSpinner, sectionSpinner;
    private EditText etDiaryDescription;
    private TextView dateValue, diaryDateText;
    private com.google.android.material.button.MaterialButton btnSendDiary, btnUploadPicture, btnSelectSections;
    private com.google.android.material.button.MaterialButton btnViewImage, btnDeleteImage, btnEditImage;
    private Button selectionButton;
    private CheckBox cbSendToApp;
    private ProgressBar progressBar;
    private ImageView backIcon, ivDiaryPicture, dateLeftArrow, dateRightArrow;
    private androidx.cardview.widget.CardView cvImagePreview;

    // Data
    private List<String> classForSubjectList = new ArrayList<>();
    private List<Teach> teachList = new ArrayList<>();
    private List<String> sectionList = new ArrayList<>();
    private List<String> selectedSectionIds = new ArrayList<>();
    private List<String> selectedSectionNames = new ArrayList<>();
    
    private String classId;
    private String selectedDate = "";
    private Uri imageUri = null;
    private String imagePath = "";
    
    private Context context;
    private boolean isFirstLaunch = true;
    private boolean sendToAllSections = false;
    
    // Request codes
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    
    // Date picker
    private final Calendar myCalendar = Calendar.getInstance();
    private final String displayDateFormat = "dd MMMM,yyyy";  // Date format
    private final String dayNameFormat = "EEE";  // Short day name format (Mon, Tue, etc)
    private final String apiDateFormat = "yyyy-MM-dd";  // For API
    
    private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_diary_section);

        context = DiarySubmitSection.this;
        Paper.init(context);

        initializeViews();
        setupClickListeners();
        setDefaultDate();  // Set current date as default
        
        // Load classes directly like attendance does (no exam session needed)
        loadClasses();
    }

    private void initializeViews() {
        backIcon = findViewById(R.id.back_icon);
        progressBar = findViewById(R.id.progress_bar);

        classSpinner = findViewById(R.id.class_spinner);
        sectionSpinner = findViewById(R.id.section_spinner);
        btnSelectSections = findViewById(R.id.btn_select_sections);
        
        etDiaryDescription = findViewById(R.id.et_diary_description);
        diaryDateText = findViewById(R.id.diary_date_text);
        dateLeftArrow = findViewById(R.id.date_left_arrow);
        dateRightArrow = findViewById(R.id.date_right_arrow);
        dateValue = findViewById(R.id.date_value);
        btnUploadPicture = findViewById(R.id.btn_upload_picture);
        
        // Image preview components
        cvImagePreview = findViewById(R.id.cv_image_preview);
        ivDiaryPicture = findViewById(R.id.iv_diary_picture);
        btnViewImage = findViewById(R.id.btn_view_image);
        btnDeleteImage = findViewById(R.id.btn_delete_image);
        btnEditImage = findViewById(R.id.btn_edit_image);
        
        cbSendToApp = findViewById(R.id.cb_send_to_app);
        
        selectionButton = findViewById(R.id.selection_button);
        btnSendDiary = findViewById(R.id.btn_send_diary);
        
        if (classSpinner != null) classSpinner.setTitle("Select Class");
        if (sectionSpinner != null) sectionSpinner.setTitle("Select Section");
    }
    
    private void setDefaultDate() {
        SimpleDateFormat apiFormat = new SimpleDateFormat(apiDateFormat, Locale.getDefault());
        selectedDate = apiFormat.format(myCalendar.getTime());
        updateStyledDateDisplay();
    }
    
    private void updateDateDisplay() {
        SimpleDateFormat apiFormat = new SimpleDateFormat(apiDateFormat, Locale.getDefault());
        selectedDate = apiFormat.format(myCalendar.getTime());
        updateStyledDateDisplay();
    }
    
    private void updateStyledDateDisplay() {
        if (diaryDateText == null) return;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(displayDateFormat, Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat(dayNameFormat, Locale.getDefault());
        
        String formattedDate = dateFormat.format(myCalendar.getTime());
        String dayName = dayFormat.format(myCalendar.getTime());
        
        // Day first: "Wed • 30 October,2024"
        diaryDateText.setText(dayName + " • " + formattedDate);
    }

    private void setupClickListeners() {
        backIcon.setOnClickListener(v -> finish());

        // Center area opens date picker
        LinearLayout diaryDateLayout = findViewById(R.id.diary_date);
        if (diaryDateLayout != null) diaryDateLayout.setOnClickListener(v -> showDatePicker());
        
        // Arrow navigation
        if (dateLeftArrow != null) dateLeftArrow.setOnClickListener(v -> navigateToPreviousDate());
        if (dateRightArrow != null) dateRightArrow.setOnClickListener(v -> navigateToNextDate());
        btnUploadPicture.setOnClickListener(v -> showImagePickerOptions());
        
        // Multi-section selection button
        if (btnSelectSections != null) btnSelectSections.setOnClickListener(v -> showSectionSelectionDialog());
        
        if (btnViewImage != null) btnViewImage.setOnClickListener(v -> viewImage());
        if (btnDeleteImage != null) btnDeleteImage.setOnClickListener(v -> deleteImage());
        if (btnEditImage != null) btnEditImage.setOnClickListener(v -> editImage());
        
        selectionButton.setOnClickListener(this);
        btnSendDiary.setOnClickListener(this);
    }
    
    private void navigateToPreviousDate() {
        myCalendar.add(Calendar.DAY_OF_MONTH, -1);
        updateDateDisplay();
    }
    
    private void navigateToNextDate() {
        myCalendar.add(Calendar.DAY_OF_MONTH, 1);
        updateDateDisplay();
    }
    
    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        String[] options = {"Camera", "Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermissionAndCapture();
            } else {
                openGallery();
            }
        });
        builder.show();
    }
    
    private void checkCameraPermissionAndCapture() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        } else {
            openCamera();
        }
    }
    
    private void openCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    imageUri = androidx.core.content.FileProvider.getUriForFile(this,
                            "topgrade.parent.com.parentseeks.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error opening camera", e);
                Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }
    
    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "DIARY_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imagePath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                displayImagePreview();
                Toast.makeText(this, "Picture captured successfully", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                imageUri = data.getData();
                try {
                    imagePath = getRealPathFromURI(imageUri);
                    displayImagePreview();
                    Toast.makeText(this, "Picture selected successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error getting image path", e);
                }
            } else if (requestCode == components.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                // Handle cropped image result
                components.cropper.CropImage.ActivityResult result = components.cropper.CropImage.getActivityResult(data);
                if (result != null) {
                    imageUri = result.getUri();
                    try {
                        imagePath = getRealPathFromURI(imageUri);
                        displayImagePreview();
                        Toast.makeText(this, "Image edited successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting cropped image path", e);
                    }
                }
            }
        }
    }
    
    private void displayImagePreview() {
        if (imageUri != null && ivDiaryPicture != null && cvImagePreview != null) {
            try {
                ivDiaryPicture.setImageURI(imageUri);
                cvImagePreview.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Error displaying image", e);
            }
        }
    }
    
    private void viewImage() {
        if (imageUri != null) {
            Intent intent = new Intent(this, topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage.class);
            intent.putExtra("image_url", imageUri.toString());
            intent.putExtra("name", "Diary");
            startActivity(intent);
        }
    }
    
    private void deleteImage() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete", (dialog, which) -> {
                imageUri = null;
                imagePath = "";
                if (cvImagePreview != null) cvImagePreview.setVisibility(View.GONE);
                if (ivDiaryPicture != null) ivDiaryPicture.setImageDrawable(null);
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void editImage() {
        if (imageUri != null) {
            // Use image cropper library
            try {
                components.cropper.CropImage.activity(imageUri)
                    .setAspectRatio(16, 9)
                    .start(this);
            } catch (Exception e) {
                Log.e(TAG, "Error opening image editor", e);
                Toast.makeText(this, "Image editor not available", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {android.provider.MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    // Load classes directly using current session (like attendance does)
    private void loadClasses() {
        try {
            Constant.loadFromPaper();

            if (Constant.campus_id == null || Constant.campus_id.isEmpty() ||
                Constant.staff_id == null || Constant.staff_id.isEmpty() ||
                Constant.current_session == null || Constant.current_session.isEmpty()) {
                Toast.makeText(context, "Session data not available. Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("staff_id", Constant.staff_id);
            postParam.put("parent_id", Constant.campus_id);
            postParam.put("session_id", Constant.current_session);

            setProgressBarVisibility(View.VISIBLE);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    (new JSONObject(postParam)).toString());

            Constant.mApiService.load_profile(body).enqueue(new Callback<TeachModel>() {
                @Override
                public void onResponse(Call<TeachModel> call, Response<TeachModel> response) {
                    setProgressBarVisibility(View.GONE);
                    
                    if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
                        teachList = response.body().getTeach();
                        
                        classForSubjectList.clear();
                        for (Teach teach : teachList) {
                            if (!classForSubjectList.contains(teach.getClassName())) {
                                classForSubjectList.add(teach.getClassName());
                            }
                        }

                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(context,
                                R.layout.simple_list_item_1, classForSubjectList);
                        classSpinner.setAdapter(classAdapter);

                        setupClassSpinnerListener();
                    } else {
                        Toast.makeText(context, "Error loading classes", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TeachModel> call, Throwable e) {
                    setProgressBarVisibility(View.GONE);
                    Log.e(TAG, "Error loading classes", e);
                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            setProgressBarVisibility(View.GONE);
            Log.e(TAG, "Exception loading classes", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClassSpinnerListener() {
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < classForSubjectList.size()) {
                    String selectedClassName = classForSubjectList.get(position);
                    
                    // Get sections for selected class
                    sectionList.clear();
                    selectedSectionIds.clear();
                    selectedSectionNames.clear();
                    
                    // Add "All Sections" option
                    sectionList.add("All Sections");
                    
                    for (Teach teach : teachList) {
                        if (teach.getClassName().equals(selectedClassName)) {
                            if (!sectionList.contains(teach.getSectionName())) {
                                sectionList.add(teach.getSectionName());
                                if (classId == null || classId.isEmpty()) {
                                    classId = teach.getStudentClassId();
                                }
                            }
                        }
                    }
                    
                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_list_item_1, sectionList);
                    sectionSpinner.setAdapter(sectionAdapter);
                    
                    setupSectionSpinnerListener();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void setupSectionSpinnerListener() {
        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "All Sections" selected
                    sendToAllSections = true;
                    selectedSectionIds.clear();
                    selectedSectionNames.clear();
                    
                    // Get all section IDs and names for this class
                    for (Teach teach : teachList) {
                        if (teach.getStudentClassId().equals(classId)) {
                            if (!selectedSectionIds.contains(teach.getSectionId())) {
                                selectedSectionIds.add(teach.getSectionId());
                                selectedSectionNames.add(teach.getSectionName());
                            }
                        }
                    }
                    updateSelectionButtonText();
                } else if (position > 0 && position < sectionList.size()) {
                    // Single section selected
                    sendToAllSections = false;
                    selectedSectionIds.clear();
                    selectedSectionNames.clear();
                    
                    String selectedSectionName = sectionList.get(position);
                    for (Teach teach : teachList) {
                        if (teach.getSectionName().equals(selectedSectionName)) {
                            selectedSectionIds.add(teach.getSectionId());
                            selectedSectionNames.add(teach.getSectionName());
                            break;
                        }
                    }
                    updateSelectionButtonText();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    
    private void updateSelectionButtonText() {
        if (btnSelectSections == null) return;
        
        if (sendToAllSections) {
            btnSelectSections.setText("All Sections");
        } else if (!selectedSectionNames.isEmpty()) {
            if (selectedSectionNames.size() > 5) {
                btnSelectSections.setText("All Sections");
            } else {
                String names = String.join(", ", selectedSectionNames);
                btnSelectSections.setText(names);
            }
        } else {
            btnSelectSections.setText("Select Multiple Sections");
        }
    }
    
    private void showSectionSelectionDialog() {
        if (sectionList.isEmpty() || sectionList.size() <= 1) {
            return; // No sections to select
        }
        
        // Create section options (excluding "All Sections" for checkbox list)
        String[] sectionNames = new String[sectionList.size() - 1];
        for (int i = 1; i < sectionList.size(); i++) {
            sectionNames[i - 1] = sectionList.get(i);
        }
        
        boolean[] checkedItems = new boolean[sectionNames.length];
        
        // Mark currently selected sections
        if (sendToAllSections) {
            for (int i = 0; i < checkedItems.length; i++) {
                checkedItems[i] = true;
            }
        } else {
            for (int i = 0; i < sectionNames.length; i++) {
                checkedItems[i] = selectedSectionNames.contains(sectionNames[i]);
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sections");
        
        // Add "Select All" option
        builder.setPositiveButton("Select All", (dialog, which) -> {
            sendToAllSections = true;
            selectedSectionIds.clear();
            selectedSectionNames.clear();
            
            for (Teach teach : teachList) {
                if (teach.getStudentClassId().equals(classId)) {
                    if (!selectedSectionIds.contains(teach.getSectionId())) {
                        selectedSectionIds.add(teach.getSectionId());
                        selectedSectionNames.add(teach.getSectionName());
                    }
                }
            }
            
            sectionSpinner.setSelection(0); // Select "All Sections"
            updateSelectionButtonText();
            Toast.makeText(context, "All sections selected", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.setNeutralButton("OK", (dialog, which) -> {
            // Update selection based on checked items
            selectedSectionIds.clear();
            selectedSectionNames.clear();
            sendToAllSections = false;
            
            int selectedCount = 0;
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedCount++;
                    String sectionName = sectionNames[i];
                    selectedSectionNames.add(sectionName);
                    
                    // Find section ID
                    for (Teach teach : teachList) {
                        if (teach.getSectionName().equals(sectionName) && teach.getStudentClassId().equals(classId)) {
                            if (!selectedSectionIds.contains(teach.getSectionId())) {
                                selectedSectionIds.add(teach.getSectionId());
                            }
                            break;
                        }
                    }
                }
            }
            
            if (selectedCount == sectionNames.length) {
                sendToAllSections = true;
                sectionSpinner.setSelection(0);
            }
            
            updateSelectionButtonText();
            
            if (selectedCount > 0) {
                Toast.makeText(context, selectedCount + " section(s) selected", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setMultiChoiceItems(sectionNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });
        
        builder.show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.selection_button) {
            if (validateSelection()) {
                // showDataSection();
            }
        } else if (id == R.id.btn_send_diary) {
            sendDiary();
        }
    }

    private boolean validateSelection() {
        if (classId == null || classId.isEmpty()) {
            Toast.makeText(context, "Please select class", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedSectionIds.isEmpty()) {
            Toast.makeText(context, "Please select at least one section", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendDiary() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateSelection()) {
            return;
        }

        String description = etDiaryDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter diary description", Toast.LENGTH_SHORT).show();
            return;
        }

        setProgressBarVisibility(View.VISIBLE);

        // Send diary to all selected sections
        String sectionCountText = sendToAllSections ? "all sections" : 
                                 selectedSectionIds.size() + " section(s)";
        
        // Send to each section individually
        sendDiaryToSections(0, description, sectionCountText);
    }
    
    private void sendDiaryToSections(int index, String description, String sectionCountText) {
        if (index >= selectedSectionIds.size()) {
            // All sections processed
            setProgressBarVisibility(View.GONE);
            Toast.makeText(this, "Diary sent successfully to " + sectionCountText, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        String sectionId = selectedSectionIds.get(index);
        
        HashMap<String, String> params = new HashMap<>();
        params.put("staff_id", Constant.staff_id);
        params.put("campus_id", Constant.campus_id);
        params.put("session_id", Constant.current_session);
        params.put("class_id", classId);
        params.put("section_id", sectionId);
        params.put("date", selectedDate);
        params.put("description", description);
        params.put("role", "section_incharge");
        params.put("send_to_app", String.valueOf(cbSendToApp != null && cbSendToApp.isChecked()));
        params.put("image_path", imagePath);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                new Gson().toJson(params));

        Constant.mApiService.sendDiary(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Move to next section
                    sendDiaryToSections(index + 1, description, sectionCountText);
                } else {
                    setProgressBarVisibility(View.GONE);
                    Toast.makeText(DiarySubmitSection.this, "Failed to send diary to " + 
                            selectedSectionNames.get(index), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setProgressBarVisibility(View.GONE);
                Log.e(TAG, "Error sending diary to section: " + selectedSectionNames.get(index), t);
                Toast.makeText(DiarySubmitSection.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProgressBarVisibility(int visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLaunch) {
            classForSubjectList.clear();
            sectionList.clear();
            teachList.clear();
            selectedSectionIds.clear();
            selectedSectionNames.clear();
            classId = null;
            selectedDate = "";
            sendToAllSections = false;
            if (etDiaryDescription != null) etDiaryDescription.setText("");
            loadClasses();
        } else {
            isFirstLaunch = false;
        }
    }
}
