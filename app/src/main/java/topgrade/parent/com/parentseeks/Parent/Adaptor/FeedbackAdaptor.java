package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Feedback;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class FeedbackAdaptor extends RecyclerView.Adapter<FeedbackAdaptor.Holder> {

    List<Feedback> list;
    View v;

    public FeedbackAdaptor(List<Feedback> list) {
        this.list = list;
    }


    @NonNull
    @Override
    public FeedbackAdaptor.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_feedback_layout, parent, false);
        return new FeedbackAdaptor.Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdaptor.Holder holder, int position) {
        String teacherWithSubject = holder.itemView.getContext().getString(
                R.string.teacher_with_subject,
                list.get(position).getTeacherName(),
                list.get(position).getSubject_name()
        );
        holder.teacher_name.setText(teacherWithSubject);
        holder.feeback_body.setText(list.get(position).getFeedback());
        String time = Util.formatDate("yyyy-MM-dd h:mm:ss",
                "dd-MM-yyyy hh:mm:a", list.get(position).getTimestamp());
        holder.date.setText(time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView teacher_name;
        private final TextView feeback_body;
        private final TextView date;

        public Holder(@NonNull View v) {
            super(v);
            feeback_body = v.findViewById(R.id.feeback_body);
            teacher_name = v.findViewById(R.id.teacher_name);
            date = v.findViewById(R.id.date);
        }
    }
}
