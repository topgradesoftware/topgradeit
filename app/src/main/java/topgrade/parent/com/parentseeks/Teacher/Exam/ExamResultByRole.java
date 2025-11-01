package topgrade.parent.com.parentseeks.Teacher.Exam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.R;

public class ExamResultByRole extends AppCompatActivity {

    private ImageView ivBack;
    ProgressBar progressBar;
    String parent_id;
    String campus_id;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_by_user_role);

        init();
        listeners();
    }

    private void listeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Class InCharge
        findViewById(R.id.class_incharge_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExamResultByRole.this, ExamSubmit.class);
                intent.putExtra("role", "class_incharge");
                startActivity(intent);
            }
        });

        // Section InCharge
        findViewById(R.id.section_incharge_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExamResultByRole.this, ExamSubmit.class);
                intent.putExtra("role", "section_incharge");
                startActivity(intent);
            }
        });

        // Subject Teacher
        findViewById(R.id.subject_teacher_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExamResultByRole.this, ExamSubmit.class);
                intent.putExtra("role", "subject_teacher");
                startActivity(intent);
            }
        });
    }

    private void init(){
        progressBar = findViewById(R.id.progress_bar);
        Paper.init(ExamResultByRole.this);
        context = ExamResultByRole.this;
        parent_id = Paper.book().read("parent_id");
        campus_id = Paper.book().read("campus_id");
        ivBack = findViewById(R.id.iv_back_icon);
    }
}
