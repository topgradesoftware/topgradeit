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
        if (list == null || position >= list.size() || list.get(position) == null) {
            android.util.Log.e("FeedbackAdaptor", "Invalid position or null item at position: " + position);
            return;
        }
        
        Feedback feedback = list.get(position);
        android.util.Log.d("FeedbackAdaptor", "Binding position " + position + ": " + 
            "Teacher=" + feedback.getTeacherName() + 
            ", Subject=" + feedback.getSubject_name() + 
            ", Feedback=" + feedback.getFeedback());
        
        try {
            String teacherName = feedback.getTeacherName() != null ? feedback.getTeacherName() : "Unknown";
            String subjectName = feedback.getSubject_name() != null ? feedback.getSubject_name() : "";
            
            // Try to use string resource, fallback to concatenation if not available
            String teacherWithSubject;
            try {
                teacherWithSubject = holder.itemView.getContext().getString(
                    R.string.teacher_with_subject,
                    teacherName,
                    subjectName
                );
            } catch (Exception e) {
                // Fallback if string resource doesn't exist
                teacherWithSubject = teacherName + (subjectName.isEmpty() ? "" : " - " + subjectName);
            }
            holder.teacher_name.setText(teacherWithSubject);
            
            String feedbackText = feedback.getFeedback() != null ? feedback.getFeedback() : "";
            holder.feeback_body.setText(feedbackText);
            
            if (feedback.getTimestamp() != null && !feedback.getTimestamp().isEmpty()) {
                String time = Util.formatDate("yyyy-MM-dd h:mm:ss",
                        "dd-MM-yyyy hh:mm:a", feedback.getTimestamp());
                holder.date.setText(time);
            } else {
                holder.date.setText("");
            }
        } catch (Exception e) {
            android.util.Log.e("FeedbackAdaptor", "Error binding view at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = list != null ? list.size() : 0;
        android.util.Log.d("FeedbackAdaptor", "getItemCount: " + count);
        return count;
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
