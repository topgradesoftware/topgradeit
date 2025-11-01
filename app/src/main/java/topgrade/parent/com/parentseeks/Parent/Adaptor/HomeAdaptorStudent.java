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

public class HomeAdaptorStudent extends RecyclerView.Adapter<HomeAdaptorStudent.Holder> {
    View v;
    List<HomeModel> list;
    OnMenuCLick onClickListener;
    int cardBackgroundColor;

    public HomeAdaptorStudent(List<HomeModel> list, OnMenuCLick onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
        this.cardBackgroundColor = R.color.student_primary; // Default student color
    }

    public HomeAdaptorStudent(List<HomeModel> list, OnMenuCLick onClickListener, int cardBackgroundColor) {
        this.list = list;
        this.onClickListener = onClickListener;
        this.cardBackgroundColor = cardBackgroundColor;
        
        android.util.Log.d("HomeAdaptorStudent", "Adapter created with " + (list != null ? list.size() : "null") + " items");
        android.util.Log.d("HomeAdaptorStudent", "onClickListener: " + (onClickListener != null ? "NOT NULL" : "NULL"));
        android.util.Log.d("HomeAdaptorStudent", "cardBackgroundColor: " + cardBackgroundColor);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        android.util.Log.d("HomeAdaptorStudent", "onCreateViewHolder called for position: " + i);
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_item_student, viewGroup, false);
        android.util.Log.d("HomeAdaptorStudent", "View inflated: " + (v != null ? "SUCCESS" : "FAILED"));
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int i) {
        android.util.Log.d("HomeAdaptorStudent", "onBindViewHolder called for position: " + i + ", title: " + list.get(i).getTitle());
        
        holder.picture.setImageResource(list.get(i).getImage());
        holder.title.setText(list.get(i).getTitle());
        
        // Set subtitle
        String subtitleText = list.get(i).getSubtitle() != null && !list.get(i).getSubtitle().isEmpty() 
            ? list.get(i).getSubtitle() : "View Details";
        holder.subtitle.setText(subtitleText);
        
        // Set card background color dynamically
        holder.profile.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(cardBackgroundColor));

        // Set click listener on the itemView (outer LinearLayout)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int position = holder.getAdapterPosition();
                    android.util.Log.d("HomeAdaptorStudent", "Card clicked at position: " + position);
                    
                    if (position >= 0 && position < list.size()) {
                        String title = list.get(position).getTitle();
                        android.util.Log.d("HomeAdaptorStudent", "Card clicked: " + title);
                        
                        if (onClickListener != null) {
                            android.util.Log.d("HomeAdaptorStudent", "Calling OnMenuCLick for: " + title);
                            onClickListener.OnMenuCLick(v, title);
                        } else {
                            android.util.Log.e("HomeAdaptorStudent", "onClickListener is null!");
                        }
                    } else {
                        android.util.Log.e("HomeAdaptorStudent", "Invalid position: " + position + ", list size: " + list.size());
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeAdaptorStudent", "Error in onClick", e);
                }
            }
        });
        
        // Debug: Log click listener setup
        android.util.Log.d("HomeAdaptorStudent", "Click listener set for position: " + i + ", title: " + list.get(i).getTitle());
        android.util.Log.d("HomeAdaptorStudent", "itemView clickable: " + holder.itemView.isClickable());
        android.util.Log.d("HomeAdaptorStudent", "itemView focusable: " + holder.itemView.isFocusable());
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
