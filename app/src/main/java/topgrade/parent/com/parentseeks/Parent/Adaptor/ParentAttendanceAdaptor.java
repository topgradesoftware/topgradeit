package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

public class ParentAttendanceAdaptor extends OptimizedRecyclerViewAdapter<ParentAttendanceModel, ParentAttendanceAdaptor.Holder> {

    private OnClickListener onClickListener;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy");
    private String userType;

    public ParentAttendanceAdaptor(Context context, List<ParentAttendanceModel> list, OnClickListener onClickListener) {
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
        
        // Apply attendance status styling
        applyAttendanceStatus(holder, attendanceStatus);
        
        // Set date
        holder.date.setText(changeDateFormat(item.getCreated_date()));
        
        // Set note
        if ("null".equals(item.getNote())) {
            holder.note.setText("");
        } else {
            holder.note.setText(item.getNote());
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
                holder.attendence_status.setText("");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.date.setTextColor(Color.parseColor("#000000"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                holder.note.setTextColor(Color.parseColor("#000000"));
                break;
            case "0":
                holder.attendence_status.setText("OFF");
                // Use dark_brown for parent theme, or ThemeHelper for other user types
                int primaryColor;
                if (userType != null && userType.equals("PARENT")) {
                    primaryColor = ContextCompat.getColor(context, R.color.dark_brown);
                } else {
                    primaryColor = ThemeHelper.getPrimaryColor(context, userType);
                }
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
                holder.row.setBackgroundColor(Color.parseColor("#DF4242"));
                holder.date.setTextColor(Color.parseColor("#ffffff"));
                holder.attendence_status.setTextColor(Color.parseColor("#ffffff"));
                holder.note.setTextColor(Color.parseColor("#ffffff"));
                break;
            case "3":
                holder.attendence_status.setText("Half Leave");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.date.setTextColor(Color.parseColor("#000000"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                holder.note.setTextColor(Color.parseColor("#000000"));
                break;
            case "4":
                holder.attendence_status.setText("Full Leave");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.date.setTextColor(Color.parseColor("#000000"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                holder.note.setTextColor(Color.parseColor("#000000"));
                break;
            case "5":
                holder.attendence_status.setText("Late");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.attendence_status.setTextColor(Color.parseColor("#FF6666"));
                holder.date.setTextColor(Color.parseColor("#000000"));
                holder.note.setTextColor(Color.parseColor("#000000"));
                break;
        }
    }


    public class Holder extends RecyclerView.ViewHolder {

        private TextView date, attendence_status, note;
        LinearLayout row;

        public Holder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            attendence_status = itemView.findViewById(R.id.attendence_status);
            note = itemView.findViewById(R.id.note);
            row = itemView.findViewById(R.id.row);


        }


    }

    private String changeDateFormat(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }

        // Log the original date string for debugging
        android.util.Log.d("AttendanceAdaptor", "Original date string: " + time);

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
                    android.util.Log.d("AttendanceAdaptor", "Fixed 1970 year issue, using current year: " + cal.get(Calendar.YEAR));
                }
                String formattedDate = outputFormat.format(date);
                android.util.Log.d("AttendanceAdaptor", "Formatted date: " + formattedDate);
                return formattedDate;
            } else {
                // If all parsing attempts fail, return original string
                return time;
            }
        } catch (Exception e) {
            return time; // Return original string if parsing fails
        }
    }
}
