package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Timetable;

public class TimetableAdaptor extends RecyclerView.Adapter<TimetableAdaptor.TimetableHolder> {

    private static final String TAG = "TimetableAdaptor";
    String startTime;
    String endTime;
    Date _24HourDtStartTime,_24HourDtEndTime;

    Timetable.Detail timetable;
    View v;
    List<Timetable.Detail> list;
    Context context;
    private final int minLectures;
    private final int maxLectures;

    public TimetableAdaptor(List<Timetable.Detail> list, Context context) {
        this(list, context, 1, 8); // Default values
    }
    
    public TimetableAdaptor(List<Timetable.Detail> list, Context context, int minLectures, int maxLectures) {
        this.list = list;
        this.context = context;
        this.minLectures = minLectures;
        this.maxLectures = maxLectures;
        Log.d(TAG, "TimetableAdaptor initialized with lecture limits: " + minLectures + "-" + maxLectures);
    }

    @NonNull
    @Override
    public TimetableAdaptor.TimetableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(context).inflate(R.layout.staff_timetable_item,parent,false);
        return new TimetableHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableAdaptor.TimetableHolder holder, int position) {
        Log.d(TAG, "=== onBindViewHolder called for position: " + position + " ===");
        
        Timetable.Detail timetable=list.get(position);
        
        // Check if this is a break row and hide the break section
        String subject = timetable.getSubject();
        String className = timetable.getClassName();
        String section = timetable.getSection();
        
        // More comprehensive break detection - but allow period 6 to load normally
        boolean isBreakRow = (subject != null && !subject.trim().isEmpty() && 
                           (subject.toLowerCase().contains("break") || 
                            subject.toLowerCase().contains("recess") ||
                            subject.toLowerCase().contains("lunch")));
        
        // Don't hide rows based on empty className or section - let them load normally
        
        Log.d(TAG, "Position " + position + " - Break check: Subject='" + subject + "', Class='" + className + "', Section='" + section + "', IsBreak=" + isBreakRow);
        
        if (isBreakRow) {
            // Hide the entire row for break periods
            holder.row.setVisibility(View.GONE);
            Log.d(TAG, "Position " + position + " - Hiding break row");
            return;
        } else {
            // Show the row for regular periods and hide the break section
            holder.row.setVisibility(View.VISIBLE);
            if (holder.breakSection != null) {
                holder.breakSection.setVisibility(View.GONE);
                Log.d(TAG, "Position " + position + " - Hiding break section");
            }
            if (holder.headerSection != null) {
                holder.headerSection.setVisibility(View.VISIBLE);
                Log.d(TAG, "Position " + position + " - Showing header section");
            }
            Log.d(TAG, "Position " + position + " - Showing regular row");
        }

        if (position % 2 != 0)
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        else
            holder.row.setBackgroundColor(Color.WHITE);
        
        // Debug: Log all timetable data for this position
        Log.d(TAG, "Position " + position + " - Timetable Data:");
        Log.d(TAG, "  - ClassName: " + timetable.getClassName());
        Log.d(TAG, "  - Subject: " + timetable.getSubject());
        Log.d(TAG, "  - Section: " + timetable.getSection());
        Log.d(TAG, "  - StartTime: " + timetable.getStartTime());
        Log.d(TAG, "  - EndTime: " + timetable.getEndTime());
        Log.d(TAG, "  - TimetableOrder: " + timetable.getTimetableOrder());
        
        // Set period number with null check and logging
        if (holder.Period_Number != null) {
            // Workaround: Use position + 1 as period number since API sends same value
            String periodNumber = String.valueOf(position + 1);
            
            // Log the original API value for debugging
            Object timetableOrder = timetable.getTimetableOrder();
            if (timetableOrder != null) {
                Log.d(TAG, "Position " + position + " - API TimetableOrder: " + timetableOrder + " (Type: " + timetableOrder.getClass().getSimpleName() + ")");
            } else {
                Log.w(TAG, "Position " + position + " - API TimetableOrder is NULL");
            }
            
            holder.Period_Number.setText(periodNumber);
            Log.d(TAG, "Position " + position + " - Final Period Number: " + periodNumber + " (using position-based numbering)");
        } else {
            Log.e(TAG, "Position " + position + " - Period_Number TextView is null!");
        }
        
        // Set other fields with null checks
        if (holder.Classes != null) {
            holder.Classes.setText(timetable.getClassName() != null ? timetable.getClassName() : "N/A");
        }
        if (holder.Subject != null) {
            holder.Subject.setText(timetable.getSubject() != null ? timetable.getSubject() : "N/A");
        }
        if (holder.Section != null) {
            holder.Section.setText(timetable.getSection() != null ? timetable.getSection() : "N/A");
        }




        try {
            startTime=timetable.getStartTime();
            endTime=timetable.getEndTime();
            
            // Log timing data for debugging
            Log.d(TAG, "Position " + position + " - Timing data: Start='" + startTime + "', End='" + endTime + "'");
            
            // Check for null or empty timing data
            if (startTime == null || startTime.trim().isEmpty() || endTime == null || endTime.trim().isEmpty()) {
                Log.w(TAG, "Position " + position + " - Missing timing data, using defaults");
                holder.Start_Time.setText("--:--");
                holder.End_Time.setText("--:--");
                return;
            }
            
            SimpleDateFormat _24HourSDF = new SimpleDateFormat("HH:mm");
            SimpleDateFormat _12HourSDF = new SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH);
            _24HourDtStartTime = _24HourSDF.parse(startTime);
            _24HourDtEndTime=_24HourSDF.parse(endTime);

            holder.Start_Time.setText(_12HourSDF.format(_24HourDtStartTime));
            holder.End_Time.setText(_12HourSDF.format(_24HourDtEndTime));
            
            Log.d(TAG, "Position " + position + " - Formatted timing: " + _12HourSDF.format(_24HourDtStartTime) + " - " + _12HourSDF.format(_24HourDtEndTime));

        } catch (Exception e) {
            Log.e(TAG, "Position " + position + " - Error parsing timing data: " + e.getMessage());
            e.printStackTrace();
            // Set default values on error
            holder.Start_Time.setText("--:--");
            holder.End_Time.setText("--:--");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    
    /**
     * Get count of non-break items for display purposes
     */
    public int getVisibleItemCount() {
        int count = 0;
        for (Timetable.Detail detail : list) {
            String subject = detail.getSubject();
            if (subject != null && !subject.trim().isEmpty() && 
                !subject.toLowerCase().contains("break") && 
                !subject.toLowerCase().contains("recess") &&
                !subject.toLowerCase().contains("lunch")) {
                count++;
            }
        }
        Log.d(TAG, "getVisibleItemCount() - Found " + count + " lectures (allowed range: " + minLectures + "-" + maxLectures + ")");
        return count;
    }
    
    /**
     * Validates lecture count against constraints
     */
    public boolean isValidLectureCount() {
        int count = getVisibleItemCount();
        boolean isValid = count >= minLectures && count <= maxLectures;
        Log.d(TAG, "isValidLectureCount() - " + count + " lectures is " + (isValid ? "VALID" : "INVALID") + " (range: " + minLectures + "-" + maxLectures + ")");
        return isValid;
    }
    
    /**
     * Get minimum allowed lectures
     */
    public int getMinLectures() {
        return minLectures;
    }
    
    /**
     * Get maximum allowed lectures
     */
    public int getMaxLectures() {
        return maxLectures;
    }

    public class TimetableHolder extends RecyclerView.ViewHolder {
        private final TextView Classes;
        private final TextView Subject;
        private final TextView Period_Number;
        private final TextView Start_Time;
        private final TextView End_Time;
        private final TextView Section;
        public LinearLayout row;
        public LinearLayout breakSection;
        public LinearLayout headerSection;
        
        public TimetableHolder(@NonNull View itemView) {
            super(itemView);
            
            row = itemView.findViewById(R.id.row_staff_timetable);
            breakSection = itemView.findViewById(R.id.linear_break_staff_timetable);
            headerSection = itemView.findViewById(R.id.header_rv_staff);
            Classes = itemView.findViewById(R.id.tv_lecture_number_staff_timetable);
            Subject = itemView.findViewById(R.id.tv_subject_name_staff_timetable);
            Period_Number = itemView.findViewById(R.id.tv_serial_no_staff_timetable);
            Start_Time = itemView.findViewById(R.id.start_time_staff_timetable);
            End_Time = itemView.findViewById(R.id.end_time_staff_timetable);
            Section = itemView.findViewById(R.id.tv_teacher_name_staff_timetable);
            
            // Log ViewHolder initialization
            Log.d(TAG, "ViewHolder initialized - Period_Number: " + (Period_Number != null ? "Found" : "NULL"));
            Log.d(TAG, "ViewHolder initialized - breakSection: " + (breakSection != null ? "Found" : "NULL"));
            Log.d(TAG, "ViewHolder initialized - headerSection: " + (headerSection != null ? "Found" : "NULL"));
        }
    }
}
