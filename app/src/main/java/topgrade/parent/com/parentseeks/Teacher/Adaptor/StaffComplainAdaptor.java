package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.StaffComplainModel;


public class StaffComplainAdaptor extends RecyclerView.Adapter<StaffComplainAdaptor.Holder> {
    List<StaffComplainModel>list;

    View v;
    public StaffComplainAdaptor(List<StaffComplainModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public StaffComplainAdaptor.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v= LayoutInflater.from(parent.getContext()).inflate(R.layout.solve_unsolve_application_layout,parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull StaffComplainAdaptor.Holder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

       private TextView head,body;
        public Holder(@NonNull View itemView) {
            super(itemView);

            head=v.findViewById(R.id.head);
            body=v.findViewById(R.id.body);
        }
    }
}
