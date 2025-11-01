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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Model.date_sheet.DateSheetData;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.Parent.Utils.ThemeHelper;
import topgrade.parent.com.parentseeks.R;

public class StudentDateSheetAdaptor extends OptimizedRecyclerViewAdapter<DateSheetData, StudentDateSheetAdaptor.StudentDateSheetHolder> {

    private int breakCount = 0;
    private boolean isSyllabus;
    private String userType;
    
    // View types
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DATA = 1;
    
    // Cached date formatters for better performance
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy", Locale.US);
    private final SimpleDateFormat time24Format = new SimpleDateFormat("HH:mm", Locale.US);
    private final SimpleDateFormat time12Format = new SimpleDateFormat("hh:mm a", Locale.US);

    public StudentDateSheetAdaptor(List<DateSheetData> list, Context context) {
        super(context, sortDataInAscendingOrder(list, context));
        // Get current user type
        this.userType = Paper.book().read(Constants.User_Type, "");
        
        // Check if this is syllabus-based (no start_time) or time-based
        if (list != null && !list.isEmpty()) {
            DateSheetData firstItem = list.get(0);
            isSyllabus = (firstItem.start_time == null);
        }
    }
    
    /**
     * Sort the date sheet data in ascending order by date
     */
    private static List<DateSheetData> sortDataInAscendingOrder(List<DateSheetData> originalList, Context context) {
        if (originalList == null || originalList.isEmpty()) {
            return originalList;
        }
        
        List<DateSheetData> sortedList = new ArrayList<>(originalList);
        sortedList.sort((item1, item2) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date1 = format.parse(item1.created_date);
                Date date2 = format.parse(item2.created_date);
                return date1.compareTo(date2);
            } catch (ParseException e) {
                return 0;
            }
        });
        
        return sortedList;
    }

    @Override
    public int getItemCount() {
        // No header row needed - header is in the table layout
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        // All rows are data rows since header is in table layout
        return VIEW_TYPE_DATA;
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        // Use unified item row layout
        return R.layout.date_sheet_item_row;
    }

    @Override
    protected StudentDateSheetHolder createViewHolder(View view, int viewType) {
        return new StudentDateSheetHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentDateSheetHolder holder, int position) {
        // Get the data item (no header offset needed)
        DateSheetData dataItem = getItem(position);
        if (dataItem == null) {
            return;
        }
        
        bindViewHolder(holder, dataItem, position);
    }
    
    @Override
    protected void bindViewHolder(StudentDateSheetHolder holder, DateSheetData item, int position) {
        // This method is called by onBindViewHolder for data rows only

        // Set serial number (position + 1 for display)
        holder.tvSerialNo.setText(String.valueOf(position + 1));
        
        // Reset text styling for data rows
        holder.tvSerialNo.setTypeface(null, android.graphics.Typeface.NORMAL);
        holder.tvDate.setTypeface(null, android.graphics.Typeface.NORMAL);
        holder.tvSubject.setTypeface(null, android.graphics.Typeface.NORMAL);
        holder.tvTimeOrSyllabus.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Reset text colors to theme colors
        holder.tvSerialNo.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvDate.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvSubject.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvTimeOrSyllabus.setTextColor(context.getResources().getColor(android.R.color.black));

        // Set date
        holder.tvDate.setText(formatDate(item.created_date));
        
        // Set subject
        holder.tvSubject.setText(item.subject);
        
        // Set time or syllabus
        if (isSyllabus) {
            holder.tvTimeOrSyllabus.setText(item.syllabus != null ? item.syllabus : "N/A");
        } else {
            String timeText = "";
            if (item.start_time != null && item.end_time != null) {
                timeText = formatTime(item.start_time.toString()) + " - " + formatTime(item.end_time.toString());
            } else if (item.start_time != null) {
                timeText = formatTime(item.start_time.toString());
            }
            holder.tvTimeOrSyllabus.setText(timeText);
        }
        
        // Apply alternating row colors
        int primaryColor = ThemeHelper.getPrimaryColor(context, userType);
        if (position % 2 == 0) {
            holder.headerRv.setBackgroundColor(Color.WHITE);
        } else {
            holder.headerRv.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
        
        // Show/hide break row (not used in date sheet, but keeping for consistency)
        if (holder.linearBreak != null) {
            holder.linearBreak.setVisibility(View.GONE);
        }
        holder.headerRv.setVisibility(View.VISIBLE);
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "N/A";
        }
        
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original if parsing fails
        }
    }

    private String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return "N/A";
        }
        
        try {
            Date time = time24Format.parse(timeString);
            return time12Format.format(time);
        } catch (ParseException e) {
            return timeString; // Return original if parsing fails
        }
    }

    public static class StudentDateSheetHolder extends RecyclerView.ViewHolder {
        LinearLayout linearBreak, headerRv;
        TextView tvSerialNo, tvDate, tvSubject, tvTimeOrSyllabus;
        TextView tvSerialNumbBreak; // For break row (not used in date sheet)

        public StudentDateSheetHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            
            // Initialize views for both header and data rows (unified layout)
            // Break row components (not used in date sheet)
            linearBreak = itemView.findViewById(R.id.linear_break_date_sheet);
            tvSerialNumbBreak = itemView.findViewById(R.id.tv_serial_numb_date_sheet_break);
            
            // Main row components
            headerRv = itemView.findViewById(R.id.header_rv_date_sheet);
            tvSerialNo = itemView.findViewById(R.id.tv_serial_no_date_sheet);
            tvDate = itemView.findViewById(R.id.tv_date_date_sheet);
            tvSubject = itemView.findViewById(R.id.tv_subject_name_date_sheet);
            tvTimeOrSyllabus = itemView.findViewById(R.id.tv_time_or_syllabus_date_sheet);
        }
    }
}