package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener;
import topgrade.parent.com.parentseeks.R;

public class GraphAdaptor extends RecyclerView.Adapter<GraphAdaptor.GraphHolder> {
    View v;
    List<String> list;
    OnClickListener onClickListener;

    public GraphAdaptor(List<String> list, OnClickListener onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;

    }

    @NonNull
    @Override
    public GraphAdaptor.GraphHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.graph_item, viewGroup,
                false);
        return new GraphAdaptor.GraphHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GraphAdaptor.GraphHolder holder, final int i) {

        holder.simpleProgressBar.setProgress(Math.round(Float.parseFloat(list.get(i))));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {

        return list.size();

    }


    public class GraphHolder extends RecyclerView.ViewHolder {

        public ProgressBar simpleProgressBar;

        public GraphHolder(@NonNull View itemView) {
            super(itemView);
            simpleProgressBar = itemView.findViewById(R.id.simpleProgressBar);

        }
    }
}
