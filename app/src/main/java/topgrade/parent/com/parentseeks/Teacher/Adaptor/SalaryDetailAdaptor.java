package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class SalaryDetailAdaptor extends RecyclerView.Adapter<SalaryDetailAdaptor.Holder> {


    List<SalaryModel.Item> list;
    View v;

    public SalaryDetailAdaptor(List<SalaryModel.Item> list
    ) {
        this.list = list;
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.salary_item, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.Head.setText(list.get(position).getSalaryHeadName());
        holder.Amount.setText("" + list.get(position).getAmount());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private TextView Head, Amount;

        public Holder(@NonNull View itemView) {
            super(itemView);
            Head = itemView.findViewById(R.id.Head);
            Amount = itemView.findViewById(R.id.Amount);

        }

    }

}
