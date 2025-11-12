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
import topgrade.parent.com.parentseeks.Parent.Utils.UserType;
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
    private UserType resolvedUserType = UserType.PARENT;

    private void applyTheme(UserType userType) {
        try {
            Log.d("PasswordsChange", "Resolved User Type: " + userType);

            switch (userType) {
                case STUDENT: {
                    ThemeHelper.applyStudentTheme(this);
                    int tealColor = ContextCompat.getColor(this, R.color.student_primary);
                    getWindow().setStatusBarColor(tealColor);
                    getWindow().setNavigationBarColor(tealColor);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (getWindow().getInsetsController() != null) {
                            getWindow().getInsetsController().setSystemBarsAppearance(
                                    0,
                                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                            );
                        }
                    }
                    Log.d("PasswordsChange", "Student theme applied successfully");
                    break;
                }
                case TEACHER:
                case STAFF: {
                    ThemeHelper.applyStaffTheme(this);
                    int navyColor = ContextCompat.getColor(this, R.color.staff_primary);
                    getWindow().setStatusBarColor(navyColor);
                    getWindow().setNavigationBarColor(navyColor);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (getWindow().getInsetsController() != null) {
                            getWindow().getInsetsController().setSystemBarsAppearance(
                                    0,
                                    android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                            );
                        }
                    }

                    Log.d("PasswordsChange", "Staff/Teacher theme applied successfully");
                    break;
                }
                case PARENT:
                default: {
                    ParentThemeHelper.applyParentTheme(this, 100);
                    ParentThemeHelper.setHeaderIconVisibility(this, false);
                    ParentThemeHelper.setMoreOptionsVisibility(this, false);
                    ParentThemeHelper.setFooterVisibility(this, true);
                    ParentThemeHelper.setHeaderTitle(this, "Change Password");

                    Log.d("PasswordsChange", "Parent theme applied successfully");
                    break;
                }
            }

            ThemeHelper.applyFooterTheme(this, userType.getValue());

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
        context = PasswordsChange.this;
        Paper.init(context);
        resolvedUserType = resolveUserType();
        applyTheme(resolvedUserType);

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

        // Apply user-specific theme based on resolved user type
        applyUserTheme();

        // Get user ID based on user type
        switch (resolvedUserType) {
            case STUDENT:
                parent_id = Paper.book().read("student_id");
                break;
            case TEACHER:
            case STAFF:
                parent_id = Paper.book().read("staff_id");
                break;
            case PARENT:
            default:
                parent_id = Paper.book().read("parent_id");
                break;
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

        String API_NAME = getPasswordApiEndpoint(resolvedUserType);
        if (API_NAME == null || API_NAME.isEmpty()) {
            Log.e("PasswordsChange", "Unable to resolve password update API for user type: " + resolvedUserType);
            Toast.makeText(context, "Unable to update password for this user type.", Toast.LENGTH_SHORT).show();
            progress_bar.setVisibility(View.GONE);
            return;
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
            Log.d("PasswordsChange", "applyUserTheme() called with resolvedUserType: " + resolvedUserType);
            ImageView headerWave = findViewById(R.id.header_wave);
            Button changeButton = findViewById(R.id.chnages);
            
            if (resolvedUserType == UserType.STUDENT) {
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
            } else if (resolvedUserType == UserType.TEACHER || resolvedUserType == UserType.STAFF) {
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

    private String getPasswordApiEndpoint(UserType userType) {
        if (userType == null) {
            return null;
        }
        switch (userType) {
            case STUDENT:
                return API.student_update_password;
            case TEACHER:
            case STAFF:
                return API.staff_update_password;
            case PARENT:
            default:
                return API.update_password;
        }
    }

    private UserType resolveUserType() {
        String typeFromIntent = null;
        if (getIntent() != null) {
            typeFromIntent = getIntent().getStringExtra("User_TYpe");
        }

        if (typeFromIntent == null || typeFromIntent.trim().isEmpty()) {
            typeFromIntent = Paper.book().read(Constants.User_Type, "");
        }

        UserType userType = UserType.Companion.fromString(typeFromIntent);
        if (userType == null) {
            Log.w("PasswordsChange", "Unknown user type '" + typeFromIntent + "', defaulting to PARENT");
            userType = UserType.PARENT;
        }
        return userType;
    }
}
