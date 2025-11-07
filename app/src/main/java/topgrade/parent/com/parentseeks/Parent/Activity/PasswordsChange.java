package topgrade.parent.com.parentseeks.Parent.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import androidx.core.content.ContextCompat;

public class PasswordsChange extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    Button chnages;
    CheckBox show_hide_pwd;
    EditText Previous_Password, Confirm_Password;
    EditText New_Password;
    String parent_id;
    String campus_id;
    Context context;
    ProgressBar progress_bar;
    String User_TYpe;

    private void applyTheme() {
        try {
            // Check user type and apply appropriate theme
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("PasswordsChange", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                getWindow().setStatusBarColor(tealColor);
                getWindow().setNavigationBarColor(tealColor);
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("PasswordsChange", "Student theme applied successfully");
            } else if (userType != null && (userType.equals("STAFF") || userType.equals("TEACHER"))) {
                // Apply staff/teacher theme (navy blue) when accessed from staff context
                ThemeHelper.applyStaffTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                int navyColor = ContextCompat.getColor(this, R.color.staff_primary);
                getWindow().setStatusBarColor(navyColor);
                getWindow().setNavigationBarColor(navyColor);
                
                // Force dark navigation bar icons (prevent light appearance)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().setSystemBarsAppearance(
                            0, // 0 = do NOT use light icons
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        );
                    }
                }
                
                Log.d("PasswordsChange", "Staff/Teacher theme applied successfully");
            } else {
                // Apply unified parent theme for password change page
                ParentThemeHelper.applyParentTheme(this, 100); // 100dp for content pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for password change
                ParentThemeHelper.setMoreOptionsVisibility(this, false); // No more options for password change
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Change Password");
                
                Log.d("PasswordsChange", "Parent theme applied successfully");
            }
            
            // Apply footer theming based on user type
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e("PasswordsChange", "Error applying theme", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_passwords_change);

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
        
        // Apply theme based on user type
        applyTheme();
        
        context = PasswordsChange.this;
        Paper.init(context);

        User_TYpe = getIntent().getStringExtra("User_TYpe");
        progress_bar = findViewById(R.id.progress_bar);

        // Setup back button click listener
        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }


        chnages = findViewById(R.id.chnages);
        show_hide_pwd = findViewById(R.id.show_hide_pwd);
        Previous_Password = findViewById(R.id.Previous_Password);
        Confirm_Password = findViewById(R.id.Confirm_Password);
        New_Password = findViewById(R.id.New_Password);

        show_hide_pwd.setOnCheckedChangeListener(PasswordsChange.this);

        // Apply user-specific theme based on User_TYpe
        applyUserTheme();

        // Get user ID based on user type
        if (User_TYpe != null && User_TYpe.equals("STUDENT")) {
            parent_id = Paper.book().read("student_id");
        } else if (User_TYpe != null && (User_TYpe.equals("STAFF") || User_TYpe.equals("TEACHER"))) {
            parent_id = Paper.book().read("staff_id");
        } else {
            parent_id = Paper.book().read("parent_id");
        }
        campus_id = Paper.book().read("campus_id");

        chnages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_password = Previous_Password.getText().toString();
                String new_password = New_Password.getText().toString();
                String Confirm_Password_ = Confirm_Password.getText().toString();

                if (old_password.isEmpty()) {
                    Toast.makeText(context, "Enter Old Password", Toast.LENGTH_SHORT).show();
                } else if (new_password.isEmpty()) {
                    Toast.makeText(context, "Enter New Password", Toast.LENGTH_SHORT).show();
                } else if (Confirm_Password_.isEmpty()) {
                    Toast.makeText(context, "Enter Confirm Password", Toast.LENGTH_SHORT).show();

                } else {
                    String current_password = Paper.book().read("password", "");
                    if (current_password.equals(old_password)) {
                        if (new_password.equals(Confirm_Password_)) {
                            Chaneg_password(parent_id, campus_id, Confirm_Password_);

                        } else {
                            Toast.makeText(context, "Both Password Not Match", Toast.LENGTH_SHORT).show();

                        }

                    } else {
                        Toast.makeText(context, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void Chaneg_password(final String parent_id, final String campus_id, final String password) {


        progress_bar.setVisibility(View.VISIBLE);

        String API_NAME = "";
        if (User_TYpe.equals("Parent")) {
            API_NAME = API.update_password;

        } else if (User_TYpe.equals("Staff")) {
            API_NAME = API.staff_update_password;

        } else if (User_TYpe.equals("STUDENT")) {
            API_NAME = API.student_update_password;

        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                API_NAME, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    JSONObject respone = new JSONObject(response);
                    JSONObject status = respone.getJSONObject("status");
                    if (status.getString("code").equals("1000")) {
                        Paper.book().write("password", password);
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(context, "Passwords Changed", Toast.LENGTH_SHORT).show();
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        String message = status.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e1) {
                    e1.printStackTrace();
                    progress_bar.setVisibility(View.GONE);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress_bar.setVisibility(View.GONE);

                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> postParam = new HashMap<String, String>();
                postParam.put("parent_parent_id", parent_id);
                postParam.put("parent_id", campus_id);
                postParam.put("password", password);


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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // Hide Password (eye icon crossed/checked = hide)
            New_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            Previous_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            Confirm_Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            // Show Password (eye icon open/unchecked = show)
            New_Password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            Previous_Password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            Confirm_Password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    private void applyUserTheme() {
        try {
            Log.d("PasswordsChange", "applyUserTheme() called with User_TYpe: " + User_TYpe);
            ImageView headerWave = findViewById(R.id.header_wave);
            Button changeButton = findViewById(R.id.chnages);
            
            if (User_TYpe != null && User_TYpe.equalsIgnoreCase("STUDENT")) {
                Log.d("PasswordsChange", "Applying STUDENT theme (teal)");
                // Apply student theme (teal)
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_teal);
                }
                if (changeButton != null) {
                    changeButton.setBackgroundColor(getResources().getColor(R.color.student_primary));
                }
                
                // Set status bar color to teal
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                }
            } else if (User_TYpe != null && (User_TYpe.equalsIgnoreCase("STAFF") || User_TYpe.equalsIgnoreCase("TEACHER"))) {
                Log.d("PasswordsChange", "Applying STAFF/TEACHER theme (navy blue)");
                // Apply staff/teacher theme (navy blue)
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_navy_blue);
                }
                if (changeButton != null) {
                    changeButton.setBackgroundColor(getResources().getColor(R.color.staff_primary));
                }
                
                // Set status bar color to navy blue
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.staff_primary));
                }
            } else {
                Log.d("PasswordsChange", "Applying PARENT theme (dark brown) - default");
                // Apply parent theme (dark brown) - default
                if (headerWave != null) {
                    headerWave.setImageResource(R.drawable.bg_wave_dark_brown);
                }
                if (changeButton != null) {
                    changeButton.setBackgroundColor(getResources().getColor(R.color.dark_brown));
                }
                
                // Set status bar color to dark brown
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.dark_brown));
                }
            }
        } catch (Exception e) {
            Log.e("PasswordsChange", "Error applying user theme", e);
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
                            Log.d("PasswordsChange", "Setting footer bottom margin: " + bottomMargin + "dp");
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) footerContainer.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = bottomMargin;
                                footerContainer.setLayoutParams(params);
                                Log.d("PasswordsChange", "Footer margin applied successfully");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e("PasswordsChange", "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            }
        } catch (Exception e) {
            Log.e("PasswordsChange", "Error setting up window insets: " + e.getMessage());
        }
    }
}
