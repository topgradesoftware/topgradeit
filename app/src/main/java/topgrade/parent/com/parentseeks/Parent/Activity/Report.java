package topgrade.parent.com.parentseeks.Parent.Activity;

import android.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import components.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.ParentThemeHelper;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import topgrade.parent.com.parentseeks.Parent.Adaptor.GraphAdaptor;
import topgrade.parent.com.parentseeks.Parent.Adaptor.MonthReportAdaptor;
import topgrade.parent.com.parentseeks.Parent.Adaptor.SubjectDetailMainAdaptor;
import topgrade.parent.com.parentseeks.Parent.Adaptor.SubjectReportAdaptor;
import topgrade.parent.com.parentseeks.Parent.Adaptor.TestAdapto;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener2;
import topgrade.parent.com.parentseeks.Parent.Model.ExamSession;
import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.Parent.Model.SessionModel;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.DayAxisValueFormatter;
import topgrade.parent.com.parentseeks.Parent.Utils.XYMarkerView;
import topgrade.parent.com.parentseeks.Parent.Utils.ActivityTransitionHelper;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class Report extends AppCompatActivity implements OnClickListener, View.OnClickListener, OnClickListener2 {

    private RecyclerView myrecycleview, subject_rcv, test_rcv, subject_detail_rcv, Graph_rcv, marks_rcv,
            percent_rcv, class_pos_rcv;
    BaseApiService mApiService;
    ProgressBar progressBar;
    List<ReportModel.Month> monthList = new ArrayList<>();
    List<ReportModel.Exam> examArrayList = new ArrayList<>();
    List<String> subject_name_list = new ArrayList<>();
    Context context;
    ImageView back_icon, Cancel;
    Call<ReportModel> call;
    SearchableSpinner select_child_spinner, select_examp_session;
    TextView show_advanced_filter;
    LinearLayout filter_area;
    HorizontalScrollView horizontal_scroll;
    String seleted_child_id = "";
    String seleted_child_name = "";
    String seleted_exam_session = "";
    String seleted_exam_session_name = "";
    ArrayAdapter<String> child_adaptor, session_adaptor;
    List<SharedStudent> studentList = new ArrayList<>();
    TextView marks_tv, Selected_Session, Selected_Child, graph_tv, percent_tv, class_pos_tv, subject_wise_tv, month_wise_report_tv, month_wise_tv, subject_wise_report_tv;
    LinearLayout monthly_layout;
    String parent_id;
    String campus_id;
    EditText search_filter;
    Button dialog_search_filter;
    public static List<String> test_name_list;
    ArrayList<BarEntry> barEntries_subject = new ArrayList<>();
    ArrayList<BarEntry> barEntries_month = new ArrayList<>();
    ArrayList<String> month_name_list = new ArrayList<>();

    BarChart barChart, month_wise_graph;
    BarData barData;
    BarDataSet barDataSet;
    ValueFormatter xAxisFormatter;
    XAxis xAxis;
    XYMarkerView mv;
    YAxis leftAxis;
    TextView Avg_Marks, Avg_Position, Avg_Percentage, Avg_Attendance;
    LinearLayout session_layout;
    AlertDialog alertDialog;
    List<String> exam_session_name_list = new ArrayList<>();
    List<ExamSession> examSessionslist = new ArrayList<>();
    LinearLayout session_filter;

    // Add lifecycle management variables
    private boolean isActivityDestroyed = false;
    private boolean isActivityPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply anti-flickering flags before setContentView
        ActivityTransitionHelper.applyAntiFlickeringFlags(this);
        ActivityTransitionHelper.setBackgroundColor(this, android.R.color.white);
        
        setContentView(R.layout.activity_student_progress_report);

        // Apply theme based on user type
        applyTheme();

        try {
            // Modern back press handling
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (call != null) {
                        call.cancel();
                    }
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            });

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Initialize Paper
            Paper.init(this);

            Avg_Marks = findViewById(R.id.Avg_Marks);
            session_filter = findViewById(R.id.session_filter);
            Avg_Position = findViewById(R.id.Avg_Position);
            Avg_Percentage = findViewById(R.id.Avg_Percentage);
            Avg_Attendance = findViewById(R.id.Avg_Attendance);
            search_filter = findViewById(R.id.search_filter);
            monthly_layout = findViewById(R.id.monthly_layout);
            filter_area = findViewById(R.id.filter_area);
            horizontal_scroll = findViewById(R.id.horizontal_scroll);
            show_advanced_filter = findViewById(R.id.show_advanced_filter);
            show_advanced_filter.setOnClickListener(this);

            mApiService = API.getAPIService();
            parent_id = Paper.book().read("parent_id");
            campus_id = Paper.book().read("campus_id");
            try {
            studentList = Paper.book().read("students");
        } catch (Exception e) {
            // If there's a serialization error, clear the corrupted data and start fresh
            Paper.book().delete("students");
            studentList = new ArrayList<>();
        }
            
            // Ensure studentList is not null
            if (studentList == null) {
                studentList = new ArrayList<>();
            }
            
            innitlization();
        } catch (Exception e) {
            Log.e("Report", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing report", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        
        // Hide progress bar when activity is paused
        if (progressBar != null && !isActivityDestroyed) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        
        // Cancel any ongoing network requests
        if (call != null) {
            call.cancel();
            call = null;
        }
        
        // Clear context reference
        context = null;
        
        // Dismiss any dialogs
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    /**
     * Check if the activity is still valid for UI operations
     */
    private boolean isActivityValid() {
        return !isActivityDestroyed && !isFinishing() && !isActivityPaused && context != null;
    }

    /**
     * Helper method to set UI visibility with null checks
     */
    private void setUIVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        
        if (myrecycleview != null) myrecycleview.setVisibility(visibility);
        if (subject_rcv != null) subject_rcv.setVisibility(visibility);
        if (test_rcv != null) test_rcv.setVisibility(visibility);
        if (subject_detail_rcv != null) subject_detail_rcv.setVisibility(visibility);
        if (Graph_rcv != null) Graph_rcv.setVisibility(visibility);
        if (marks_rcv != null) marks_rcv.setVisibility(visibility);
        if (percent_rcv != null) percent_rcv.setVisibility(visibility);
        if (class_pos_rcv != null) class_pos_rcv.setVisibility(visibility);

        if (marks_tv != null) marks_tv.setVisibility(visibility);
        if (graph_tv != null) graph_tv.setVisibility(visibility);
        if (percent_tv != null) percent_tv.setVisibility(visibility);
        if (class_pos_tv != null) class_pos_tv.setVisibility(visibility);

        if (subject_wise_report_tv != null) subject_wise_report_tv.setVisibility(visibility);
        if (month_wise_report_tv != null) month_wise_report_tv.setVisibility(visibility);
        if (monthly_layout != null) monthly_layout.setVisibility(visibility);

        if (barChart != null) barChart.setVisibility(visibility);
        if (subject_wise_tv != null) subject_wise_tv.setVisibility(visibility);
        if (month_wise_tv != null) month_wise_tv.setVisibility(visibility);
        if (month_wise_graph != null) month_wise_graph.setVisibility(visibility);
    }

    private void innitlization() {
        try {
            context = Report.this;
            Paper.init(context);

            // Initialize progress bar first
            progressBar = findViewById(R.id.progress_bar);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            // Initialize UI components with null checks
            back_icon = findViewById(R.id.back_icon);
            myrecycleview = findViewById(R.id.attendence_rcv);
            subject_rcv = findViewById(R.id.subject_rcv);
            test_rcv = findViewById(R.id.test_rcv);
            subject_detail_rcv = findViewById(R.id.subject_detail_rcv);
            Graph_rcv = findViewById(R.id.Graph_rcv);
            marks_rcv = findViewById(R.id.marks_rcv);
            percent_rcv = findViewById(R.id.percent_rcv);
            class_pos_rcv = findViewById(R.id.class_pos_rcv);

            // Initialize LayoutManagers for all RecyclerViews to prevent "No adapter attached" errors
            if (myrecycleview != null) {
                myrecycleview.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
                // Set an empty adapter initially
                myrecycleview.setAdapter(new MonthReportAdaptor(new ArrayList<>()));
            }
            
            if (subject_rcv != null) {
                subject_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (test_rcv != null) {
                test_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (subject_detail_rcv != null) {
                subject_detail_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (Graph_rcv != null) {
                Graph_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (marks_rcv != null) {
                marks_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (percent_rcv != null) {
                percent_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }
            
            if (class_pos_rcv != null) {
                class_pos_rcv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            }

            Selected_Child = findViewById(R.id.Selected_Child);
            Selected_Session = findViewById(R.id.Selected_Session);
            marks_tv = findViewById(R.id.marks_tv);
            graph_tv = findViewById(R.id.graph_tv);
            percent_tv = findViewById(R.id.percent_tv);
            class_pos_tv = findViewById(R.id.class_pos_tv);
            subject_wise_tv = findViewById(R.id.subject_wise_tv);
            barChart = findViewById(R.id.chart1);
            month_wise_tv = findViewById(R.id.month_wise_tv);
            subject_wise_report_tv = findViewById(R.id.subject_wise_report_tv);
            month_wise_graph = findViewById(R.id.month_wise_graph);
            month_wise_report_tv = findViewById(R.id.month_wise_report_tv);

            // Set initial visibility with null checks
            if (marks_tv != null) marks_tv.setVisibility(View.GONE);
            if (graph_tv != null) graph_tv.setVisibility(View.GONE);
            if (percent_tv != null) percent_tv.setVisibility(View.GONE);
            if (class_pos_tv != null) class_pos_tv.setVisibility(View.GONE);

            if (subject_wise_report_tv != null) subject_wise_report_tv.setVisibility(View.GONE);
            if (month_wise_report_tv != null) month_wise_report_tv.setVisibility(View.GONE);
            if (monthly_layout != null) monthly_layout.setVisibility(View.GONE);

            if (barChart != null) barChart.setVisibility(View.GONE);
            if (subject_wise_tv != null) subject_wise_tv.setVisibility(View.GONE);
            if (month_wise_tv != null) month_wise_tv.setVisibility(View.GONE);
            if (month_wise_graph != null) month_wise_graph.setVisibility(View.GONE);

            // Set click listener with null check
            if (back_icon != null) {
                back_icon.setOnClickListener(Report.this);
            }

            // Initialize student list with null checks
            List<String> student_name_list = new ArrayList<>();
            if (studentList != null && !studentList.isEmpty()) {
                for (int i = 0; i < studentList.size(); i++) {
                    SharedStudent student = studentList.get(i);
                    if (student != null && student.getFullName() != null) {
                        student_name_list.add(student.getFullName());
                    } else {
                        student_name_list.add("SharedStudent " + (i + 1));
                    }
                }
            }
            
            child_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                    student_name_list);

            // Check intent extras with proper null checks
            if (getIntent() != null && getIntent().hasExtra("child_id")) {
                // When Activity comes from notification
                String child_id = getIntent().getStringExtra("child_id");
                String examSession = getIntent().getStringExtra("examSession");
                
                if (child_id != null && !child_id.trim().isEmpty()) {
                    load_exam_report_local(parent_id, campus_id, child_id, examSession);
                } else {
                    Log.w("Report", "Invalid child_id in intent");
                    if (show_advanced_filter != null) {
                        show_advanced_filter.performClick();
                    }
                }
            } else {
                if (show_advanced_filter != null) {
                    show_advanced_filter.performClick();
                } else {
                    Log.e("Report", "show_advanced_filter is null");
                    Toast.makeText(context, "Error initializing filters", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("Report", "Error in innitlization", e);
            Toast.makeText(context, "Error initializing report", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void load_exam_report_api(final String parent_id,
                                      final String campus_id,
                                      final String student_id, final String exam_session_id) {
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("parent_parent_id", parent_id);
        postParam.put("parent_id", campus_id);
        postParam.put("student_id", student_id);
        postParam.put("exam_session_id", exam_session_id);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        // Show progress using modern approach
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        call = mApiService.load_exam(body);
        call.enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Call<ReportModel> call, retrofit2.Response<ReportModel> reportModel) {
                if (isActivityValid()) {
                    if (reportModel.body() != null) {
                        if (reportModel.body().getStatus().getCode().equals("1000")) {
                            String exam_key = exam_session_id + "+" + student_id + "+" + Constants.exam_key;
                            String month_key = exam_session_id + "+" + student_id + "+" + Constants.month_key;
                            String cp_key = exam_session_id + "+" + student_id + "+" + Constants.cp_key;
                            Paper.book().write(exam_key, reportModel.body().getExam());
                            Paper.book().write(month_key, reportModel.body().getMonth());
                            Paper.book().write(cp_key, reportModel.body().getCp());
                            monthList = reportModel.body().getMonth();
                            if (monthList.size() > 0) {
                                myrecycleview.setVisibility(View.VISIBLE);
                                subject_rcv.setVisibility(View.VISIBLE);
                                test_rcv.setVisibility(View.VISIBLE);
                                subject_detail_rcv.setVisibility(View.VISIBLE);
                                Graph_rcv.setVisibility(View.VISIBLE);
                                marks_rcv.setVisibility(View.VISIBLE);
                                percent_rcv.setVisibility(View.VISIBLE);
                                class_pos_rcv.setVisibility(View.VISIBLE);

                                marks_tv.setVisibility(View.VISIBLE);
                                graph_tv.setVisibility(View.VISIBLE);
                                percent_tv.setVisibility(View.VISIBLE);
                                class_pos_tv.setVisibility(View.VISIBLE);

                                subject_wise_report_tv.setVisibility(View.VISIBLE);
                                month_wise_report_tv.setVisibility(View.VISIBLE);
                                monthly_layout.setVisibility(View.VISIBLE);

                                barChart.setVisibility(View.VISIBLE);
                                subject_wise_tv.setVisibility(View.VISIBLE);
                                month_wise_tv.setVisibility(View.VISIBLE);
                                month_wise_graph.setVisibility(View.VISIBLE);

                                month_wise_report_tv.setVisibility(View.VISIBLE);
                                monthly_layout.setVisibility(View.VISIBLE);

                                MonthReportAdaptor attendenceAdaptor = new MonthReportAdaptor(
                                        monthList);

                                float obtained_marks = 0;
                                float total_marks = 0;
                                float percentage = 0;
                                float attendece = 0;
                                barEntries_month.clear();
                                month_name_list.clear();
                                for (int i = 0; i < monthList.size(); i++) {
                                    barEntries_month.add(new BarEntry(i, Float.parseFloat(monthList.get(i).getPercentage())));
                                    month_name_list.add(monthList.get(i).getMonthName());
                                    obtained_marks += monthList.get(i).getObtainedMarks();
                                    total_marks += monthList.get(i).getTotalMarks();
                                    percentage += Float.parseFloat(monthList.get(i).getPercentage());
                                    String attendence = monthList.get(i).getAttendance().replace("%", "");
                                    attendece += Float.parseFloat(attendence);
                                }

                                float avg_obtained_marks = obtained_marks / monthList.size();
                                float avg_total_marks = total_marks / monthList.size();
                                float avg_percentage = percentage / monthList.size();
                                float avg_attendece = attendece / monthList.size();
                                Avg_Position.setText(reportModel.body().getCp());
                                Avg_Marks.setText(String.format("%.2f", avg_obtained_marks) + "/" +
                                        String.format("%.2f", avg_total_marks));
                                Avg_Percentage.setText("" + String.format("%.2f", avg_percentage));
                                Avg_Attendance.setText("" + String.format("%.2f", avg_attendece));

                                myrecycleview.setAdapter(attendenceAdaptor);
                            } else {
                                no_record_found();

                                myrecycleview.setVisibility(View.GONE);
                                subject_rcv.setVisibility(View.GONE);
                                test_rcv.setVisibility(View.GONE);
                                subject_detail_rcv.setVisibility(View.GONE);
                                Graph_rcv.setVisibility(View.GONE);
                                marks_rcv.setVisibility(View.GONE);
                                percent_rcv.setVisibility(View.GONE);
                                class_pos_rcv.setVisibility(View.GONE);

                                marks_tv.setVisibility(View.GONE);
                                graph_tv.setVisibility(View.GONE);
                                percent_tv.setVisibility(View.GONE);
                                class_pos_tv.setVisibility(View.GONE);

                                subject_wise_report_tv.setVisibility(View.GONE);
                                month_wise_report_tv.setVisibility(View.GONE);
                                monthly_layout.setVisibility(View.GONE);

                                barChart.setVisibility(View.GONE);
                                subject_wise_tv.setVisibility(View.GONE);
                                month_wise_tv.setVisibility(View.GONE);
                                month_wise_graph.setVisibility(View.GONE);
                            }

                            examArrayList = reportModel.body().getExam();

                            if (examArrayList.size() > 0) {
                                subject_wise_report_tv.setVisibility(View.VISIBLE);

                                marks_tv.setVisibility(View.VISIBLE);
                                graph_tv.setVisibility(View.VISIBLE);
                                percent_tv.setVisibility(View.VISIBLE);
                                class_pos_tv.setVisibility(View.VISIBLE);

                                subject_name_list = new ArrayList<>();
                                for (int i = 0; i < examArrayList.size(); i++) {
                                    subject_name_list.add(examArrayList.get(i).getSubjectName());
                                }

                                // Subject Marks Total
                                List<String> marks_list = new ArrayList<>();
                                for (int i = 0; i < examArrayList.size(); i++) {
                                    marks_list.add(examArrayList.get(i).getObtainedMarks() + "/" +
                                            examArrayList.get(i).getTotalMarks());
                                }
                                SubjectReportAdaptor subjectReportAdaptor = new SubjectReportAdaptor(
                                        marks_list, Report.this);
                                marks_rcv.setAdapter(subjectReportAdaptor);
                                SubjectReportAdaptor marks_adaptor = new SubjectReportAdaptor(
                                        subject_name_list, Report.this);
                                subject_rcv.setAdapter(marks_adaptor);

                                /*--------------------------------------------*/
                                // Percentage  Total
                                List<String> percent_list = new ArrayList<>();
                                barEntries_subject.clear();
                                for (int i = 0; i < examArrayList.size(); i++) {
                                    percent_list.add(examArrayList.get(i).getPercentage());
                                    barEntries_subject.add(new BarEntry(i, Float.parseFloat(examArrayList.get(i).getPercentage())));
                                }

                                SubjectReportAdaptor percent_adaptor = new SubjectReportAdaptor(
                                        percent_list, Report.this);
                                percent_rcv.setAdapter(percent_adaptor);

                                /*--------------------------------------------*/
                                // Graps Marks Total

                                GraphAdaptor graphAdaptor = new GraphAdaptor(
                                        percent_list, Report.this);
                                Graph_rcv.setAdapter(graphAdaptor);

                                /*--------------------------------------------*/
                                // Class Position  Total
                                List<String> class_position_list = new ArrayList<>();
                                for (int i = 0; i < examArrayList.size(); i++) {
                                    class_position_list.add("" + examArrayList.get(i).getCp());
                                }

                                SubjectReportAdaptor class_pos_adaptor = new SubjectReportAdaptor(
                                        class_position_list, Report.this);
                                class_pos_rcv.setAdapter(class_pos_adaptor);

                                List<Integer> test_list_size = new ArrayList<>();
                                for (int i = 0; i < examArrayList.size(); i++) {
                                    test_list_size.add(examArrayList.get(i).getDetail().size());
                                }

                                int largest = Collections.max(test_list_size);
                                int index_term_list = -1;

                                for (int i = 0; i < test_list_size.size(); i++) {
                                    if (test_list_size.get(i) == largest) {
                                        index_term_list = i;
                                    }
                                }
                                if (index_term_list != -1) {
                                    test_name_list = new ArrayList<>();
                                    for (int i = 0; i < examArrayList.get(index_term_list).getDetail().size(); i++) {
                                        test_name_list.add(examArrayList.get(index_term_list).getDetail().get(i).getExamName());
                                    }
                                    TestAdapto testAdapto = new TestAdapto(
                                            test_name_list);
                                    test_rcv.setAdapter(testAdapto);
                                }

                                SubjectDetailMainAdaptor testAdapto = new SubjectDetailMainAdaptor(
                                        examArrayList, Report.this);
                                subject_detail_rcv.setAdapter(testAdapto);

                                /*--------------------------------------------*/

                                // Subject  Grapht CHat

                                barChart.setVisibility(View.VISIBLE);
                                subject_wise_tv.setVisibility(View.VISIBLE);

                                leftAxis = barChart.getAxisLeft();

                                leftAxis.setAxisMinimum(0f);
                                barChart.getDescription().setEnabled(false);

                                xAxisFormatter = new DayAxisValueFormatter(barChart, subject_name_list);

                                xAxis = barChart.getXAxis();
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setDrawGridLines(false);
                                xAxis.setGranularity(1f); // only intervals of 1 day
                                xAxis.setLabelCount(7);
                                xAxis.setTextSize(10f);

                                xAxis.setValueFormatter(xAxisFormatter);
                                barDataSet = new BarDataSet(barEntries_subject, "");
                                barData = new BarData(barDataSet);
                                barChart.setData(barData);
                                barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                                barDataSet.setValueTextColor(Color.BLACK);
                                barChart.setDoubleTapToZoomEnabled(false);
                                barDataSet.setValueTextSize(18f);
                                mv = new XYMarkerView(Report.this, xAxisFormatter);
                                mv.setChartView(barChart); // For bounds control
                                barChart.setMarker(mv);

                                /*--------------------------------------------*/

                                // Month Wise Graph

                                month_wise_tv.setVisibility(View.VISIBLE);
                                month_wise_graph.setVisibility(View.VISIBLE);
                                leftAxis = month_wise_graph.getAxisLeft();
                                leftAxis.setAxisMinimum(0f);

                                month_wise_graph.getDescription().setEnabled(false);

                                xAxisFormatter = new DayAxisValueFormatter(month_wise_graph, month_name_list);

                                xAxis = month_wise_graph.getXAxis();
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setDrawGridLines(false);
                                xAxis.setGranularity(1f); // only intervals of 1 day
                                xAxis.setLabelCount(7);
                                xAxis.setTextSize(10f);

                                xAxis.setValueFormatter(xAxisFormatter);
                                barDataSet = new BarDataSet(barEntries_month, "");
                                barData = new BarData(barDataSet);
                                month_wise_graph.setData(barData);
                                barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                                barDataSet.setValueTextColor(Color.BLACK);
                                month_wise_graph.setDoubleTapToZoomEnabled(false);
                                barDataSet.setValueTextSize(18f);
                                mv = new XYMarkerView(Report.this, xAxisFormatter);
                                mv.setChartView(month_wise_graph); // For bounds control
                                month_wise_graph.setMarker(mv);
                            }

                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, reportModel.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, reportModel.raw().message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReportModel> call, Throwable e) {
                if (isActivityValid()) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void load_exam_report_local(final String parent_id,
                                        final String campus_id, final String student_id, final String exam_session_id) {
        try {
            // Set selected child and session names with null checks
            if (Selected_Child != null) {
                Selected_Child.setText(seleted_child_name != null ? seleted_child_name : "Selected Child");
            }
            if (Selected_Session != null) {
                Selected_Session.setText(seleted_exam_session_name != null ? seleted_exam_session_name : "Selected Session");
            }
            
            String exam_key = exam_session_id + "+" + student_id + "+" + Constants.exam_key;
            String month_key = exam_session_id + "+" + student_id + "+" + Constants.month_key;
            String cp_key = exam_session_id + "+" + student_id + "+" + Constants.cp_key;

            if (Paper.book().contains(exam_key)) {
                examArrayList = Paper.book().read(exam_key);
                monthList = Paper.book().read(month_key);
                String Avg_Position_s = Paper.book().read(cp_key);

                // Ensure lists are not null
                if (examArrayList == null) examArrayList = new ArrayList<>();
                if (monthList == null) monthList = new ArrayList<>();

                if (monthList.size() > 0) {
                    // Show all UI elements with null checks
                    setUIVisibility(true);
                    
                    MonthReportAdaptor attendenceAdaptor = new MonthReportAdaptor(monthList);

                    float obtained_marks = 0;
                    float total_marks = 0;
                    float percentage = 0;
                    float attendece = 0;
                    barEntries_month.clear();
                    month_name_list.clear();
                    
                    for (int i = 0; i < monthList.size(); i++) {
                        ReportModel.Month month = monthList.get(i);
                        if (month != null) {
                            try {
                                barEntries_month.add(new BarEntry(i, Float.parseFloat(month.getPercentage())));
                                month_name_list.add(month.getMonthName());
                                obtained_marks += month.getObtainedMarks();
                                total_marks += month.getTotalMarks();
                                percentage += Float.parseFloat(month.getPercentage());
                                String attendance = month.getAttendance();
                                if (attendance != null) {
                                    attendance = attendance.replace("%", "");
                                    attendece += Float.parseFloat(attendance);
                                }
                            } catch (NumberFormatException e) {
                                Log.e("Report", "Error parsing month data at index " + i, e);
                            }
                        }
                    }

                    float avg_obtained_marks = obtained_marks / monthList.size();
                    float avg_total_marks = total_marks / monthList.size();
                    float avg_percentage = percentage / monthList.size();
                    float avg_attendece = attendece / monthList.size();
                    
                    // Update average displays with null checks
                    if (Avg_Position != null) Avg_Position.setText(Avg_Position_s);
                    if (Avg_Marks != null) Avg_Marks.setText(String.format("%.2f", avg_obtained_marks) + "/" + String.format("%.2f", avg_total_marks));
                    if (Avg_Percentage != null) Avg_Percentage.setText("" + String.format("%.2f", avg_percentage));
                    if (Avg_Attendance != null) Avg_Attendance.setText("" + String.format("%.2f", avg_attendece));

                    if (myrecycleview != null) {
                        myrecycleview.setAdapter(attendenceAdaptor);
                    }
                } else {
                    no_record_found();
                    setUIVisibility(false);
                }

                if (examArrayList.size() > 0) {
                    if (subject_wise_report_tv != null) subject_wise_report_tv.setVisibility(View.VISIBLE);
                    if (marks_tv != null) marks_tv.setVisibility(View.VISIBLE);
                    if (graph_tv != null) graph_tv.setVisibility(View.VISIBLE);
                    if (percent_tv != null) percent_tv.setVisibility(View.VISIBLE);
                    if (class_pos_tv != null) class_pos_tv.setVisibility(View.VISIBLE);

                    subject_name_list = new ArrayList<>();
                    for (int i = 0; i < examArrayList.size(); i++) {
                        ReportModel.Exam exam = examArrayList.get(i);
                        if (exam != null && exam.getSubjectName() != null) {
                            subject_name_list.add(exam.getSubjectName());
                        }
                    }

                    // Subject Marks Total
                    List<String> marks_list = new ArrayList<>();
                    for (int i = 0; i < examArrayList.size(); i++) {
                        ReportModel.Exam exam = examArrayList.get(i);
                        if (exam != null) {
                            marks_list.add(exam.getObtainedMarks() + "/" + exam.getTotalMarks());
                        }
                    }
                    
                    if (marks_rcv != null) {
                        SubjectReportAdaptor subjectReportAdaptor = new SubjectReportAdaptor(marks_list, Report.this);
                        marks_rcv.setAdapter(subjectReportAdaptor);
                    }
                    
                    if (subject_rcv != null) {
                        SubjectReportAdaptor marks_adaptor = new SubjectReportAdaptor(subject_name_list, Report.this);
                        subject_rcv.setAdapter(marks_adaptor);
                    }

                    // Percentage Total
                    List<String> percent_list = new ArrayList<>();
                    barEntries_subject.clear();
                    for (int i = 0; i < examArrayList.size(); i++) {
                        ReportModel.Exam exam = examArrayList.get(i);
                        if (exam != null && exam.getPercentage() != null) {
                            percent_list.add(exam.getPercentage());
                            try {
                                barEntries_subject.add(new BarEntry(i, Float.parseFloat(exam.getPercentage())));
                            } catch (NumberFormatException e) {
                                Log.e("Report", "Error parsing percentage for exam at index " + i, e);
                            }
                        }
                    }

                    if (percent_rcv != null) {
                        SubjectReportAdaptor percent_adaptor = new SubjectReportAdaptor(percent_list, Report.this);
                        percent_rcv.setAdapter(percent_adaptor);
                    }

                    // Graph Marks Total
                    if (Graph_rcv != null) {
                        GraphAdaptor graphAdaptor = new GraphAdaptor(percent_list, Report.this);
                        Graph_rcv.setAdapter(graphAdaptor);
                    }

                    // Class Position Total
                    List<String> class_position_list = new ArrayList<>();
                    for (int i = 0; i < examArrayList.size(); i++) {
                        ReportModel.Exam exam = examArrayList.get(i);
                        if (exam != null) {
                            class_position_list.add("" + exam.getCp());
                        }
                    }

                    if (class_pos_rcv != null) {
                        SubjectReportAdaptor class_pos_adaptor = new SubjectReportAdaptor(class_position_list, Report.this);
                        class_pos_rcv.setAdapter(class_pos_adaptor);
                    }

                    List<Integer> test_list_size = new ArrayList<>();
                    for (int i = 0; i < examArrayList.size(); i++) {
                        ReportModel.Exam exam = examArrayList.get(i);
                        if (exam != null && exam.getDetail() != null) {
                            test_list_size.add(exam.getDetail().size());
                        } else {
                            test_list_size.add(0);
                        }
                    }

                    if (!test_list_size.isEmpty()) {
                        int largest = Collections.max(test_list_size);
                        int index_term_list = -1;

                        for (int i = 0; i < test_list_size.size(); i++) {
                            if (test_list_size.get(i) == largest) {
                                index_term_list = i;
                            }
                        }
                        
                        if (index_term_list != -1 && examArrayList.get(index_term_list) != null && 
                            examArrayList.get(index_term_list).getDetail() != null) {
                            test_name_list = new ArrayList<>();
                            for (int i = 0; i < examArrayList.get(index_term_list).getDetail().size(); i++) {
                                ReportModel.Detail detail = examArrayList.get(index_term_list).getDetail().get(i);
                                if (detail != null && detail.getExamName() != null) {
                                    test_name_list.add(detail.getExamName());
                                }
                            }
                    }
                    TestAdapto testAdapto = new TestAdapto(
                            test_name_list);
                    test_rcv.setAdapter(testAdapto);
                }

                SubjectDetailMainAdaptor testAdapto = new SubjectDetailMainAdaptor(
                        examArrayList, Report.this);
                subject_detail_rcv.setAdapter(testAdapto);

                // Subject  Grapht CHat

                barChart.setVisibility(View.VISIBLE);
                subject_wise_tv.setVisibility(View.VISIBLE);

                leftAxis = barChart.getAxisLeft();

                leftAxis.setAxisMinimum(0f);
                barChart.getDescription().setEnabled(false);

                xAxisFormatter = new DayAxisValueFormatter(barChart, subject_name_list);

                xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f); // only intervals of 1 day
                xAxis.setLabelCount(7);
                xAxis.setTextSize(10f);

                xAxis.setValueFormatter(xAxisFormatter);
                barDataSet = new BarDataSet(barEntries_subject, "");
                barData = new BarData(barDataSet);
                barChart.setData(barData);
                barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                barChart.setDoubleTapToZoomEnabled(false);
                barDataSet.setValueTextSize(18f);
                mv = new XYMarkerView(Report.this, xAxisFormatter);
                mv.setChartView(barChart); // For bounds control
                barChart.setMarker(mv);

                // Month Wise Graph

                month_wise_tv.setVisibility(View.VISIBLE);
                month_wise_graph.setVisibility(View.VISIBLE);
                leftAxis = month_wise_graph.getAxisLeft();
                leftAxis.setAxisMinimum(0f);

                month_wise_graph.getDescription().setEnabled(false);

                xAxisFormatter = new DayAxisValueFormatter(month_wise_graph, month_name_list);

                xAxis = month_wise_graph.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setGranularity(1f); // only intervals of 1 day
                xAxis.setLabelCount(7);
                xAxis.setTextSize(10f);

                xAxis.setValueFormatter(xAxisFormatter);
                barDataSet = new BarDataSet(barEntries_month, "");
                barData = new BarData(barDataSet);
                month_wise_graph.setData(barData);
                barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                barDataSet.setValueTextColor(Color.BLACK);
                month_wise_graph.setDoubleTapToZoomEnabled(false);
                barDataSet.setValueTextSize(18f);
                mv = new XYMarkerView(Report.this, xAxisFormatter);
                mv.setChartView(month_wise_graph); // For bounds control
                month_wise_graph.setMarker(mv);
            }
        } else {
            load_exam_report_api(parent_id, campus_id, seleted_child_id, seleted_exam_session);
        }
        } catch (Exception e) {
            Log.e("Report", "Error in load_exam_report_local", e);
            Toast.makeText(context, "Error loading report data", Toast.LENGTH_SHORT).show();
            no_record_found();
        }
    }

    private void no_record_found() {
        Toast.makeText(context, "No Record Fonud.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(View view, int position) {
        String Subjcet_Name = "Subject Name: " + subject_name_list.get(position);
        Toast.makeText(context, Subjcet_Name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.search_filter) {
            if (alertDialog != null) {
            alertDialog.dismiss();
            }
            load_exam_report_local(parent_id, campus_id, seleted_child_id, seleted_exam_session);
        } else if (id == R.id.back_icon) {
            finish();
        } else if (id == R.id.Cancel) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        } else if (id == R.id.show_advanced_filter) {
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.progress_report_advanced_search_layout, null);

            select_child_spinner = dialogView.findViewById(R.id.select_child_spinner);
            dialog_search_filter = dialogView.findViewById(R.id.search_filter);
            select_examp_session = dialogView.findViewById(R.id.select_examp_session);
            session_layout = dialogView.findViewById(R.id.session_layout);
            Cancel = dialogView.findViewById(R.id.Cancel);
            if (Cancel != null) {
                Cancel.setOnClickListener(Report.this);
            }

            select_child_spinner.setTitle("Select Child");
            // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable

            select_examp_session.setTitle("Select Exam Session");
            // Removed setPositiveButton("OK") to enable auto-dismiss behavior like staff timetable
            dialog_search_filter.setOnClickListener(Report.this);

            select_child_spinner.setAdapter(child_adaptor);

            select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    seleted_child_id = studentList.get(position).getUniqueId();
                    seleted_child_name = studentList.get(position).getFullName();
                    load_exam_session(campus_id, seleted_child_id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(dialogView);
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    }

    private void load_exam_session(
            final String campus_id, String child_id) {
        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("parent_id", campus_id);
        postParam.put("employee_id", child_id);
        postParam.put("session_id", Constant.current_session);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));
        Constant.mApiService.load_exam_session(body).enqueue(new Callback<SessionModel>() {
            @Override
            public void onResponse(Call<SessionModel> call, retrofit2.Response<SessionModel> reportModel) {
                if (reportModel.body() != null) {
                    if (reportModel.body().getStatus().getCode().equals("1000")) {
                        examSessionslist = reportModel.body().getExamSession();
                        for (int i = 0; i < examSessionslist.size(); i++) {
                            exam_session_name_list.add(examSessionslist.get(i).getFullName());
                        }

                        if (examSessionslist.size() > 0) {
                            if (examSessionslist.size() > 1) {
                                session_layout.setVisibility(View.VISIBLE);

                                session_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                                        exam_session_name_list);

                                select_examp_session.setAdapter(session_adaptor);

                                select_examp_session.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        seleted_exam_session = examSessionslist.get(position).getUniqueId();
                                        seleted_exam_session_name = examSessionslist.get(position).getFullName();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                            } else {
                                session_layout.setVisibility(View.GONE);
                                seleted_exam_session = examSessionslist.get(0).getUniqueId();
                                seleted_exam_session_name = examSessionslist.get(0).getFullName();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SessionModel> call, Throwable e) {
                if (isActivityValid()) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position, int position2) {
        String Subjcet_Name = "Subject Name: " + subject_name_list.get(position2);
        String Test_Name = "Test Name: " + test_name_list.get(position);
        String Message = Subjcet_Name + "\n" + Test_Name;
        Toast.makeText(context, Message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Apply theme based on user type
     */
    private void applyTheme() {
        try {
            String userType = Paper.book().read(Constants.User_Type, "");
            Log.d("Report", "User Type: " + userType);
            
            if (userType != null && userType.equals("STUDENT")) {
                // Apply student theme (teal) when accessed from student context
                ThemeHelper.applyStudentTheme(this);
                
                // Apply system bars theme (status bar and navigation bar)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.student_primary));
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.student_primary));
                }
                
                Log.d("Report", "Student theme applied");
            } else {
                // Apply unified parent theme for report page
                ParentThemeHelper.applyParentTheme(this, 120); // 120dp for full-screen pages
                ParentThemeHelper.setHeaderIconVisibility(this, false); // No icon for report
                ParentThemeHelper.setMoreOptionsVisibility(this, true); // Show more options for report (export, share)
                ParentThemeHelper.setFooterVisibility(this, true); // Show footer
                ParentThemeHelper.setHeaderTitle(this, "Progress Report");
                
                Log.d("Report", "Parent theme applied");
            }
            
            // Apply footer theme
            ThemeHelper.applyFooterTheme(this, userType);
            
        } catch (Exception e) {
            Log.e("Report", "Error applying theme", e);
        }
    }
}
