package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.LegderModel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;

public class LegderAdaptor extends RecyclerView.Adapter<LegderAdaptor.Holder> {


    List<LegderModel.Invoice> list;
    View v;

    public LegderAdaptor(List<LegderModel.Invoice> list
    ) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ledger_item, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position % 2 != 0)
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        else
            holder.row.setBackgroundColor(Color.WHITE);

        int new_position = position + 1;
        holder.Sr.setText("" + new_position);
        holder.Date.setText(changeDateFormat(list.get(position).getCreatedDate()));

        holder.Description.setText(list.get(position).getFullName());
        if (list.get(position).getIsType().equals(Constant.Credit)) {
            holder.Debit.setText("");
            holder.Credit.setText(list.get(position).getTotal());
        }
        if (list.get(position).getIsType().equals(Constant.Debit)) {
            holder.Credit.setText("");
            holder.Debit.setText(list.get(position).getTotal());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class Holder extends RecyclerView.ViewHolder {

        TextView Sr, Description, Debit, Credit, Date;
        LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            Sr = itemView.findViewById(R.id.Sr);
            Description = itemView.findViewById(R.id.Description);
            Debit = itemView.findViewById(R.id.Debit);
            Credit = itemView.findViewById(R.id.Credit);
            Date = itemView.findViewById(R.id.Date);
        }
    }

    private String changeDateFormat(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd/MM/yy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        String str = null;

        try {
            Date date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}
