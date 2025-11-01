# Parent Complaint Filtering - Quick Reference

## Quick Overview
The parent complaint module now uses the same professional filtering logic as the staff leave application module, with dynamic status-based filtering and real-time counter updates.

## What Changed

### Before
- Used string literals: `"all"`, `"pending"`, `"under_discussion"`, `"solved"`
- Basic status text matching
- Simple counter logic

### After
- Uses constants: `Constant.FILTER_COMPLAINT_ALL`, `Constant.FILTER_COMPLAINT_PENDING`, etc.
- Robust status-based filtering with fallbacks
- Clean, maintainable code structure following staff leave pattern

## Filter Constants

Located in: `Constant.java`

```java
public static final String FILTER_COMPLAINT_ALL = "all_complaints";
public static final String FILTER_COMPLAINT_PENDING = "pending_complaints";
public static final String FILTER_COMPLAINT_DISCUSSION = "discussion_complaints";
public static final String FILTER_COMPLAINT_SOLVED = "solved_complaints";
```

## How It Works

### Menu Screen (ParentComplaintMenu)
1. **Loads all complaints** from backend
2. **Categorizes by status** using `categorizeComplaints()`
3. **Updates badge counters** showing count for each status
4. **User clicks filter** → Opens filtered list

### List Screen (ParentComplaintList)
1. **Receives filter type** from menu
2. **Loads all complaints** from backend
3. **Filters client-side** using `filterComplaints()`
4. **Displays filtered results** with correct header title

## Status Mapping

### Pending
- Backend values: `"pending"`
- Badge color: Orange
- Default for unknown statuses

### Under Discussion
- Backend values: `"discussion"`, `"progress"`, `"under discussion"`
- Badge color: Blue

### Solved
- Backend values: `"solved"`, `"resolved"`, `"closed"`
- Badge color: Green

## Key Methods

### ParentComplaintMenu.java

```java
categorizeComplaints(complaints)  // Counts complaints by status
resetCounts()                      // Resets all counters to 0
updateBadges()                     // Updates UI badge numbers
```

### ParentComplaintList.java

```java
filterComplaints(complaints, filter)  // Filters by status
setHeaderTitle()                      // Updates header based on filter
```

## Code Pattern

This follows the exact same pattern as:
- `StaffApplicationMenu.java` → Counter/badge logic
- `StaffApplicationList.java` → Filtering logic

## Testing

Deploy to device and verify:

1. **Menu counters** show correct numbers
2. **Filter buttons** open correct filtered lists
3. **Header titles** match the filter
4. **Empty state** shows when no complaints
5. **Refresh** updates counters correctly

## Backend Status Field

The implementation checks two fields for status:
1. `status_text` (primary)
2. `complaint_status` (fallback)

This ensures compatibility with different backend responses.

## Example Usage

```java
// In ParentComplaintMenu
btnPendingComplaints.setOnClickListener(v -> 
    openComplaintList(Constant.FILTER_COMPLAINT_PENDING));

// In ParentComplaintList
private void setHeaderTitle() {
    if (Constant.FILTER_COMPLAINT_PENDING.equals(filterType)) {
        headerTitle.setText("Pending Complaints");
    }
}
```

## Benefits

✅ **Type-safe** - No typos possible  
✅ **Maintainable** - Constants in one place  
✅ **Dynamic** - Updates based on backend status  
✅ **Consistent** - Matches project patterns  
✅ **Debuggable** - Enhanced logging  
✅ **Performant** - Client-side filtering  

## Files Modified

1. `Constant.java` - Added filter constants
2. `ParentComplaintMenu.java` - Enhanced counter logic
3. `ParentComplaintList.java` - Improved filtering

## Common Issues & Solutions

**Issue**: Counters not updating  
**Solution**: Check `onResume()` calls `loadComplaintCounts()`

**Issue**: Wrong filter applied  
**Solution**: Verify constant usage in click listeners

**Issue**: Status not recognized  
**Solution**: Add status keyword to filtering logic

## Related Documentation

- See `PARENT_COMPLAINT_STATUS_FILTERING_IMPLEMENTATION.md` for complete details
- See `StaffApplicationMenu.java` for reference pattern
- See `StaffApplicationList.java` for filtering pattern

## Support

For issues or questions:
1. Check debug logs in Android Studio Logcat
2. Filter by tag: `ParentComplaintMenu` or `ParentComplaintList`
3. Look for log messages showing complaint counts and filtering

## Last Updated
October 30, 2025

