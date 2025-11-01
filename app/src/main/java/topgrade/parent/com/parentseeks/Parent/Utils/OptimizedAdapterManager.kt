package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Optimized Adapter Manager
 * Prevents memory leaks in RecyclerView adapters and improves performance
 */
class OptimizedAdapterManager {
    
    companion object {
        private const val TAG = "OptimizedAdapterManager"
        private const val MAX_CACHED_ADAPTERS = 10
    }
    
    // Track adapters with weak references to prevent memory leaks
    private val adapterReferences = ConcurrentHashMap<String, WeakReference<RecyclerView.Adapter<*>>>()
    private val viewHolderReferences = ConcurrentHashMap<String, WeakReference<RecyclerView.ViewHolder>>()
    
    /**
     * Register adapter for memory leak detection
     */
    fun registerAdapter(adapter: RecyclerView.Adapter<*>, tag: String) {
        val key = "${adapter.javaClass.simpleName}_$tag"
        adapterReferences[key] = WeakReference(adapter)
        
        // Clean up old adapters if we have too many
        if (adapterReferences.size > MAX_CACHED_ADAPTERS) {
            cleanupStaleAdapters()
        }
        
        Log.d(TAG, "Registered adapter: $key (Total: ${adapterReferences.size})")
    }
    
    /**
     * Unregister adapter
     */
    fun unregisterAdapter(adapter: RecyclerView.Adapter<*>, tag: String) {
        val key = "${adapter.javaClass.simpleName}_$tag"
        adapterReferences.remove(key)
        
        Log.d(TAG, "Unregistered adapter: $key (Remaining: ${adapterReferences.size})")
    }
    
    /**
     * Register ViewHolder for memory leak detection
     */
    fun registerViewHolder(viewHolder: RecyclerView.ViewHolder, tag: String) {
        val key = "${viewHolder.javaClass.simpleName}_$tag"
        viewHolderReferences[key] = WeakReference(viewHolder)
        
        Log.d(TAG, "Registered ViewHolder: $key (Total: ${viewHolderReferences.size})")
    }
    
    /**
     * Unregister ViewHolder
     */
    fun unregisterViewHolder(viewHolder: RecyclerView.ViewHolder, tag: String) {
        val key = "${viewHolder.javaClass.simpleName}_$tag"
        viewHolderReferences.remove(key)
        
        Log.d(TAG, "Unregistered ViewHolder: $key (Remaining: ${viewHolderReferences.size})")
    }
    
    /**
     * Clean up stale adapters
     */
    private fun cleanupStaleAdapters() {
        val keysToRemove = mutableListOf<String>()
        
        adapterReferences.forEach { (key, weakRef) ->
            if (weakRef.get() == null) {
                keysToRemove.add(key)
            }
        }
        
        keysToRemove.forEach { key ->
            adapterReferences.remove(key)
        }
        
        if (keysToRemove.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${keysToRemove.size} stale adapters")
        }
    }
    
    /**
     * Check for adapter memory leaks
     */
    fun checkAdapterLeaks(): AdapterLeakReport {
        val report = AdapterLeakReport()
        
        // Check adapters
        adapterReferences.forEach { (key, weakRef) ->
            val adapter = weakRef.get()
            if (adapter == null) {
                report.leakedAdapters.add(key)
            }
        }
        
        // Check ViewHolders
        viewHolderReferences.forEach { (key, weakRef) ->
            val viewHolder = weakRef.get()
            if (viewHolder == null) {
                report.leakedViewHolders.add(key)
            }
        }
        
        Log.d(TAG, "Adapter leak check completed: ${report.leakedAdapters.size} adapter leaks, ${report.leakedViewHolders.size} ViewHolder leaks")
        
        return report
    }
    
    /**
     * Clear all references
     */
    fun clearAllReferences() {
        adapterReferences.clear()
        viewHolderReferences.clear()
        Log.d(TAG, "All adapter references cleared")
    }
    
    /**
     * Get adapter statistics
     */
    fun getAdapterStats(): String {
        return "Adapter Statistics:\n" +
                "Active Adapters: ${adapterReferences.size}\n" +
                "Active ViewHolders: ${viewHolderReferences.size}"
    }
    
    /**
     * Adapter leak report data class
     */
    data class AdapterLeakReport(
        val leakedAdapters: MutableList<String> = mutableListOf(),
        val leakedViewHolders: MutableList<String> = mutableListOf()
    ) {
        val totalLeaks: Int get() = leakedAdapters.size + leakedViewHolders.size
        val hasLeaks: Boolean get() = totalLeaks > 0
    }
}

/**
 * Base optimized adapter with memory leak prevention
 */
abstract class OptimizedBaseAdapter<T, VH : RecyclerView.ViewHolder>(
    protected val context: Context
) : RecyclerView.Adapter<VH>() {
    
    private val adapterManager = OptimizedAdapterManager()
    private val dataList = mutableListOf<T>()
    private var isAdapterRegistered = false
    
    init {
        registerAdapter()
    }
    
    private fun registerAdapter() {
        if (!isAdapterRegistered) {
            adapterManager.registerAdapter(this, javaClass.simpleName)
            isAdapterRegistered = true
        }
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        if (position >= 0 && position < dataList.size) {
            val item = dataList[position]
            bindViewHolder(holder, item, position)
            
            // Register ViewHolder for memory leak detection
            adapterManager.registerViewHolder(holder, "${javaClass.simpleName}_$position")
        }
    }
    
    override fun onViewDetachedFromWindow(holder: VH) {
        super.onViewDetachedFromWindow(holder)
        
        // Clear animation to prevent memory leaks
        holder.itemView.clearAnimation()
        
        // Unregister ViewHolder
        adapterManager.unregisterViewHolder(holder, "${javaClass.simpleName}_${holder.adapterPosition}")
    }
    
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        
        // Clear any references to prevent memory leaks
        clearViewHolderReferences(holder)
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        
        // Unregister adapter when detached
        if (isAdapterRegistered) {
            adapterManager.unregisterAdapter(this, javaClass.simpleName)
            isAdapterRegistered = false
        }
    }
    
    /**
     * Update data list efficiently
     */
    fun updateData(newData: List<T>) {
        val oldSize = dataList.size
        dataList.clear()
        dataList.addAll(newData)
        
        if (oldSize == newData.size) {
            // Same size, just notify data changed
            notifyDataSetChanged()
        } else if (oldSize < newData.size) {
            // Items added
            notifyItemRangeInserted(oldSize, newData.size - oldSize)
        } else {
            // Items removed
            notifyItemRangeRemoved(newData.size, oldSize - newData.size)
        }
    }
    
    /**
     * Add item efficiently
     */
    fun addItem(item: T) {
        dataList.add(item)
        notifyItemInserted(dataList.size - 1)
    }
    
    /**
     * Remove item efficiently
     */
    fun removeItem(position: Int) {
        if (position >= 0 && position < dataList.size) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    
    /**
     * Clear all data
     */
    fun clearData() {
        val oldSize = dataList.size
        dataList.clear()
        notifyItemRangeRemoved(0, oldSize)
    }
    
    /**
     * Get item at position
     */
    fun getItem(position: Int): T? {
        return if (position >= 0 && position < dataList.size) {
            dataList[position]
        } else null
    }
    
    /**
     * Get all data
     */
    fun getAllData(): List<T> = dataList.toList()
    
    /**
     * Get data size
     */
    fun getDataSize(): Int = dataList.size
    
    override fun getItemCount(): Int = dataList.size
    
    /**
     * Bind data to ViewHolder (to be implemented by subclasses)
     */
    protected abstract fun bindViewHolder(holder: VH, item: T, position: Int)
    
    /**
     * Clear ViewHolder references (to be overridden by subclasses if needed)
     */
    protected open fun clearViewHolderReferences(holder: VH) {
        // Default implementation - override in subclasses if needed
    }
    
    /**
     * Check for memory leaks
     */
    fun checkMemoryLeaks(): OptimizedAdapterManager.AdapterLeakReport {
        return adapterManager.checkAdapterLeaks()
    }
    
    /**
     * Get adapter statistics
     */
    fun getAdapterStats(): String {
        return adapterManager.getAdapterStats()
    }
}

/**
 * Optimized adapter for simple list items
 */
class OptimizedListAdapter<T>(
    context: Context,
    private val layoutResId: Int,
    private val binder: (View, T, Int) -> Unit
) : OptimizedBaseAdapter<T, OptimizedListAdapter<T>.ViewHolder>(context) {
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: T, position: Int) {
            binder(itemView, item, position)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
        return ViewHolder(view)
    }
    
    override fun bindViewHolder(holder: ViewHolder, item: T, position: Int) {
        holder.bind(item, position)
    }
}

/**
 * Memory leak prevention utilities for adapters
 */
object AdapterMemoryUtils {
    
    /**
     * Safely update adapter data with null checks
     */
    fun <T> safeUpdateAdapterData(
        adapter: RecyclerView.Adapter<*>?,
        newData: List<T>?,
        updateFunction: (List<T>) -> Unit
    ) {
        if (adapter == null) {
            Log.w("AdapterMemoryUtils", "Adapter is null, skipping update")
            return
        }
        
        if (newData == null) {
            Log.w("AdapterMemoryUtils", "Data is null, skipping update")
            return
        }
        
        try {
            updateFunction(newData)
        } catch (e: Exception) {
            Log.e("AdapterMemoryUtils", "Error updating adapter data", e)
        }
    }
    
    /**
     * Safely notify adapter changes
     */
    fun safeNotifyAdapterChanges(adapter: RecyclerView.Adapter<*>?, action: () -> Unit) {
        if (adapter == null) {
            Log.w("AdapterMemoryUtils", "Adapter is null, skipping notification")
            return
        }
        
        try {
            action()
        } catch (e: Exception) {
            Log.e("AdapterMemoryUtils", "Error notifying adapter changes", e)
        }
    }
    
    /**
     * Clear adapter references to prevent memory leaks
     */
    fun clearAdapterReferences(adapter: RecyclerView.Adapter<*>?) {
        if (adapter == null) return
        
        try {
            // Clear any cached data
            if (adapter is OptimizedBaseAdapter<*, *>) {
                adapter.clearData()
            }
            
            // Force garbage collection for adapter
            System.gc()
            
        } catch (e: Exception) {
            Log.e("AdapterMemoryUtils", "Error clearing adapter references", e)
        }
    }
} 