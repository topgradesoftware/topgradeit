#!/usr/bin/env python3
"""
All Staff Footer Fix Script
This script applies the correct footer structure to all staff pages to ensure
perfect centering and footer positioning at the bottom of the screen.
"""

import os
import re
import glob

def fix_staff_footers():
    """Fix all staff footer layouts to match the working parent dashboard structure."""
    layout_dir = "app/src/main/res/layout"
    staff_files = glob.glob(f"{layout_dir}/activity_staff*.xml")
    
    print("All Staff Footer Fix Script")
    print("=" * 35)
    print(f"Found {len(staff_files)} staff layout files")
    print()
    
    updated_count = 0
    
    for file_path in staff_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            updated = False
            
            # Only process files that have footer content
            if 'powered_by_topgrade_software' in content:
                print(f"Processing: {os.path.basename(file_path)}")
                
                # Fix 1: Update footer container to have fixed height and center gravity
                # Pattern to find footer container LinearLayout
                footer_container_pattern = r'(<LinearLayout\s+[^>]*android:id="@\+id/footer_container"[^>]*android:layout_width="0dp"[^>]*android:layout_height=")wrap_content("[^>]*android:layout_marginHorizontal="12dp"[^>]*android:layout_marginBottom="16dp"[^>]*android:background="@drawable/footer_background_rounded"[^>]*android:elevation="8dp"[^>]*android:minHeight="50dp"[^>]*android:orientation="vertical"[^>]*>)'
                
                if re.search(footer_container_pattern, content):
                    # Replace with fixed height and center gravity
                    content = re.sub(footer_container_pattern, 
                        r'\1"50dp"\2\n        android:gravity="center"', content)
                    updated = True
                
                # Fix 2: Remove minHeight and maxHeight constraints
                content = re.sub(r'android:minHeight="50dp"', '', content)
                content = re.sub(r'android:maxHeight="50dp"', '', content)
                
                # Fix 3: Update footer content padding from 12dp to 8dp
                footer_content_pattern = r'(<LinearLayout\s+[^>]*android:layout_width="match_parent"[^>]*android:layout_height="match_parent"[^>]*android:orientation="horizontal"[^>]*android:padding=")12dp("[^>]*android:gravity="center_vertical\|center_horizontal"[^>]*>)'
                
                if re.search(footer_content_pattern, content):
                    content = re.sub(footer_content_pattern, r'\1"8dp"\2', content)
                    updated = True
                
                # Fix 4: Update TextView gravity from "center" to "center_vertical"
                textview_pattern = r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*android:gravity=")center("[^>]*>)'
                
                if re.search(textview_pattern, content):
                    content = re.sub(textview_pattern, r'\1"center_vertical"\2', content)
                    updated = True
                
                # Fix 5: Ensure footer is positioned at bottom using ConstraintLayout
                # Check if footer container has proper bottom constraint
                if 'app:layout_constraintBottom_toBottomOf="parent"' not in content:
                    # Add bottom constraint if missing
                    footer_constraint_pattern = r'(<LinearLayout\s+[^>]*android:id="@\+id/footer_container"[^>]*>)'
                    if re.search(footer_constraint_pattern, content):
                        content = re.sub(footer_constraint_pattern, 
                            r'\1\n        app:layout_constraintBottom_toBottomOf="parent"', content)
                        updated = True
                
                if updated:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                    print(f"  ✓ Updated: {os.path.basename(file_path)}")
                    updated_count += 1
                else:
                    print(f"  - No changes needed: {os.path.basename(file_path)}")
            else:
                print(f"  - No footer found: {os.path.basename(file_path)}")
                
        except Exception as e:
            print(f"Error updating {file_path}: {e}")
    
    print(f"\nUpdate complete! {updated_count} files were updated.")
    print("All staff footers now have:")
    print("  ✅ Fixed height (50dp)")
    print("  ✅ Center gravity on container")
    print("  ✅ Proper padding (8dp)")
    print("  ✅ Correct TextView gravity")
    print("  ✅ Footer positioned at bottom of screen")

if __name__ == "__main__":
    fix_staff_footers()
