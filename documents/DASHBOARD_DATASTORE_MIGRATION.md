# Dashboard DataStore Migration

## Overview

This migration replaces the legacy **Paper DB** storage system used in `DashboardManager` with the modern **DataStore** implementation. This provides better performance, type safety, and follows Android best practices.

## What Was Migrated

### 1. Dashboard Configuration
- Layout settings (grid, list, card)
- Grid columns configuration
- Show/hide settings for categories, search, favorites
- Animation settings
- Refresh interval
- Theme configuration
- Categories and menu items

### 2. User Preferences
- Favorite menu items
- Hidden menu items
- Custom menu order
- Layout preferences
- Theme preferences
- Notification settings
- Auto-refresh settings
- Badge display settings
- Compact mode settings

### 3. User Role and Permissions
- User role (teacher, admin, coordinator)
- User permissions list

## New Files Created

### 1. `DashboardDataStore.kt`
**Location**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/DashboardDataStore.kt`

**Features**:
- Type-safe DataStore implementation
- Reactive data flows using Kotlin Flow
- JSON serialization for complex objects
- Error handling with fallbacks
- Convenience methods for common operations

**Key Methods**:
```kotlin
// Dashboard Configuration
suspend fun saveDashboardConfig(config: DashboardConfig)
val dashboardConfig: Flow<DashboardConfig>

// User Preferences
suspend fun saveUserPreferences(preferences: UserPreferences)
val userPreferences: Flow<UserPreferences>

// Convenience Methods
suspend fun toggleFavorite(itemId: String)
suspend fun toggleHiddenItem(itemId: String, hidden: Boolean)
suspend fun updateCustomOrder(order: List<String>)
suspend fun updateLayoutPreference(layout: String)
suspend fun updateThemePreference(theme: String)
```

### 2. `DashboardMigrationHelper.kt`
**Location**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/DashboardMigrationHelper.kt`

**Features**:
- Automatic migration from Paper DB to DataStore
- Migration status tracking
- Rollback capability for testing
- Error handling and logging

**Key Methods**:
```kotlin
suspend fun isMigrationCompleted(): Boolean
suspend fun migrateToDataStore()
suspend fun rollbackMigration()
suspend fun getMigrationStatus(): MigrationStatus
```

### 3. `ModernDashboardManager.kt`
**Location**: `app/src/main/java/topgrade/parent/com/parentseeks/Parent/Utils/ModernDashboardManager.kt`

**Features**:
- Reactive UI updates using StateFlow
- Automatic migration handling
- Modern coroutine-based operations
- Type-safe data access

**Key Features**:
```kotlin
// Reactive StateFlows
val dashboardConfig: StateFlow<DashboardConfig>
val userPreferences: StateFlow<UserPreferences>
val filteredMenuItems: StateFlow<List<DashboardMenuItem>>

// Modern Operations
fun toggleFavorite(itemId: String)
fun hideItem(itemId: String)
fun showItem(itemId: String)
fun updateLayoutPreference(layout: String)
```

## Migration Process

### Automatic Migration
The migration happens automatically when `ModernDashboardManager` is initialized:

1. **Check Migration Status**: Verifies if migration has been completed
2. **Migrate Data**: If needed, migrates all data from Paper DB to DataStore
3. **Mark Complete**: Sets migration flag to prevent re-migration
4. **Initialize**: Sets up default menu items and configurations

### Manual Migration
You can also trigger migration manually:

```kotlin
val migrationHelper = DashboardMigrationHelper(context)
lifecycleScope.launch {
    if (!migrationHelper.isMigrationCompleted()) {
        migrationHelper.migrateToDataStore()
    }
}
```

## Usage Examples

### 1. Basic Usage
```kotlin
// Initialize the modern dashboard manager
val dashboardManager = ModernDashboardManager(context)

// Observe dashboard configuration changes
lifecycleScope.launch {
    dashboardManager.dashboardConfig.collect { config ->
        // Update UI with new configuration
        updateLayout(config.layoutType)
        updateTheme(config.theme)
    }
}

// Observe user preferences changes
lifecycleScope.launch {
    dashboardManager.userPreferences.collect { prefs ->
        // Update UI with new preferences
        updateGridColumns(prefs.gridColumns)
        updateFavorites(prefs.favoriteItems)
    }
}
```

### 2. Reactive Menu Items
```kotlin
// Observe filtered menu items
lifecycleScope.launch {
    dashboardManager.filteredMenuItems.collect { items ->
        // Update RecyclerView or other UI components
        adapter.submitList(items)
    }
}
```

### 3. User Interactions
```kotlin
// Toggle favorite
dashboardManager.toggleFavorite("staff_profile")

// Hide/show menu item
dashboardManager.hideItem("salary")
dashboardManager.showItem("salary")

// Update preferences
dashboardManager.updateLayoutPreference("list")
dashboardManager.updateThemePreference("dark")
dashboardManager.updateGridColumns(4)
```

### 4. Custom Order
```kotlin
// Update custom menu order
val customOrder = listOf("staff_profile", "salary", "timetable", "events")
dashboardManager.updateCustomOrder(customOrder)
```

## Benefits of Migration

### 1. Performance Improvements
- **Faster Read/Write**: DataStore is optimized for modern Android
- **Reduced Memory Usage**: More efficient data storage
- **Background Operations**: Non-blocking I/O operations

### 2. Type Safety
- **Compile-time Safety**: Type-safe preference keys
- **Null Safety**: Proper handling of nullable values
- **Error Prevention**: Reduced runtime errors

### 3. Modern Android Features
- **Kotlin Coroutines**: Async/await pattern
- **Reactive Programming**: Flow-based data streams
- **Lifecycle Awareness**: Proper lifecycle management

### 4. Better Error Handling
- **Graceful Degradation**: Fallback values on errors
- **Detailed Logging**: Better debugging capabilities
- **Recovery Mechanisms**: Automatic error recovery

## Migration Status

### Migration Tracking
The migration status is tracked using a flag in Paper DB:
- `dashboard_migration_completed`: Boolean flag indicating migration completion

### Migration Status Information
```kotlin
val status = migrationHelper.getMigrationStatus()
// Returns:
// - isCompleted: Boolean
// - hasPaperData: Boolean  
// - hasConfig: Boolean
// - hasPreferences: Boolean
```

## Rollback Capability

For testing or debugging purposes, you can rollback the migration:

```kotlin
// Rollback migration (for testing only)
migrationHelper.rollbackMigration()
```

## Data Structure

### DataStore Keys
```kotlin
// Dashboard Configuration
LAYOUT_TYPE = "layout_type"
GRID_COLUMNS = "grid_columns"
SHOW_CATEGORIES = "show_categories"
SHOW_SEARCH = "show_search"
SHOW_FAVORITES = "show_favorites"
ENABLE_ANIMATIONS = "enable_animations"
REFRESH_INTERVAL = "refresh_interval"
THEME = "theme"
CATEGORIES_JSON = "categories_json"
MENU_ITEMS_JSON = "menu_items_json"

// User Preferences
USER_ID = "user_id"
FAVORITE_ITEMS_JSON = "favorite_items_json"
HIDDEN_ITEMS_JSON = "hidden_items_json"
CUSTOM_ORDER_JSON = "custom_order_json"
LAYOUT_PREFERENCE = "layout_preference"
USER_GRID_COLUMNS = "user_grid_columns"
THEME_PREFERENCE = "theme_preference"
SHOW_NOTIFICATIONS = "show_notifications"
AUTO_REFRESH = "auto_refresh"
USER_REFRESH_INTERVAL = "user_refresh_interval"
SHOW_BADGES = "show_badges"
COMPACT_MODE = "compact_mode"
LAST_UPDATED = "last_updated"

// User Role and Permissions
USER_ROLE = "user_role"
USER_PERMISSIONS_JSON = "user_permissions_json"
```

## Testing

### Migration Testing
```kotlin
// Test migration status
val status = migrationHelper.getMigrationStatus()
assert(status.isCompleted == false)

// Perform migration
migrationHelper.migrateToDataStore()

// Verify migration
val newStatus = migrationHelper.getMigrationStatus()
assert(newStatus.isCompleted == true)
```

### Data Integrity Testing
```kotlin
// Verify data migration
val originalConfig = Paper.book().read("dashboard_config", "")
val migratedConfig = dashboardDataStore.dashboardConfig.first()

// Compare configurations
assert(originalConfig == gson.toJson(migratedConfig))
```

## Future Enhancements

### 1. Server Sync
- Implement server-side dashboard configuration
- Real-time preference synchronization
- Conflict resolution strategies

### 2. Advanced Features
- Dashboard templates
- User-specific configurations
- Analytics and usage tracking

### 3. Performance Optimizations
- Lazy loading of menu items
- Caching strategies
- Background data prefetching

## Troubleshooting

### Common Issues

1. **Migration Fails**
   - Check Paper DB data integrity
   - Verify DataStore permissions
   - Review error logs

2. **Data Loss**
   - Migration preserves original Paper DB data
   - Rollback capability available
   - Backup mechanisms in place

3. **Performance Issues**
   - Monitor DataStore operations
   - Check for memory leaks
   - Optimize Flow collection

### Debug Information
```kotlin
// Enable debug logging
Log.d("DashboardDataStore", "Migration status: ${migrationHelper.getMigrationStatus()}")
Log.d("DashboardDataStore", "DataStore keys: ${dashboardDataStore.getAllKeys()}")
```

## Conclusion

The Dashboard DataStore migration provides a modern, efficient, and type-safe solution for managing dashboard configuration and user preferences. The migration is automatic, safe, and maintains backward compatibility while providing significant performance and developer experience improvements. 