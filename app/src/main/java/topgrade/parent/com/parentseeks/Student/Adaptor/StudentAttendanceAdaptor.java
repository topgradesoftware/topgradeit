package topgrade.parent.com.parentseeks.Student.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.R;

public class StudentAttendanceAdaptor extends OptimizedRecyclerViewAdapter<ParentAttendanceModel, StudentAttendanceAdaptor.Holder> {

    private OnClickListener onClickListener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy");
    private String userType;

    public StudentAttendanceAdaptor(Context context, List<ParentAttendanceModel> list, OnClickListener onClickListener) {
        super(context, list);
        this.onClickListener = onClickListener;
        // Get current user type
        this.userType = Paper.book().read(Constants.User_Type, "");
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        return R.layout.attendance_layout;
    }

    @Override
    protected Holder createViewHolder(View view, int viewType) {
        return new Holder(view);
    }

    @Override
    protected void bindViewHolder(Holder holder, ParentAttendanceModel item, int position) {
        String attendanceStatus = item.getAttendance();
        
        // Set date
        if (holder.date != null) {
            holder.date.setText(changeDateFormat(item.getCreated_date()));
        }
        
        // Set note
        if (holder.note != null) {
            holder.note.setText(item.getNote() != null ? item.getNote() : "");
        }
        
        // Set attendance status and colors based on status
        if (holder.attendence_status != null) {
            switch (attendanceStatus) {
                case "null":
                    holder.attendence_status.setText("");
                    holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                case "0":
                    holder.attendence_status.setText("OFF");
                    // Use ThemeHelper to get appropriate color based on user type
                    int primaryColor = ThemeHelper.getPrimaryColor(context, userType);
                    holder.row.setBackgroundColor(primaryColor);
                    holder.date.setTextColor(Color.parseColor("#ffffff"));
                    holder.attendence_status.setTextColor(Color.parseColor("#ffffff"));
                    holder.note.setTextColor(Color.parseColor("#ffffff"));
                    break;
                case "1":
                    holder.attendence_status.setText("Present");
                    holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                case "2":
                    holder.attendence_status.setText("Absent");
                    holder.row.setBackgroundColor(Color.parseColor("#ffcccc"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                case "3":
                    holder.attendence_status.setText("Half Leave");
                    holder.row.setBackgroundColor(Color.parseColor("#fff2cc"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                case "4":
                    holder.attendence_status.setText("Full Leave");
                    holder.row.setBackgroundColor(Color.parseColor("#ffe6cc"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                case "5":
                    holder.attendence_status.setText("Late");
                    holder.row.setBackgroundColor(Color.parseColor("#e6ccff"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
                default:
                    holder.attendence_status.setText("Unknown");
                    holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                    holder.date.setTextColor(Color.parseColor("#000000"));
                    holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                    holder.note.setTextColor(Color.parseColor("#000000"));
                    break;
            }
        }
    }

    private String changeDateFormat(String inputDate) {
        try {
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputDate; // Return original if parsing fails
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView date, attendence_status, note;
        LinearLayout row;

        public Holder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            attendence_status = itemView.findViewById(R.id.attendence_status);
            note = itemView.findViewById(R.id.note);
            row = itemView.findViewById(R.id.row);
        }
    }
}
