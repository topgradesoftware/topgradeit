# Timetable API Fix for Period Number Issue

## Problem:
The period number is showing as "10" for all entries because the API is not properly incrementing the `timetable_order` field.

## Root Cause:
In the API code, the `timetable_order` is set to `e.display_order` from the outer query, but the inner loop doesn't properly increment this value for each period.

## Current API Structure:
```php
// Outer query - gets staff info
$cols = Array("e.display_order as timetable_order", ...);
$data_info_all = $db->get('timetable e', null, $cols);

// Inner loop - gets period details
foreach ($data_info_all as $value) {
    $cols = Array("e.display_order as timetable_order", ...);
    $data_info_all2 = $db->get('timetable e', null, $cols);
    
    foreach ($data_info_all2 as $value2) {
        // All items get the same display_order value
        $value['detail'][] = $value2;
    }
}
```

## Suggested Fix:

### Option 1: Use Row Number in Inner Query
```php
// In the inner query, use ROW_NUMBER() to generate sequential period numbers
$cols = Array(
    "e.parent_id", 
    "e.unique_id", 
    "e.session_id", 
    "e.display_order", 
    "e.student_class_id", 
    "ts.section_id", 
    "e.timetable_session_id", 
    "e.full_name", 
    "e.is_active", 
    "e.full_name as display_name_order", 
    "c.display_order as display_name_class", 
    "c.display_order as class_order", 
    "ROW_NUMBER() OVER (ORDER BY ts.start_time ASC) as timetable_order", // Use ROW_NUMBER instead
    "c.full_name as class_name", 
    "es.full_name as timetable_session_name", 
    "es.shift as shift", 
    "su.full_name as subject", 
    "st.full_name as staff", 
    "st.unique_id as stid", 
    "st.phone as phone", 
    "ts.start_time", 
    "ts.end_time", 
    "sec.full_name as section"
);
```

### Option 2: Manual Counter in PHP
```php
foreach ($data_info_all as $value) {
    $periodCounter = 1; // Reset counter for each staff
    
    $cols = Array("e.display_order as timetable_order", ...);
    $data_info_all2 = $db->get('timetable e', null, $cols);
    
    foreach ($data_info_all2 as $value2) {
        $value2['timetable_order'] = $periodCounter; // Set sequential period number
        $value['detail'][] = $value2;
        $periodCounter++;
    }
}
```

### Option 3: Use ORDER BY Position
```php
// In the inner query, use the position from ORDER BY
$db->orderBy('ts.start_time', 'asc');
$data_info_all2 = $db->get('timetable e', null, $cols);

// Then in PHP, set the period number based on array position
foreach ($data_info_all2 as $index => $value2) {
    $value2['timetable_order'] = $index + 1; // 1-based period numbering
    $value['detail'][] = $value2;
}
```

## Recommended Solution:
Use **Option 1** with `ROW_NUMBER()` as it's the most efficient and handles the ordering at the database level.

## Testing:
After implementing the fix, the period numbers should show as:
- Period 1
- Period 2  
- Period 3
- etc.

Instead of all showing "10".
