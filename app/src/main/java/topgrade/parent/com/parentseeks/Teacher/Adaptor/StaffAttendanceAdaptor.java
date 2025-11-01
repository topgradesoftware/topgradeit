package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.R;

public class StaffAttendanceAdaptor extends OptimizedRecyclerViewAdapter<ParentAttendanceModel, StaffAttendanceAdaptor.Holder> {

    private OnClickListener onClickListener;
    
    // Output date format
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    // Attendance status constants
    public static final String STATUS_OFF = "0";
    public static final String STATUS_PRESENT = "1";
    public static final String STATUS_ABSENT = "2";
    public static final String STATUS_HALF_LEAVE = "3";
    public static final String STATUS_FULL_LEAVE = "4";
    public static final String STATUS_LATE = "5";

    public StaffAttendanceAdaptor(Context context, List<ParentAttendanceModel> list, OnClickListener onClickListener) {
        super(context, list);
        this.onClickListener = onClickListener;
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        return R.layout.attendance_layout;
    }

    @Override
    protected Holder createViewHolder(View view, int viewType) {
        return new Holder(view);
    }

    // getItemCount() is already implemented in OptimizedRecyclerViewAdapter
    // It returns dataList.size() automatically

    @Override
    protected void bindViewHolder(Holder holder, ParentAttendanceModel item, int position) {
        String attendanceStatus = item.getAttendance();
        
        // Apply attendance status styling
        applyAttendanceStatus(holder, attendanceStatus);
        
        // Set date
        holder.date.setText(changeDateFormat(item.getCreated_date()));
        
        // Set note - match parent implementation exactly with debugging
        String noteValue = item.getNote();
        Log.d("StaffAttendanceAdaptor", "=== ADAPTER NOTE PROCESSING ===");
        Log.d("StaffAttendanceAdaptor", "Raw note value: '" + noteValue + "'");
        Log.d("StaffAttendanceAdaptor", "Note is null: " + (noteValue == null));
        Log.d("StaffAttendanceAdaptor", "Note equals 'null': " + "null".equals(noteValue));
        
        if ("null".equals(noteValue)) {
            holder.note.setText("");
            Log.d("StaffAttendanceAdaptor", "Set empty text for null note");
        } else {
            holder.note.setText(noteValue);
            Log.d("StaffAttendanceAdaptor", "Set note text to: '" + noteValue + "'");
        }

        // Set click listener
        holder.note.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && onClickListener != null) {
                onClickListener.onItemClick(v, adapterPosition);
            }
        });
    }
    
    private void applyAttendanceStatus(Holder holder, String status) {
        switch (status) {
            case "null":
            case "":
                holder.attendence_status.setText("");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.date.setTextColor(Color.parseColor("#000000"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                holder.note.setTextColor(Color.parseColor("#000000"));
                break;
            case "0":
                holder.attendence_status.setText("OFF");
                // Navy blue for staff/teacher
                holder.row.setBackgroundColor(Color.parseColor("#000080"));
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
        }
    }

    // updateData() is already implemented in OptimizedRecyclerViewAdapter
    // It efficiently updates the data list and notifies the adapter

    private String changeDateFormat(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }

        // Log the original date string for debugging
        android.util.Log.d("StaffAttendanceAdaptor", "Original date string: " + time);

        try {
            // Try multiple date formats to handle different API responses
            SimpleDateFormat[] formats = {
                new SimpleDateFormat("yyyy-MM-dd"),           // Standard format
                new SimpleDateFormat("dd-MM-yyyy"),           // Alternative format
                new SimpleDateFormat("dd/MM/yyyy"),           // Another alternative
                new SimpleDateFormat("MM-dd-yyyy"),           // US format
                new SimpleDateFormat("yyyy/MM/dd")            // Another format
            };
            
            Date date = null;
            for (SimpleDateFormat format : formats) {
                try {
                    date = format.parse(time);
                    break; // If parsing succeeds, break out of loop
                } catch (ParseException ignored) {
                    // Continue to next format
                }
            }
            
            if (date != null) {
                // Check if the year is 1970 (Unix epoch) which indicates a parsing issue
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (cal.get(Calendar.YEAR) == 1970) {
                    // If year is 1970, it might be a time-only format, use current year
                    Calendar currentCal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                    date = cal.getTime();
                    android.util.Log.d("StaffAttendanceAdaptor", "Fixed 1970 year issue, using current year: " + cal.get(Calendar.YEAR));
                }
                String formattedDate = OUTPUT_FORMAT.format(date);
                android.util.Log.d("StaffAttendanceAdaptor", "Formatted date: " + formattedDate);
                return formattedDate;
            } else {
                // If all parsing attempts fail, return original string
                return time;
            }
        } catch (Exception e) {
            return time; // Return original string if parsing fails
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
