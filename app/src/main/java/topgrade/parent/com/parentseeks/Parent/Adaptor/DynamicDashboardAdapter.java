package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.util.List;

import topgrade.parent.com.parentseeks.Parent.Model.DashboardMenuItem;
import topgrade.parent.com.parentseeks.Parent.Utils.OptimizedRecyclerViewAdapter;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick;

public class DynamicDashboardAdapter extends OptimizedRecyclerViewAdapter<DashboardMenuItem, DynamicDashboardAdapter.ViewHolder> {
    
    private OnMenuCLick onClickListener;
    private String layoutType;
    private boolean enableAnimations;
    private boolean showBadges;
    private boolean compactMode;
    private int lastPosition = -1;

    public DynamicDashboardAdapter(Context context, List<DashboardMenuItem> menuItems, 
                                 OnMenuCLick onClickListener, String layoutType, 
                                 boolean enableAnimations, boolean showBadges, boolean compactMode) {
        super(context, menuItems);
        this.onClickListener = onClickListener;
        this.layoutType = layoutType;
        this.enableAnimations = enableAnimations;
        this.showBadges = showBadges;
        this.compactMode = compactMode;
    }

    @Override
    protected int getLayoutResourceId(int viewType) {
        switch (layoutType) {
            case "list":
                return R.layout.dashboard_item_list;
            case "card":
                return R.layout.dashboard_item_card;
            case "grid":
            default:
                return R.layout.dashboard_item_grid;
        }
    }

    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    protected void bindViewHolder(ViewHolder holder, DashboardMenuItem item, int position) {
        // Set basic content
        holder.title.setText(item.getTitle());
        holder.icon.setImageResource(item.getIconResource());
        holder.title.setTextColor(ContextCompat.getColor(context, R.color.white));
        holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.white));
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.navy_blue));
        
        // Set description if available
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(item.getDescription());
        } else {
            holder.description.setVisibility(View.GONE);
        }
        
        // Handle badges
        if (showBadges && item.hasBadge()) {
            holder.badge.setVisibility(View.VISIBLE);
            if (item.getBadgeCount() > 0) {
                holder.badge.setText(String.valueOf(item.getBadgeCount()));
            } else if (item.getBadgeText() != null && !item.getBadgeText().isEmpty()) {
                holder.badge.setText(item.getBadgeText());
            }
        } else {
            holder.badge.setVisibility(View.GONE);
        }
        
        // Handle enabled/disabled state
        if (!item.isEnabled()) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setEnabled(false);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setEnabled(true);
        }
        
        // Handle compact mode
        if (compactMode) {
            holder.title.setTextSize(10);
            holder.description.setVisibility(View.GONE);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (item.isEnabled() && onClickListener != null) {
                onClickListener.OnMenuCLick(v, item.getTitle());
            }
        });
        
        // Add animations
        if (enableAnimations) {
            setAnimation(holder.itemView, position);
        }
        
        // Handle featured items
        if (item.isFeatured()) {
            holder.cardView.setCardElevation(8f);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.cardView.setCardElevation(2f);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void updateMenuItems(List<DashboardMenuItem> newItems) {
        updateData(newItems);
    }

    public void updateBadge(String itemId, int count) {
        for (int i = 0; i < getDataSize(); i++) {
            DashboardMenuItem item = getItem(i);
            if (item != null && item.getId().equals(itemId)) {
                item.setBadgeCount(count);
                updateItem(i, item);
                break;
            }
        }
    }

    public void updateBadgeText(String itemId, String text) {
        for (int i = 0; i < getDataSize(); i++) {
            DashboardMenuItem item = getItem(i);
            if (item != null && item.getId().equals(itemId)) {
                item.setBadgeText(text);
                updateItem(i, item);
                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView icon;
        TextView title;
        TextView description;
        TextView badge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            badge = itemView.findViewById(R.id.badge);
        }
    }
} 