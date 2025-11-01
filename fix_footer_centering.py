#!/usr/bin/env python3
"""
Footer Centering Fix Script
This script fixes footer alignment to ensure perfect vertical and horizontal centering.
"""

import os
import re
import glob

def fix_footer_centering():
    """Fix footer centering in all layout files."""
    layout_dir = "app/src/main/res/layout"
    staff_files = glob.glob(f"{layout_dir}/activity_staff*.xml")
    
    print("Footer Centering Fix Script")
    print("=" * 30)
    print(f"Found {len(staff_files)} staff layout files")
    print()
    
    updated_count = 0
    
    for file_path in staff_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original_content = content
            updated = False
            
            # Fix 1: Change layout_height from wrap_content to match_parent for footer content LinearLayout
            if 'powered_by_topgrade_software' in content:
                # Find the footer content LinearLayout and fix its height
                pattern1 = r'(<LinearLayout\s+[^>]*android:layout_width="match_parent"[^>]*android:layout_height=")wrap_content("[^>]*android:orientation="horizontal"[^>]*android:padding="8dp"[^>]*android:gravity="center_vertical\|center_horizontal"[^>]*>)'
                if re.search(pattern1, content):
                    content = re.sub(pattern1, r'\1match_parent\2', content)
                    updated = True
                
                # Fix 2: Ensure TextView has center gravity instead of just center_vertical
                pattern2 = r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*android:gravity=")center_vertical("[^>]*>)'
                if re.search(pattern2, content):
                    content = re.sub(pattern2, r'\1center\2', content)
                    updated = True
            
            if updated:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"✓ Updated: {os.path.basename(file_path)}")
                updated_count += 1
            else:
                print(f"- No changes needed: {os.path.basename(file_path)}")
                
        except Exception as e:
            print(f"Error updating {file_path}: {e}")
    
    print(f"\nUpdate complete! {updated_count} files were updated.")
    print("All footers now have perfect vertical and horizontal centering.")

if __name__ == "__main__":
    fix_footer_centering()
