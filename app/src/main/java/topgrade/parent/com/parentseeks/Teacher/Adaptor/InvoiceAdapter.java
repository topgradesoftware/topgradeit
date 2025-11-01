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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Invoice_Model;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.Holder> {

    private List<Invoice_Model.InvoiceItem> invoiceItems;

    public InvoiceAdapter(List<Invoice_Model.InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ledger_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Invoice_Model.InvoiceItem item = invoiceItems.get(position);
        
        // Set alternating row colors
        if (position % 2 != 0) {
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        } else {
            holder.row.setBackgroundColor(Color.WHITE);
        }

        // Set data
        holder.serial.setText(item.getSerial());
        holder.description.setText(item.getDescription());
        holder.debit.setText(item.getDebit());
        holder.credit.setText(item.getCredit());
        holder.date.setText(changeDateFormat(item.getDate()));
    }

    @Override
    public int getItemCount() {
        return invoiceItems.size();
    }

    public void updateData(List<Invoice_Model.InvoiceItem> newInvoiceItems) {
        this.invoiceItems = newInvoiceItems;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView serial, description, debit, credit, date;
        LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            serial = itemView.findViewById(R.id.Sr);
            description = itemView.findViewById(R.id.Description);
            debit = itemView.findViewById(R.id.Debit);
            credit = itemView.findViewById(R.id.Credit);
            date = itemView.findViewById(R.id.Date);
        }
    }

    private String changeDateFormat(String time) {
        if (time == null || time.isEmpty()) {
            return "N/A";
        }
        
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMM, yy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // Return original string if parsing fails
        }
    }
}
