package topgrade.parent.com.parentseeks.Parent.Utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import topgrade.parent.com.parentseeks.Parent.Model.DashboardConfig
import topgrade.parent.com.parentseeks.Parent.Model.DashboardMenuItem
import topgrade.parent.com.parentseeks.Parent.Model.UserPreferences
import java.io.IOException

/**
 * DataStore implementation for Dashboard Configuration and User Preferences
 * Modern replacement for Paper DB dashboard storage
 */
class DashboardDataStore(private val context: Context) {
    
    companion object {
        private val Context.dashboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "dashboard_data")
        
        // Dashboard Configuration Keys
        private val LAYOUT_TYPE = stringPreferencesKey("layout_type")
        private val GRID_COLUMNS = intPreferencesKey("grid_columns")
        private val SHOW_CATEGORIES = booleanPreferencesKey("show_categories")
        private val SHOW_SEARCH = booleanPreferencesKey("show_search")
        private val SHOW_FAVORITES = booleanPreferencesKey("show_favorites")
        private val ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")
        private val REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        private val THEME = stringPreferencesKey("theme")
        private val CATEGORIES_JSON = stringPreferencesKey("categories_json")
        private val MENU_ITEMS_JSON = stringPreferencesKey("menu_items_json")
        
        // User Preferences Keys
        private val USER_ID = stringPreferencesKey("user_id")
        private val FAVORITE_ITEMS_JSON = stringPreferencesKey("favorite_items_json")
        private val HIDDEN_ITEMS_JSON = stringPreferencesKey("hidden_items_json")
        private val CUSTOM_ORDER_JSON = stringPreferencesKey("custom_order_json")
        private val LAYOUT_PREFERENCE = stringPreferencesKey("layout_preference")
        private val USER_GRID_COLUMNS = intPreferencesKey("user_grid_columns")
        private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        private val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        private val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        private val USER_REFRESH_INTERVAL = intPreferencesKey("user_refresh_interval")
        private val SHOW_BADGES = booleanPreferencesKey("show_badges")
        private val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        private val LAST_UPDATED = longPreferencesKey("last_updated")
        
        // User Role and Permissions
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_PERMISSIONS_JSON = stringPreferencesKey("user_permissions_json")
    }
    
    private val gson = Gson()
    
    // ==================== DASHBOARD CONFIGURATION ====================
    
    /**
     * Save complete dashboard configuration
     */
    suspend fun saveDashboardConfig(config: DashboardConfig) {
        context.dashboardDataStore.edit { preferences ->
            preferences[LAYOUT_TYPE] = config.layoutType ?: "grid"
            preferences[GRID_COLUMNS] = config.gridColumns
            preferences[SHOW_CATEGORIES] = config.isShowCategories()
            preferences[SHOW_SEARCH] = config.isShowSearch()
            preferences[SHOW_FAVORITES] = config.isShowFavorites()
            preferences[ENABLE_ANIMATIONS] = config.isEnableAnimations()
            preferences[REFRESH_INTERVAL] = config.refreshInterval
            preferences[THEME] = config.theme ?: "light"
            
            // Save complex objects as JSON
            config.categories?.let { categories ->
                preferences[CATEGORIES_JSON] = gson.toJson(categories)
            }
            
            config.menuItems?.let { menuItems ->
                preferences[MENU_ITEMS_JSON] = gson.toJson(menuItems)
            }
        }
    }
    
    /**
     * Get dashboard configuration as Flow
     */
    val dashboardConfig: Flow<DashboardConfig> = context.dashboardDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DashboardConfig().apply {
                layoutType = preferences[LAYOUT_TYPE] ?: "grid"
                gridColumns = preferences[GRID_COLUMNS] ?: 3
                setShowCategories(preferences[SHOW_CATEGORIES] ?: true)
                setShowSearch(preferences[SHOW_SEARCH] ?: true)
                setShowFavorites(preferences[SHOW_FAVORITES] ?: true)
                setEnableAnimations(preferences[ENABLE_ANIMATIONS] ?: true)
                refreshInterval = preferences[REFRESH_INTERVAL] ?: 300
                theme = preferences[THEME] ?: "light"
                
                // Load complex objects from JSON
                preferences[CATEGORIES_JSON]?.let { json ->
                    try {
                        val type = object : TypeToken<List<DashboardMenuItem>>() {}.type
                        categories = gson.fromJson(json, type)
                    } catch (e: Exception) {
                        // Handle parsing error
                    }
                }
                
                preferences[MENU_ITEMS_JSON]?.let { json ->
                    try {
                        val type = object : TypeToken<List<DashboardMenuItem>>() {}.type
                        menuItems = gson.fromJson(json, type)
                    } catch (e: Exception) {
                        // Handle parsing error
                    }
                }
            }
        }
    
    // ==================== USER PREFERENCES ====================
    
    /**
     * Save complete user preferences
     */
    suspend fun saveUserPreferences(preferences: UserPreferences) {
        context.dashboardDataStore.edit { prefs ->
            prefs[USER_ID] = preferences.userId ?: ""
            prefs[LAYOUT_PREFERENCE] = preferences.layoutPreference ?: "grid"
            prefs[USER_GRID_COLUMNS] = preferences.gridColumns
            prefs[THEME_PREFERENCE] = preferences.themePreference ?: "light"
            prefs[SHOW_NOTIFICATIONS] = preferences.isShowNotifications()
            prefs[AUTO_REFRESH] = preferences.isAutoRefresh()
            prefs[USER_REFRESH_INTERVAL] = preferences.getRefreshInterval()
            prefs[SHOW_BADGES] = preferences.isShowBadges()
            prefs[COMPACT_MODE] = preferences.isCompactMode()
            prefs[LAST_UPDATED] = preferences.lastUpdated
            
            // Save lists as JSON
            preferences.favoriteItems?.let { items ->
                prefs[FAVORITE_ITEMS_JSON] = gson.toJson(items)
            }
            
            preferences.hiddenItems?.let { items ->
                prefs[HIDDEN_ITEMS_JSON] = gson.toJson(items)
            }
            
            preferences.customOrder?.let { order ->
                prefs[CUSTOM_ORDER_JSON] = gson.toJson(order)
            }
        }
    }
    
    /**
     * Get user preferences as Flow
     */
    val userPreferences: Flow<UserPreferences> = context.dashboardDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences().apply {
                userId = preferences[USER_ID] ?: ""
                layoutPreference = preferences[LAYOUT_PREFERENCE] ?: "grid"
                gridColumns = preferences[USER_GRID_COLUMNS] ?: 3
                themePreference = preferences[THEME_PREFERENCE] ?: "light"
                setShowNotifications(preferences[SHOW_NOTIFICATIONS] ?: true)
                setAutoRefresh(preferences[AUTO_REFRESH] ?: true)
                setRefreshInterval(preferences[USER_REFRESH_INTERVAL] ?: 300)
                setShowBadges(preferences[SHOW_BADGES] ?: true)
                setCompactMode(preferences[COMPACT_MODE] ?: false)
                lastUpdated = preferences[LAST_UPDATED] ?: System.currentTimeMillis()
                
                // Load lists from JSON
                preferences[FAVORITE_ITEMS_JSON]?.let { json ->
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        favoriteItems = gson.fromJson(json, type)
                    } catch (e: Exception) {
                        favoriteItems = mutableListOf()
                    }
                }
                
                preferences[HIDDEN_ITEMS_JSON]?.let { json ->
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        hiddenItems = gson.fromJson(json, type)
                    } catch (e: Exception) {
                        hiddenItems = mutableListOf()
                    }
                }
                
                preferences[CUSTOM_ORDER_JSON]?.let { json ->
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        customOrder = gson.fromJson(json, type)
                    } catch (e: Exception) {
                        customOrder = mutableListOf()
                    }
                }
            }
        }
    
    // ==================== USER ROLE AND PERMISSIONS ====================
    
    /**
     * Save user role
     */
    suspend fun saveUserRole(role: String) {
        context.dashboardDataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }
    
    /**
     * Get user role
     */
    val userRole: Flow<String> = context.dashboardDataStore.data
        .map { preferences ->
            preferences[USER_ROLE] ?: "teacher"
        }
    
    /**
     * Save user permissions
     */
    suspend fun saveUserPermissions(permissions: List<String>) {
        context.dashboardDataStore.edit { preferences ->
            preferences[USER_PERMISSIONS_JSON] = gson.toJson(permissions)
        }
    }
    
    /**
     * Get user permissions
     */
    val userPermissions: Flow<List<String>> = context.dashboardDataStore.data
        .map { preferences ->
            preferences[USER_PERMISSIONS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    
    // ==================== CONVENIENCE METHODS ====================
    
    /**
     * Toggle favorite item
     */
    suspend fun toggleFavorite(itemId: String) {
        context.dashboardDataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_ITEMS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson<List<String>>(json, type).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()
            
            if (currentFavorites.contains(itemId)) {
                currentFavorites.remove(itemId)
            } else {
                currentFavorites.add(itemId)
            }
            
            preferences[FAVORITE_ITEMS_JSON] = gson.toJson(currentFavorites)
        }
    }
    
    /**
     * Hide/show menu item
     */
    suspend fun toggleHiddenItem(itemId: String, hidden: Boolean) {
        context.dashboardDataStore.edit { preferences ->
            val currentHidden = preferences[HIDDEN_ITEMS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson<List<String>>(json, type).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()
            
            if (hidden) {
                if (!currentHidden.contains(itemId)) {
                    currentHidden.add(itemId)
                }
            } else {
                currentHidden.remove(itemId)
            }
            
            preferences[HIDDEN_ITEMS_JSON] = gson.toJson(currentHidden)
        }
    }
    
    /**
     * Update custom order
     */
    suspend fun updateCustomOrder(order: List<String>) {
        context.dashboardDataStore.edit { preferences ->
            preferences[CUSTOM_ORDER_JSON] = gson.toJson(order)
        }
    }
    
    /**
     * Update layout preference
     */
    suspend fun updateLayoutPreference(layout: String) {
        context.dashboardDataStore.edit { preferences ->
            preferences[LAYOUT_PREFERENCE] = layout
        }
    }
    
    /**
     * Update theme preference
     */
    suspend fun updateThemePreference(theme: String) {
        context.dashboardDataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = theme
        }
    }
    
    /**
     * Update grid columns
     */
    suspend fun updateGridColumns(columns: Int) {
        context.dashboardDataStore.edit { preferences ->
            preferences[USER_GRID_COLUMNS] = columns
        }
    }
    
    /**
     * Clear all dashboard data
     */
    suspend fun clearAllData() {
        context.dashboardDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Check if item is favorite
     */
    suspend fun isFavorite(itemId: String): Boolean {
        return context.dashboardDataStore.data.map { preferences ->
            preferences[FAVORITE_ITEMS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    val favorites = gson.fromJson<List<String>>(json, type)
                    favorites.contains(itemId)
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }.first()
    }
    
    /**
     * Check if item is hidden
     */
    suspend fun isHidden(itemId: String): Boolean {
        return context.dashboardDataStore.data.map { preferences ->
            preferences[HIDDEN_ITEMS_JSON]?.let { json ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    val hidden = gson.fromJson<List<String>>(json, type)
                    hidden.contains(itemId)
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }.first()
    }
} 