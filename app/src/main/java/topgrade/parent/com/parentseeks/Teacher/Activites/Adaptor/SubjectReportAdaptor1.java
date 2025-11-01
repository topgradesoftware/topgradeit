package topgrade.parent.com.parentseeks.Teacher.Activites.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;

public class SubjectReportAdaptor1 extends RecyclerView.Adapter<SubjectReportAdaptor1.SubjectNameHolder> {
    View v;
    List<String> list;

    public SubjectReportAdaptor1(List<String> list) {
        this.list = list;

    }

    @NonNull
    @Override
    public SubjectReportAdaptor1.SubjectNameHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_progress_item3, viewGroup,
                false);
        return new SubjectReportAdaptor1.SubjectNameHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectReportAdaptor1.SubjectNameHolder holder, final int i) {

        holder.subject_name.setText(list.get(i));
    }

    @Override
    public int getItemCount() {

        return list.size();

    }


    public class SubjectNameHolder extends RecyclerView.ViewHolder {

        public TextView subject_name;

        public SubjectNameHolder(@NonNull View itemView) {
            super(itemView);
            subject_name = itemView.findViewById(R.id.Subjects);

        }
    }
}
