package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.MarksEnterInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamResult;
import topgrade.parent.com.parentseeks.Teacher.Model.ExamSubmitStrcu;

public class ExamAdaptor extends RecyclerView.Adapter<ExamAdaptor.Holder> {


    List<ExamResult> list;
    View v;
    Context context;
    Activity activity;
    MarksEnterInterface marksEnterInterface;
    OnClickListener onClickListener;
    String total_marks;
    SmsCheck smsCheck;
    List<ExamSubmitStrcu> exam_submit_list;

    public ExamAdaptor(Context context, Activity activity, List<ExamResult> list,
                       List<ExamSubmitStrcu> exam_submit_list,
                       MarksEnterInterface marksEnterInterface, OnClickListener onClickListener, String total_marks,
                       SmsCheck smsCheck
    ) {
        this.list = list;
        this.exam_submit_list = exam_submit_list;
        this.activity = activity;
        this.context = context;
        this.marksEnterInterface = marksEnterInterface;
        this.onClickListener = onClickListener;
        this.total_marks = total_marks;
        this.smsCheck = smsCheck;


    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.exam_submit_layout, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {

        ExamResult exam = list.get(position);


        if (position % 2 != 0)
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        else
            holder.row.setBackgroundColor(Color.WHITE);


        holder.roll_number.setText("" + exam.getRollNumber());
        holder.student_name.setText(exam.getFullName());
        holder.total_marks.setText(total_marks);
        if (exam_submit_list.get(position).getMarks().equals("0")) {
            holder.obtained_marks.setBackgroundColor(Color.parseColor("#DF4242"));//RED
            holder.obtained_marks.setTextColor(Color.parseColor("#ffffff"));// white
        } else {
            holder.obtained_marks.setBackgroundColor(Color.parseColor("#ffffff"));//RED
            holder.obtained_marks.setTextColor(Color.parseColor("#000000"));// white
        }
        holder.obtained_marks.setText(exam_submit_list.get(position).getMarks());

        holder.sms_check.setChecked(exam_submit_list.get(position).getSms().equals("1"));
        holder.sms_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    smsCheck.OnSmsCheck(b, adapterPosition);
                }
            }
        });


        holder.obtained_marks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    marksEnterInterface.OnMarksEnter(adapterPosition, s.toString());
                }
            }
        });
     /*   holder.absent_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infterface.ExamSubmit(v, position, holder);

;
            }
        });

        holder.presnt_exam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infterface.ExamSubmit(v, position, holder);

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public TextView roll_number, student_name, exam_note, total_marks;
        public EditText obtained_marks;
        public CheckBox sms_check;
        public LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);

            sms_check = itemView.findViewById(R.id.sms_check);
            roll_number = itemView.findViewById(R.id.roll_number);
            obtained_marks = itemView.findViewById(R.id.obtained_marks);
            total_marks = itemView.findViewById(R.id.total_marks);
            student_name = itemView.findViewById(R.id.student_name);
            exam_note = itemView.findViewById(R.id.attendence_note);

        }

    }
}
