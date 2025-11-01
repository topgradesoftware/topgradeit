package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.AttendanceSubmitInterface;
import topgrade.parent.com.parentseeks.Teacher.Interface.SmsCheck;
import topgrade.parent.com.parentseeks.Teacher.Model.AttendanceSubmitModel;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;

public class StaffAttendanceSubmitAdapter extends RecyclerView.Adapter<StaffAttendanceSubmitAdapter.AttendanceSubmitViewHolder> {


    List<StudentListSigel> list;
    View v;
    Context context;
    List<String> attendence_student = new ArrayList<>();
    ArrayAdapter<String> attendece_status_spinner;
    AttendanceSubmitInterface attendenceSubmitInterface;
    List<AttendanceSubmitModel> attendanceSubmitModels;
    SmsCheck smsCheck;

    public StaffAttendanceSubmitAdapter(Context context, List<StudentListSigel> list,

                                   AttendanceSubmitInterface attendenceSubmitInterface,
                                   List<AttendanceSubmitModel> attendanceSubmitModels,
                                   SmsCheck smsCheck
    ) {
        this.list = list;
        this.context = context;
        this.attendenceSubmitInterface = attendenceSubmitInterface;
        this.attendanceSubmitModels = attendanceSubmitModels;
        this.smsCheck = smsCheck;


        attendece_status_spinner = new ArrayAdapter<String>(
                context, R.layout.simple_list_item_1, attendence_student) {
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setBackgroundColor(Color.parseColor("#F5F5F5"));

                return view;
            }
        };

    }


    @NonNull
    @Override
    public AttendanceSubmitViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance_submit_layout, viewGroup, false);
        return new AttendanceSubmitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceSubmitViewHolder holder, int position) {

        StudentListSigel attendence = list.get(position);
        holder.roll_number.setText(attendence.getRollNumber());
        holder.student_name.setText(attendence.getFullName());
        holder.attendence_note.setText(attendanceSubmitModels.get(position).getNote());

        String attendence_status = attendanceSubmitModels.get(position).getAttendance();
        switch (attendence_status) {
            case "1":
                holder.attendence_status.setText("Pre");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#ffffff"));//RED
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// white
                break;
            case "2":
                holder.attendence_status.setText("Abs");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#DF4242"));//RED
                holder.attendence_status.setTextColor(Color.parseColor("#ffffff"));// white
                break;
            case "3":
                holder.attendence_status.setText("H.lev");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90"));// Green
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// white
                break;
            case "4":
                holder.attendence_status.setText("F.lev");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90"));// Green
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// white
                break;

        }

        holder.attendence_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    attendenceSubmitInterface.StatusSubmit(view, adapterPosition,
                            holder.attendence_status.getText().toString(), holder);
                }
            }
        });


        holder.sms_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    smsCheck.OnSmsCheck(b, adapterPosition);
                }
            }
        });

        holder.attendence_note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    attendenceSubmitInterface.NoteSubmit(adapterPosition, s.toString());
                }
            }
        });

        holder.sms_check.setChecked(attendanceSubmitModels.get(position).getSms().equals("1"));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AttendanceSubmitViewHolder extends RecyclerView.ViewHolder {

        public TextView roll_number, student_name, attendence_note;
        public CheckBox sms_check;
        public TextView attendence_status;
        public LinearLayout row;

        AttendanceSubmitViewHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            sms_check = itemView.findViewById(R.id.sms_check);
            roll_number = itemView.findViewById(R.id.roll_number);
            student_name = itemView.findViewById(R.id.student_name);
            attendence_status = itemView.findViewById(R.id.attendence_status);
            attendence_note = itemView.findViewById(R.id.attendence_note);

        }

    }
}
