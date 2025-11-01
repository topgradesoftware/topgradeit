package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener2;
import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.R;

public class SubjectDetailMainAdaptor extends RecyclerView.Adapter<SubjectDetailMainAdaptor.SubjectDetailMainAdaptorHolder> {
    View v;
    List<ReportModel.Exam> list;
    private final OnClickListener2 onClickListener;

    public SubjectDetailMainAdaptor(
            List<ReportModel.Exam> list,
            OnClickListener2 onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SubjectDetailMainAdaptor.SubjectDetailMainAdaptorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_main_progress_item, viewGroup,
                false);
        return new SubjectDetailMainAdaptor.SubjectDetailMainAdaptorHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectDetailMainAdaptor.SubjectDetailMainAdaptorHolder holder, final int i) {
        holder.rcv.setAdapter(new SubjectDetailAdaptor(list.get(i).getDetail(), onClickListener, i));
    }

    @Override
    public int getItemCount() {

        return list.size();

    }


    public class SubjectDetailMainAdaptorHolder extends RecyclerView.ViewHolder {

        public RecyclerView rcv;

        public SubjectDetailMainAdaptorHolder(@NonNull View itemView) {
            super(itemView);
            rcv = itemView.findViewById(R.id.rcv);

        }
    }
}
