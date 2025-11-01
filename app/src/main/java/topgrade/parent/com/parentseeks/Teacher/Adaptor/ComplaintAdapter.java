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
        
        holder.titleText.setText(complaint.getComplaintTitle());
        holder.descriptionText.setText(complaint.getComplaintDescription());
        holder.statusText.setText(complaint.getComplaintStatus());
        holder.dateText.setText(complaint.getComplaintDate());
        
        // Set status color based on complaint status
        String status = complaint.getComplaintStatus().toLowerCase();
        if (status.contains("pending")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.warning_500));
        } else if (status.contains("solved") || status.contains("resolved")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.success_500));
        } else if (status.contains("discussion") || status.contains("progress")) {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.error_500));
        } else {
            holder.statusText.setTextColor(context.getResources().getColor(R.color.gray));
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
        TextView titleText;
        TextView descriptionText;
        TextView statusText;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.complaint_title);
            descriptionText = itemView.findViewById(R.id.complaint_description);
            statusText = itemView.findViewById(R.id.complaint_status);
            dateText = itemView.findViewById(R.id.complaint_date);
        }
    }
}
