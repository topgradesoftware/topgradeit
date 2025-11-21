package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Activity.DiaryDetailActivity;
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
            // Set class name
            holder.tvClassName.setText(diaryEntry.getClassName() != null ? diaryEntry.getClassName() : "N/A");
            
            // Set subject name (for subject diary) - show only if available
            if (holder.tvSubjectName != null) {
                String subjectName = diaryEntry.getSubjectName();
                if (subjectName != null && !subjectName.isEmpty()) {
                    holder.tvSubjectName.setText(subjectName);
                    holder.tvSubjectName.setVisibility(android.view.View.VISIBLE);
                } else {
                    holder.tvSubjectName.setVisibility(android.view.View.GONE);
                }
            }
            
            // Set date - format if needed
            String dateText = diaryEntry.getDate() != null ? diaryEntry.getDate() : "N/A";
            holder.tvDate.setText(dateText);
            
            // Set description/body
            String description = diaryEntry.getDescription() != null ? diaryEntry.getDescription() : 
                               (diaryEntry.getBody() != null ? diaryEntry.getBody() : "");
            holder.tvDescription.setText(description.isEmpty() ? "" : description);
            
            // Set click listener on the entire card to open detail view
            holder.itemView.setOnClickListener(v -> {
                try {
                    // Convert diary entry to JSON and pass to detail activity
                    Gson gson = new Gson();
                    String diaryEntryJson = gson.toJson(diaryEntry);
                    
                    Intent intent = new Intent(context, DiaryDetailActivity.class);
                    intent.putExtra("DIARY_ENTRY", diaryEntryJson);
                    context.startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("DiaryListAdapter", "Error opening diary detail", e);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return diaryList != null ? diaryList.size() : 0;
    }

    public static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvDate, tvDescription, tvSubjectName;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
        }
    }
} 