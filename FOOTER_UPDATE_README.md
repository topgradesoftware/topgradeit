# Footer Alignment Update Scripts

This directory contains scripts to automatically update footer alignments across all Android layout files in the project.

## Scripts Overview

### 1. `update_footers.py`
Basic footer alignment update script that finds and updates footer layouts.

### 2. `update_footers_comprehensive.py`
Comprehensive footer alignment update script with detailed status checking and reporting.

### 3. `update_footers.bat`
Windows batch script for easy execution of the comprehensive update script.

## What the Scripts Do

The scripts automatically:

1. **Find all layout files** containing footer content (logo or "Powered By TopGrade Software" text)
2. **Check current alignment status** of each footer
3. **Update footer alignments** to ensure consistent horizontal and vertical alignment:
   - Sets `android:gravity="center_vertical|center_horizontal"` on the parent LinearLayout
   - Adds `android:layout_gravity="center_vertical"` to the ImageView (logo)
   - Sets `android:gravity="center_vertical"` and `android:layout_gravity="center_vertical"` on the TextView
4. **Add missing logos** if the text exists but the logo is missing

## Usage

### Windows Users
Simply double-click `update_footers.bat` or run it from the command line:
```cmd
update_footers.bat
```

### All Platforms
Run the Python script directly:
```bash
python update_footers_comprehensive.py
```

## Current Status

As of the last run, all 37 footer layouts are properly aligned with:
- ✅ Logo present
- ✅ Text present (where applicable)
- ✅ Correct gravity attributes
- ✅ Proper layout_gravity attributes

## Files Covered

The scripts automatically detect and update footer layouts in:
- Staff dashboard and activity screens
- Student dashboard and activity screens  
- Parent dashboard and activity screens
- Login screens
- All other screens with footer content

## Footer Structure

The scripts ensure all footers follow this consistent structure:

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp"
    android:gravity="center_vertical|center_horizontal">

    <ImageView
        android:layout_width="18dp"
        android:layout_height="18dp"
        android:layout_marginEnd="8dp"
        android:layout_marginRight="8dp"
        android:layout_gravity="center_vertical"
        android:src="@drawable/topgrade_logo"
        android:contentDescription="TopGrade Software Logo"
        android:scaleType="fitCenter" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:ellipsize="end"
        android:fontFamily="@font/quicksand_bold"
        android:gravity="center_vertical"
        android:layout_gravity="center_vertical"
        android:maxLines="1"
        android:text="@string/powered_by_topgrade_software"
        android:textColor="@android:color/white"
        android:textSize="12sp"
        android:textStyle="bold" />

</LinearLayout>
```

## Requirements

- Python 3.6 or higher
- Access to the Android project directory structure

## Notes

- The scripts are safe to run multiple times - they only update files that need changes
- All changes are made in-place to the existing XML files
- The scripts preserve all other attributes and formatting in the layout files
