package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.ComplaintModel;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    private List<ComplaintModel.Complaint> complaints;
    private Context context;
    private String filterType;

    public ComplaintAdapter(List<ComplaintModel.Complaint> complaints, Context context, String filterType) {
        this.complaints = complaints;
        this.context = context;
        this.filterType = filterType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.complaint_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComplaintModel.Complaint complaint = complaints.get(position);
        
        // Set serial number
        if (holder.serialNumber != null) {
            holder.serialNumber.setText(String.valueOf(position + 1));
        }
        
        holder.titleText.setText(complaint.getComplaintTitle());
        holder.descriptionText.setText(complaint.getComplaintDescription());
        holder.dateText.setText(complaint.getComplaintDate());
        
        // Update header color to navy blue (staff theme)
        updateHeaderColor(holder.itemView);
        
        // Set status badge with colors (same as leave applications)
        updateStatusBadge(holder, complaint);
    }
    
    /**
     * Update status badge with colors similar to leave applications
     * Pending -> Orange, Solved -> Green, Under Discussion -> Orange
     */
    private void updateStatusBadge(ViewHolder holder, ComplaintModel.Complaint complaint) {
        if (holder.statusText != null) {
            String status = complaint.getComplaintStatus();
            if (status != null) {
                String statusLower = status.toLowerCase();
                
                if (statusLower.contains("solved") || statusLower.contains("resolved")) {
                    holder.statusText.setText("Solved");
                    holder.statusText.setBackgroundTintList(context.getResources().getColorStateList(R.color.success_500));
                } else if (statusLower.contains("discussion") || statusLower.contains("progress")) {
                    holder.statusText.setText("Under Discussion");
                    holder.statusText.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));
                } else {
                    holder.statusText.setText("Pending");
                    holder.statusText.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));
                }
                
                // Text color always white for visibility on colored background
                holder.statusText.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }
    
    /**
     * Update header color - all staff complaint cards use navy blue (staff theme)
     */
    private void updateHeaderColor(View itemView) {
        View headerLayout = itemView.findViewById(R.id.header_layout);
        if (headerLayout != null) {
            // All complaint cards use staff theme (navy blue) for consistency
            headerLayout.setBackgroundColor(context.getResources().getColor(R.color.navy_blue));
        }
    }

    @Override
    public int getItemCount() {
        return complaints != null ? complaints.size() : 0;
    }

    public void updateData(List<ComplaintModel.Complaint> newComplaints) {
        this.complaints = newComplaints;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serialNumber;
        TextView titleText;
        TextView descriptionText;
        TextView statusText;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serialNumber = itemView.findViewById(R.id.serial_number);
            titleText = itemView.findViewById(R.id.complaint_title);
            descriptionText = itemView.findViewById(R.id.complaint_description);
            statusText = itemView.findViewById(R.id.complaint_status);
            dateText = itemView.findViewById(R.id.complaint_date);
        }
    }
}
