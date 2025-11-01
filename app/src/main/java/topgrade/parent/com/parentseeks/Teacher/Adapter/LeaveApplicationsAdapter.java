package topgrade.parent.com.parentseeks.Teacher.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;

public class LeaveApplicationsAdapter extends RecyclerView.Adapter<LeaveApplicationsAdapter.ApplicationViewHolder> {
    
    private static final String TAG = "LeaveApplicationsAdapter";
    
    private List<StaffApplicationModel.Application> applicationsList;
    private Context context;
    private OnApplicationClickListener clickListener;
    
    public interface OnApplicationClickListener {
        void onApplicationClick(StaffApplicationModel.Application application);
        void onApplicationLongClick(StaffApplicationModel.Application application);
    }
    
    public LeaveApplicationsAdapter(List<StaffApplicationModel.Application> applicationsList, Context context) {
        this.applicationsList = applicationsList;
        this.context = context;
        
        // Set click listener if context implements the interface
        if (context instanceof OnApplicationClickListener) {
            this.clickListener = (OnApplicationClickListener) context;
        }
    }
    
    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leave_application, parent, false);
        return new ApplicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        StaffApplicationModel.Application application = applicationsList.get(position);
        holder.bind(application);
    }
    
    @Override
    public int getItemCount() {
        return applicationsList.size();
    }
    
    public class ApplicationViewHolder extends RecyclerView.ViewHolder {
        
        private TextView serialNumber;
        private TextView applicationTitle;
        private TextView applicationStatus;
        private TextView dateRange;
        private TextView applicationBody;
        private TextView tapToViewDetails;
        
        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            serialNumber = itemView.findViewById(R.id.serial_number);
            applicationTitle = itemView.findViewById(R.id.application_title);
            applicationStatus = itemView.findViewById(R.id.application_status);
            dateRange = itemView.findViewById(R.id.date_range);
            applicationBody = itemView.findViewById(R.id.application_body);
            tapToViewDetails = itemView.findViewById(R.id.tap_to_view_details);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onApplicationClick(applicationsList.get(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onApplicationLongClick(applicationsList.get(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(StaffApplicationModel.Application application) {
            // Set serial number (position + 1)
            serialNumber.setText(String.valueOf(getAdapterPosition() + 1));
            
            // Set application title
            if (application.getTitle() != null && !application.getTitle().isEmpty()) {
                applicationTitle.setText(application.getTitle());
            } else {
                applicationTitle.setText("Leave Application");
            }
            
            // Set status (you might want to add status field to your model)
            applicationStatus.setText("Pending");
            
            // Set date range
            String dateRangeText = formatDateRange(application.getStartDate(), application.getEndDate());
            dateRange.setText(dateRangeText);
            
            // Set application body (truncated)
            if (application.getBody() != null && !application.getBody().isEmpty()) {
                String body = application.getBody();
                if (body.length() > 50) {
                    body = body.substring(0, 50) + "...";
                }
                applicationBody.setText(body);
            } else {
                applicationBody.setText("No description provided");
            }
            
            // Set tap to view details text
            tapToViewDetails.setText("Tap to view details →");
        }
        
        private String formatDateRange(String startDate, String endDate) {
            try {
                // Parse dates (assuming dd/MM/yyyy format from your API)
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                Date start = inputFormat.parse(startDate);
                Date end = inputFormat.parse(endDate);
                
                if (start != null && end != null) {
                    String formattedStart = outputFormat.format(start);
                    String formattedEnd = outputFormat.format(end);
                    
                    if (formattedStart.equals(formattedEnd)) {
                        return formattedStart; // Same day
                    } else {
                        return formattedStart + " to " + formattedEnd;
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing dates: " + e.getMessage());
            }
            
            // Fallback to original format
            if (startDate != null && endDate != null) {
                if (startDate.equals(endDate)) {
                    return startDate;
                } else {
                    return startDate + " to " + endDate;
                }
            }
            
            return "Date not specified";
        }
    }
}
