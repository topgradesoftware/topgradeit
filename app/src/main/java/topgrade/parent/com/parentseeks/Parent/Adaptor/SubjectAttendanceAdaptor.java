package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.SubjectAttendeceModel;
import topgrade.parent.com.parentseeks.R;

public class SubjectAttendanceAdaptor extends RecyclerView.Adapter<SubjectAttendanceAdaptor.SubjectHolder> {

    View v;
    List<SubjectAttendeceModel.AttendanceList> list;
    OnClickListener onClickListener;
    List<String> dateList;

    public SubjectAttendanceAdaptor(List<SubjectAttendeceModel.AttendanceList> list, List<String> dateList, OnClickListener onClickListener) {
        this.list = list;
        this.dateList = dateList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SubjectHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_row_item, viewGroup, false);
        return new SubjectHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectHolder holder, int position) {
        SubjectAttendeceModel.AttendanceList attendanceList = list.get(position);
        List<SubjectAttendeceModel.Attendance> attendances = attendanceList.getAttendance();
        
        // Clear existing views
        holder.attendanceContainer.removeAllViews();
        
        // Add attendance status for each date
        for (int i = 0; i < dateList.size(); i++) {
            TextView statusView = new TextView(holder.itemView.getContext());
            statusView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            statusView.setPadding(8, 8, 8, 8);
            statusView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            statusView.setTextSize(12);
            
            // Set status based on attendance data
            if (i < attendances.size()) {
                String status = String.valueOf(attendances.get(i).getAttendance());
                applyStatusStyle(statusView, status);
            } else {
                applyStatusStyle(statusView, "null");
            }
            
            holder.attendanceContainer.addView(statusView);
        }
    }
    
    private void applyStatusStyle(TextView textView, String status) {
        switch (status) {
            case "null":
                textView.setText("");
                textView.setBackgroundColor(0xFFFFFFFF);
                textView.setTextColor(0xFF000000);
                break;
            case "0":
                textView.setText("OFF");
                textView.setBackgroundColor(0xFF8B4513); // Dark brown
                textView.setTextColor(0xFFFFFFFF);
                break;
            case "1":
                textView.setText("P");
                textView.setBackgroundColor(0xFFFFFFFF);
                textView.setTextColor(0xFF000000);
                break;
            case "2":
                textView.setText("A");
                textView.setBackgroundColor(0xFFDF4242);
                textView.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SubjectHolder extends RecyclerView.ViewHolder {
        public LinearLayout attendanceContainer;

        SubjectHolder(@NonNull View itemView) {
            super(itemView);
            attendanceContainer = itemView.findViewById(R.id.attendance_container);
        }
    }
}
