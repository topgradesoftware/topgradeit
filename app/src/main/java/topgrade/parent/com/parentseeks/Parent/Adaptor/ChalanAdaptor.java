package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.Parent.Model.New.Challan;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.R;

public class ChalanAdaptor extends OptimizedRecyclerViewAdapter<Challan, ChalanAdaptor.Holder> implements OnClickListener {

    private final OnClickListener onClickListener;
    
    // Cached date formatter for better performance
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMMM-yy");

    public ChalanAdaptor(Context context, List<Challan> list, OnClickListener onClickListener) {
        super(context, list);
        this.onClickListener = onClickListener;
    }


    @Override
    protected int getLayoutResourceId(int viewType) {
        return R.layout.chalan_item;
    }

    @Override
    protected Holder createViewHolder(View view, int viewType) {
        return new Holder(view);
    }

    @Override
    protected void bindViewHolder(Holder holder, Challan challan, int position) {
        holder.Chalan_No.setText(String.valueOf(challan.getChalanId()));
        holder.Month.setText(challan.getFeeParticularRecurringName());
        holder.Fee.setText(String.valueOf(challan.getAmount()));
        holder.Arrears.setText(String.valueOf(challan.getPreviousBalance()));
        holder.Discount.setText(String.valueOf(challan.getDiscount()));
        holder.Payable.setText(String.valueOf(challan.getPayable()));
        holder.Payment.setText(String.valueOf(challan.getPaid()));
        holder.Remaining.setText(String.valueOf(challan.getRemaining()));
        
        holder.fine.setText(String.valueOf(challan.getFine()));
        
        // Use challan generation date (timestamp) as due date
        String dueDate = changeDateFormat(challan.getTimestamp());
        String validDate = changeDateFormat(challan.getValidityDate());
        String paidDate = changeDateFormat(challan.getPaidDate());
        
        // Show "N/A" if paid date is empty
        if (paidDate.isEmpty()) {
            paidDate = "N/A";
        }
        
        holder.fee_detail.setText("Due: " + dueDate + "\n" +
                "Valid: " + validDate + "\n" +
                "Paid: " + paidDate
        );

        holder.show_detail.setAdapter(new ChalanDeatilAdapor(context, challan.getItems(), ChalanAdaptor.this));
        
        holder.Chalan_No.setOnClickListener(v -> {
            if (holder.show_detail.getVisibility() == View.GONE) {
                holder.show_detail.setVisibility(View.VISIBLE);
                holder.Chalan_No.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            } else {
                holder.show_detail.setVisibility(View.GONE);
                holder.Chalan_No.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
            }
            
            // Notify adapter that item has changed to trigger layout recalculation
            notifyItemChanged(position);
        });
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    public class Holder extends RecyclerView.ViewHolder {

        private final TextView Chalan_No;
        private final TextView Month;
        private final TextView Fee;
        private final TextView Arrears;
        private final TextView fine;
        private final TextView Discount;
        private final TextView Payable;
        private final TextView Payment;
        private final TextView Remaining;
        private final TextView fee_detail;
        RecyclerView show_detail;

        public Holder(@NonNull View itemView) {
            super(itemView);
            Remaining = itemView.findViewById(R.id.Remaining);
            Payment = itemView.findViewById(R.id.Payment);
            Payable = itemView.findViewById(R.id.Payable);
            Discount = itemView.findViewById(R.id.Discount);
            Arrears = itemView.findViewById(R.id.Arrears);
            Fee = itemView.findViewById(R.id.Fee);
            Month = itemView.findViewById(R.id.Month);
            Chalan_No = itemView.findViewById(R.id.Chalan_No);
            show_detail = itemView.findViewById(R.id.show_detail);
            fee_detail = itemView.findViewById(R.id.fee_detail);
            fine = itemView.findViewById(R.id.fine);


        }


    }

    private String changeDateFormat(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return time; // Return original string if parsing fails
        }
    }
}
