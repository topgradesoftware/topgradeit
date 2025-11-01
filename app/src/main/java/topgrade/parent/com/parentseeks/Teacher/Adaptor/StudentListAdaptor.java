package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StudentListSigel;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class StudentListAdaptor extends RecyclerView.Adapter<StudentListAdaptor.StudentHolder> {

    View v;
    List<StudentListSigel> list;
    boolean is_empty = false;

    public StudentListAdaptor(List<StudentListSigel> list) {
        this.list = list;
        android.util.Log.d("StudentListAdaptor", "Adapter created with " + (list != null ? list.size() : 0) + " items");
    }

    @NonNull
    @Override
    public StudentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.students_list, viewGroup, false);
        return new StudentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentHolder holder, int position) {

        if (position % 2 != 0) {
            holder.row.setBackgroundColor(Color.parseColor("#DFE5E2"));
        } else {
            holder.row.setBackgroundColor(Color.WHITE);
        }
        int new_position = position + 1;
        StudentListSigel s = list.get(position);
        
        // Handle null values safely
        holder.Serial.setText(holder.itemView.getContext().getString(R.string.serial_number_format, new_position));
        holder.Roll_No.setText(s.getRollNumber() != null ? s.getRollNumber() : "N/A");
        holder.Name.setText(s.getFullName() != null ? s.getFullName() : "N/A");
        holder.Father_Name.setText(s.getParentName() != null ? s.getParentName() : "N/A");
        
        String format_time = "N/A";
        if (s.getCreatedDate() != null && !s.getCreatedDate().isEmpty()) {
            try {
                format_time = Util.formatDate("yyyy-MM-dd", "dd-MM-yy", s.getCreatedDate());
            } catch (Exception e) {
                format_time = s.getCreatedDate(); // Use original date if formatting fails
            }
        }
        holder.Date_of_admission.setText(format_time);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class StudentHolder extends RecyclerView.ViewHolder {

        private final TextView Serial;
        private final TextView Roll_No;
        private final TextView Name;
        private final TextView Father_Name;
        private final TextView Date_of_admission;
        public final LinearLayout row;

        public StudentHolder(@NonNull View itemView) {
            super(itemView);
            row = itemView.findViewById(R.id.row);
            Serial = itemView.findViewById(R.id.serial_list);
            Roll_No = itemView.findViewById(R.id.roll_no_list);
            Name = itemView.findViewById(R.id.name_list);
            Father_Name = itemView.findViewById(R.id.father_name_list);
            Date_of_admission = itemView.findViewById(R.id.date_of_admission_list);


        }


    }
}
