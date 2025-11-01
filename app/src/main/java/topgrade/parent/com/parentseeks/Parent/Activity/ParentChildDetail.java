package topgrade.parent.com.parentseeks.Parent.Activity;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.content.ContextCompat;

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Base64Converter;
import topgrade.parent.com.parentseeks.Parent.Utils.HelperAlertDialogMessage;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage;
import topgrade.parent.com.parentseeks.Parent.Activity.StudentProfileUpdateActivity;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;

public class ParentChildDetail extends AppCompatActivity {
    
    private static final String TAG = "ChildDetail";
    
    // UI Components
    private CircleImageView ChildImage;
    private TextView studentName, BirthDate, ChildGender, ParentsMobileNumber, registration_number,
            shift, roll_number, section_name, form_submission_date, Advanced, tvFatherName;
    private RelativeLayout image_action;
    private ProgressBar progress_bar;
    private TextView save_changes;
    private Button btnEditProfile;
    
    // Data
    private SharedStudent student;
    private Context context;
    private String parent_id;
    private String campus_id;
    private Bitmap bitmap_img;
    private String picture_str = "";
    
    // Constants
    public static final int REQUEST_CODE_GALLERY = 8000;
    public static final int REQUEST_CODE_CAMERA = 7000;
    
    // Activity Result Launchers for modern API
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    
    // Date formatting
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_child_detail);
        
        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        setupWindowInsets();
        
        try {
            initializeComponents();
            setupHeader();
            initializeViews();
            applyTheme();
            setupClickListeners();
            initializeActivityResultLaunchers();
            loadStudentData();
            setupDateFormatters();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing child detail", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeComponents() {
        context = ParentChildDetail.this;
        Paper.init(context);
        student = Paper.book().read("current_child_model");
        
        if (student == null) {
            Log.e(TAG, "SharedStudent data not found in Paper DB");
            Toast.makeText(this, "SharedStudent data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        parent_id = Paper.book().read("parent_id", "");
        campus_id = Paper.book().read("campus_id", "");
        
        if (parent_id.isEmpty() || campus_id.isEmpty()) {
            Log.w(TAG, "Parent ID or Campus ID not found");
        }
    }

    private void setupHeader() {
        try {
            // Set up back button with new header structure
            ImageView backIcon = findViewById(R.id.back_icon);
            TextView headerTitle = findViewById(R.id.header_title);
            
            if (backIcon != null) {
                backIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            
            if (headerTitle != null && student != null) {
                String title = student.getFullName() + " Profile";
                headerTitle.setText(title);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up header", e);
        }
    }

    private void initializeViews() {
        try {
            // Initialize all UI components
            ChildImage = findViewById(R.id.ChildImage);
            image_action = findViewById(R.id.image_action);
            progress_bar = findViewById(R.id.progress_bar);
            tvFatherName = findViewById(R.id.tv_father_name);
            studentName = findViewById(R.id.tv_student_name);
            BirthDate = findViewById(R.id.BirthDate);
            ChildGender = findViewById(R.id.ChildGender);
            ParentsMobileNumber = findViewById(R.id.ParentsMobileNumber);
            registration_number = findViewById(R.id.registration_number);
            shift = findViewById(R.id.shift);
            roll_number = findViewById(R.id.roll_number);
            section_name = findViewById(R.id.section_name);
            form_submission_date = findViewById(R.id.form_submission_date);
            Advanced = findViewById(R.id.Advanced);
            save_changes = findViewById(R.id.save_changes);
            btnEditProfile = findViewById(R.id.btn_edit_profile);
            
            // Validate that all views were found
            if (!validateViews()) {
                Log.e(TAG, "One or more views not found!");
                Toast.makeText(this, "Error: Some views not found", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateViews() {
        return ChildImage != null && image_action != null && progress_bar != null &&
               tvFatherName != null && studentName != null && BirthDate != null &&
               ChildGender != null && ParentsMobileNumber != null && registration_number != null &&
               shift != null && roll_number != null && section_name != null &&
               form_submission_date != null && Advanced != null && save_changes != null &&
               btnEditProfile != null;
    }

    private void setupClickListeners() {
        try {
            // Image action click listener
            if (image_action != null) {
                image_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showImageActionPopup();
                    }
                });
            }
            
            // Save changes click listener
            if (save_changes != null) {
                save_changes.setVisibility(View.GONE);
                save_changes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleSaveChanges();
                    }
                });
            }
            
            // Edit profile button click listener
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startEditProfileActivity();
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
        }
    }

    private void initializeActivityResultLaunchers() {
        try {
            // Camera launcher
            cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleCameraResult(result.getData());
                    }
                }
            );

            // Gallery launcher
            galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleGalleryResult(result.getData());
                    }
                }
            );

            // Permission launcher
            permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    handlePermissionResult(permissions);
                }
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing activity result launchers", e);
        }
    }

    private void loadStudentData() {
        try {
            if (student != null) {
                // Set student information
                setStudentInfo();
                
                // Load student image
                loadStudentImage();
                
                // Set parent information
                if (tvFatherName != null) {
                    tvFatherName.setText(student.getParentName());
                }
                
                // Refresh student data from API to get correct roll number
                refreshStudentDataFromAPI();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading student data", e);
            Toast.makeText(this, "Error loading student information", Toast.LENGTH_SHORT).show();
        }
    }

    private void setStudentInfo() {
        try {
            // Debug logging for student data
            Log.d(TAG, "=== STUDENT DATA DEBUG ===");
            Log.d(TAG, "SharedStudent registration number: '" + student.getRegistrationNumber() + "'");
            Log.d(TAG, "SharedStudent registration number is null: " + (student.getRegistrationNumber() == null));
            Log.d(TAG, "SharedStudent registration number is empty: " + (student.getRegistrationNumber() != null && student.getRegistrationNumber().isEmpty()));
            Log.d(TAG, "SharedStudent roll number: '" + student.getRollNo() + "'");
            Log.d(TAG, "SharedStudent roll number is null: " + (student.getRollNo() == null));
            Log.d(TAG, "SharedStudent roll number is empty: " + (student.getRollNo() != null && student.getRollNo().isEmpty()));
            Log.d(TAG, "=== END STUDENT DATA DEBUG ===");
            
            if (studentName != null) studentName.setText(student.getFullName());
            if (BirthDate != null) BirthDate.setText(student.getDob());
            if (ChildGender != null) ChildGender.setText(student.getGender());
            if (ParentsMobileNumber != null) ParentsMobileNumber.setText(student.getParentPhone());
            if (registration_number != null) registration_number.setText(String.valueOf(student.getRegistrationNumber()));
            if (shift != null) shift.setText(student.getClassName());
            if (form_submission_date != null) form_submission_date.setText(student.getFormSubmissionDate());
            if (roll_number != null) {
                String rollNoValue = student.getRollNo();
                if (rollNoValue != null && !rollNoValue.isEmpty()) {
                    roll_number.setText(rollNoValue);
                } else {
                    roll_number.setText("N/A");
                    Log.w(TAG, "Roll number is null or empty, setting to N/A");
                }
            }
            if (section_name != null) section_name.setText(student.getSectionName());
            if (Advanced != null) Advanced.setText(String.valueOf(student.getAdvance()));
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting student info", e);
        }
    }

    private void loadStudentImage() {
        try {
            if (ChildImage != null && student.getPicture() != null && !student.getPicture().isEmpty()) {
                String imageUrl = API.image_base_url + student.getPicture();
                Log.d(TAG, "Loading student image from: " + imageUrl);
                
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.man_brown)
                        .error(R.drawable.man_brown)
                        .into(ChildImage);
            } else {
                Log.d(TAG, "Setting default student image");
                if (ChildImage != null) {
                    ChildImage.setImageResource(R.drawable.man_brown);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading student image", e);
            if (ChildImage != null) {
                ChildImage.setImageResource(R.drawable.man_brown);
            }
        }
    }

    private void setupDateFormatters() {
        try {
            inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up date formatters", e);
        }
    }

    private void applyTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal)
                ThemeHelper.applyStudentTheme(this);
                
                // Set system bar colors for student theme
                // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.student_primary));
                    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.student_primary));
                
                // Set header wave for student theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Set edit button color for student theme
                Button editButton = findViewById(R.id.btn_edit_profile);
                if (editButton != null) {
                    editButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.student_primary));
                }
                
                // Set profile image edit icon for student theme
                ImageView editIcon = findViewById(R.id.edit_image);
                if (editIcon != null) {
                    editIcon.setBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                // Change footer card background to student theme (teal)
                com.google.android.material.card.MaterialCardView footerCard = findViewById(R.id.footer_card);
                if (footerCard != null) {
                    footerCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
            } else {
                // Apply unified parent theme for child detail page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for child detail
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for child detail
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Child Details");
                
                // Set header wave for parent theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_dark_brown);
                }
                
                // Set edit button color for parent theme
                Button editButton = findViewById(R.id.btn_edit_profile);
                if (editButton != null) {
                    editButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.parent_primary));
                }
                
                // Set profile image edit icon for parent theme
                ImageView editIcon = findViewById(R.id.edit_image);
                if (editIcon != null) {
                    editIcon.setBackgroundColor(ContextCompat.getColor(this, R.color.parent_primary));
                }
                
                // Footer card already set to dark brown in XML - no action needed
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }

    private void showImageActionPopup() {
        try {
            PopupMenu popup = new PopupMenu(ParentChildDetail.this, image_action);
            popup.getMenuInflater().inflate(R.menu.image_action_menu, popup.getMenu());
            
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.Edit_Image) {
                        openBottomSheet();
                        return true;
                    } else if (id == R.id.View_Image) {
                        openImageZoom();
                        return true;
                    }
                    return false;
                }
            });
            
            popup.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing image action popup", e);
            Toast.makeText(this, "Error showing image options", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageZoom() {
        try {
            if (student.getPicture() != null && !student.getPicture().isEmpty()) {
                Intent intent = new Intent(ParentChildDetail.this, ZoomImage.class);
                intent.putExtra("link", API.image_base_url + student.getPicture());
                intent.putExtra("name", student.getFullName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No image available to view", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening image zoom", e);
            Toast.makeText(this, "Error opening image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSaveChanges() {
        try {
            if (!picture_str.isEmpty()) {
                if (parent_id != null && !parent_id.isEmpty() && 
                    campus_id != null && !campus_id.isEmpty()) {
                    updatePicture(parent_id, campus_id, picture_str, student.getUniqueId());
                } else {
                    Toast.makeText(context, "Parent or campus information not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling save changes", e);
            Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show();
        }
    }

    private void startEditProfileActivity() {
        try {
            Intent intent = new Intent(ParentChildDetail.this, StudentProfileUpdateActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting edit profile activity", e);
            Toast.makeText(this, "Error opening edit profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCameraResult(Intent data) {
        try {
            if (data.getExtras() != null) {
                bitmap_img = (Bitmap) data.getExtras().get("data");
                if (bitmap_img != null) {
                    picture_str = new Base64Converter().getStringImage(bitmap_img);
                    if (ChildImage != null) {
                        ChildImage.setImageBitmap(bitmap_img);
                    }
                    if (save_changes != null) {
                        save_changes.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "Camera image captured successfully");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling camera result", e);
            Toast.makeText(this, "Error processing camera image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGalleryResult(Intent data) {
        try {
            Uri image_path = data.getData();
            if (image_path != null) {
                bitmap_img = MediaStore.Images.Media.getBitmap(getContentResolver(), image_path);
                if (bitmap_img != null) {
                    picture_str = new Base64Converter().getStringImage(bitmap_img);
                    if (ChildImage != null) {
                        ChildImage.setImageBitmap(bitmap_img);
                    }
                    if (save_changes != null) {
                        save_changes.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, "Gallery image selected successfully");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling gallery result", e);
            Toast.makeText(this, "Error loading image from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePermissionResult(Map<String, Boolean> permissions) {
        try {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                openCameraIntent();
            } else {
                HelperAlertDialogMessage.showAlertMessage(context, "Camera and storage permissions are required");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result", e);
        }
    }

    private void openBottomSheet() {
        try {
            View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);
            final BottomSheetDialog dialog = new BottomSheetDialog(context);
            dialog.setContentView(view);
            dialog.show();
            
            LinearLayout gallery = view.findViewById(R.id.gallery);
            LinearLayout camera = view.findViewById(R.id.camera);
            ImageView cancel = view.findViewById(R.id.Cancel);
            
            if (gallery != null) {
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        openGallery();
                    }
                });
            }
            
            if (camera != null) {
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        checkAndRequestPermissions();
                    }
                });
            }
            
            if (cancel != null) {
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening bottom sheet", e);
            Toast.makeText(this, "Error opening image options", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRequestPermissions() {
        try {
            // Check permissions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    
                    permissionLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
                } else {
                    openCameraIntent();
                }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions", e);
        }
    }

    private void openCameraIntent() {
        try {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(camera_intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera intent", e);
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening gallery", e);
            Toast.makeText(this, "Error opening gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePicture(final String parent_id, final String campus_id,
                              final String file, final String student_id) {
        try {
            if (progress_bar != null) {
                progress_bar.setVisibility(View.VISIBLE);
            }
            
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                    API.update_picture_student, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Update picture response: " + response);
                    try {
                        JSONObject responseObj = new JSONObject(response);
                        JSONObject status = responseObj.getJSONObject("status");
                        
                        if (status.getString("code").equals("1000")) {
                            String new_picture = responseObj.getString("data");
                            Log.d(TAG, "Picture updated successfully: " + new_picture);
                            loadChildData(parent_id, campus_id);
                        } else {
                            if (progress_bar != null) {
                                progress_bar.setVisibility(View.GONE);
                            }
                            String message = status.getString("message");
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing update picture response", e);
                        if (progress_bar != null) {
                            progress_bar.setVisibility(View.GONE);
                        }
                        Toast.makeText(context, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error updating picture", error);
                    if (progress_bar != null) {
                        progress_bar.setVisibility(View.GONE);
                    }
                    Toast.makeText(context, "Error updating picture: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        HashMap<String, String> postParam = new HashMap<>();
                        postParam.put("parent_parent_id", parent_id);
                        postParam.put("parent_id", campus_id);
                        postParam.put("student_id", student_id);
                        postParam.put("file", file);
                        return new JSONObject(postParam).toString().getBytes();
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating request body", e);
                        return new byte[0];
                    }
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> header_parameter = new HashMap<>();
                    header_parameter.put("Content-Type", "application/json");
                    return header_parameter;
                }
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            
            requestQueue.add(jsonObjectRequest);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in updatePicture", e);
            if (progress_bar != null) {
                progress_bar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error updating picture", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChildData(final String parent_parent_id, final String campus_id) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                    API.load_profile, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Load child data response: " + response);
                    Log.d(TAG, "=== RAW API RESPONSE FOR ROLL NUMBER DEBUG ===");
                    Log.d(TAG, "Full response: " + response);
                    Log.d(TAG, "=== END RAW API RESPONSE ===");
                    try {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();

                        JSONObject responseObj = new JSONObject(response);
                        JSONObject status = responseObj.getJSONObject("status");

                        if (status.getString("code").equals("1000")) {
                            JSONArray students_array = responseObj.getJSONArray("students");
                            if (students_array.length() > 0) {
                                List<SharedStudent> studentList = Arrays.asList(gson.fromJson(students_array.toString(), SharedStudent[].class));
                                Paper.book().write("students", studentList);
                                
                                // Update current student data with fresh API data
                                if (student != null) {
                                    for (SharedStudent freshStudent : studentList) {
                                        if (freshStudent.getUniqueId().equals(student.getUniqueId())) {
                                            // Update the current student with fresh data
                                            student = freshStudent;
                                            Paper.book().write("current_child_model", student);
                                            
                                            // Debug log the fresh roll number
                                            Log.d(TAG, "=== FRESH STUDENT DATA FROM API ===");
                                            Log.d(TAG, "Fresh roll number: '" + student.getRollNo() + "'");
                                            Log.d(TAG, "Fresh roll number is null: " + (student.getRollNo() == null));
                                            Log.d(TAG, "=== END FRESH STUDENT DATA ===");
                                            
                                            // Refresh the UI with fresh data
                                            refreshStudentData();
                                            break;
                                        }
                                    }
                                }
                                
                                if (progress_bar != null) {
                                    progress_bar.setVisibility(View.GONE);
                                }
                                
                                // Only show success message if this was triggered by picture update
                                if (!picture_str.isEmpty()) {
                                    Toast.makeText(context, "Profile Image Updated Successfully", Toast.LENGTH_SHORT).show();
                                    
                                    // Reset the save changes button
                                    if (save_changes != null) {
                                        save_changes.setVisibility(View.GONE);
                                    }
                                    
                                    // Clear the picture string
                                    picture_str = "";
                                } else {
                                    Log.d(TAG, "SharedStudent data refreshed successfully with correct roll number");
                                }
                                
                                Log.d(TAG, "Child data loaded successfully");
                            }
                        } else {
                            String message = status.getString("message");
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing load child data response", e);
                        Toast.makeText(context, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error loading child data", error);
                    Toast.makeText(context, "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        HashMap<String, String> postParam = new HashMap<>();
                        postParam.put("parent_parent_id", parent_parent_id);
                        postParam.put("parent_id", campus_id);
                        return new JSONObject(postParam).toString().getBytes();
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating request body", e);
                        return new byte[0];
                    }
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> header_parameter = new HashMap<>();
                    header_parameter.put("Content-Type", "application/json");
                    return header_parameter;
                }
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    50000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            
            requestQueue.add(jsonObjectRequest);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in loadChildData", e);
            Toast.makeText(this, "Error loading child data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        try {
            Log.d(TAG, "onResume: refreshing student data");
            
            // Refresh student data from Paper DB
            student = Paper.book().read("current_child_model");
            if (student != null) {
                refreshStudentData();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    private void refreshStudentDataFromAPI() {
        try {
            if (parent_id != null && !parent_id.isEmpty() && 
                campus_id != null && !campus_id.isEmpty()) {
                Log.d(TAG, "Refreshing student data from API to get correct roll number");
                loadChildData(parent_id, campus_id);
            } else {
                Log.w(TAG, "Cannot refresh student data: parent_id or campus_id is missing");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing student data from API", e);
        }
    }

    private void refreshStudentData() {
        try {
            if (studentName != null) studentName.setText(student.getFullName());
            
            // Format birth date
            if (BirthDate != null && student.getDob() != null) {
                try {
                    if (inputFormat != null && outputFormat != null) {
                        Date date = inputFormat.parse(student.getDob());
                        String formattedDate = outputFormat.format(date);
                        BirthDate.setText(formattedDate);
                    } else {
                        BirthDate.setText(student.getDob());
                    }
                } catch (ParseException e) {
                    Log.w(TAG, "Error parsing birth date, using original format", e);
                    BirthDate.setText(student.getDob());
                }
            }
            
            if (ChildGender != null) ChildGender.setText(student.getGender());
            if (ParentsMobileNumber != null) ParentsMobileNumber.setText(student.getParentPhone());
            if (registration_number != null) registration_number.setText(String.valueOf(student.getRegistrationNumber()));
            if (shift != null) shift.setText(student.getClassName());
            if (form_submission_date != null) form_submission_date.setText(student.getFormSubmissionDate());
            if (roll_number != null) roll_number.setText(String.valueOf(student.getRollNo()));
            if (section_name != null) section_name.setText(student.getSectionName());
            if (Advanced != null) Advanced.setText(String.valueOf(student.getAdvance()));
            
            // Refresh save changes button state
            if (save_changes != null) {
                save_changes.setVisibility(picture_str.isEmpty() ? View.GONE : View.VISIBLE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing student data", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        try {
            // Clean up resources
            if (bitmap_img != null && !bitmap_img.isRecycled()) {
                bitmap_img.recycle();
                bitmap_img = null;
            }
            
            context = null;
            student = null;
            
            Log.d(TAG, "ParentChildDetail activity destroyed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the content won't be hidden behind the system bars
     */
    private void setupWindowInsets() {
        try {
            android.view.View rootLayout = findViewById(android.R.id.content);
            
            if (rootLayout != null) {
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                    try {
                        androidx.core.graphics.Insets systemInsets = insets.getInsets(
                            androidx.core.view.WindowInsetsCompat.Type.systemBars()
                        );

                        // Add bottom margin to footer container to push it above navigation bar
                        android.widget.LinearLayout footerContainer = findViewById(R.id.footer_container);
                        if (footerContainer != null) {
                            // Set bottom margin to navigation bar height to ensure footer is visible
                            int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            Log.d("ParentChildDetail", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("ParentChildDetail", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("ParentChildDetail", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("ParentChildDetail", "Error setting up window insets: " + e.getMessage());
        }
    }
}
