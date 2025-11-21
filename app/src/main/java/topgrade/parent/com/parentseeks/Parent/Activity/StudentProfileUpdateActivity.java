package topgrade.parent.com.parentseeks.Parent.Activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
// import retrofit2.Response; // Removed to avoid conflict with Volley Response
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Model.StatusModel;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Base64Converter;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.HelperAlertDialogMessage;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.R;

public class StudentProfileUpdateActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_GALLERY = 8000;
    public static final int REQUEST_CODE_CAMERA = 7000;
    private EditText etName, etFatherName, etEmail, etPhone, etLandline, etAddress;
    private CircleImageView profileCircleImageView;
    private RelativeLayout imageAction;
    private ImageView  ivEditButton;
    private Bitmap bitmapImg;
    private TextView tvDateOfBirth, tvUploadImage;
    private LinearLayout linearDOD;
    private RadioGroup genderRadioGroup;
    private Button btnUpdateProfile;
    private RadioButton genderRadioButton;
    private SharedStudent student;
    private ProgressBar progressBar;
    private String studentId, parentId, campusId, fullName, gender, pictureString, datePatternForApi;
    private RadioButton radioButtonMale, radioButtonFemale;
    Calendar calendar;
    private Context context;

    // Activity Result Launchers for modern API
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    private void applyTheme() {
        try {
            // Get user type from Paper DB
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("StudentProfileUpdate", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                    getWindow().setStatusBarColor(tealColor);
                    getWindow().setNavigationBarColor(tealColor);
                }
                
                // Change header wave to teal for student theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Change edit image button to teal for student theme
                ImageView editImageButton = findViewById(R.id.iv_edit_image_student);
                if (editImageButton != null) {
                    editImageButton.setBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                // Change update button to teal for student theme
                Button updateButton = findViewById(R.id.btn_student_profile_update);
                if (updateButton != null) {
                    updateButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.student_primary));
                }
                
                // Force light status bar icons for better visibility on teal background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(
                        getWindow().getDecorView().getSystemUiVisibility() &
                        ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    );
                }
                
                // Force dark navigation bar icons (prevent light appearance)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("StudentProfileUpdate", "Applied STUDENT theme (teal)");
            } else {
                // For parent theme, system bars are already configured in onCreate()
                // Only update UI elements here (not system bars)
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_dark_brown);
                }
                
                ImageView editImageButton = findViewById(R.id.iv_edit_image_student);
                if (editImageButton != null) {
                    editImageButton.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_brown));
                }
                
                Button updateButton = findViewById(R.id.btn_student_profile_update);
                if (updateButton != null) {
                    updateButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_brown));
                }
                
                Log.d("StudentProfileUpdate", "Applied PARENT theme (dark brown)");
            }
            
        } catch (Exception e) {
            Log.e("StudentProfileUpdate", "Error applying theme, using parent theme as fallback", e);
            // Fallback to parent theme
            ThemeHelper.applyParentTheme(this);
        }
    }
    
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        android.view.View footerContainer = findViewById(R.id.footer_container);
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
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("StudentProfileUpdate", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e("StudentProfileUpdate", "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e("StudentProfileUpdate", "Error setting up window insets: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Reapply theme in case it was overridden (only for student theme)
        String userType = Paper.book().read(Constants.User_Type, "");
        if ("STUDENT".equals(userType)) {
            getWindow().getDecorView().postDelayed(() -> {
                applyTheme();
            }, 50); // Small delay to ensure it overrides system settings
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_student_edit_profile);

        // Get user type
        String userType = Paper.book().read(Constants.User_Type, "");
        
        if ("STUDENT".equals(userType)) {
            // Apply student theme using StudentThemeHelper
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.applyStudentTheme(this, 100);
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderIconVisibility(this, false);
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setMoreOptionsVisibility(this, false);
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setFooterVisibility(this, true);
            topgrade.parent.com.parentseeks.Parent.Utils.StudentThemeHelper.setHeaderTitle(this, "Edit Profile");
        } else {
            // Configure system bars for parent theme
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Set transparent status bar to allow header wave to cover it
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_brown));
                
                // For Android M and above, ensure white status bar icons on dark background
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int flags = getWindow().getDecorView().getSystemUiVisibility();
                    // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
                    flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
            
            // Configure status bar and navigation bar icons for Android R and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(
                        0, // No light icons for status bar (white icons on dark background)
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | 
                        android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                    );
                }
            }
            
            // Setup window insets for footer positioning
            setupWindowInsets();
            
            // Apply theme-specific UI elements (not system bars - already configured above)
            applyTheme();
        }

        init();
        setData();
        listeners();

        // Initialize Activity Result Launchers
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    bitmapImg = ((Bitmap) result.getData().getExtras().get("data"));
                    pictureString = new Base64Converter().getStringImage(bitmapImg);
                    profileCircleImageView.setImageBitmap(bitmapImg);
                    tvUploadImage.setVisibility(View.VISIBLE);
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri image_path = result.getData().getData();
                    try {
                        bitmapImg = MediaStore.Images.Media.getBitmap(getContentResolver(), image_path);
                        pictureString = new Base64Converter().getStringImage(bitmapImg);
                        profileCircleImageView.setImageBitmap(bitmapImg);
                        tvUploadImage.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    open_camera_intent();
                } else {
                    HelperAlertDialogMessage.showAlertMessage(context, "Need Camera permission");
                }
            }
        );
    }

    private void setData() {
        student = Paper.book().read("current_child_model");
        
        // Check if student data is available
        if (student == null) {
            Log.e("StudentProfileUpdate", "Student data is null, trying to get from Paper DB");
            
            // Try to get student ID from Paper DB directly
            // For students, try different possible keys
            studentId = Paper.book().read("parent_id", "");
            if (studentId == null || studentId.isEmpty()) {
                studentId = Paper.book().read("student_id", "");
            }
            if (studentId == null || studentId.isEmpty()) {
                studentId = Paper.book().read("user_id", "");
            }
            
            parentId = Paper.book().read("parent_id", "");
            campusId = Paper.book().read("campus_id", "");
            
            Log.d("StudentProfileUpdate", "Retrieved from Paper DB - studentId: " + studentId + ", parentId: " + parentId + ", campusId: " + campusId);
            
            if (studentId == null || studentId.isEmpty()) {
                Log.e("StudentProfileUpdate", "No student ID found in Paper DB");
                Toast.makeText(this, "Student data not found. Please login again.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            // Set default values
            etName.setText("Student Profile");
            datePatternForApi = "";
        } else {
            studentId = student.getUniqueId();
            parentId = Paper.book().read("parent_id", "");
            campusId = Paper.book().read("campus_id", "");
            etName.setText(student.getFullName());
            datePatternForApi = student.getDob();
        }
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd/MM/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        
        // Only parse date if student is not null and has a valid DOB
        if (student != null && datePatternForApi != null && !datePatternForApi.isEmpty()) {
            try {
                Date date = inputFormat.parse(datePatternForApi);
                String strDate = outputFormat.format(date);
                tvDateOfBirth.setText(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
                tvDateOfBirth.setText(datePatternForApi);
            }
        } else {
            tvDateOfBirth.setText("Not available");
        }
//        etFatherName.setText(student.getParentName());
//        etEmail.setText(student.getEmail());
//        etLandline.setText(student.getLandline());
//        etPhone.setText(student.getPhone());
        gender = student != null ? student.getGender() : "";

        if(gender != null && gender.equals("Male")){
            radioButtonMale.setChecked(true);
        }else{
            radioButtonFemale.setChecked(true);
        }

        if (student != null && student.getPicture() != null && !student.getPicture().isEmpty()) {
            Glide.with(this)
                    .load(API.image_base_url + student.getPicture())
                    .placeholder(R.drawable.man_brown)
                    .error(R.drawable.man_brown)
                    .into(profileCircleImageView);
        } else {
            // Set default image if no picture available
            profileCircleImageView.setImageResource(R.drawable.man_brown);
        }
    }

    private void listeners() {
        // Back button click listener
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        }

        tvUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pictureString.isEmpty()) {
                    update_picture(pictureString);
                } else {
                    Toast.makeText(context, "Select Image", Toast.LENGTH_SHORT).show();
                }

            }
        });
        
        linearDOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDateOfBirth();
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileRequest();
            }
        });


        imageAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                PopupMenu popup = new PopupMenu(StudentProfileUpdateActivity.this, view);
                popup.getMenuInflater()
                        .inflate(R.menu.image_action_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.Edit_Image) {
                            open_bottom_sheet();

                        } else if (id == R.id.View_Image) {

                            startActivity(new Intent(StudentProfileUpdateActivity.this, ZoomImage.class)
                                    .putExtra("link", API.image_base_url + student.getPicture())
                                    .putExtra("name", student.getFullName())

                            );
                        }

                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });
    }

    private void updateProfileRequest() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        genderRadioButton = findViewById(selectedId);
        String selectedGender = genderRadioButton.getText().toString();
        Log.d("AppDebug", selectedGender);
        fullName = etName.getText().toString();
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("campus_id", campusId);
        postParam.put("student_id", studentId);
        postParam.put("full_name", fullName);
        postParam.put("dob", datePatternForApi);
        postParam.put("gender", selectedGender);
        progressBar.setVisibility(View.VISIBLE);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        BaseApiService mApiService = API.getAPIService();
        mApiService.updateStudentProfile(body).enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(Call<StatusModel> call, retrofit2.Response<StatusModel> response) {
                if (response.body() != null) {


                    if (response.body().status.getCode().equals("1000")) {
                        student.setFullName(fullName);
                        student.setDob(datePatternForApi);
                        Paper.book().write("current_child_model", student);
                        
                        List<SharedStudent> studentList;
                        try {
                            studentList = Paper.book().read("students");
                        } catch (Exception e) {
                            // If there's a serialization error, clear the corrupted data and start fresh
                            Paper.book().delete("students");
                            studentList = new ArrayList<>();
                        }
                        
                        int positionItem = Paper.book().read("item_position");
                        for (int i=0; i<studentList.size(); i++)
                        {
                            if (i == positionItem){
                                studentList.get(i).setFullName(fullName);
                                studentList.get(i).setDob(datePatternForApi);
                                studentList.get(i).setGender(selectedGender);
                            }
                        }
                        Paper.book().write("students", studentList);
                        Toast.makeText(context, response.body().status.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().status.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<StatusModel> call, Throwable e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void pickDateOfBirth() {
        calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String inputPattern = "yyyy-MM-dd";

                SimpleDateFormat sdf = new SimpleDateFormat(inputPattern, Locale.getDefault());
                datePatternForApi = sdf.format(calendar.getTime());
                String outputPattern = "dd/MM/yyyy";
                SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
                try {
                    Date date = inputFormat.parse(sdf.format(calendar.getTime()));
                    String strDate = outputFormat.format(date);
                    tvDateOfBirth.setText(strDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//        Date newDate = calendar.getTime();
//        datePickerDialog.getDatePicker().setMinDate(newDate.getTime());

        datePickerDialog.show();
    }

    private void init() {
        context = StudentProfileUpdateActivity.this;
        etName = findViewById(R.id.et_student_name);
        profileCircleImageView = findViewById(R.id.circle_iv_student_profile_edit);
        ivEditButton = findViewById(R.id.iv_edit_image_student);
        tvUploadImage = findViewById(R.id.tv_upload_student_image);
        tvDateOfBirth = findViewById(R.id.tv_dob_student_edit);
        linearDOD = findViewById(R.id.linear_dob_student_edit);
//        etFatherName = findViewById(R.id.et_father_name);
//        etEmail = findViewById(R.id.et_email_student_edit);
//        etPhone = findViewById(R.id.et_phone_student_edit);
//        etLandline = findViewById(R.id.et_landline_student_edit);
//        etAddress = findViewById(R.id.et_address_student_edit);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        btnUpdateProfile = findViewById(R.id.btn_student_profile_update);
        progressBar = findViewById(R.id.progress_bar_update_student_profile);
        imageAction = findViewById(R.id.image_action_student_edit);
        radioButtonMale = findViewById(R.id.radio_male);
        radioButtonFemale = findViewById(R.id.radio_female);
        Paper.init(context);
    }


    private void open_bottom_sheet() {
        View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);
        dialog.show();
        LinearLayout gallery = view.findViewById(R.id.gallery);
        LinearLayout camera = view.findViewById(R.id.camera);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                opne_gallery();

            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // minSdk is 26, so M (API 23) check is unnecessary
if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            ||
                            ContextCompat.checkSelfPermission(context,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                    ) {
                        permissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    } else {
                        open_camera_intent();
                    }
            }
        });
        ImageView Cancel = view.findViewById(R.id.Cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



    private void open_camera_intent() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(camera_intent);
    }

    private void opne_gallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryLauncher.launch(intent);
    }


    private void update_picture(final String file) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.update_picture_student, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    JSONObject respone = new JSONObject(response);
                    JSONObject status = respone.getJSONObject("status");
                    if (status.getString("code").equals("1000")) {
                        String new_picture = respone.getString("data");
                        load_child();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        String message = status.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e1) {
                    e1.printStackTrace();
                    progressBar.setVisibility(View.GONE);

                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("parent_parent_id", parentId);
                postParam.put("parent_id", campusId);
                postParam.put("student_id", studentId);
                postParam.put("file", file);


                return new JSONObject(postParam).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header_parameter = new HashMap<String, String>();

                header_parameter.put("Content-Type", "application/json");

                return header_parameter;
            }


        };


        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }


    private void load_child() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.load_profile, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();

                    JSONObject respone = new JSONObject(response);

                    JSONObject status = respone.getJSONObject("status");

                    if (status.getString("code").equals("1000")) {
                        JSONArray students_array = respone.getJSONArray("students");
                        if (students_array.length() > 0) {
                            List<SharedStudent> studentList = Arrays.asList(gson.fromJson(students_array.toString(), SharedStudent[].class));
                            Paper.book().write("students", studentList);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        String message = status.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("parent_parent_id", parentId);
                postParam.put("parent_id", campusId);
                return new JSONObject(postParam).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header_parameter = new HashMap<String, String>();
                header_parameter.put("Content-Type", "application/json");
                return header_parameter;
            }


        };


        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }

}
