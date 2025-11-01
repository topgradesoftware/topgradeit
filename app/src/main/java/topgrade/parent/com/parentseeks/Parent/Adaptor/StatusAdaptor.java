package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.SubjectAttendeceModel;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.R;

public class StatusAdaptor extends OptimizedRecyclerViewAdapter<SubjectAttendeceModel.Attendance, StatusAdaptor.StatusHolder> {

    private final OnClickListener onClickListener;


    StatusAdaptor(Context context, List<SubjectAttendeceModel.Attendance> list, OnClickListener onClickListener) {
        super(context, list);
        this.onClickListener = onClickListener;
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        return R.layout.subject_status_item;
    }

    @Override
    protected StatusHolder createViewHolder(View view, int viewType) {
        return new StatusHolder(view);
    }

    @Override
    protected void bindViewHolder(StatusHolder holder, SubjectAttendeceModel.Attendance attendance, int position) {
        String attendanceStatus = String.valueOf(attendance.getAttendance());
        applyAttendanceStatus(holder, attendanceStatus);
    }
    
    private void applyAttendanceStatus(StatusHolder holder, String status) {
        switch (status) {
            case "null":
                holder.attendence_status.setText("");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                break;
            case "0":
                holder.attendence_status.setText("OFF");
                holder.row.setBackgroundColor(Color.parseColor("#8B4513"));
                holder.attendence_status.setTextColor(Color.parseColor("#ffffff"));
                break;
            case "1":
                holder.attendence_status.setText("P");
                holder.row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.attendence_status.setTextColor(Color.parseColor("#000000"));
                break;
            case "2":
                holder.attendence_status.setText("A");
                holder.row.setBackgroundColor(Color.parseColor("#DF4242"));
                holder.attendence_status.setTextColor(Color.parseColor("#ffffff"));
                break;
        }
    }

    public class StatusHolder extends RecyclerView.ViewHolder {

        public TextView attendence_status;
        RelativeLayout row;

        StatusHolder(@NonNull View itemView) {
            super(itemView);
            attendence_status = itemView.findViewById(R.id.attendence_status);
            row = itemView.findViewById(R.id.row);

        }
    }
}
