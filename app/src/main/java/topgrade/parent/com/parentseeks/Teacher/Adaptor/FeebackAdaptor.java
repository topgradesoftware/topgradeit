package topgrade.parent.com.parentseeks.Teacher.Adaptor;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Model.Feedback;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class FeebackAdaptor extends RecyclerView.Adapter<FeebackAdaptor.Holder> {


    List<Feedback> list;
    View v;
    OnClickListener onClickListener;

    public FeebackAdaptor(List<Feedback> list, OnClickListener onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
        
        // Debug logging
        android.util.Log.d("FeebackAdaptor", "Adapter created with " + (list != null ? list.size() : "null") + " items");
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                topgrade.parent.com.parentseeks.Teacher.Model.Feedback item = list.get(i);
                android.util.Log.d("FeebackAdaptor", "Item " + i + ": " + item.getChildName() + " - " + item.getFeedback());
            }
        }
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_layout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
        // Debug logging
        android.util.Log.d("FeebackAdaptor", "Binding item at position " + position + ": " + list.get(position).getChildName());

        String childWithSubject = holder.itemView.getContext().getString(
                R.string.child_with_subject,
                list.get(position).getChildName(),
                list.get(position).getSubject_name()
        );
        holder.name.setText(childWithSubject);
        holder.feeback_body.setText(list.get(position).getFeedback());

        String time = Util.formatDate("yyyy-MM-dd h:mm:ss",
                "dd-MM-yyyy hh:mm:a", list.get(position).getTimestamp());
        holder.date.setText(time);
        holder.context_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = list != null ? list.size() : 0;
        android.util.Log.d("FeebackAdaptor", "getItemCount called, returning: " + count);
        return count;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView feeback_body;
        private final TextView name;
        private final TextView date;
        private final ImageView context_menu;

        public Holder(@NonNull View v) {
            super(v);

            feeback_body = v.findViewById(R.id.feeback_body);
            date = v.findViewById(R.id.date);
            name = v.findViewById(R.id.name);
            context_menu = v.findViewById(R.id.context_menu);
        }
    }
}
