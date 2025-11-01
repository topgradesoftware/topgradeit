package topgrade.parent.com.parentseeks.Teacher.Activity;

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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.Parent.Activity.ZoomImage;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Base64Converter;
import topgrade.parent.com.parentseeks.Parent.Utils.HelperAlertDialogMessage;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel;
import topgrade.parent.com.parentseeks.Teacher.Model.UpdateProfilModel;
import topgrade.parent.com.parentseeks.Teacher.Model.City;
import topgrade.parent.com.parentseeks.Teacher.Model.CityModel;
import topgrade.parent.com.parentseeks.Teacher.Model.State;
import topgrade.parent.com.parentseeks.Teacher.Model.StateModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import android.graphics.ImageDecoder;
import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class Edit_Profile extends AppCompatActivity implements View.OnClickListener {


    Context context;
    EditText phone, email, landline, address, Name, subject, cnic, qualification;
    EditText dob, parent_name;
    TextView gender_edit, state_edit, city_edit;

    StaffModel parentModel;

    CircleImageView image;
    public static final int REQUEST_CODE_GALLERY = 8000;
    public static final int REQUEST_CODE_CAMERA = 7000;
    Bitmap bitmap_img;
    public static String picture_str = "";
    MaterialButton save_changes;
    ProgressBar progress_bar;
            String parent_id;

        String campus_id, datePatternForApi, city_id, state_id;
    List<State> stateList = new ArrayList<>();
    List<City> city_list = new ArrayList<>();
    
    RelativeLayout image_action;


    final Calendar myCalendar = Calendar.getInstance();
    String inputPattern = "yyy-MM-dd";
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat(inputPattern, Locale.US);
            datePatternForApi = sdf.format(myCalendar.getTime());

            String outputPattern = "dd/MM/yyyy";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
            try {
                Date date = inputFormat.parse(sdf.format(myCalendar.getTime()));
                String strDate = outputFormat.format(date);
                dob.setText(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //TODO: change Date Format
//            dob.setText(sdf.format(myCalendar.getTime()));

        }

    };

    // Activity Result Launchers for modern API
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private ActivityResultLauncher<String[]> galleryPermissionLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Safety check: Ensure this activity is only opened by staff users
        Paper.init(this);
        String userType = Paper.book().read("User_Type", "");
        if (!"Teacher".equals(userType)) {
            Log.e("Edit_Profile", "Edit_Profile opened by non-staff user: " + userType);
            Toast.makeText(this, "Access denied: Staff profile editing is only available for staff members", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Disable StrictMode to prevent ANR issues
        android.os.StrictMode.setThreadPolicy(new android.os.StrictMode.ThreadPolicy.Builder()
            .build());
        
        // Remove problematic anti-flickering flags that cause visibility issues
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
        // topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.optimizeWindow(this);
        
        setContentView(R.layout.activity_edit_staff_profile);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
        // Set transparent status bar to allow header wave to cover it
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
        
        // minSdk is 26, so M (API 23) check is unnecessary
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        // Clear the LIGHT_STATUS_BAR flag to ensure white icons on dark background
        flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        // Configure status bar and navigation bar icons for Android R and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (getWindow().getInsetsController() != null) {
                getWindow().getInsetsController().setSystemBarsAppearance(
                    0, // No light icons for status bar (white icons on dark background)
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Additional fix - Force white icons
        // minSdk is 26, so LOLLIPOP (API 21) check is unnecessary
        // Ensure status bar icons are light (white) on dark background
        getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        context = Edit_Profile.this;
        Paper.init(context);

        parentModel = Paper.book().read("Staff_Model", new StaffModel());

        // Initialize back button
        ImageView back_icon = findViewById(R.id.back_icon);
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_action = findViewById(R.id.image_action);
        Name = findViewById(R.id.name_edit);
        image = findViewById(R.id.image);
        landline = findViewById(R.id.landline_edit);
        email = findViewById(R.id.email_edit);
        phone = findViewById(R.id.phone_edit);
        dob = findViewById(R.id.dob_edit);
        parent_name = findViewById(R.id.parent_name_edit);
        address = findViewById(R.id.address_edit);
        qualification = findViewById(R.id.qualification_edit);
        subject = findViewById(R.id.subject_edit);
        cnic = findViewById(R.id.cnic_edit);
        gender_edit = findViewById(R.id.gender_edit);
        state_edit = findViewById(R.id.state_edit);
        city_edit = findViewById(R.id.city_edit);
        progress_bar = findViewById(R.id.progress_bar);
        save_changes = findViewById(R.id.btn_save_changes);

        dob.setKeyListener(null);
        gender_edit.setKeyListener(null);
        state_edit.setKeyListener(null);
        city_edit.setKeyListener(null);
        
        gender_edit.setOnClickListener(Edit_Profile.this);
        state_edit.setOnClickListener(Edit_Profile.this);
        city_edit.setOnClickListener(Edit_Profile.this);


        String image_link = Paper.book().read("picture", "");

        String link = API.employee_image_base_url + image_link;

        Glide.with(this)
                .load(link)
                .placeholder(R.drawable.man)
                .error(R.drawable.man)
                .into(image);

        email.setText(parentModel.getEmail());
        phone.setText(parentModel.getPhone());
        landline.setText(parentModel.getLandline());
        address.setText(parentModel.getAddress());
        qualification.setText(parentModel.getQualification());
        subject.setText(parentModel.getSubject());
        cnic.setText(parentModel.getCnic());
        Name.setText(parentModel.getFull_name());
        parent_name.setText(parentModel.getParent_name());
        datePatternForApi = parentModel.getDob();
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd/MM/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        try {
            Date date = inputFormat.parse(parentModel.getDob());
            String strDate = outputFormat.format(date);
            dob.setText(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        // Set initial values for gender, state, and city
        gender_edit.setText(parentModel.getGender() != null ? parentModel.getGender() : "");
        state_edit.setText(parentModel.getState_name() != null ? parentModel.getState_name() : "");
        city_edit.setText(parentModel.getCity_name() != null ? parentModel.getCity_name() : "");
        state_id = parentModel.getState_id();
        city_id = parentModel.getCity_id();
        



        image_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(Edit_Profile.this, view);
                popup.getMenuInflater()
                        .inflate(R.menu.image_action_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.Edit_Image) {
                            open_botton_sheet();
                        } else if (id == R.id.View_Image) {
                            final String picture_ = Paper.book().read("picture");
                            startActivity(new Intent(Edit_Profile.this, ZoomImage.class)
                                    .putExtra("link", API.employee_image_base_url + picture_)
                                    .putExtra("name", parentModel.getFull_name())

                            );
                        }

                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });


        parent_id = Paper.book().read("parent_id");
        campus_id = Paper.book().read("campus_id");
        Constant.staff_id = Paper.book().read("staff_id");
        Constant.campus_id = campus_id;
        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                update_profile();


            }
        });


        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Edit_Profile.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        // Initialize Activity Result Launchers
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    bitmap_img = ((Bitmap) result.getData().getExtras().get("data"));
                    picture_str = new Base64Converter().getStringImage(bitmap_img);
                    image.setImageBitmap(bitmap_img);
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri image_path = result.getData().getData();
                    if (image_path != null) {
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

        cropImageLauncher = registerForActivityResult(
            new CropImageContract(),
            result -> {
                if (result.isSuccessful()) {
                    Uri resultUri = result.getUriContent();
                    try {
                        // minSdk is 26, but ImageDecoder is available from API 28
                        // Use ImageDecoder for better performance and memory management
                        bitmap_img = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(getContentResolver(), resultUri)
                        );
                        picture_str = new Base64Converter().getStringImage(bitmap_img);
                        image.setImageBitmap(bitmap_img);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Exception error = result.getError();
                    if (error != null) {
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

    }

    private void update_picture(final String file) {


        progress_bar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        final StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API.update_picture_staff, new com.android.volley.Response.Listener<String>() {


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
                    e1.printStackTrace();
                    Toast.makeText(context, e1.getMessage(), Toast.LENGTH_SHORT).show();
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
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("staff_id", Constant.staff_id);
                postParam.put("parent_id", Constant.campus_id);
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

    private void update_profile() {


        String phone_ = phone.getText().toString();
        String email_ = email.getText().toString();
        String landline_ = landline.getText().toString();
        String address_ = address.getText().toString();
        String qualification_ = qualification.getText().toString();
        String subject_ = subject.getText().toString();
        String cnic_ = cnic.getText().toString();
        String Name_ = Name.getText().toString();
        String dob_ = datePatternForApi;
        String gender_ = gender_edit.getText().toString();
        String parent_name_ = parent_name.getText().toString();


        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("full_name", Name_);
        postParam.put("email", email_);
        postParam.put("parent_name", parent_name_);
        postParam.put("phone", phone_);
        postParam.put("landline", landline_);
        postParam.put("address", address_);
        postParam.put("qualification", qualification_);
        postParam.put("subject", subject_);
        postParam.put("cnic", cnic_);
        postParam.put("dob", dob_);
        postParam.put("gender", gender_);
        postParam.put("city_id", city_id);
        postParam.put("state_id", state_id);



        progress_bar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        Constant.mApiService.update_profile(body).enqueue(new Callback<UpdateProfilModel>() {
            @Override
            public void onResponse(Call<UpdateProfilModel> call, Response<UpdateProfilModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        
                        // Debug: Log the received staff data
                        StaffModel receivedStaff = response.body().getStaff().get(0);
                        Log.d("Edit_Profile", "API Response - Gender: " + receivedStaff.getGender());
                        Log.d("Edit_Profile", "API Response - State: " + receivedStaff.getState_name());
                        Log.d("Edit_Profile", "API Response - City: " + receivedStaff.getCity_name());
                        Log.d("Edit_Profile", "API Response - Full Name: " + receivedStaff.getFull_name());
                        Log.d("Edit_Profile", "API Response - Email: " + receivedStaff.getEmail());
                        
                        Paper.book().write("Staff_Model", receivedStaff);
                        progress_bar.setVisibility(View.GONE);
                        finish();
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    progress_bar.setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<UpdateProfilModel> call, Throwable e) {

                e.printStackTrace();
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
                // minSdk is 26, so M (API 23) check is unnecessary
if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            ||
                            ContextCompat.checkSelfPermission(context,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                    ) {
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.gender_edit) {
            show_Gender_popup(v);
        } else if (id == R.id.city_edit) {
            Cities_Get();
        } else if (id == R.id.state_edit) {
            States_Get();
        }
    }



    private String changeDateFormat(String time) {
        String inputPattern = "dd-MMMM-yyyy";
        String outputPattern = "dd/MM/yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
    
    /**
     * Setup window insets to respect system bars (status bar, navigation bar, notches)
     * This ensures the footer is visible above the navigation bar, not hidden behind it
     */
    private void setupWindowInsets() {
        android.view.View rootLayout = findViewById(android.R.id.content);
        
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, insets) -> {
                try {
                    androidx.core.graphics.Insets systemInsets = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                    );

                    // Add bottom margin to save changes button to push it above navigation bar
                    com.google.android.material.button.MaterialButton saveButton = findViewById(R.id.btn_save_changes);
                    if (saveButton != null) {
                        // Set bottom margin to navigation bar height to ensure button is visible
                        int bottomMargin = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                        android.view.ViewGroup.MarginLayoutParams params = 
                            (android.view.ViewGroup.MarginLayoutParams) saveButton.getLayoutParams();
                        if (params != null) {
                            params.bottomMargin = bottomMargin + 16; // 16dp original margin + navigation bar height
                            saveButton.setLayoutParams(params);
                        }
                    }
                    
                    // No padding on root layout to avoid touch interference
                    view.setPadding(0, 0, 0, 0);

                    // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                } catch (Exception e) {
                    Log.e("Edit_Profile", "Error in window insets listener: " + e.getMessage());
                    return androidx.core.view.WindowInsetsCompat.CONSUMED;
                }
            });
        } else {
            Log.e("Edit_Profile", "rootLayout is null - cannot setup window insets");
        }
    }
    
    private void show_Gender_popup(View view) {
        PopupMenu popup = new PopupMenu(Edit_Profile.this, view);
        popup.getMenuInflater().inflate(R.menu.gender_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                gender_edit.setText(item.getTitle().toString());
                return true;
            }
        });
        popup.show();
    }

    private void States_Get() {
        progress_bar.setVisibility(View.VISIBLE);
        Constant.mApiService.getstate().enqueue(new Callback<StateModel>() {
            @Override
            public void onResponse(Call<StateModel> call, Response<StateModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        stateList = response.body().getState();
                        Show_State_Dialog();
                        progress_bar.setVisibility(View.GONE);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StateModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Show_State_Dialog() {
        ArrayList<String> stateList_name = new ArrayList<>();
        for (State state : stateList) {
            stateList_name.add(state.getFull_name());
        }
        SpinnerDialog spinnerDialog = new SpinnerDialog(Edit_Profile.this, stateList_name, "Select State", "Close");
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                state_edit.setText(stateList.get(position).getFull_name());
                state_id = stateList.get(position).getUniqueId();
                // Clear city when state changes
                city_edit.setText("");
                city_id = null;
            }
        });
        spinnerDialog.showSpinerDialog();
    }

    private void Cities_Get() {
        if (state_id == null || state_id.isEmpty()) {
            Toast.makeText(context, "Please select a state first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progress_bar.setVisibility(View.VISIBLE);
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("state_id", state_id);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        Constant.mApiService.getcity(body).enqueue(new Callback<CityModel>() {
            @Override
            public void onResponse(Call<CityModel> call, Response<CityModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        city_list = response.body().getCity();
                        Show_City_Dialog();
                        progress_bar.setVisibility(View.GONE);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CityModel> call, Throwable e) {
                e.printStackTrace();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void Show_City_Dialog() {
        ArrayList<String> cityList_name = new ArrayList<>();
        for (City city : city_list) {
            cityList_name.add(city.getFull_name());
        }
        SpinnerDialog spinnerDialog = new SpinnerDialog(Edit_Profile.this, cityList_name, "Select City", "Close");
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                city_edit.setText(city_list.get(position).getFull_name());
                city_id = city_list.get(position).getUniqueId();
            }
        });
        spinnerDialog.showSpinerDialog();
    }
}

