package topgrade.parent.com.parentseeks.Teacher.Activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Adaptor.TimetableAdaptor;
import topgrade.parent.com.parentseeks.Teacher.Model.Timetable;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSession;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSessionModel;
import topgrade.parent.com.parentseeks.Teacher.Model.TimetableSmsModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class StaffTimeTable extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StaffTimeTable";
    
    // Lecture validation constants
    private static final int MIN_LECTURES = 1;
    private static final int MAX_LECTURES = 8;
    
    ProgressBar progress_bar;
    TextView total_records;
    Button sent_timetable_in_sms;
    Context context;
    SearchableSpinner timetable_spinner;
    RecyclerView timetable_rcv;
    List<Timetable> list = new ArrayList<>();
    String timetable_sms = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_staff_time_table);
        
        // Configure status bar for navy blue background with white icons - COMPREHENSIVE FIX
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Set transparent status bar to allow header wave to cover it
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navy_blue));
            
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
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        }

        // Additional fix for older Android versions - Force white icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Ensure status bar icons are light (white) on dark background
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility() & ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
        
        // Setup window insets to respect system bars (status bar, navigation bar, notches)
        setupWindowInsets();
        
        context = StaffTimeTable.this;
        
        // Load constants from Paper database (CRITICAL FIX)
        Paper.init(context);
        Constant.loadFromPaper();
        Log.d(TAG, "Constants loaded - staff_id: " + Constant.staff_id + ", campus_id: " + Constant.campus_id);
        
        Initlization();
        load_timetable_section();
        
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
    }



    private void Initlization() {
        total_records = findViewById(R.id.total_records);
        sent_timetable_in_sms = findViewById(R.id.send_timetable_sms_button);
        timetable_rcv = findViewById(R.id.timetable_rcv);
        progress_bar = findViewById(R.id.progress_bar);
        timetable_spinner = findViewById(R.id.timetable_spinner);
        timetable_spinner.setTitle("Select Time Session");
        
        // Center the spinner text programmatically
        try {
            timetable_spinner.setGravity(android.view.Gravity.CENTER);
        } catch (Exception e) {
            Log.d(TAG, "Could not set spinner gravity: " + e.getMessage());
        }
        
        progress_bar = findViewById(R.id.progress_bar);
        
        // Setup RecyclerView with LinearLayoutManager
        if (timetable_rcv != null) {
            timetable_rcv.setLayoutManager(new LinearLayoutManager(this));
            Log.d(TAG, "Initlization() - RecyclerView initialized with LinearLayoutManager");
        } else {
            Log.e(TAG, "Initlization() - RecyclerView is null!");
        }
    }

    private void load_timetable(String session_id) {
        Log.d(TAG, "load_timetable() - staff_id: " + Constant.staff_id);
        Log.d(TAG, "load_timetable() - campus_id: " + Constant.campus_id);
        Log.d(TAG, "load_timetable() - session_id: " + session_id);
        
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("timetable_session_id", session_id);
        
        Log.d(TAG, "load_timetable() - API request body: " + (new JSONObject(postParam)).toString());
        
        findViewById(R.id.progress_overlay).setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        Constant.mApiService.load_timetable(body).enqueue(new Callback<TimetableModel>() {
            @Override
            public void onResponse(Call<TimetableModel> call, Response<TimetableModel> response) {
                Log.d(TAG, "load_timetable() - API response code: " + response.code());
                
                if (response.body() != null) {
                    Log.d(TAG, "load_timetable() - SharedStatus code: " + response.body().getStatus().getCode());
                    Log.d(TAG, "load_timetable() - SharedStatus message: " + response.body().getStatus().getMessage());
                    
                    if (response.body().getStatus().getCode().equals("1000")) {
                        list = response.body().getTimetable();
                        Log.d(TAG, "load_timetable() - Timetable list size: " + (list != null ? list.size() : "null"));

                        if (list != null && list.size() > 0) {
                            List<Timetable.Detail> detailList = list.get(0).getDetail();
                            Log.d(TAG, "load_timetable() - Detail list size: " + (detailList != null ? detailList.size() : "null"));
                            
                            // Validate lecture count
                            if (!validateLectureCount(detailList)) {
                                findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                                Toast.makeText(StaffTimeTable.this, "Invalid timetable: Must have " + MIN_LECTURES + "-" + MAX_LECTURES + " lectures", Toast.LENGTH_LONG).show();
                                Log.e(TAG, "load_timetable() - Lecture count validation failed");
                                return;
                            }
                            
                            sent_timetable_in_sms.setVisibility(View.VISIBLE);
                            timetable_sms = response.body().getTimetable_sms();
                            
                            if (timetable_rcv != null) {
                                TimetableAdaptor adapter = new TimetableAdaptor(detailList, StaffTimeTable.this, MIN_LECTURES, MAX_LECTURES);
                                timetable_rcv.setAdapter(adapter);
                                // Update total records to show only visible (non-break) items
                                total_records.setText("Total Records: " + adapter.getVisibleItemCount());
                                Log.d(TAG, "load_timetable() - Adapter set successfully");
                            } else {
                                Log.e(TAG, "load_timetable() - RecyclerView is null!");
                            }
                        } else {
                            sent_timetable_in_sms.setVisibility(View.GONE);
                            Toast.makeText(StaffTimeTable.this, "No Timetable Uploaded", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "load_timetable() - No timetable data found");
                        }

                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "load_timetable() - API error: " + response.body().getStatus().getMessage());
                    }
                } else {
                    findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "load_timetable() - Response body is null");
                }
            }

            @Override
            public void onFailure(Call<TimetableModel> call, Throwable e) {
                e.printStackTrace();
                findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                Log.e(TAG, "load_timetable() - API call failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    private void send_timetable() {
        Log.d(TAG, "send_timetable() - staff_id: " + Constant.staff_id);
        Log.d(TAG, "send_timetable() - campus_id: " + Constant.campus_id);

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);
        postParam.put("message", timetable_sms);

        findViewById(R.id.progress_overlay).setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());
        Constant.mApiService.load_timetable_sms(body).enqueue(new Callback<TimetableSmsModel>() {
            @Override
            public void onResponse(Call<TimetableSmsModel> call, Response<TimetableSmsModel> response) {
                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        Toast.makeText(StaffTimeTable.this, "TimeTable Send in Your Number Soon.", Toast.LENGTH_LONG).show();
                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    findViewById(R.id.progress_overlay).setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<TimetableSmsModel> call, Throwable e) {

                e.printStackTrace();
                findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void load_timetable_section() {
        Log.d(TAG, "load_timetable_section() - staff_id: " + Constant.staff_id);
        Log.d(TAG, "load_timetable_section() - campus_id: " + Constant.campus_id);

        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("staff_id", Constant.staff_id);
        postParam.put("parent_id", Constant.campus_id);

        findViewById(R.id.progress_overlay).setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                (new JSONObject(postParam)).toString());

        Constant.mApiService.load_timetable_session(body).enqueue(new Callback<TimetableSessionModel>() {
            @Override
            public void onResponse(Call<TimetableSessionModel> call, Response<TimetableSessionModel> response) {

                if (response.body() != null) {
                    if (response.body().getStatus().getCode().equals("1000")) {
                        final List<TimetableSession> list = response.body().getTimetableSession();


                        List<String> sesssion_list = new ArrayList<>();

                        for (TimetableSession timetableSession : list) {


                            sesssion_list.add(timetableSession.getFullName() + " (" + timetableSession.getShift() + " )");
                        }


                        // Create centered adapter for spinner
                        ArrayAdapter timetable_session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sesssion_list) {
                            @Override
                            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView textView = (TextView) view;
                                textView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                                textView.setGravity(android.view.Gravity.CENTER);
                                return view;
                            }
                            
                            @Override
                            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView textView = (TextView) view;
                                textView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
                                textView.setGravity(android.view.Gravity.CENTER);
                                return view;
                            }
                        };
                        timetable_spinner.setAdapter(timetable_session_adaptor);

                        timetable_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String session_id = list.get(position).getUniqueId();
                                load_timetable(session_id);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });


                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.progress_overlay).setVisibility(View.GONE);
                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    findViewById(R.id.progress_overlay).setVisibility(View.GONE);

                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<TimetableSessionModel> call, Throwable e) {
                e.printStackTrace();
                findViewById(R.id.progress_overlay).setVisibility(View.GONE);
            }
        });
    }

    public void send_sms(View view) {
        Send_Sms_Option(view);

    }

    private void Send_Sms_Option(View view) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenu().add("Local SMS");
        popup.getMenu().add("Whatsapp");
        popup.getMenu().add("Whatsapp(Business)");
        popup.getMenu().add("Other");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Whatsapp":
                        String phone = Paper.book().read("phone", "");
                        Util.shareToWhatsAppWithNumber(context, timetable_sms, phone, "com.whatsapp");
                        break;
                    case "Whatsapp(Business)":
                        String phone_business = Paper.book().read("phone", "");
                        Util.shareToWhatsAppWithNumber(context, timetable_sms, phone_business, "com.whatsapp.w4b");
                        break;
                    case "Local SMS":
                        String phone_sms = Paper.book().read("phone", "");
                        Util.showSmsIntent(context, timetable_sms, phone_sms);
                        break;
                    case "Other":
                        String phone_other = Paper.book().read("phone", "");
                        Util.shareWithPhoneNumber(context, timetable_sms, phone_other);
                        break;
                    default:
                        send_timetable();
                        break;

                }
                return true;
            }
        });
        popup.show();
    }
    
    /**
     * Validates that the timetable has the correct number of lectures (1-8)
     */
    private boolean validateLectureCount(List<Timetable.Detail> detailList) {
        if (detailList == null) {
            Log.w(TAG, "validateLectureCount() - Detail list is null");
            return false;
        }
        
        int lectureCount = 0;
        for (Timetable.Detail detail : detailList) {
            String subject = detail.getSubject();
            // Count only non-break periods as lectures
            if (subject != null && !subject.trim().isEmpty() && 
                !subject.toLowerCase().contains("break") && 
                !subject.toLowerCase().contains("recess") &&
                !subject.toLowerCase().contains("lunch")) {
                lectureCount++;
            }
        }
        
        Log.d(TAG, "validateLectureCount() - Found " + lectureCount + " lectures (range: " + MIN_LECTURES + "-" + MAX_LECTURES + ")");
        
        if (lectureCount < MIN_LECTURES || lectureCount > MAX_LECTURES) {
            Log.w(TAG, "validateLectureCount() - Invalid lecture count: " + lectureCount);
            return false;
        }
        
        return true;
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

                        // Add bottom margin to send button to push it well above navigation bar
                        Button sendButton = findViewById(R.id.send_timetable_sms_button);
                        if (sendButton != null) {
                            // Calculate bottom margin: navigation bar height + extra padding for visibility
                            int navBarHeight = systemInsets.bottom > 0 ? systemInsets.bottom : 0;
                            int extraPadding = (int) (24 * getResources().getDisplayMetrics().density); // 24dp
                            int totalBottomMargin = navBarHeight + extraPadding;
                            
                            android.view.ViewGroup.MarginLayoutParams params = 
                                (android.view.ViewGroup.MarginLayoutParams) sendButton.getLayoutParams();
                            if (params != null) {
                                params.bottomMargin = totalBottomMargin;
                                sendButton.setLayoutParams(params);
                                Log.d(TAG, "Send button bottom margin set to: " + totalBottomMargin + "px (navBar: " + navBarHeight + "px + padding: " + extraPadding + "px)");
                            }
                        }
                        
                        // No padding on root layout to avoid touch interference
                        view.setPadding(0, 0, 0, 0);

                        // Return CONSUMED to prevent child views from getting default padding and allow header wave to cover status bar
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in window insets listener: " + e.getMessage());
                        return androidx.core.view.WindowInsetsCompat.CONSUMED;
                    }
                });
            } else {
                Log.e(TAG, "rootLayout is null - cannot setup window insets");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up window insets: " + e.getMessage(), e);
        }
    }
}

