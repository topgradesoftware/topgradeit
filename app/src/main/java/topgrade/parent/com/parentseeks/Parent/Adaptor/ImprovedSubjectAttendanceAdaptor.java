package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import topgrade.parent.com.parentseeks.Parent.Model.SubjectAttendeceModel;
import topgrade.parent.com.parentseeks.R;

/**
 * Improved adapter for subject-wise attendance that creates a proper grid layout
 * matching the design shown in the user's images
 */
public class ImprovedSubjectAttendanceAdaptor extends RecyclerView.Adapter<ImprovedSubjectAttendanceAdaptor.AttendanceRowHolder> {

    private Context context;
    private List<SubjectAttendeceModel.AttendanceList> attendanceData;
    private List<String> dateList;
    private List<String> subjectList;

    public ImprovedSubjectAttendanceAdaptor(Context context, 
                                          List<SubjectAttendeceModel.AttendanceList> attendanceData,
                                          List<String> dateList, 
                                          List<String> subjectList) {
        this.context = context;
        this.attendanceData = attendanceData;
        this.dateList = dateList;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public AttendanceRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attendance_row_item, parent, false);
        return new AttendanceRowHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceRowHolder holder, int position) {
        if (position < dateList.size()) {
            String date = dateList.get(position);
            holder.bindDateRow(date, position);
        }
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public class AttendanceRowHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private LinearLayout attendanceContainer;

        public AttendanceRowHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text);
            attendanceContainer = itemView.findViewById(R.id.attendance_container);
        }

        public void bindDateRow(String date, int rowIndex) {
            // Set date text
            dateTextView.setText(formatDate(date));
            dateTextView.setBackgroundResource(R.drawable.date_cell_background);
            dateTextView.setTextColor(Color.WHITE);
            dateTextView.setIncludeFontPadding(false); // Remove extra font padding
            dateTextView.setLineSpacing(0, 0); // Remove line spacing

            // Clear existing attendance views
            attendanceContainer.removeAllViews();

            // Create attendance cells for each subject with exact alignment
            for (int i = 0; i < subjectList.size(); i++) {
                TextView attendanceCell = createAttendanceCell();
                String attendanceStatus = getAttendanceStatus(rowIndex, i);
                applyAttendanceStatus(attendanceCell, attendanceStatus);
                
                // Ensure proper layout params for alignment
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, 40, 1.0f);
                cellParams.setMargins(0, 0, 0, 0);
                attendanceCell.setLayoutParams(cellParams);
                
                attendanceContainer.addView(attendanceCell);
                
                // Debug logging
                android.util.Log.d("AttendanceAdapter", "Created cell " + i + " with width: " + cellParams.width + " for subject: " + subjectList.get(i));
            }
        }

        private TextView createAttendanceCell() {
            TextView cell = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 40, 1.0f); // Use weight for equal distribution
            params.setMargins(0, 0, 0, 0); // No margins for perfect alignment
            cell.setLayoutParams(params);
            cell.setBackgroundResource(R.drawable.attendance_present_background); // Use bordered background from start
            cell.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cell.setGravity(android.view.Gravity.CENTER);
            cell.setTextSize(14); // Match date text size for consistency
            cell.setPadding(0, 0, 0, 0); // Remove all padding to maximize text space
            cell.setMinHeight(40); // Ensure minimum height
            cell.setMaxHeight(40); // Ensure maximum height
            cell.setIncludeFontPadding(false); // Remove extra font padding
            cell.setLineSpacing(0, 0); // Remove line spacing
            return cell;
        }

        private String getAttendanceStatus(int dateIndex, int subjectIndex) {
            if (attendanceData != null && subjectIndex < attendanceData.size()) {
                SubjectAttendeceModel.AttendanceList subjectAttendance = attendanceData.get(subjectIndex);
                if (subjectAttendance.getAttendance() != null && 
                    dateIndex < subjectAttendance.getAttendance().size()) {
                    return subjectAttendance.getAttendance().get(dateIndex).getAttendance();
                }
            }
            return "null";
        }

        private void applyAttendanceStatus(TextView textView, String status) {
            switch (status) {
                case "null":
                case "":
                    textView.setText("");
                    textView.setBackgroundResource(R.drawable.attendance_present_background);
                    textView.setTextColor(Color.BLACK);
                    break;
                case "0":
                    textView.setText("OFF");
                    textView.setBackgroundResource(R.drawable.attendance_off_background);
                    textView.setTextColor(Color.WHITE);
                    break;
                case "1":
                    textView.setText("P");
                    textView.setBackgroundResource(R.drawable.attendance_present_background);
                    textView.setTextColor(Color.BLACK);
                    break;
                case "2":
                    textView.setText("A");
                    textView.setBackgroundResource(R.drawable.attendance_absent_background);
                    textView.setTextColor(Color.WHITE);
                    break;
                case "3":
                    textView.setText("H");
                    textView.setBackgroundResource(R.drawable.attendance_holiday_background);
                    textView.setTextColor(Color.BLACK);
                    break;
                case "4":
                    textView.setText("F");
                    textView.setBackgroundResource(R.drawable.attendance_fun_background);
                    textView.setTextColor(Color.BLACK);
                    break;
                case "5":
                    textView.setText("L");
                    textView.setBackgroundResource(R.drawable.attendance_late_background);
                    textView.setTextColor(Color.BLACK);
                    break;
                default:
                    textView.setText("");
                    textView.setBackgroundResource(R.drawable.attendance_present_background);
                    textView.setTextColor(Color.BLACK);
                    break;
            }
        }

        private String formatDate(String dateString) {
            try {
                // Parse the date string and format it as DD/MM/YY
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString; // Return original if parsing fails
            }
        }
    }
}
