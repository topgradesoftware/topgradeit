package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.AttendanceEditnterface;
import topgrade.parent.com.parentseeks.Teacher.Model.API.TeacherAttendanceModel;

public class AttendanceUpdateAdaptor extends RecyclerView.Adapter<AttendanceUpdateAdaptor.AttendanceSubmitViewHolder> {


    List<TeacherAttendanceModel> list;
    View v;
    Context context;
    AttendanceEditnterface attendenceSubmitInterface;
    public AttendanceUpdateAdaptor(Context context, List<TeacherAttendanceModel> list,

                                   AttendanceEditnterface attendenceSubmitInterface
    ) {
        this.list = list;
        this.context = context;
        this.attendenceSubmitInterface = attendenceSubmitInterface;


    }

    @NonNull
    @Override
    public AttendanceSubmitViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance_update_layout, viewGroup, false);
        return new AttendanceSubmitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceSubmitViewHolder holder, final int position) {
        final TeacherAttendanceModel attendenceModel = list.get(position);
        holder.roll_number.setText("" + attendenceModel.getRoll_no());
        holder.student_name.setText(attendenceModel.getFullName());
        holder.attendence_note.setText(attendenceModel.getNote());


        final String attendence_status = "" + list.get(position).getAttendance();
        switch (attendence_status) {
            case "1":
                holder.attendence_status.setText("Pre");
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// black
                holder.attendence_status.setBackgroundColor(Color.parseColor("#ffffff"));//WHITE

                break;
            case "2":
                holder.attendence_status.setText("Abs");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#DF4242"));//RED
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// black

                break;
            case "3":
                holder.attendence_status.setText("H.lev");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90"));//GREEN
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// black
                break;
            case "4":
                holder.attendence_status.setText("F.lev");
                holder.attendence_status.setBackgroundColor(Color.parseColor("#90ee90"));//GREEN
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));// black
                break;


        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    attendenceSubmitInterface.AttendanceUpdate(view, adapterPosition, attendence_status,
                            attendenceModel.getNote());
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AttendanceSubmitViewHolder extends RecyclerView.ViewHolder {

        private final TextView roll_number;
        private final TextView student_name;
        private final TextView attendence_note;
        private final TextView attendence_status;

        LinearLayout row;

        AttendanceSubmitViewHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            roll_number = itemView.findViewById(R.id.roll_number);
            student_name = itemView.findViewById(R.id.student_name);
            attendence_status = itemView.findViewById(R.id.attendence_status);
            attendence_note = itemView.findViewById(R.id.attendence_note);

        }

    }
}
