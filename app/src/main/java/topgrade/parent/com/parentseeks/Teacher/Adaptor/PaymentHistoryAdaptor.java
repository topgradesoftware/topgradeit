package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class PaymentHistoryAdaptor extends RecyclerView.Adapter<PaymentHistoryAdaptor.ViewHolder> {

    private List<SalaryModel.Salary> paymentList;
    private Context context;

    public PaymentHistoryAdaptor(List<SalaryModel.Salary> paymentList, Context context) {
        this.paymentList = paymentList;
        this.context = context;
    }
    
    public void updateData(List<SalaryModel.Salary> newPaymentList) {
        this.paymentList = newPaymentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalaryModel.Salary payment = paymentList.get(position);
        
        // Set background color for alternating rows
        if (position % 2 != 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#DFE5E2"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        
        // Set month name
        if (payment.getSalaryMonthName() != null && !payment.getSalaryMonthName().isEmpty()) {
            holder.monthText.setText(payment.getSalaryMonthName());
        } else {
            holder.monthText.setText("N/A");
        }
        
        // Set payable amount
        if (payment.getPayable() != null) {
            holder.payableText.setText("" + payment.getPayable());
        } else {
            holder.payableText.setText("0");
        }
        
        // Set paid amount
        if (payment.getPaid() != null) {
            holder.paidText.setText("" + payment.getPaid());
        } else {
            holder.paidText.setText("0");
        }
        
        // Set remaining amount
        if (payment.getRemaining() != null) {
            holder.remainingText.setText("" + payment.getRemaining());
        } else {
            holder.remainingText.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView monthText, payableText, paidText, remainingText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            monthText = itemView.findViewById(R.id.month_text);
            payableText = itemView.findViewById(R.id.payable_text);
            paidText = itemView.findViewById(R.id.paid_text);
            remainingText = itemView.findViewById(R.id.remaining_text);
        }
    }
}
