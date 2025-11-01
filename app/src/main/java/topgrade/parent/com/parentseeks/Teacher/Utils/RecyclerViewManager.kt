@file:Suppress("DEPRECATION")
package topgrade.parent.com.parentseeks.Teacher.Utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import topgrade.parent.com.parentseeks.Parent.Adaptor.HomeAdaptorStaff
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickListener
import topgrade.parent.com.parentseeks.Parent.Model.HomeModel
import topgrade.parent.com.parentseeks.Teacher.Interface.OnMenuCLick
import topgrade.parent.com.parentseeks.R

/**
 * Manages RecyclerView functionality
 * Handles setup, optimization, and data management
 */
class RecyclerViewManager(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onClickListener: OnClickListener,
    private val onMenuClick: OnMenuCLick
) {
    
    private lateinit var adapter: HomeAdaptorStaff
    private val dataList = mutableListOf<HomeModel>()
    
    fun initialize() {
        setupRecyclerView()
        setupAdapter()
        loadInitialData()
    }
    
    private fun setupRecyclerView() {
        // Use GridLayoutManager with 3 columns for better card width
        val layoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = layoutManager
        
        // Performance optimizations
        recyclerView.setHasFixedSize(true)
        recyclerView.setNestedScrollingEnabled(false)
        
        // Remove deprecated method
        // recyclerView.setDrawingCacheEnabled(false) // DEPRECATED
        
        // Additional optimizations
        recyclerView.isDrawingCacheEnabled = false
        recyclerView.itemAnimator = null // Disable animations for better performance
    }
    
    private fun setupAdapter() {
        adapter = HomeAdaptorStaff(dataList, onMenuClick)
        recyclerView.adapter = adapter
    }
    
    private fun loadInitialData() {
        dataList.clear()
        dataList.addAll(getDashboardItems())
        adapter.notifyDataSetChanged()
    }
    
    private fun getDashboardItems(): List<HomeModel> {
        return listOf(
            HomeModel(image = R.drawable.ic_student_list, title = "Student List", subtitle = "View"),
            HomeModel(image = R.drawable.ic_attendance, title = "Attendance", subtitle = "Manage"),
            HomeModel(image = R.drawable.ic_submit, title = "Exam", subtitle = "Manage Exams"),
            HomeModel(image = R.drawable.ic_reports, title = "Progress Report", subtitle = "View"),
            HomeModel(image = R.drawable.ic_feedback, title = "Feedback Student", subtitle = "Student"),
            HomeModel(image = R.drawable.ic_home, title = "Back to Home", subtitle = "Return to"),
            HomeModel(image = R.drawable.ic_logout, title = "Logout", subtitle = "Sign Out")
        )
    }
    
    fun updateData(newData: List<HomeModel>) {
        dataList.clear()
        dataList.addAll(newData)
        adapter.notifyDataSetChanged()
    }
    
    fun addItem(item: HomeModel) {
        dataList.add(item)
        adapter.notifyItemInserted(dataList.size - 1)
    }
    
    fun removeItem(position: Int) {
        if (position in 0 until dataList.size) {
            dataList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }
    
    fun getItemCount(): Int {
        return dataList.size
    }
    
    fun getItem(position: Int): HomeModel? {
        return if (position in 0 until dataList.size) {
            dataList[position]
        } else {
            null
        }
    }
    
    fun clearData() {
        dataList.clear()
        adapter.notifyDataSetChanged()
    }
    
    fun refreshData() {
        adapter.notifyDataSetChanged()
    }
    
    fun setItemClickListener(_listener: OnClickListener) {
        // Note: HomeAdaptorStaff doesn't have setOnClickListener method
        // The click listener is set in the constructor
    }
    
    fun setMenuClickListener(_listener: OnMenuCLick) {
        // Note: HomeAdaptorStaff doesn't have setOnMenuClickListener method
        // The menu click listener is set in the constructor
    }
}
