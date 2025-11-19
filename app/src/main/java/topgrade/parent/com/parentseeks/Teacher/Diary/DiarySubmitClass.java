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
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

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
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Teach;
import topgrade.parent.com.parentseeks.Teacher.Model.TeachModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class DiarySubmitClass extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DiarySubmitClass";

    // UI Components
    private SearchableSpinner classSpinner;
    private EditText etDiaryDescription;
    private TextView dateValue, diaryDateText;
    private com.google.android.material.button.MaterialButton btnSendDiary, btnUploadPicture;
    private com.google.android.material.button.MaterialButton btnViewImage, btnDeleteImage, btnEditImage;
    private Button selectionButton;
    private CheckBox cbSendToApp;
    private ProgressBar progressBar;
    private ImageView backIcon, ivDiaryPicture, dateLeftArrow, dateRightArrow;
    private androidx.cardview.widget.CardView cvImagePreview;

    // Data
    private List<String> classForSubjectList = new ArrayList<>();
    private List<Teach> teachList = new ArrayList<>();
    
    private String classId;
    private String selectedDate = "";
    private Uri imageUri = null;
    private String imagePath = "";
    
    private Context context;
    private boolean isFirstLaunch = true;
    
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
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_diary_class);
        
        // Configure status bar for navy blue background with white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0,
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        
        // Setup window insets to respect system bars
        setupWindowInsets();

        context = DiarySubmitClass.this;
        Paper.init(context);

        initializeViews();
        setupClickListeners();
        setDefaultDate();  // Set current date as default
        
        // Load classes directly like attendance does (no exam session needed)
        loadClasses();
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        );

                        // Add bottom margin to footer container to push it above navigation bar
                        LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                            }
                        }
                        
                        view.setPadding(0, 0, 0, 0);
                        return WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }

    private void initializeViews() {
        backIcon = findViewById(R.id.back_icon);
        progressBar = findViewById(R.id.progress_bar);

        classSpinner = findViewById(R.id.class_spinner);
        
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
        backIcon.setOnClickListener(v -> {
            ScrollView dataSection = findViewById(R.id.data_section);
            if (dataSection != null && dataSection.getVisibility() == View.VISIBLE) {
                showSelectionPage();
            } else {
                finish();
            }
        });

        // Center area opens date picker
        LinearLayout diaryDateLayout = findViewById(R.id.diary_date);
        if (diaryDateLayout != null) diaryDateLayout.setOnClickListener(v -> showDatePicker());
        
        // Arrow navigation
        if (dateLeftArrow != null) dateLeftArrow.setOnClickListener(v -> navigateToPreviousDate());
        if (dateRightArrow != null) dateRightArrow.setOnClickListener(v -> navigateToNextDate());
        btnUploadPicture.setOnClickListener(v -> showImagePickerOptions());
        
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

    private void showSelectionPage() {
        LinearLayout selectionPage = findViewById(R.id.selection_page);
        ScrollView dataSection = findViewById(R.id.data_section);

        if (selectionPage != null) selectionPage.setVisibility(View.VISIBLE);
        if (dataSection != null) dataSection.setVisibility(View.GONE);
    }

    private void showDataSection() {
        LinearLayout selectionPage = findViewById(R.id.selection_page);
        ScrollView dataSection = findViewById(R.id.data_section);

        if (selectionPage != null) selectionPage.setVisibility(View.GONE);
        if (dataSection != null) dataSection.setVisibility(View.VISIBLE);
        
        updateSelectionSummary();
    }

    // Exam session methods removed - using current_session instead

    private void loadClasses() {
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
                    List<Teach> teachList = response.body().getTeach();
                    
                    classForSubjectList.clear();
                    for (Teach teach : teachList) {
                        if (!classForSubjectList.contains(teach.getClassName())) {
                            classForSubjectList.add(teach.getClassName());
                        }
                    }

                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(context,
                            R.layout.simple_list_item_1, classForSubjectList);
                    classSpinner.setAdapter(classAdapter);

                    if (classForSubjectList.size() > 0) {
                        classSpinner.setSelection(0);
                    }

                    setupClassSpinnerListener(teachList);
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
    }

    private void setupClassSpinnerListener(List<Teach> teachList) {
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < teachList.size()) {
                    classId = teachList.get(position).getStudentClassId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
                showDataSection();
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
        return true;
    }

    private void sendDiary() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (classId == null || classId.isEmpty()) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDiaryDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter diary description", Toast.LENGTH_SHORT).show();
            return;
        }

        setProgressBarVisibility(View.VISIBLE);

        HashMap<String, String> params = new HashMap<>();
        params.put("staff_id", Constant.staff_id);
        params.put("campus_id", Constant.campus_id);
        params.put("session_id", Constant.current_session);  // Use current_session instead of exam session
        params.put("class_id", classId);
        params.put("date", selectedDate);
        params.put("description", description);
        params.put("role", "class_incharge");
        params.put("send_to_app", String.valueOf(cbSendToApp != null && cbSendToApp.isChecked()));
        params.put("image_path", imagePath);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                new Gson().toJson(params));

        Constant.mApiService.sendDiary(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setProgressBarVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(DiarySubmitClass.this, "Diary sent successfully to entire class", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DiarySubmitClass.this, "Failed to send diary", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setProgressBarVisibility(View.GONE);
                Log.e(TAG, "Error sending diary", t);
                Toast.makeText(DiarySubmitClass.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProgressBarVisibility(int visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility);
        }
    }

    private void updateSelectionSummary() {
        try {
            TextView classValue = findViewById(R.id.class_value);

            if (classValue != null) {
                classValue.setText(getSelectedClassName());
            }

            if (dateValue != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                dateValue.setText(currentDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating selection summary", e);
        }
    }

    private String getSelectedClassName() {
        try {
            if (classSpinner != null && classSpinner.getSelectedItem() != null) {
                return classSpinner.getSelectedItem().toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting class name", e);
        }
        return "Class";
    }

    @Override
    public void onBackPressed() {
        ScrollView dataSection = findViewById(R.id.data_section);
        if (dataSection != null && dataSection.getVisibility() == View.VISIBLE) {
            showSelectionPage();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstLaunch) {
            resetAllSelections();
            loadClasses();
        } else {
            isFirstLaunch = false;
        }
    }

    private void resetAllSelections() {
        classForSubjectList.clear();
        teachList.clear();

        classId = null;
        selectedDate = "";

        if (etDiaryDescription != null) etDiaryDescription.setText("");
        setDefaultDate();
    }
}

