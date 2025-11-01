package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.New.Item;
import topgrade.parent.com.parentseeks.R;

public class ChalanDeatilAdapor extends RecyclerView.Adapter<ChalanDeatilAdapor.Holder> {


    Context context;
    List<Item> list;
    View v;
    OnClickListener onClickListener;


    public ChalanDeatilAdapor(Context context, List<Item> list, OnClickListener onClickListener
    ) {
        this.context = context;
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chalan_detail_item, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.Fee.setText("" + list.get(position).getAmount());
        holder.Arrears.setText("" + list.get(position).getPreviousBalance());
        holder.Discount.setText("" + list.get(position).getDiscount());
        holder.Payable.setText("" + list.get(position).getPayable());
        holder.Payment.setText("" + list.get(position).getPaid());
        holder.Remaining.setText("" + list.get(position).getRemaining());
        holder.fee_detail.setText(list.get(position).getFeeParticularName());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final TextView Chalan_No;
        private final TextView Month;
        private final TextView fee_detail;
        private final TextView Fee;
        private final TextView Arrears;
        private final TextView fine;
        private final TextView Discount;
        private final TextView Payable;
        private final TextView Payment;
        private final TextView Remaining;
        RecyclerView show_detail;

        public Holder(@NonNull View itemView) {
            super(itemView);
            fee_detail = itemView.findViewById(R.id.fee_detail);
            Fee = itemView.findViewById(R.id.Fee);
            Remaining = itemView.findViewById(R.id.Remaining);
            Payment = itemView.findViewById(R.id.Payment);
            Payable = itemView.findViewById(R.id.Payable);
            Discount = itemView.findViewById(R.id.Discount);
            Arrears = itemView.findViewById(R.id.Arrears);
            fine = itemView.findViewById(R.id.fine);
            Month = itemView.findViewById(R.id.Month);
            Chalan_No = itemView.findViewById(R.id.Chalan_No);
            show_detail = itemView.findViewById(R.id.show_detail);


        }


    }
}
