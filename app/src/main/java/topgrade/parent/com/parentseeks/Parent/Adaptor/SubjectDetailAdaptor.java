package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Activity.Report;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener2;
import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.R;

public class SubjectDetailAdaptor extends RecyclerView.Adapter<SubjectDetailAdaptor.SubjectNameHolder> {
    View v;
    List<ReportModel.Detail> list;
    private final OnClickListener2 onClickListener;
    int position;

    public SubjectDetailAdaptor(List<ReportModel.Detail> list, OnClickListener2 onClickListener, int position) {
        this.list = list;
        this.onClickListener = onClickListener;
        this.position = position;


    }

    @NonNull
    @Override
    public SubjectDetailAdaptor.SubjectNameHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_progress_item_2, viewGroup,
                false);
        return new SubjectDetailAdaptor.SubjectNameHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectDetailAdaptor.SubjectNameHolder holder, final int i) {


        try {
            holder.subject_name.setText
                    (

                            list.get(i).getObtainedMarks() + "/" +
                                    list.get(i).getTotalMarks()


                    );

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onItemClick(v, holder.getAdapterPosition(), position);
                }
            });
        } catch (Exception error) {
            error.printStackTrace();

        }


    }

    @Override
    public int getItemCount() {
        return Report.test_name_list.size();


    }


    public class SubjectNameHolder extends RecyclerView.ViewHolder {

        public TextView subject_name;

        public SubjectNameHolder(@NonNull View itemView) {
            super(itemView);
            subject_name = itemView.findViewById(R.id.subject_name);

        }
    }
}
