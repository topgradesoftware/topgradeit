package topgrade.parent.com.parentseeks.Teacher.Utils

import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import de.hdodenhof.circleimageview.CircleImageView
import topgrade.parent.com.parentseeks.Parent.Adaptor.NavDrawerAdapter
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickDrawerItem
import topgrade.parent.com.parentseeks.Parent.Interface.OnCloseNavigationDrawer
import topgrade.parent.com.parentseeks.Parent.Model.NavDrawerItem
import topgrade.parent.com.parentseeks.R
import topgrade.parent.com.parentseeks.Parent.Utils.MenuActionConstants
import java.util.*
import android.widget.TextView

/**
 * Manages navigation drawer functionality
 * Separates drawer concerns from main activity
 */
class DrawerManager(
    private val context: Context,
    private val drawerLayout: DrawerLayout,
    private val navigationView: NavigationView,
    private val toolbar: Toolbar,
    private val onDrawerItemClick: OnClickDrawerItem,
    private val onCloseDrawer: OnCloseNavigationDrawer
) {
    
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navDrawerAdapter: NavDrawerAdapter
    
    companion object {
        // Constants for menu items (using consolidated MenuActionConstants)
        val MENU_SHARE_APP = MenuActionConstants.Actions.MENU_SHARE_APP
        val MENU_RATE_US = MenuActionConstants.Actions.MENU_RATE_US
        val MENU_CHANGE_PASSWORD = MenuActionConstants.Actions.MENU_CHANGE_PASSWORD
        val MENU_LOGOUT = MenuActionConstants.Actions.MENU_LOGOUT
    }
    
    fun initialize() {
        setupDrawerToggle()
        setupNavigationView()
        setupDrawerRecyclerView()
    }
    
    private fun setupDrawerToggle() {
        toggle = ActionBarDrawerToggle(
            context as androidx.appcompat.app.AppCompatActivity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }
    
    private fun setupNavigationView() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    onDrawerItemClick.clickDrawerItem("Home")
                    closeDrawer()
                    true
                }
                R.id.nav_profile -> {
                    onDrawerItemClick.clickDrawerItem("Profile")
                    closeDrawer()
                    true
                }
                R.id.nav_settings -> {
                    onDrawerItemClick.clickDrawerItem("Settings")
                    closeDrawer()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupDrawerRecyclerView() {
        // Note: NavDrawerAdapter extends BaseAdapter, not RecyclerView.Adapter
        // This method is not needed for the current implementation
        // The drawer items are handled by the NavigationView menu
    }
    
    private fun getDrawerItems(): ArrayList<NavDrawerItem> {
        return arrayListOf(
            NavDrawerItem(MENU_SHARE_APP),
            NavDrawerItem(MENU_RATE_US),
            NavDrawerItem(MENU_CHANGE_PASSWORD),
            NavDrawerItem(MENU_LOGOUT)
        )
    }
    
    fun updateUserInfo(name: String, _location: String, _profileImageUrl: String?) {
        // Note: This method requires specific layout IDs that may not exist
        // Implementation depends on the actual navigation header layout
        try {
            val headerView = navigationView.getHeaderView(0)
            val nameTextView = headerView.findViewById<android.widget.TextView>(R.id.nav_user_name)
            nameTextView?.text = name
            
            // Load profile image with Glide if image view exists
            // Note: nav_profile_image ID may not exist in all layouts
            // Profile image loading is disabled for now to avoid compilation errors
            // TODO: Add proper profile image view ID when layout is available
        } catch (e: Exception) {
            // Handle missing layout elements gracefully
        }
    }
    
    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }
    
    fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        onCloseDrawer.closeNavigationDrawer()
    }
    
    fun isDrawerOpen(): Boolean {
        return drawerLayout.isDrawerOpen(GravityCompat.START)
    }
    
    fun onBackPressed(): Boolean {
        return if (isDrawerOpen()) {
            closeDrawer()
            true
        } else {
            false
        }
    }
}
