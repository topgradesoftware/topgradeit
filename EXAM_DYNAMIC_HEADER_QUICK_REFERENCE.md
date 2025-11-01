# Dynamic Header Title - Quick Reference Guide

## 🎯 Quick Overview
The ExamSubmit activity now displays different header titles and button texts based on the mode it's launched in.

## 📋 Mode Reference Table

| Mode | Header Title | Button Text | Button State | Use Case |
|------|--------------|-------------|--------------|----------|
| `null` or `SUBMIT` | Exam Mark Entry | Submit Marks | ✅ Enabled | New exam submission |
| `UPDATE` | Update Exam Marks | Update Marks | ✅ Enabled | Modify existing marks |
| `VIEW` or `VIEW_RESULTS` | View Exam Results | View Results | ❌ Disabled | Read-only view |

## 💻 Code Examples

### Default Mode (Submit New Marks)
```java
Intent intent = new Intent(context, ExamSubmit.class);
// No MODE parameter = default SUBMIT mode
startActivity(intent);
```

### Update Existing Marks
```java
Intent intent = new Intent(context, ExamSubmit.class);
intent.putExtra("MODE", "UPDATE");
intent.putExtra("LOAD_SUBMITTED_DATA", true);
startActivity(intent);
```

### View Results Only
```java
Intent intent = new Intent(context, ExamSubmit.class);
intent.putExtra("MODE", "VIEW_RESULTS");
intent.putExtra("LOAD_SUBMITTED_DATA", true);
startActivity(intent);
```

## 🔧 Implementation Details

### Modified Files
- ✅ `ExamSubmit.java` - Added `updateHeaderTitleBasedOnMode()` method
- ✅ Called automatically in `onCreate()`

### Method Location
```java
// ExamSubmit.java - Line 228
private void updateHeaderTitleBasedOnMode()
```

### Key Features
- ✅ Automatic mode detection
- ✅ Null-safe implementation
- ✅ Case-insensitive mode comparison
- ✅ Comprehensive logging
- ✅ Error handling

## 🎨 UI Elements Updated

1. **Header Title** (`R.id.header_title`)
   - Changes based on mode
   - Located in the top header wave

2. **Submit Button** (`R.id.Submit_Marks`)
   - Text changes based on mode
   - Disabled in VIEW mode
   - Enabled in SUBMIT/UPDATE mode

## 🔍 Debugging

### Check Mode in Logcat
```
Filter: "ExamSubmit"
Look for: "Header title set to: [Mode Name]"
```

### Example Log Output
```
D/ExamSubmit: Header title set to: Exam Mark Entry (Mode: SUBMIT)
D/ExamSubmit: Header title set to: Update Exam Marks
D/ExamSubmit: Header title set to: View Exam Results (Read-only)
```

## ⚠️ Important Notes

1. **Mode String**: Case-insensitive (`"UPDATE"` = `"update"` = `"Update"`)
2. **VIEW Modes**: Both `"VIEW"` and `"VIEW_RESULTS"` work the same
3. **Default Behavior**: No mode = SUBMIT mode
4. **Button State**: VIEW mode disables the submit button for safety

## 🎯 Common Use Cases

### From ExamManagementDashboard

1. **Submit Exam Button** → `MODE: SUBMIT`
2. **Update Exam Button** → `MODE: UPDATE`
3. **View Results Button** → `MODE: VIEW_RESULTS`
4. **Submit Marks Button** → `MODE: SUBMIT`

## 📱 Visual Indicators

### Submit Mode
```
┌─────────────────────────────────────┐
│  ← Exam Mark Entry                  │
└─────────────────────────────────────┘
[Submit Marks] ← Button enabled
```

### Update Mode
```
┌─────────────────────────────────────┐
│  ← Update Exam Marks                │
└─────────────────────────────────────┘
[Update Marks] ← Button enabled
```

### View Mode
```
┌─────────────────────────────────────┐
│  ← View Exam Results                │
└─────────────────────────────────────┘
[View Results] ← Button disabled
```

## 🚀 Testing Quick Steps

1. Launch ExamSubmit with no MODE → Check header says "Exam Mark Entry"
2. Launch with `MODE: UPDATE` → Check header says "Update Exam Marks"
3. Launch with `MODE: VIEW_RESULTS` → Check header says "View Exam Results" and button is disabled
4. Check logs for mode detection confirmation

## 📞 Support

If the header doesn't update:
1. Check if MODE parameter is being passed correctly
2. Check Logcat for "Header title set to:" messages
3. Verify `updateHeaderTitleBasedOnMode()` is called in onCreate
4. Ensure `R.id.header_title` exists in layout

---

**Last Updated**: October 27, 2025  
**Status**: ✅ Production Ready

