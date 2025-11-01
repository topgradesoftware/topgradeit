#!/usr/bin/env python3
"""
Footer Positioning Fix Script
This script checks and fixes footer positioning issues in all staff pages.
"""

import os
import re
import glob

def check_and_fix_footer_positioning():
    """Check and fix footer positioning in all staff layout files."""
    layout_dir = "app/src/main/res/layout"
    staff_files = glob.glob(f"{layout_dir}/activity_staff*.xml")
    
    print("Footer Positioning Check and Fix Script")
    print("=" * 45)
    print(f"Found {len(staff_files)} staff layout files")
    print()
    
    issues_found = 0
    fixed_count = 0
    
    for file_path in staff_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if 'powered_by_topgrade_software' in content:
                print(f"Checking: {os.path.basename(file_path)}")
                
                # Check for positioning issues
                issues = []
                
                # Issue 1: Footer before content container
                if 'footer_container' in content and 'content_container' in content:
                    footer_pos = content.find('footer_container')
                    content_pos = content.find('content_container')
                    
                    if footer_pos < content_pos:
                        issues.append("Footer positioned before content container")
                
                # Issue 2: Wrong footer structure (wrap_content instead of 50dp)
                if 'android:layout_height="wrap_content"' in content and 'footer_container' in content:
                    issues.append("Footer has wrap_content height instead of 50dp")
                
                # Issue 3: Missing center gravity on footer container
                if 'footer_container' in content and 'android:gravity="center"' not in content:
                    issues.append("Footer container missing center gravity")
                
                # Issue 4: Wrong padding (12dp instead of 8dp)
                if 'android:padding="12dp"' in content and 'footer_container' in content:
                    issues.append("Footer has 12dp padding instead of 8dp")
                
                # Issue 5: Wrong TextView gravity (center instead of center_vertical)
                if 'android:gravity="center"' in content and 'powered_by_topgrade_software' in content:
                    issues.append("TextView has center gravity instead of center_vertical")
                
                if issues:
                    issues_found += 1
                    print(f"  ❌ Issues found:")
                    for issue in issues:
                        print(f"    - {issue}")
                    
                    # Fix the issues
                    updated_content = content
                    
                    # Fix 1: Move footer to correct position (after content container)
                    if "Footer positioned before content container" in issues:
                        # This requires manual intervention - mark for review
                        print(f"    ⚠️  Manual fix needed: Footer positioning")
                    
                    # Fix 2: Update footer height
                    if "Footer has wrap_content height instead of 50dp" in issues:
                        updated_content = re.sub(
                            r'(<LinearLayout\s+[^>]*android:id="@\+id/footer_container"[^>]*android:layout_height=")wrap_content(")',
                            r'\1"50dp"\2',
                            updated_content
                        )
                    
                    # Fix 3: Add center gravity
                    if "Footer container missing center gravity" in issues:
                        updated_content = re.sub(
                            r'(<LinearLayout\s+[^>]*android:id="@\+id/footer_container"[^>]*android:orientation="vertical"[^>]*>)',
                            r'\1\n        android:gravity="center"',
                            updated_content
                        )
                    
                    # Fix 4: Update padding
                    if "Footer has 12dp padding instead of 8dp" in issues:
                        updated_content = re.sub(
                            r'android:padding="12dp"',
                            'android:padding="8dp"',
                            updated_content
                        )
                    
                    # Fix 5: Update TextView gravity
                    if "TextView has center gravity instead of center_vertical" in issues:
                        updated_content = re.sub(
                            r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*android:gravity=")center(")',
                            r'\1"center_vertical"\2',
                            updated_content
                        )
                    
                    # Remove minHeight and maxHeight constraints
                    updated_content = re.sub(r'android:minHeight="50dp"', '', updated_content)
                    updated_content = re.sub(r'android:maxHeight="50dp"', '', updated_content)
                    
                    if updated_content != content:
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(updated_content)
                        print(f"    ✅ Fixed: {os.path.basename(file_path)}")
                        fixed_count += 1
                    else:
                        print(f"    ⚠️  Manual fix needed: {os.path.basename(file_path)}")
                else:
                    print(f"  ✅ No issues found")
            else:
                print(f"  - No footer found: {os.path.basename(file_path)}")
                
        except Exception as e:
            print(f"Error processing {file_path}: {e}")
    
    print(f"\nSummary:")
    print(f"  Files with issues: {issues_found}")
    print(f"  Files auto-fixed: {fixed_count}")
    print(f"  Files needing manual review: {issues_found - fixed_count}")
    
    if issues_found > 0:
        print(f"\n⚠️  Some files may need manual review for footer positioning!")
        print(f"   Check files where footer appears before content container.")

if __name__ == "__main__":
    check_and_fix_footer_positioning()
