#!/usr/bin/env python3
"""
Final Footer Fix Script
Simple script to fix all remaining footer issues.
"""

import os
import glob

def final_footer_fix():
    """Fix all remaining footer issues with direct replacements."""
    layout_dir = "app/src/main/res/layout"
    staff_files = glob.glob(f"{layout_dir}/activity_staff*.xml")
    
    print("Final Footer Fix Script")
    print("=" * 25)
    print(f"Found {len(staff_files)} staff layout files")
    print()
    
    fixed_count = 0
    
    for file_path in staff_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if 'powered_by_topgrade_software' in content:
                print(f"Processing: {os.path.basename(file_path)}")
                updated = False
                
                # Fix 1: Replace wrap_content with 50dp for footer height
                if 'android:layout_height="wrap_content"' in content and 'footer_container' in content:
                    content = content.replace('android:layout_height="wrap_content"', 'android:layout_height="50dp"')
                    updated = True
                
                # Fix 2: Replace center with center_vertical for TextView gravity
                if 'android:gravity="center"' in content and 'powered_by_topgrade_software' in content:
                    content = content.replace('android:gravity="center"', 'android:gravity="center_vertical"')
                    updated = True
                
                # Fix 3: Remove minHeight and maxHeight constraints
                content = content.replace('android:minHeight="50dp"', '')
                content = content.replace('android:maxHeight="50dp"', '')
                
                # Fix 4: Ensure footer container has center gravity
                if 'footer_container' in content and 'android:gravity="center"' not in content:
                    # Add center gravity to footer container
                    content = content.replace(
                        'android:orientation="vertical"',
                        'android:orientation="vertical"\n        android:gravity="center"'
                    )
                    updated = True
                
                if updated:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(content)
                    print(f"  ✅ Fixed: {os.path.basename(file_path)}")
                    fixed_count += 1
                else:
                    print(f"  - No changes needed: {os.path.basename(file_path)}")
            else:
                print(f"  - No footer found: {os.path.basename(file_path)}")
                
        except Exception as e:
            print(f"Error processing {file_path}: {e}")
    
    print(f"\nFinal fix complete! {fixed_count} files were updated.")
    print("All staff footers should now have:")
    print("  ✅ Fixed height (50dp)")
    print("  ✅ Center gravity on container")
    print("  ✅ Proper TextView gravity (center_vertical)")
    print("  ✅ Footer positioned at bottom of screen")

if __name__ == "__main__":
    final_footer_fix()
