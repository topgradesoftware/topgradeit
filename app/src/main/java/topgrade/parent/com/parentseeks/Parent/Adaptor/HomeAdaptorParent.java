package topgrade.parent.com.parentseeks.Parent.Adaptor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;

public class HomeAdaptorParent extends RecyclerView.Adapter<HomeAdaptorParent.Holder> {
    View v;
    List<HomeModel> list;
    OnMenuCLick onClickListener;
    int cardBackgroundColor;

    public HomeAdaptorParent(List<HomeModel> list, OnMenuCLick onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
        this.cardBackgroundColor = R.color.parent_primary; // Default parent color
    }

    public HomeAdaptorParent(List<HomeModel> list, OnMenuCLick onClickListener, int cardBackgroundColor) {
        this.list = list;
        this.onClickListener = onClickListener;
        this.cardBackgroundColor = cardBackgroundColor;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_item_parent, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int i) {
        holder.picture.setImageResource(list.get(i).getImage());
        holder.title.setText(list.get(i).getTitle());
        
        // Set subtitle
        String subtitleText = list.get(i).getSubtitle() != null && !list.get(i).getSubtitle().isEmpty() 
            ? list.get(i).getSubtitle() : "View Details";
        holder.subtitle.setText(subtitleText);
        
        // Set card background color dynamically
        holder.profile.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(cardBackgroundColor));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.OnMenuCLick(v, list.get(holder.getAdapterPosition()).getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView subtitle;
        private final ImageView picture;
        private final com.google.android.material.card.MaterialCardView profile;

        public Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            subtitle = itemView.findViewById(R.id.card_subtitle);
            picture = itemView.findViewById(R.id.card_icon);
            profile = itemView.findViewById(R.id.profile);
        }
    }
} 