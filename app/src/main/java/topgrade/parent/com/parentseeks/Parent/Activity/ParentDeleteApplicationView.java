//package seeks.parent.com.parentseeks.Parent.Activity.Application;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import org.json.JSONObject;
//
//import java.util.HashMap;
//
//import io.paperdb.Paper;
//import okhttp3.MediaType;
//import okhttp3.RequestBody;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import seeks.parent.com.parentseeks.R;
//import seeks.parent.com.parentseeks.Teacher.Activites.Model.StaffApplicationModel;
//import seeks.parent.com.parentseeks.Teacher.Activites.Utils.Constant;
//
//public class ParentDeleteApplicationView extends AppCompatActivity {
//    private ProgressBar progress_bar;
//
//    private ImageView back_icon;
//    Context context;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_staff_application_delete_view);
//        context = ParentDeleteApplicationView.this;
//        Paper.init(context);
//        progress_bar = findViewById(R.id.progress_bar);
//        ImageView back_icon = findViewById(R.id.back_icon);
//        back_icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        delete_application();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        delete_application();
//    }
//
//    private void delete_application() {
//        String title_id = getIntent().getStringExtra("title_id");
//        String student_id = getIntent().getStringExtra("student_id");
//
//        HashMap<String, String> postParam = new HashMap<>();
//        postParam.put("student_id", student_id);
//        postParam.put("campus_id", Constant.campus_id);
//        postParam.put("operation", "delete_application");
//        postParam.put("title_id", title_id);
//
//        progress_bar.setVisibility(View.VISIBLE);
//        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
//                (new JSONObject(postParam)).toString());
//
//        Constant.mApiService.leave_applicaton_parent(body).enqueue(new Callback<StaffApplicationModel>() {
//            @Override
//            public void onResponse(Call<StaffApplicationModel> call, Response<StaffApplicationModel> response) {
//                if (response.body() != null) {
//                    if (response.body() != null && response.body().getStatus() != null && response.body().getStatus().getCode().equals("1000")) {
//                        progress_bar.setVisibility(View.GONE);
//                        Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
//                        finish(); // Automatically go back to the previous activity after successful delete
//                    } else {
//                        progress_bar.setVisibility(View.GONE);
//                        Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                } else {
//                    progress_bar.setVisibility(View.GONE);
//                    finish();
//                    Toast.makeText(context, response.raw().message(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<StaffApplicationModel> call, Throwable e) {
//                e.printStackTrace();
//                progress_bar.setVisibility(View.GONE);
//                finish();
//            }
//        });
//    }
//
//
//
//    private void navigateToApplicationListView() {
////        Intent intent = new Intent(ParentDeleteApplicationView.this, ApplicationListView.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        startActivity(intent);
////        finish();
//    }
//}
