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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
// import android.widget.RelativeLayout; // Removed - not needed
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import androidx.annotation.NonNull;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Model.GeneralModel;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Base64Converter;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.EnhancedImageLoader;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.HelperAlertDialogMessage;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;

public class Edit_ProfileParent extends AppCompatActivity {


    Context context;
    TextView phone, email, landline, address, Name;

    CircleImageView image;
    Bitmap bitmap_img;
    public static String picture_str = "";
    TextView save_changes;
    ProgressBar progress_bar;
    View image_action;

    // Activity Result Launchers for modern API
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> galleryPermissionLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("Edit_ProfileParent", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
                int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                getWindow().setStatusBarColor(tealColor);
                getWindow().setNavigationBarColor(tealColor);
                
                // Force light status bar icons for better visibility on teal background
                // minSdk is 26, so M (API 23) check is unnecessary
                getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() &
                    ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                );
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                // Change wave background to teal for student theme
                ImageView headerWave = findViewById(R.id.header_wave);
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                
                // Change update button color to student theme (teal)
                MaterialButton updateButton = findViewById(R.id.save_changes);
                if (updateButton != null) {
                    updateButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.student_primary));
                }
                
                // Change image edit icon background to student theme (teal)
                ImageView editIcon = findViewById(R.id.edit_icon);
                if (editIcon != null) {
                    editIcon.setBackgroundColor(ContextCompat.getColor(this, R.color.student_primary));
                }
                
                Log.d("Edit_ProfileParent", "Student theme applied successfully");
            } else {
                // Apply unified parent theme for edit profile page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for edit profile
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for edit profile
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Edit Profile");
                
                Log.d("Edit_ProfileParent", "Parent theme applied successfully");
            }
        } catch (Exception e) {
            Log.e("Edit_ProfileParent", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Safety check: Ensure this activity is only opened by parent users
        Paper.init(this);
        String userType = Paper.book().read("User_Type", "");
        if ("Teacher".equals(userType)) {
            Log.e("Edit_ProfileParent", "Edit_ProfileParent opened by staff user: " + userType);
            Toast.makeText(this, "Access denied: Parent profile editing is not available for staff members", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        setContentView(R.layout.activity_edit_profile_parent);
        
        // Configure status bar for dark brown background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            // Set navigation bar to dark brown to match parent theme
            getWindow().setNavigationBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_brown));
            
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
                    0, // No light icons for status bar and navigation bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar and navigation bar icons are light (white) on dark background
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            // For Android O and above, also ensure navigation bar icons are white
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();


        context = Edit_ProfileParent.this;

        // Load constants from Paper database (CRITICAL FIX)
        Constant.loadFromPaper();
        Log.d("Edit_ProfileParent", "Constants loaded - parent_id: " + Constant.parent_id + 
              ", campus_id: " + Constant.campus_id);

        // Initialize image_action (cache lookup to avoid duplicate calls)
        image_action = findViewById(R.id.image_action);
        Name = findViewById(R.id.et_parent_name);
        image = findViewById(R.id.image);
        landline = findViewById(R.id.et_landline_parent);
        email = findViewById(R.id.et_email_parent);
        phone = findViewById(R.id.et_phone_parent);
        address = findViewById(R.id.et_address_parent);
        progress_bar = findViewById(R.id.progress_bar);
        save_changes = findViewById(R.id.save_changes);

        // Apply theme based on user type
        applyTheme();

        String image_link = Paper.book().read("picture", "");

        String link = API.parent_image_base_url + image_link;

        // Use enhanced image loader with better error handling
        EnhancedImageLoader imageLoader = new EnhancedImageLoader(this);
        imageLoader.loadImage(image, link, new EnhancedImageLoader.ImageLoadCallback() {
            @Override
            public void onSuccess(Drawable drawable) {
                Log.d("Edit_ProfileParent", "Initial profile image loaded successfully");
            }
            
            @Override
            public void onError(Exception e) {
                Log.w("Edit_ProfileParent", "Failed to load initial profile image, using fallback", e);
                // The enhanced loader will automatically show the fallback image
            }
        });


        // Load data using same keys for both user types (only theme differs)
        Log.d("Edit_ProfileParent", "Current user type: " + userType);
        
        // Use same data keys for both student and parent users when viewing parent profile
        final String full_name_ = Paper.book().read("full_name");
        String email_ = Paper.book().read("email");
        String phone_ = Paper.book().read("phone");
        String landline_ = Paper.book().read("landline");
        String address_ = Paper.book().read("address");
        final String picture_ = Paper.book().read("picture");


        if (picture_ != null) {
            if (!picture_.isEmpty()) {
                // Use enhanced image loader with better error handling
                String imageUrl = API.parent_image_base_url + picture_;
                
                imageLoader.loadImage(image, imageUrl, new EnhancedImageLoader.ImageLoadCallback() {
                    @Override
                    public void onSuccess(Drawable drawable) {
                        Log.d("Edit_ProfileParent", "Profile image loaded successfully");
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        Log.w("Edit_ProfileParent", "Failed to load profile image, using fallback", e);
                        // The enhanced loader will automatically show the fallback image
                    }
                });
            }
        }


        email.setText(email_);
        phone.setText(phone_);
        landline.setText(landline_);
        address.setText(address_);
        Name.setText(full_name_);


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
        
        if (headerTitle != null) {
            String title = (full_name_ != null && !full_name_.isEmpty()) ? 
                full_name_ + " - " + getString(R.string.edit_profile) : getString(R.string.edit_profile);
            headerTitle.setText(title);
        }


        // Set up image action click listener with null safety
        if (image_action != null) {
            image_action.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenuInflater().inflate(R.menu.image_action_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.Edit_Image) {
                        open_botton_sheet();
                    } else if (id == R.id.View_Image) {
                        final String currentPicture = Paper.book().read("picture");
                        startActivity(new Intent(context, ZoomImage.class)
                                .putExtra("link", API.parent_image_base_url + currentPicture)
                                .putExtra("name", full_name_));
                    }
                    return true;
                });
                popup.show();
            });
        } else {
            Log.w("Edit_ProfileParent", "image_action is null, skipping click listener setup");
        }


        save_changes.setOnClickListener(v -> update_profile());

        // Initialize Activity Result Launchers
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        bitmap_img = (Bitmap) extras.get("data");
                        if (bitmap_img != null) {
                            picture_str = new Base64Converter().getStringImage(bitmap_img);
                            image.setImageBitmap(bitmap_img);
                        }
                    }
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri image_path = result.getData().getData();
                    if (image_path != null) {
                        @SuppressWarnings("deprecation")
                        CropImageContractOptions options = new CropImageContractOptions(
                            image_path,
                            new CropImageOptions()
                        );
                        cropImageLauncher.launch(options);
                    }
                }
            }
        );

        cameraPermissionLauncher = registerForActivityResult(
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

        galleryPermissionLauncher = registerForActivityResult(
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
                    opne_gallery();
                } else {
                    HelperAlertDialogMessage.showAlertMessage(context, "Need Gallery permission");
                }
            }
        );

        @SuppressWarnings("deprecation")
        ActivityResultLauncher<CropImageContractOptions> tempCropLauncher = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    Uri resultUri = result.getUriContent();
                    if (resultUri != null) {
                        try {
                            // Use MediaStore for API level 26+ compatibility
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                // Use ImageDecoder for API 28+
                                bitmap_img = ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(getContentResolver(), resultUri)
                                );
                            } else {
                                // Use MediaStore for older versions
                                bitmap_img = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                            }
                            picture_str = new Base64Converter().getStringImage(bitmap_img);
                            image.setImageBitmap(bitmap_img);
                        } catch (Exception e) {
                            Log.e("Edit_ProfileParent", "Error loading cropped image", e);
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Exception error = result.getError();
                    if (error != null) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        cropImageLauncher = tempCropLauncher;
    }

    private void update_picture(final String file) {


        progress_bar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.update_picture, new com.android.volley.Response.Listener<>() {


            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    JSONObject respone = new JSONObject(response);
                    JSONObject status = respone.getJSONObject("status");
                    if (status.getString("code").equals("1000")) {
                        String new_picture = respone.getString("data");
                        Paper.book().write("picture", new_picture);
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        String message = status.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e1) {
                    Log.e("Edit_ProfileParent", "Error parsing update picture response", e1);
                    progress_bar.setVisibility(View.GONE);
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public byte[] getBody() {
                HashMap<String, String> postParam = new HashMap<>();
                postParam.put("parent_parent_id", Constant.parent_id);
                postParam.put("parent_id", Constant.campus_id);
                postParam.put("file", file);
                
                // Log the request parameters for debugging
                Log.d("Edit_ProfileParent", "Update Picture API Request Parameters:");
                Log.d("Edit_ProfileParent", "- parent_parent_id: " + Constant.parent_id);
                Log.d("Edit_ProfileParent", "- parent_id: " + Constant.campus_id);
                Log.d("Edit_ProfileParent", "- Request JSON: " + new JSONObject(postParam));
                
                return new JSONObject(postParam).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
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

    }

    private void update_profile() {


        final String phone_ = phone.getText().toString();
        final String email_ = email.getText().toString();
        final String landline_ = landline.getText().toString();
        final String address_ = address.getText().toString();
        final String Name_ = Name.getText().toString();


        HashMap<String, String> postParam = new HashMap<>();
        postParam.put("parent_id", Constant.parent_id);
        postParam.put("campus_id", Constant.campus_id);
        postParam.put("full_name", Name_);
        postParam.put("email", email_);
        postParam.put("phone", phone_);
        postParam.put("landline", landline_);
        postParam.put("address", address_);


        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        Constant.mApiService.update_profile_(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeneralModel> call, @NonNull Response<GeneralModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Paper.book().write("full_name", Name_);
                        Paper.book().write("email", email_);
                        Paper.book().write("phone", phone_);
                        Paper.book().write("landline", landline_);
                        Paper.book().write("address", address_);
                        
                        // If a new picture was selected, update it too
                        if (picture_str != null && !picture_str.isEmpty()) {
                            update_picture(picture_str);
                        } else {
                            progress_bar.setVisibility(View.GONE);
                            finish();
                        }
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);

                    try (okhttp3.Response rawResponse = response.raw()) {
                        Toast.makeText(context, rawResponse.message(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("Edit_ProfileParent", "Error accessing response", e);
                        Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
                Log.e("Edit_ProfileParent", "Error updating profile", e);
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void open_botton_sheet() {
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
                check_permission();

            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // Check camera permissions
                if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionLauncher.launch(new String[]{
                                Manifest.permission.CAMERA,
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


    private void check_permission() {
        // Check gallery permissions
        if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                galleryPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
            } else {
                opne_gallery();
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                            Log.d("Edit_ProfileParent", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("Edit_ProfileParent", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("Edit_ProfileParent", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Edit_ProfileParent", "Error setting up window insets: " + e.getMessage());
        }
    }

}
