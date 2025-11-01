package topgrade.parent.com.parentseeks.Teacher.Model

import android.content.Intent

data class StaffDashboardCard(
    val id: Int,
    val title: String,
    val subtitle: String,
    val iconResId: Int,
    val targetActivity: Class<*>? = null,
    val action: Runnable? = null,
    val actionType: String = ""
)
