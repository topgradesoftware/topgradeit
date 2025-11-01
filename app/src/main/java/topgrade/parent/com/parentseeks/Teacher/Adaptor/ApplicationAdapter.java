package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.Application.StaffAddApplication;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffApplicationModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class ApplicationAdapter extends ListAdapter<StaffApplicationModel.Application, ApplicationAdapter.Holder> {

    private Context context;
    private String filterType = "";

    public static final DiffUtil.ItemCallback<StaffApplicationModel.Application> DIFF_CALLBACK = new DiffUtil.ItemCallback<StaffApplicationModel.Application>() {
        @Override
        public boolean areItemsTheSame(@NonNull StaffApplicationModel.Application oldItem, @NonNull StaffApplicationModel.Application newItem) {
            return oldItem.getTitleId().equals(newItem.getTitleId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull StaffApplicationModel.Application oldItem, @NonNull StaffApplicationModel.Application newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getBody().equals(newItem.getBody()) &&
                   oldItem.getIsActive().equals(newItem.getIsActive());
        }
    };

    public ApplicationAdapter(List<StaffApplicationModel.Application> list, Context context, String filterType) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.filterType = filterType;
        submitList(list);
    }

    @NonNull
    @Override
    public ApplicationAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_application_item, parent, false);
        return new ApplicationAdapter.Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationAdapter.Holder holder, int position) {
        try {
            StaffApplicationModel.Application application = getItem(position);
            
            // Set serial number
            if (holder.serialNumber != null) {
                holder.serialNumber.setText(String.valueOf(position + 1));
            }
            
            if (holder.applicationTitle != null) {
                holder.applicationTitle.setText(application.getTitle());
            }
            
            // Check application status
            String isActive = application.getIsActive();
            boolean isApproved = "1".equals(isActive);
            boolean isRejected = "2".equals(isActive);
            
            // Hide body for approved and rejected applications
            if (holder.applicationBody != null) {
                if (isApproved || isRejected) {
                    holder.applicationBody.setVisibility(View.GONE);
                } else {
                    holder.applicationBody.setVisibility(View.VISIBLE);
                    holder.applicationBody.setText(application.getBody());
                }
            }
            
            // Set date range
            if (holder.dateRangeText != null) {
                String dateRange = application.getStartDate() + " to " + application.getEndDate();
                holder.dateRangeText.setText(dateRange);
            }
            
            // Set status badge
            updateStatusBadge(holder, application);
            
            // Set header color based on filter type and status
            updateHeaderColor(holder, application);
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> openApplicationDetails(application));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateStatusBadge(ApplicationAdapter.Holder holder, StaffApplicationModel.Application application) {
        if (holder.statusBadge != null) {
            String isActive = application.getIsActive();
            
            if ("1".equals(isActive)) {
                holder.statusBadge.setText("Approved");
                holder.statusBadge.setBackgroundTintList(context.getResources().getColorStateList(R.color.success_500));
            } else if ("2".equals(isActive)) {
                holder.statusBadge.setText("Rejected");
                holder.statusBadge.setBackgroundTintList(context.getResources().getColorStateList(R.color.error_500));
            } else {
                holder.statusBadge.setText("Pending");
                holder.statusBadge.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));
            }
        }
    }
    
    private void updateHeaderColor(ApplicationAdapter.Holder holder, StaffApplicationModel.Application application) {
        View headerLayout = holder.itemView.findViewById(R.id.header_layout);
        if (headerLayout != null) {
            int backgroundColor;
            
            // If viewing all applications, color by application status
            if (Constant.FILTER_LEAVE_ALL.equals(filterType)) {
                String isActive = application.getIsActive();
                
                if ("1".equals(isActive)) {
                    backgroundColor = context.getResources().getColor(R.color.success_500);
                } else if ("2".equals(isActive)) {
                    backgroundColor = context.getResources().getColor(R.color.error_500);
                } else {
                    backgroundColor = context.getResources().getColor(R.color.orange);
                }
            } else {
                // For filtered views, use the filter type color
                if (Constant.FILTER_LEAVE_PENDING.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.orange);
                } else if (Constant.FILTER_LEAVE_APPROVED.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.success_500);
                } else if (Constant.FILTER_LEAVE_REJECTED.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.error_500);
                } else {
                    backgroundColor = context.getResources().getColor(R.color.navy_blue);
                }
            }
            
            headerLayout.setBackgroundColor(backgroundColor);
        }
    }
    
    private void openApplicationDetails(StaffApplicationModel.Application application) {
        Intent intent = new Intent(context, StaffAddApplication.class);
        intent.putExtra("application_id", application.getTitleId());
        intent.putExtra("application_title", application.getTitle());
        intent.putExtra("application_body", application.getBody());
        intent.putExtra("start_date", application.getStartDate());
        intent.putExtra("end_date", application.getEndDate());
        intent.putExtra("is_active", application.getIsActive());
        intent.putExtra("view_mode", true); // View mode, not create mode
        intent.putExtra("HEADER_TITLE", "Application Details");
        context.startActivity(intent);
    }

    public void updateList(List<StaffApplicationModel.Application> newList) {
        submitList(newList);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView serialNumber;
        private final TextView applicationTitle;
        private final TextView dateRangeText;
        private final TextView applicationBody;
        private final TextView statusBadge;

        public Holder(@NonNull View itemView) {
            super(itemView);

            serialNumber = itemView.findViewById(R.id.serial_number);
            applicationTitle = itemView.findViewById(R.id.application_title);
            dateRangeText = itemView.findViewById(R.id.date_range_text);
            applicationBody = itemView.findViewById(R.id.application_body);
            statusBadge = itemView.findViewById(R.id.status_badge);
        }
    }
}

