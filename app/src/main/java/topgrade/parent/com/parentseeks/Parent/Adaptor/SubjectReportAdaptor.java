package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.R;

public class SubjectReportAdaptor extends RecyclerView.Adapter<SubjectReportAdaptor.SubjectNameHolder> {
    View v;
    List<String> list;
    OnClickListener onClickListener;

    public SubjectReportAdaptor(List<String> list, OnClickListener onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SubjectReportAdaptor.SubjectNameHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_progress_item, viewGroup,
                false);
        return new SubjectReportAdaptor.SubjectNameHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectReportAdaptor.SubjectNameHolder holder, final int i) {

        holder.subject_name.setText(list.get(i));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {

        return list.size();

    }


    public class SubjectNameHolder extends RecyclerView.ViewHolder {

        public TextView subject_name;

        public SubjectNameHolder(@NonNull View itemView) {
            super(itemView);
            subject_name = itemView.findViewById(R.id.subject_name);

        }
    }
}
