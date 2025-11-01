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
import topgrade.parent.com.parentseeks.Teacher.Model.AdvancedSalaryModel;

public class AdvanceSalaryAdaptor extends RecyclerView.Adapter<AdvanceSalaryAdaptor.Holder> {

    List<AdvancedSalaryModel.Advanced> list;
    List<AdvancedSalaryModel.Advanced> filteredList;
    View v;

    public AdvanceSalaryAdaptor(List<AdvancedSalaryModel.Advanced> list) {
        this.list = list;
        this.filteredList = filterValidData(list);
    }

    /**
     * Filter out rows that have empty or null data
     * Only show rows with actual values
     */
    private List<AdvancedSalaryModel.Advanced> filterValidData(List<AdvancedSalaryModel.Advanced> originalList) {
        List<AdvancedSalaryModel.Advanced> validData = new ArrayList<>();
        
        if (originalList != null) {
            for (AdvancedSalaryModel.Advanced item : originalList) {
                // Check if the row has valid data
                if (isValidRow(item)) {
                    validData.add(item);
                }
            }
        }
        
        return validData;
    }

    /**
     * Check if a row contains valid data
     * Returns true if the row has meaningful data, false if empty/null
     */
    private boolean isValidRow(AdvancedSalaryModel.Advanced item) {
        if (item == null) {
            return false;
        }

        // Check if fullName is not null and not empty
        boolean hasValidName = item.getFullName() != null && 
                              !item.getFullName().trim().isEmpty() && 
                              !item.getFullName().equals("null");

        // Check if amount is not null and greater than 0
        boolean hasValidAmount = item.getAmount() != null && item.getAmount() > 0;

        // Check if deduction is not null and greater than 0
        boolean hasValidDeduction = item.getDeduction() != null && item.getDeduction() > 0;

        // Check if month name is not null and not empty
        boolean hasValidMonth = item.getSalaryMonthName() != null && 
                               !item.getSalaryMonthName().trim().isEmpty() && 
                               !item.getSalaryMonthName().equals("null");

        // Check if date is not null and not empty
        boolean hasValidDate = item.getCreatedDate() != null && 
                              !item.getCreatedDate().trim().isEmpty() && 
                              !item.getCreatedDate().equals("null");

        // Return true only if the row has meaningful data
        return hasValidName && hasValidAmount && hasValidDeduction && hasValidMonth && hasValidDate;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.advanced_salary_layout, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position % 2 != 0)
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        else
            holder.row.setBackgroundColor(Color.WHITE);

        AdvancedSalaryModel.Advanced item = filteredList.get(position);

        // Set data with null checks
        holder.reson.setText(item.getFullName() != null ? item.getFullName() : "");
        holder.Month.setText(item.getSalaryMonthName() != null ? item.getSalaryMonthName() : "");
        holder.Date.setText(item.getCreatedDate() != null ? changeDateFormat(item.getCreatedDate()) : "");
        holder.Deduction.setText(item.getDeduction() != null ? String.valueOf(item.getDeduction()) : "0");
        holder.Amount.setText(item.getAmount() != null ? String.valueOf(item.getAmount()) : "0");
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    /**
     * Get the filtered list for external use (e.g., calculating totals)
     */
    public List<AdvancedSalaryModel.Advanced> getFilteredList() {
        return filteredList;
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView reson, Month, Date, Amount, Deduction;
        LinearLayout row;

        Holder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            reson = itemView.findViewById(R.id.reson);
            Month = itemView.findViewById(R.id.Month);
            Date = itemView.findViewById(R.id.Date);
            Amount = itemView.findViewById(R.id.Amount);
            Deduction = itemView.findViewById(R.id.Deduction);
        }
    }

    private String changeDateFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "";
        }

        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd/MM/yy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        String str = "";

        try {
            Date date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
}
