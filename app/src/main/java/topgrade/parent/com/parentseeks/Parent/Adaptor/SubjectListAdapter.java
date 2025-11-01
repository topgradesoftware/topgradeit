package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Model.DiaryEntry;
import topgrade.parent.com.parentseeks.R;

public class SubjectListAdapter extends RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder> {

    private final List<DiaryEntry> subjectList;
    private final Context context;

    public SubjectListAdapter(List<DiaryEntry> subjectList, Context context) {
        this.subjectList = subjectList;
        this.context = context;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.subject_diary_item_layout, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        DiaryEntry diaryEntry = subjectList.get(position);
        
        if (diaryEntry != null) {
            holder.tvSubjectName.setText(diaryEntry.getSubjectName() != null ? diaryEntry.getSubjectName() : "N/A");
            holder.tvDescription.setText(diaryEntry.getDescription() != null ? diaryEntry.getDescription() : "N/A");
            holder.tvDate.setText(diaryEntry.getDate() != null ? diaryEntry.getDate() : "N/A");
            holder.tvTeacherName.setText(diaryEntry.getTeacherName() != null ? diaryEntry.getTeacherName() : "N/A");
        }
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvDescription, tvDate, tvTeacherName;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTeacherName = itemView.findViewById(R.id.tv_teacher_name);
        }
    }
} 