package topgrade.parent.com.parentseeks.Parent.Adaptor;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import topgrade.parent.com.parentseeks.Parent.Model.timetable.Detail;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.R;

public class StudentTimetableAdaptor extends OptimizedRecyclerViewAdapter<Detail, StudentTimetableAdaptor.StudentTimetableHolder> {

    private static final String TAG = "StudentTimetableAdaptor";
    
    // View types
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DATA = 1;
    
    // Cached date formatters for better performance
    private final SimpleDateFormat time24Format = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    
    private int breakCount = 0;

    public StudentTimetableAdaptor(List<Detail> list, Context context) {
        super(context, sortTimetableInAscendingOrder(list, context)); // Use sorted data
    }
    
    /**
     * Sort the timetable data in ascending order by time
     */
    private static List<Detail> sortTimetableInAscendingOrder(List<Detail> originalList, Context context) {
        if (originalList == null || originalList.isEmpty()) {
            return originalList;
        }
        
        // Create a new list to avoid modifying the original
        List<Detail> sortedList = new ArrayList<>(originalList);
        
        // Sort by start time in ascending order
        sortedList.sort((detail1, detail2) -> {
            try {
                // Use getter methods to access private fields
                String time1 = detail1.getStartTime() != null ? detail1.getStartTime() : "00:00";
                String time2 = detail2.getStartTime() != null ? detail2.getStartTime() : "00:00";
                
                return time1.compareTo(time2);
                
            } catch (Exception e) {
                // If sorting fails, maintain original order
                return 0;
            }
        });
        
        return sortedList;
    }
    
    // Break times will now come from the server API
    // No need for client-side break generation

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_DATA;
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        return viewType == VIEW_TYPE_HEADER ? R.layout.parent_timetable_header_item : R.layout.parent_timetable_item;
    }

    @Override
    protected StudentTimetableHolder createViewHolder(View view, int viewType) {
        return new StudentTimetableHolder(view, viewType);
    }

    @Override
    protected void bindViewHolder(StudentTimetableHolder holder, Detail timetable, int position) {
        if (holder == null) {
            Log.w(TAG, "Null holder at position: " + position);
            return;
        }

        try {
            // Handle header row (position 0)
            if (position == 0) {
                // Header row - no data binding needed, layout handles it
                return;
            }

            // Handle data rows
            if (timetable == null) {
                Log.w(TAG, "Null timetable at position: " + position);
                return;
            }

            // Set alternating background colors for data rows
            int dataPosition = position - 1; // Adjust for header row
            if (holder.row != null) {
                if (dataPosition % 2 != 0) {
                    holder.row.setBackgroundColor(Color.parseColor("#DFE5E2")); // Light gray-green
                } else {
                    holder.row.setBackgroundColor(Color.WHITE); // White
                }
            }
            
            int index = dataPosition + 1; // Match date sheet calculation
            
            if (timetable.getSubject() == null) {
                // Handle break time - match date sheet logic exactly
                breakCount = breakCount - 1;
                if (holder.linearBreakTime != null) holder.linearBreakTime.setVisibility(View.VISIBLE);
                if (holder.linearSubjects != null) holder.linearSubjects.setVisibility(View.GONE);
                if (holder.tvBreakSerialNum != null) holder.tvBreakSerialNum.setText(String.valueOf(index));
            } else {
                // Handle subject data - match date sheet logic exactly
                if (holder.linearBreakTime != null) holder.linearBreakTime.setVisibility(View.GONE);
                if (holder.linearSubjects != null) holder.linearSubjects.setVisibility(View.VISIBLE);
                handleSubjectData(holder, timetable, index);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position + ": " + e.getMessage());
            setDefaultValues(holder);
        }
    }


    private boolean isBreakTime(Detail timetable) {
        if (timetable == null) return false;
        
        String subject = timetable.getSubject();
        if (subject == null || subject.trim().isEmpty()) {
            return true;
        }
        
        // Check for common break time keywords
        String subjectLower = subject.toLowerCase().trim();
        return subjectLower.equals("break") || 
               subjectLower.equals("lunch") || 
               subjectLower.equals("recess") || 
               subjectLower.equals("interval") ||
               subjectLower.equals("break time") ||
               subjectLower.equals("lunch break") ||
               subjectLower.equals("tea break");
    }

    private void handleBreakTime(StudentTimetableHolder holder, int index) {
        // Increment break count for lecture numbering
        breakCount++;
        
        if (holder.linearBreakTime != null) {
            holder.linearBreakTime.setVisibility(View.VISIBLE);
        }
        
        if (holder.linearSubjects != null) {
            holder.linearSubjects.setVisibility(View.GONE);
        }
        
        if (holder.tvBreakSerialNum != null) {
            holder.tvBreakSerialNum.setText(String.valueOf(index));
        }
    }

    private void handleSubjectData(StudentTimetableHolder holder, Detail timetable, int index) {
        if (holder.linearBreakTime != null) {
            holder.linearBreakTime.setVisibility(View.GONE);
        }
        
        if (holder.linearSubjects != null) {
            holder.linearSubjects.setVisibility(View.VISIBLE);
        }
        
        // Set serial number
        if (holder.tvSerialNum != null) {
            holder.tvSerialNum.setText(String.valueOf(index));
        }
        
        // Set lecture number (excluding breaks from lecture count)
        if (holder.tvLectureNumber != null) {
            holder.tvLectureNumber.setText(String.valueOf(index));
        }
        
        // Set subject name
        if (holder.tvSubject != null) {
            String subject = timetable.getSubject();
            holder.tvSubject.setText(subject != null ? subject : "N/A");
        }
        
        // Set teacher name
        if (holder.tvTeacher != null) {
            String staff = timetable.getStaff();
            holder.tvTeacher.setText(staff != null ? staff : "N/A");
        }
        
        // Set start and end times directly to prevent ANR (remove seconds)
        if (holder.tvStartTime != null && holder.tvEndTime != null) {
            String startTime = timetable.getStartTime();
            String endTime = timetable.getEndTime();
            
            // Remove seconds if present (e.g., "14:30:00" -> "14:30")
            if (startTime != null && startTime.length() > 5) {
                startTime = startTime.substring(0, 5);
            }
            if (endTime != null && endTime.length() > 5) {
                endTime = endTime.substring(0, 5);
            }
            
            holder.tvStartTime.setText(startTime != null ? startTime : "N/A");
            holder.tvEndTime.setText(endTime != null ? endTime : "N/A");
        }
    }

    private String formatTimeRange(Detail timetable) {
        try {
            String startTime = timetable.getStartTime();
            String endTime = timetable.getEndTime();
            
            if (startTime == null || endTime == null) {
                return "N/A - N/A";
            }
            
            // Remove seconds if present (e.g., "14:30:00" -> "14:30")
            if (startTime.length() > 5) {
                startTime = startTime.substring(0, 5);
            }
            if (endTime.length() > 5) {
                endTime = endTime.substring(0, 5);
            }
            
            return startTime + " - " + endTime;
            
        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + e.getMessage());
            return "N/A - N/A";
        }
    }

    private void setDefaultValues(StudentTimetableHolder holder) {
        if (holder.tvSerialNum != null) {
            holder.tvSerialNum.setText("N/A");
        }
        if (holder.tvLectureNumber != null) {
            holder.tvLectureNumber.setText("N/A");
        }
        if (holder.tvSubject != null) {
            holder.tvSubject.setText("N/A");
        }
        if (holder.tvTeacher != null) {
            holder.tvTeacher.setText("N/A");
        }
        if (holder.tvStartTime != null) {
            holder.tvStartTime.setText("N/A");
        }
        if (holder.tvEndTime != null) {
            holder.tvEndTime.setText("N/A");
        }
        if (holder.tvBreakSerialNum != null) {
            holder.tvBreakSerialNum.setText("N/A");
        }
    }

    public static class StudentTimetableHolder extends RecyclerView.ViewHolder {
        private TextView tvSerialNum, tvLectureNumber, tvStartTime, tvEndTime, tvSubject, tvTeacher, tvBreakSerialNum;
        public LinearLayout row, linearSubjects, linearBreakTime;
        private int viewType;
        
        public StudentTimetableHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            initializeViews(itemView);
        }

        private void initializeViews(View itemView) {
            try {
                // Only initialize data row views if this is not a header
                if (viewType == VIEW_TYPE_DATA) {
                    row = itemView.findViewById(R.id.row_parent_timetable);
                    linearBreakTime = itemView.findViewById(R.id.linear_break_parent_timetable);
                    tvBreakSerialNum = itemView.findViewById(R.id.tv_serial_numb_parent_timetable_break);
                    linearSubjects = itemView.findViewById(R.id.header_rv_parent);
                    tvSerialNum = itemView.findViewById(R.id.tv_serial_no_parent_timetable);
                    tvLectureNumber = itemView.findViewById(R.id.tv_lecture_number_parent_timetable);
                    tvStartTime = itemView.findViewById(R.id.start_time_parent_timetable);
                    tvEndTime = itemView.findViewById(R.id.end_time_parent_timetable);
                    tvSubject = itemView.findViewById(R.id.tv_subject_name_parent_timetable);
                    tvTeacher = itemView.findViewById(R.id.tv_teacher_name_parent_timetable);
                }
                // Header views are handled by the layout itself
            } catch (Exception e) {
                Log.e(TAG, "Error initializing views: " + e.getMessage());
            }
        }
    }
}
