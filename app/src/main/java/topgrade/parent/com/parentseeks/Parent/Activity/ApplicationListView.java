//package seeks.parent.com.parentseeks.Parent.Activity.Application;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import components.searchablespinnerlibrary.SearchableSpinner;
//
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import io.paperdb.Paper;
//import okhttp3.MediaType;
//import okhttp3.RequestBody;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import seeks.parent.com.parentseeks.Parent.Adaptor.ParentApplicationAdapter;
//import seeks.parent.com.parentseeks.Parent.Interface.OnClickListener;
//import seeks.parent.com.parentseeks.Parent.Model.SharedStudent;
//import seeks.parent.com.parentseeks.R;
//import seeks.parent.com.parentseeks.Teacher.Activites.Model.StaffApplication;
//import seeks.parent.com.parentseeks.Teacher.Activites.Model.StaffApplicationModel;
//import seeks.parent.com.parentseeks.Teacher.Activites.Utils.Constant;
//
//public class ApplicationListView extends AppCompatActivity implements OnClickListener {
//
//
//    FloatingActionButton fab;
//    private ProgressBar progress_bar;
//    Context context;
//    RecyclerView feedback_rcv;
//
//    TextView Application_list;
//
//    List<StaffApplication> title_list = new ArrayList<>();
//
//    String titleId;
//    String body;
//
//    SearchableSpinner select_child_spinner;
//    ArrayAdapter<String> child_adaptor;
//
//    List<String> student_name_list = new ArrayList<>();
//    List<SharedStudent> studentList = new ArrayList<>();
//
//    String seleted_child_id = "";
//    String parent_id;
//    String campus_id;
//
//    TextView no_records_text;
//
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_parent_application_view);
//        context = ApplicationListView.this;
//        Paper.init(context);
//        progress_bar = findViewById(R.id.progress_bar);
//        feedback_rcv = findViewById(R.id.feedback_rcv);
//        fab = findViewById(R.id.fab);
//
//        no_records_text = findViewById(R.id.no_records_text);
//
//        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // Handle the refresh event
//                read_title();
//            }
//        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(ApplicationListView.this, ParentAddApplication.class));
//            }
//        });
//
//
//        ImageView back_icon = findViewById(R.id.back_icon);
//        back_icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//
//
//        innitlization();
//        studentList = Paper.book().read("students");
//        for (int i = 0; i < studentList.size(); i++) {
//            student_name_list.add(studentList.get(i).getFullName());
//        }
//        Load_child();
//
//    }
//
//    private void innitlization() {
//
//
//        context = ApplicationListView.this;
//        Paper.init(context);
//
//        progress_bar = findViewById(R.id.progress_bar);
//        feedback_rcv = findViewById(R.id.feedback_rcv);
//        Application_list = findViewById(R.id.Application_list);
//        select_child_spinner = findViewById(R.id.select_child_spinner);
//
//        select_child_spinner.setTitle("Select Child");
//        select_child_spinner.setPositiveButton("OK");
//
//        parent_id = Paper.book().read("parent_id");
//        campus_id = Paper.book().read("campus_id");
//    }
//
//    private void Load_child() {
//        child_adaptor = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
//                student_name_list);
//        select_child_spinner.setAdapter(child_adaptor);
//
//
//        if (getIntent().hasExtra("child_id")) {
//            String child_id = getIntent().getStringExtra("child_id");
//            int index = -1;
//            for (int i = 0; i < studentList.size(); i++) {
//                if (studentList.get(i).getUniqueId().equals(child_id)) {
//                    index = i;
//                }
//            }
//            if (index > -1) {
//                select_child_spinner.setSelection(index);
//            }
//
//        }
//        select_child_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                seleted_child_id = studentList.get(position).getUniqueId();
//                read_title();
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        read_title();
//    }
//
//    private void read_title() {
//        HashMap<String, String> postParam = new HashMap<String, String>();
//        postParam.put("student_id", seleted_child_id);
//        postParam.put("campus_id", Constant.campus_id);
//        postParam.put("operation", "read_application_title");
//
//        progress_bar.setVisibility(View.VISIBLE);
//        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
//                (new JSONObject(postParam)).toString());
//
//        Constant.mApiService.leave_applicaton_parent(body).enqueue(new Callback<StaffApplicationModel>() {
//            @Override
//            public void onResponse(Call<StaffApplicationModel> call, Response<StaffApplicationModel> response) {
//                swipeRefreshLayout.setRefreshing(false);
//                progress_bar.setVisibility(View.GONE);
//
//                if (response.isSuccessful()) {
//                    StaffApplicationModel staffApplicationModel = response.body();
//                    if (staffApplicationModel != null && staffApplicationModel.getStatus() != null) {
//                        if (staffApplicationModel.getStatus().getCode().equals("1000")) {
//                            // Data is available and has status code 1000
//                            title_list = staffApplicationModel.getTitle();
//                            feedback_rcv.setAdapter(new ParentApplicationAdapter(title_list, ApplicationListView.this));
//
//                            // Hide the "no_records_text" since data is available
//                            no_records_text.setVisibility(View.GONE);
//                        } else {
//                            // Data is not available or has a status code other than 1000
//                            // Show the "no_records_text"
//                            no_records_text.setVisibility(View.VISIBLE);
//                            Toast.makeText(context, staffApplicationModel.getStatus().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // Data is null or status is null, show "no_records_text"
//                        no_records_text.setVisibility(View.VISIBLE);
//                        Toast.makeText(context, "Response body is empty or invalid.", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    // Response is not successful, show "no_records_text"
//                    no_records_text.setVisibility(View.VISIBLE);
//                    Toast.makeText(context, "Response was not successful.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<StaffApplicationModel> call, Throwable e) {
//                e.printStackTrace();
//                progress_bar.setVisibility(View.GONE);
//                no_records_text.setVisibility(View.VISIBLE);
//            }
//        });
//    }
//
//
//    @Override
//    public void onItemClick(View view, int position) {
//        pop_uop_menu(view, position);
//    }
//
//    private void pop_uop_menu(View view, final int position) {
//        PopupMenu popup = new PopupMenu(context, view);
//        popup.getMenuInflater().inflate(R.menu.feedback_menu, popup.getMenu());
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//
//                int id = item.getItemId();
//                if (id == R.id.Delete) {
//
//
//                    titleId = "" + title_list.get(position).getTitleId();
//                    body = title_list.get(position).getBody();
//
//                }
//
//
//                return true;
//            }
//        });
//
//        popup.show();
//    }
//
//}
