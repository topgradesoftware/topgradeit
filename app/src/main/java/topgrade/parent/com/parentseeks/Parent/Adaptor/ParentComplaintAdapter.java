package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Parent.Model.ParentComplaintModel;

public class ParentComplaintAdapter extends RecyclerView.Adapter<ParentComplaintAdapter.ViewHolder> {

    private List<ParentComplaintModel.Complaint> complaints;
    private Context context;
    private String filterType;
    private OnComplaintActionListener actionListener;

    public interface OnComplaintActionListener {
        void onDeleteComplaint(String complaintId, int position);
    }

    public ParentComplaintAdapter(List<ParentComplaintModel.Complaint> complaints, Context context, String filterType) {
        this.complaints = complaints;
        this.context = context;
        this.filterType = filterType;
    }

    public void setActionListener(OnComplaintActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.parent_complaint_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParentComplaintModel.Complaint complaint = complaints.get(position);
        
        holder.titleText.setText(complaint.getComplaintTitle() != null ? complaint.getComplaintTitle() : "");
        holder.descriptionText.setText(complaint.getComplaintDescription() != null ? complaint.getComplaintDescription() : "");
        
        // Handle null status safely
        String complaintStatus = complaint.getComplaintStatus();
        if (complaintStatus == null || complaintStatus.isEmpty()) {
            complaintStatus = "Unknown";
        }
        holder.statusText.setText(complaintStatus);
        holder.dateText.setText(formatDate(complaint.getComplaintDate()));
        
        // Set status color based on complaint status
        String status = complaintStatus.toLowerCase();
        if (status.contains("pending")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.warning_500));
        } else if (status.contains("solved") || status.contains("resolved")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.success_500));
        } else if (status.contains("discussion") || status.contains("progress")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.error_500));
        } else {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.gray));
        }
        
        // Show/hide response section
        if (complaint.getResponse() != null && !complaint.getResponse().isEmpty()) {
            holder.responseSection.setVisibility(View.VISIBLE);
            holder.responseText.setText(complaint.getResponse());
            holder.responseDateText.setText(formatDate(complaint.getResponseDate()));
        } else {
            holder.responseSection.setVisibility(View.GONE);
        }
        
        // Set up menu click listener
        holder.menuIcon.setOnClickListener(v -> showComplaintMenu(v, complaint, position));
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        
        try {
            // Convert "2024-01-15" to "15 Jan, 24"
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd MMM, yy", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateStr);
            return date != null ? outputFormat.format(date) : dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private void showComplaintMenu(View view, ParentComplaintModel.Complaint complaint, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.complaint_menu);
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                if (actionListener != null) {
                    actionListener.onDeleteComplaint(complaint.getComplaintId(), position);
                }
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return complaints != null ? complaints.size() : 0;
    }

    public void updateData(List<ParentComplaintModel.Complaint> newComplaints) {
        this.complaints = newComplaints;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView statusText;
        TextView dateText;
        ImageView menuIcon;
        View responseSection;
        TextView responseText;
        TextView responseDateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.complaint_title);
            descriptionText = itemView.findViewById(R.id.complaint_description);
            statusText = itemView.findViewById(R.id.complaint_status);
            dateText = itemView.findViewById(R.id.complaint_date);
            menuIcon = itemView.findViewById(R.id.complaint_menu);
            responseSection = itemView.findViewById(R.id.response_section);
            responseText = itemView.findViewById(R.id.response_text);
            responseDateText = itemView.findViewById(R.id.response_date);
        }
    }
}

