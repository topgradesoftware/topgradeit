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

public class DiaryListAdapter extends RecyclerView.Adapter<DiaryListAdapter.DiaryViewHolder> {

    private final List<DiaryEntry> diaryList;
    private final Context context;

    public DiaryListAdapter(List<DiaryEntry> diaryList, Context context) {
        this.diaryList = diaryList;
        this.context = context;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.diary_item_layout, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry diaryEntry = diaryList.get(position);
        
        if (diaryEntry != null) {
            holder.tvSubjectName.setText(diaryEntry.getSubjectName() != null ? diaryEntry.getSubjectName() : "N/A");
            holder.tvClassName.setText(diaryEntry.getClassName() != null ? diaryEntry.getClassName() : "N/A");
            holder.tvSectionName.setText(diaryEntry.getSectionName() != null ? diaryEntry.getSectionName() : "N/A");
            holder.tvStudentName.setText(diaryEntry.getStudentName() != null ? diaryEntry.getStudentName() : "N/A");
            holder.tvDescription.setText(diaryEntry.getDescription() != null ? diaryEntry.getDescription() : "N/A");
            holder.tvDate.setText(diaryEntry.getDate() != null ? diaryEntry.getDate() : "N/A");
            holder.tvTeacherName.setText(diaryEntry.getTeacherName() != null ? diaryEntry.getTeacherName() : "N/A");
        }
    }

    @Override
    public int getItemCount() {
        return diaryList != null ? diaryList.size() : 0;
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvClassName, tvSectionName, tvStudentName, tvDescription, tvDate, tvTeacherName;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvSectionName = itemView.findViewById(R.id.tv_section_name);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTeacherName = itemView.findViewById(R.id.tv_teacher_name);
        }
    }
} 