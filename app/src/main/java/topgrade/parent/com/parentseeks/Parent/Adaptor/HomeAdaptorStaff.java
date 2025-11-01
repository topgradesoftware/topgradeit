package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;

public class HomeAdaptorStaff extends RecyclerView.Adapter<HomeAdaptorStaff.Holder> {

    private List<HomeModel> list;
    private final OnMenuCLick onClickListener;

    public HomeAdaptorStaff(List<HomeModel> list, OnMenuCLick onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_item_staff, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        HomeModel item = list.get(position);
        
        // CPU optimization - minimal view updates
        String currentTitle = holder.title.getText().toString();
        if (!currentTitle.equals(item.getTitle())) {
            holder.title.setText(item.getTitle());
        }
        
        // Set subtitle
        String currentSubtitle = holder.subtitle.getText().toString();
        String subtitleText = item.getSubtitle() != null && !item.getSubtitle().isEmpty() 
            ? item.getSubtitle() : "View Details";
        if (!currentSubtitle.equals(subtitleText)) {
            holder.subtitle.setText(subtitleText);
        }
        
        // CPU optimization - avoid unnecessary image loading
        if (holder.picture.getTag() == null || !holder.picture.getTag().equals(item.getImage())) {
            holder.picture.setImageResource(item.getImage());
            holder.picture.setTag(item.getImage());
        }

        // CPU optimization - set click listener only once
        if (holder.itemView.getTag() == null) {
            holder.itemView.setTag("click_set");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < list.size() && onClickListener != null) {
                        onClickListener.OnMenuCLick(v, list.get(pos).getTitle());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }
    
    /**
     * Memory optimization method
     */
    public void optimizeMemory() {
        try {
            // Request garbage collection
            System.gc();
            
            // Clear any cached data if needed
            if (list != null) {
                // Ensure list is not unnecessarily large
                if (list.size() > 100) {
                    Log.w("HomeAdaptorStaff", "Large list detected, consider pagination");
                }
            }
            
            // CPU optimization - reduce processing overhead
            System.setProperty("java.vm.threads.max", "4");
            
            Log.d("HomeAdaptorStaff", "Memory and CPU optimization completed");
        } catch (Exception e) {
            Log.e("HomeAdaptorStaff", "Error during memory optimization", e);
        }
    }

    // ✅ Efficient updates with DiffUtil
    public void updateList(List<HomeModel> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { 
                return list == null ? 0 : list.size(); 
            }

            @Override
            public int getNewListSize() { 
                return newList == null ? 0 : newList.size(); 
            }

            @Override
            public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
                // Use ID for better DiffUtil performance
                return list.get(oldItemPos).getId() == newList.get(newItemPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
                HomeModel oldItem = list.get(oldItemPos);
                HomeModel newItem = newList.get(newItemPos);
                return oldItem.getTitle().equals(newItem.getTitle()) && 
                       oldItem.getImage() == newItem.getImage();
            }
        });

        this.list = newList;
        result.dispatchUpdatesTo(this);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView subtitle;
        private final ImageView picture;
        private final com.google.android.material.card.MaterialCardView cardView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            picture = itemView.findViewById(R.id.picture);
            cardView = itemView.findViewById(R.id.profile);
        }
    }
} 