package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.SalaryModel;

public class SalaryAdaptor extends RecyclerView.Adapter<SalaryAdaptor.Holder> {

    private static final String TAG = "SalaryAdaptor";
    List<SalaryModel.Salary> list;
    View v;

    public SalaryAdaptor(List<SalaryModel.Salary> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        try {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.salary_item_, viewGroup, false);
            return new Holder(v);
        } catch (Exception e) {
            Log.e(TAG, "Error creating view holder: " + e.getMessage(), e);
            // Return a simple fallback view
            View fallbackView = new View(viewGroup.getContext());
            return new Holder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
        try {
            if (position % 2 != 0)
                holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
            else
                holder.row.setBackgroundColor(Color.WHITE);

            if (list != null && position < list.size() && list.get(position) != null) {
                SalaryModel.Salary salary = list.get(position);
                holder.Paid.setText("" + salary.getPaid());
                holder.Salary_Month.setText("" + (salary.getSalaryMonthName() != null ? salary.getSalaryMonthName() : ""));
                holder.Payable.setText("" + salary.getPayable());
                holder.Remain.setText("" + salary.getRemaining());
            } else {
                // Set default values if data is null
                holder.Paid.setText("0");
                holder.Salary_Month.setText("");
                holder.Payable.setText("0");
                holder.Remain.setText("0");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position + ": " + e.getMessage(), e);
            // Set safe default values
            try {
                holder.Paid.setText("0");
                holder.Salary_Month.setText("");
                holder.Payable.setText("0");
                holder.Remain.setText("0");
                holder.row.setBackgroundColor(Color.WHITE);
            } catch (Exception ex) {
                Log.e(TAG, "Error setting default values: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView Salary_Month, Payable, Paid, Remain;
        LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            try {
                Salary_Month = itemView.findViewById(R.id.Salary_Month);
                Payable = itemView.findViewById(R.id.Payable);
                Paid = itemView.findViewById(R.id.Paid);
                Remain = itemView.findViewById(R.id.Remain);
                row = itemView.findViewById(R.id.row);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing view holder: " + e.getMessage(), e);
                // Initialize with null to prevent crashes
                Salary_Month = null;
                Payable = null;
                Paid = null;
                Remain = null;
                row = null;
            }
        }
    }
}
