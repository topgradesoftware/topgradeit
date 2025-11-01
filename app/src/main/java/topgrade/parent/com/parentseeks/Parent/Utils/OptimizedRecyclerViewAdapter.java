package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimized base RecyclerView adapter with performance best practices
 * Implements efficient data management, view recycling, and memory optimization
 */
public abstract class OptimizedRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> 
        extends RecyclerView.Adapter<VH> {
    
    protected final Context context;
    protected final List<T> dataList;
    protected final LayoutInflater inflater;
    private final Object dataLock = new Object();
    
    // Performance optimization flags
    private boolean enableAnimations = true;
    private boolean enableItemAnimations = true;
    private int lastPosition = -1;
    
    public OptimizedRecyclerViewAdapter(Context context) {
        this.context = context.getApplicationContext();
        this.dataList = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }
    
    public OptimizedRecyclerViewAdapter(Context context, List<T> dataList) {
        this.context = context.getApplicationContext();
        this.dataList = new ArrayList<>(dataList);
        this.inflater = LayoutInflater.from(context);
    }
    
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(getLayoutResourceId(viewType), parent, false);
        return createViewHolder(view, viewType);
    }
    
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (position >= 0 && position < dataList.size()) {
            T item = dataList.get(position);
            bindViewHolder(holder, item, position);
            
            // Add animation if enabled
            if (enableAnimations && enableItemAnimations && position > lastPosition) {
                addAnimation(holder.itemView, position);
                lastPosition = position;
            }
        }
    }
    
    @Override
    public int getItemCount() {
        synchronized (dataLock) {
            return dataList.size();
        }
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
        super.onViewDetachedFromWindow(holder);
        // Clear animation to prevent memory leaks
        holder.itemView.clearAnimation();
    }
    
    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        // Clear any references to prevent memory leaks
        clearViewHolderReferences(holder);
    }
    
    /**
     * Get layout resource ID for the given view type
     */
    protected abstract int getLayoutResourceId(int viewType);
    
    /**
     * Create ViewHolder instance
     */
    protected abstract VH createViewHolder(View view, int viewType);
    
    /**
     * Bind data to ViewHolder
     */
    protected abstract void bindViewHolder(VH holder, T item, int position);
    
    /**
     * Clear ViewHolder references to prevent memory leaks
     */
    protected void clearViewHolderReferences(VH holder) {
        // Override in subclasses if needed
    }
    
    /**
     * Add animation to view
     */
    protected void addAnimation(View view, int position) {
        // Override in subclasses to add custom animations
    }
    
    /**
     * Update data list efficiently
     */
    public void updateData(List<T> newData) {
        synchronized (dataLock) {
            dataList.clear();
            if (newData != null) {
                dataList.addAll(newData);
            }
        }
        notifyDataSetChanged();
    }
    
    /**
     * Add item to the end of the list
     */
    public void addItem(T item) {
        if (item != null) {
            synchronized (dataLock) {
                dataList.add(item);
            }
            notifyItemInserted(dataList.size() - 1);
        }
    }
    
    /**
     * Add item at specific position
     */
    public void addItem(int position, T item) {
        if (item != null && position >= 0 && position <= dataList.size()) {
            synchronized (dataLock) {
                dataList.add(position, item);
            }
            notifyItemInserted(position);
        }
    }
    
    /**
     * Remove item at position
     */
    public void removeItem(int position) {
        if (position >= 0 && position < dataList.size()) {
            synchronized (dataLock) {
                dataList.remove(position);
            }
            notifyItemRemoved(position);
        }
    }
    
    /**
     * Update item at position
     */
    public void updateItem(int position, T item) {
        if (item != null && position >= 0 && position < dataList.size()) {
            synchronized (dataLock) {
                dataList.set(position, item);
            }
            notifyItemChanged(position);
        }
    }
    
    /**
     * Get item at position
     */
    public T getItem(int position) {
        synchronized (dataLock) {
            if (position >= 0 && position < dataList.size()) {
                return dataList.get(position);
            }
        }
        return null;
    }
    
    /**
     * Get all data
     */
    public List<T> getData() {
        synchronized (dataLock) {
            return new ArrayList<>(dataList);
        }
    }
    
    /**
     * Clear all data
     */
    public void clearData() {
        synchronized (dataLock) {
            dataList.clear();
        }
        notifyDataSetChanged();
    }
    
    /**
     * Check if list is empty
     */
    public boolean isEmpty() {
        synchronized (dataLock) {
            return dataList.isEmpty();
        }
    }
    
    /**
     * Get list size
     */
    public int getDataSize() {
        synchronized (dataLock) {
            return dataList.size();
        }
    }
    
    /**
     * Enable/disable animations
     */
    public void setEnableAnimations(boolean enable) {
        this.enableAnimations = enable;
    }
    
    /**
     * Enable/disable item animations
     */
    public void setEnableItemAnimations(boolean enable) {
        this.enableItemAnimations = enable;
    }
    
    /**
     * Set last position for animation tracking
     */
    public void setLastPosition(int position) {
        this.lastPosition = position;
    }
    
    /**
     * Reset animation position
     */
    public void resetAnimationPosition() {
        this.lastPosition = -1;
    }
    
    /**
     * Get context
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Get inflater
     */
    public LayoutInflater getInflater() {
        return inflater;
    }
} 