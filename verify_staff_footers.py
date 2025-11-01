#!/usr/bin/env python3
"""
Staff Footer Verification Script
This script verifies that all staff layout files have proper footer alignment.
"""

import os
import re
import glob

def verify_staff_footers():
    """Verify all staff footer alignments."""
    layout_dir = "app/src/main/res/layout"
    staff_files = glob.glob(f"{layout_dir}/activity_staff*.xml")
    
    print("Staff Footer Alignment Verification")
    print("=" * 40)
    print(f"Found {len(staff_files)} staff layout files")
    print()
    
    all_correct = True
    files_with_footers = 0
    
    for file_path in staff_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            has_footer = 'powered_by_topgrade_software' in content or 'topgrade_logo' in content
            has_correct_gravity = 'android:gravity="center_vertical|center_horizontal"' in content
            has_layout_gravity = 'android:layout_gravity="center_vertical"' in content
            
            if has_footer:
                files_with_footers += 1
                status = "✓" if has_correct_gravity and has_layout_gravity else "✗"
                print(f"{status} {os.path.basename(file_path)}")
                
                if not has_correct_gravity:
                    print(f"    Missing: android:gravity=\"center_vertical|center_horizontal\"")
                    all_correct = False
                
                if not has_layout_gravity:
                    print(f"    Missing: android:layout_gravity=\"center_vertical\"")
                    all_correct = False
            else:
                print(f"- {os.path.basename(file_path)} (no footer)")
                
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
            all_correct = False
    
    print()
    print(f"Files with footers: {files_with_footers}")
    
    if all_correct:
        print("✓ All staff footers are properly aligned!")
    else:
        print("✗ Some staff footers need alignment fixes.")
    
    return all_correct

if __name__ == "__main__":
    verify_staff_footers()
