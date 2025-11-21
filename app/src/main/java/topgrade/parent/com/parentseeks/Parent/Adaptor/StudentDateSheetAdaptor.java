package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
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

    public StudentDateSheetAdaptor(List<DateSheetData> list, Context context) {
        super(context, sortDataInAscendingOrder(list, context));
        // Get current user type
        this.userType = Paper.book().read(Constants.User_Type, "");
        
        // Enable stable IDs to improve RecyclerView performance with dynamic heights
        setHasStableIds(false);
        
        android.util.Log.d("StudentDateSheetAdaptor", "Constructor: Original list size: " + (list != null ? list.size() : 0));
        android.util.Log.d("StudentDateSheetAdaptor", "Constructor: Sorted list size: " + getItemCount());
        
        // Check if this is syllabus-based (no start_time) or time-based
        if (list != null && !list.isEmpty()) {
            DateSheetData firstItem = list.get(0);
            isSyllabus = (firstItem.start_time == null);
            android.util.Log.d("StudentDateSheetAdaptor", "Constructor: First item subject: " + firstItem.subject + ", isSyllabus: " + isSyllabus);
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
                // Handle null items first
                if (item1 == null && item2 == null) {
                    return 0;
                }
                if (item1 == null) {
                    return 1; // Put null items at the end
                }
                if (item2 == null) {
                    return -1; // Put null items at the end
                }
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                
                // Handle null or empty created_date
                String date1Str = (item1.created_date != null && !item1.created_date.isEmpty()) ? item1.created_date : null;
                String date2Str = (item2.created_date != null && !item2.created_date.isEmpty()) ? item2.created_date : null;
                
                // If both dates are null/empty, they are equal
                if (date1Str == null && date2Str == null) {
                    return 0;
                }
                
                // If date1 is null/empty, put it at the end
                if (date1Str == null) {
                    return 1;
                }
                
                // If date2 is null/empty, put it at the end
                if (date2Str == null) {
                    return -1;
                }
                
                // Both dates are valid, parse and compare
                Date date1 = format.parse(date1Str);
                Date date2 = format.parse(date2Str);
                return date1.compareTo(date2);
            } catch (ParseException e) {
                // If parsing fails, maintain original order
                android.util.Log.w("StudentDateSheetAdaptor", "ParseException in sorting: " + e.getMessage());
                return 0;
            } catch (NullPointerException e) {
                // Handle NPE specifically
                android.util.Log.e("StudentDateSheetAdaptor", "NullPointerException in sorting: " + e.getMessage(), e);
                return 0;
            } catch (Exception e) {
                // Handle any other exceptions
                android.util.Log.e("StudentDateSheetAdaptor", "Error sorting date sheet data: " + e.getMessage(), e);
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
            android.util.Log.w("StudentDateSheetAdaptor", "onBindViewHolder: dataItem is null at position " + position);
            return;
        }
        
        android.util.Log.d("StudentDateSheetAdaptor", "onBindViewHolder: Binding item at position " + position + 
            " - Subject: " + dataItem.subject + ", Date: " + dataItem.created_date);
        
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
        holder.tvSyllabus.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        // Reset text colors to theme colors
        holder.tvSerialNo.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvDate.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvSubject.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.tvSyllabus.setTextColor(context.getResources().getColor(android.R.color.black));

        // Set date
        holder.tvDate.setText(formatDate(item.created_date));
        
        // Set subject
        holder.tvSubject.setText(item.subject);
        
        // Set time - use separate start and end time views for proper alignment
        if (holder.tvStartTime != null && holder.tvEndTime != null) {
            String startTime = formatTime(item.start_time);
            String endTime = formatTime(item.end_time);
            
            holder.tvStartTime.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tvEndTime.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tvStartTime.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.tvEndTime.setTextColor(context.getResources().getColor(android.R.color.black));
            
            holder.tvStartTime.setText(startTime);
            holder.tvEndTime.setText(endTime);
        }
        
        // Set syllabus (always show syllabus if available)
        // Syllabus may contain HTML, so parse it properly
        final String syllabusText = (item.syllabus != null && !item.syllabus.trim().isEmpty()) 
            ? item.syllabus.trim() 
            : "N/A";
        
        if (!syllabusText.equals("N/A")) {
            // Check if the text contains HTML tags
            if (syllabusText.contains("<") && syllabusText.contains(">")) {
                try {
                    // Parse HTML content and make links clickable
                    Spanned htmlContent = HtmlCompat.fromHtml(
                        syllabusText, 
                        HtmlCompat.FROM_HTML_MODE_LEGACY,
                        null, // No custom tag handler needed
                        null  // No custom image getter needed
                    );
                    holder.tvSyllabus.setText(htmlContent);
                    // Enable movement method to make links clickable
                    holder.tvSyllabus.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                    // If HTML parsing fails, show as plain text
                    android.util.Log.e("StudentDateSheetAdaptor", "Error parsing HTML syllabus: " + e.getMessage());
                    holder.tvSyllabus.setText(syllabusText);
                }
            } else {
                // Plain text, set directly
                holder.tvSyllabus.setText(syllabusText);
            }
        } else {
            holder.tvSyllabus.setText("N/A");
        }
        
        // Force TextView to remeasure and adjust height dynamically
        // Set minLines to 1 so it can shrink to single line when content is short
        holder.tvSyllabus.setMinLines(1);
        holder.tvSyllabus.setMaxLines(5);
        
        // Reset any previous layout constraints that might prevent shrinking
        holder.tvSyllabus.setSingleLine(false);
        
        // Post to ensure layout is measured after text is set and width is available
        holder.itemView.post(() -> {
            try {
                // Wait for the view to be measured
                if (holder.tvSyllabus.getWidth() > 0) {
                    // Get the actual text (strip HTML if present)
                    String plainText;
                    if (syllabusText != null && !syllabusText.equals("N/A") && syllabusText.contains("<") && syllabusText.contains(">")) {
                        plainText = syllabusText.replaceAll("<[^>]+>", "").trim();
                    } else {
                        plainText = syllabusText;
                    }
                    
                    if (plainText != null && !plainText.isEmpty() && !plainText.equals("N/A")) {
                        // Measure text width
                        android.graphics.Paint paint = holder.tvSyllabus.getPaint();
                        float textWidth = paint.measureText(plainText);
                        int availableWidth = holder.tvSyllabus.getWidth() - holder.tvSyllabus.getPaddingStart() - holder.tvSyllabus.getPaddingEnd();
                        
                        // If text fits in one line, set maxLines to 1 to force single line
                        if (textWidth > 0 && availableWidth > 0 && textWidth <= availableWidth) {
                            holder.tvSyllabus.setMaxLines(1);
                            android.util.Log.d("StudentDateSheetAdaptor", "Syllabus fits in one line, setting maxLines=1 for: " + plainText);
                        } else {
                            holder.tvSyllabus.setMaxLines(5);
                            android.util.Log.d("StudentDateSheetAdaptor", "Syllabus needs multiple lines, setting maxLines=5 for: " + plainText);
                        }
                    } else {
                        // Empty or N/A - should be single line
                        holder.tvSyllabus.setMaxLines(1);
                    }
                }
                
                // Force remeasure to ensure height adjusts based on actual content
                holder.tvSyllabus.requestLayout();
                holder.tvSyllabus.invalidate();
                
                if (holder.headerRv != null) {
                    holder.headerRv.requestLayout();
                    holder.headerRv.invalidate();
                }
                
                // Also invalidate the item view to force full remeasure
                holder.itemView.requestLayout();
            } catch (Exception e) {
                android.util.Log.e("StudentDateSheetAdaptor", "Error adjusting syllabus height", e);
            }
        });
        
        // Dynamically adjust syllabus column width based on content length
        adjustSyllabusColumnWidth(holder.tvSyllabus, syllabusText);
        
        // Ensure the entire row remeasures when content changes
        holder.itemView.post(() -> {
            try {
                // Force the row to remeasure
                holder.itemView.requestLayout();
                holder.headerRv.requestLayout();
            } catch (Exception e) {
                android.util.Log.e("StudentDateSheetAdaptor", "Error remeasuring row", e);
            }
        });
        
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

    /**
     * Format time range in 24-hour format: "08:25 - 08:45"
     */
    private String formatTimeRange(Object startTime, Object endTime) {
        String startTimeStr = formatTime(startTime);
        String endTimeStr = formatTime(endTime);
        
        if (startTimeStr.equals("N/A") && endTimeStr.equals("N/A")) {
            return "N/A";
        } else if (startTimeStr.equals("N/A")) {
            return endTimeStr;
        } else if (endTimeStr.equals("N/A")) {
            return startTimeStr;
        } else {
            // Show both start and end time in 24-hour format: "08:25 - 08:45"
            return startTimeStr + " - " + endTimeStr;
        }
    }
    
    /**
     * Format time to 24-hour format (HH:mm) - matching timetable view exactly
     * Simply removes seconds if present (e.g., "14:30:00" -> "14:30")
     * Same approach as StudentTimetableAdaptor
     */
    private String formatTime(Object timeObject) {
        if (timeObject == null) {
            return "N/A";
        }
        
        String timeString = timeObject.toString().trim();
        if (timeString.isEmpty() || timeString.equals("null")) {
            return "N/A";
        }
        
        // Remove seconds if present (e.g., "14:30:00" -> "14:30")
        // This matches the exact approach used in StudentTimetableAdaptor
        if (timeString.length() > 5) {
            timeString = timeString.substring(0, 5);
        }
        
        return timeString;
    }
    
    /**
     * Dynamically adjust syllabus column width based on content length
     * This ensures the column expands when syllabus content is longer
     */
    private void adjustSyllabusColumnWidth(TextView syllabusTextView, String syllabusText) {
        if (syllabusTextView == null) {
            return;
        }
        
        try {
            // Get the plain text length (strip HTML tags for calculation)
            String plainText = syllabusText;
            if (syllabusText != null && syllabusText.contains("<") && syllabusText.contains(">")) {
                // Strip HTML tags to get actual text length
                plainText = syllabusText.replaceAll("<[^>]+>", "").trim();
            }
            
            int textLength = (plainText != null && !plainText.isEmpty()) ? plainText.length() : 0;
            
            // Calculate dynamic weight based on text length
            // Base weight: 2.0 (current default)
            // Increase weight for longer content
            float dynamicWeight;
            if (textLength <= 20) {
                dynamicWeight = 1.5f; // Short content
            } else if (textLength <= 50) {
                dynamicWeight = 2.0f; // Medium content (default)
            } else if (textLength <= 100) {
                dynamicWeight = 2.5f; // Long content
            } else if (textLength <= 200) {
                dynamicWeight = 3.0f; // Very long content
            } else {
                dynamicWeight = 3.5f; // Extremely long content
            }
            
            // Update layout params with new weight
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) syllabusTextView.getLayoutParams();
            if (params != null) {
                params.weight = dynamicWeight;
                syllabusTextView.setLayoutParams(params);
                
                android.util.Log.d("StudentDateSheetAdaptor", "Adjusted syllabus column weight to " + dynamicWeight + 
                    " for text length: " + textLength);
            }
        } catch (Exception e) {
            android.util.Log.e("StudentDateSheetAdaptor", "Error adjusting syllabus column width", e);
        }
    }

    public static class StudentDateSheetHolder extends RecyclerView.ViewHolder {
        LinearLayout linearBreak, headerRv;
        TextView tvSerialNo, tvDate, tvSubject, tvStartTime, tvEndTime, tvSyllabus;
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
            
            // Separate start and end time views for proper alignment
            tvStartTime = itemView.findViewById(R.id.tv_start_time_date_sheet);
            tvEndTime = itemView.findViewById(R.id.tv_end_time_date_sheet);
            
            tvSyllabus = itemView.findViewById(R.id.tv_syllabus_date_sheet);
        }
    }
}