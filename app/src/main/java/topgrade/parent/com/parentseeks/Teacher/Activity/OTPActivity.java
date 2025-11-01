package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dpizarro.pinview.library.PinView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Activity.Splash;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class OTPActivity extends AppCompatActivity {


    String otp, login_email, login_pass, login_id, phone, userType;
    TextView phone_number;
    Button verify_code;
    PinView pinView;
    ProgressBar progress_bar;
    Context context;
    String fcm_token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply anti-flickering flags for fullscreen experience
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
        
        setContentView(R.layout.activity_otp);

        context = OTPActivity.this;

        otp = getIntent().getStringExtra("otp");
        login_email = getIntent().getStringExtra("login_email");
        login_pass = getIntent().getStringExtra("login_pass");
        login_id = getIntent().getStringExtra("login_id");
        phone = getIntent().getStringExtra("phone");
        userType = getIntent().getStringExtra("user_type");
        if (userType == null) {
            userType = "PARENT"; // Default to parent if not specified
        }
        pinView = findViewById(R.id.pinView);
        phone_number = findViewById(R.id.phone_number);
        verify_code = findViewById(R.id.verify_code);
        progress_bar = findViewById(R.id.progress_bar);

        phone_number.setText("Enter the 4-digit code sent to you at " + phone);

        verify_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_pin = pinView.getPinResults();
                if (!otp.equals(str_pin)) {
                    Toast.makeText(OTPActivity.this, "Verification Code Not Match.", Toast.LENGTH_SHORT).show();
                } else {
                    fcm_token = Paper.book().read(Constants.PREFERENCE_EXTRA_REGISTRATION_ID, "123");
                    login_api_hint(login_email, login_pass, login_id);
                }

            }
        });
    }

    private void login_api_hint(final String name, final String password, final String campus_id) {

        progress_bar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        
        // Choose the appropriate API endpoint based on user type
        String apiEndpoint;
        if ("PARENT".equalsIgnoreCase(userType)) {
            apiEndpoint = API.login;
        } else {
            apiEndpoint = API.staff_login;
        }
        
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                apiEndpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject status = obj.getJSONObject("status");
                    if (status.getString("code").equals("1000")) {
                        try {


                            JSONArray jsonArray = obj.getJSONArray("data");
                            JSONObject data = jsonArray.getJSONObject(0);
                            StaffModel parentModel = new Gson().fromJson(data.toString(), StaffModel.class);

                            Paper.book().write("Staff_Model", parentModel);
                            if (parentModel.getFull_name() != null) {
                                if (!parentModel.getFull_name().isEmpty()) {
                                    Paper.book().write("full_name", parentModel.getFull_name());

                                } else {
                                    Paper.book().write("full_name", "");

                                }
                            } else {
                                Paper.book().write("full_name", "");

                            }
                            if (parentModel.getPicture() != null) {
                                if (!parentModel.getPicture().isEmpty()) {
                                    Paper.book().write("picture", parentModel.getPicture());

                                } else {
                                    Paper.book().write("picture", "");

                                }

                            } else {
                                Paper.book().write("picture", "");

                            }
                            if (parentModel.getPhone() != null) {
                                if (!parentModel.getPhone().isEmpty()) {
                                    Paper.book().write("phone", parentModel.getPhone());

                                } else {
                                    Paper.book().write("phone", "");

                                }

                            } else {
                                Paper.book().write("phone", "");

                            }

                            String staff_id = data.getString("unique_id");
                            JSONObject campus = obj.getJSONObject("campus");
                            String campus_id = campus.getString("unique_id"); // Get campus ID from campus object
                            String campus_name = campus.getString("full_name");
                            String campus_address = campus.getString("address");
                            String campus_phone = campus.getString("phone");
                            Paper.book().write("staff_id", staff_id);
                            Paper.book().write("campus_id", campus_id);
                            Constant.staff_id = staff_id;
                            Constant.campus_id = campus_id;

                            Paper.book().write("password", password);
                            Paper.book().write(Constants.is_login, true);
                            Paper.book().write(Constants.User_Type, userType);
                            Paper.book().write("campus_name", campus_name);
                            Paper.book().write("campus_address", campus_address);
                            Paper.book().write("campus_phone", campus_phone);


                            Paper.book().write("staff_password", password);


                            JSONObject campus_session = obj.getJSONObject("campus_session");
                            String current_session = campus_session.getString("unique_id");
                            Paper.book().write("current_session", current_session);
                            Constant.current_session = current_session;

                            progress_bar.setVisibility(View.GONE);
                            startActivity(new Intent(context, Splash.class)
                                .putExtra("from_login", true)
                            );
                            finish();

                        } catch (Exception error) {
                            error.printStackTrace();
                            progress_bar.setVisibility(View.GONE);
                            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();

                        }


                    } else {
                        progress_bar.setVisibility(View.GONE);
                        String T1 = status.getString("message");
                        Toast.makeText(context, T1, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e1) {
                    e1.printStackTrace();
                    Toast.makeText(context, e1.getMessage(), Toast.LENGTH_SHORT).show();
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

                postParam.put("login_email", name);
                postParam.put("login_pass", password);
                postParam.put("login_id", campus_id);  // CAMPUS ID
                postParam.put("fcm_token", fcm_token);  // fcm_token
                postParam.put("operation", "login");  // fcm_token

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
                60000, // 60 seconds timeout - increased for better data loading
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);

    }

}
