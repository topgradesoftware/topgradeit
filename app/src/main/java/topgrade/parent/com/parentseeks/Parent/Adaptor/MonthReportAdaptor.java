package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.R;

public class MonthReportAdaptor extends RecyclerView.Adapter<MonthReportAdaptor.MonthReportHolder> {
    View v;
    List<ReportModel.Month> list;
    boolean is_empty = false;

    public MonthReportAdaptor(List<ReportModel.Month> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MonthReportAdaptor.MonthReportHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.month_progress_item, viewGroup, false);
        return new MonthReportAdaptor.MonthReportHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthReportAdaptor.MonthReportHolder holder, final int i) {
        if (!is_empty) {
            holder.Month.setText(list.get(i).getMonthName());
            holder.Marks.setText(list.get(i).getObtainedMarks() + "/" + list.get(i).getTotalMarks());
            holder.Position.setText("" + list.get(i).getCp());
            holder.Percentage.setText(list.get(i).getPercentage());
            holder.Attendance.setText(list.get(i).getAttendance());
        }


    }

    @Override
    public int getItemCount() {

        if (list.size() == 0) {
            is_empty = true;
            return 5;
        }
        return list.size();

    }


    public static class MonthReportHolder extends RecyclerView.ViewHolder {

        private final TextView Month;
        private final TextView Marks;
        private final TextView Position;
        private final TextView Percentage;
        private final TextView Attendance;

        public MonthReportHolder(@NonNull View itemView) {
            super(itemView);
            Month = itemView.findViewById(R.id.Month);
            Marks = itemView.findViewById(R.id.Marks);
            Position = itemView.findViewById(R.id.Position);
            Percentage = itemView.findViewById(R.id.Percentage);
            Attendance = itemView.findViewById(R.id.Attendance);


        }


    }
}
