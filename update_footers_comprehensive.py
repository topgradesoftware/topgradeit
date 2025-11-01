#!/usr/bin/env python3
"""
Comprehensive Footer Alignment Update Script
This script automatically updates all footer layouts in the Android project to ensure
consistent horizontal and vertical alignment of the logo and text.
"""

import os
import re
import glob
from pathlib import Path

def find_layout_files():
    """Find all layout files that contain footer content."""
    layout_dir = "app/src/main/res/layout"
    layout_files = []
    
    # Find all XML files in layout directory
    xml_files = glob.glob(f"{layout_dir}/*.xml")
    
    for xml_file in xml_files:
        try:
            with open(xml_file, 'r', encoding='utf-8') as f:
                content = f.read()
                # Check if file contains footer content (logo or powered by text)
                if ('topgrade_logo' in content or 
                    'powered_by_topgrade_software' in content):
                    layout_files.append(xml_file)
        except Exception as e:
            print(f"Error reading {xml_file}: {e}")
    
    return layout_files

def update_footer_alignment(file_path):
    """Update footer alignment in a single file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        updated = False
        
        # Pattern 1: Find LinearLayout containing footer content with horizontal orientation
        footer_patterns = [
            # Pattern for LinearLayout with horizontal orientation and padding
            r'<LinearLayout\s+[^>]*android:orientation="horizontal"[^>]*android:padding="8dp"[^>]*>',
            # Pattern for LinearLayout with horizontal orientation (more flexible)
            r'<LinearLayout\s+[^>]*android:orientation="horizontal"[^>]*>',
            # Pattern for any LinearLayout containing footer elements
            r'<LinearLayout\s+[^>]*>'
        ]
        
        for pattern in footer_patterns:
            matches = re.finditer(pattern, content, re.DOTALL)
            
            for match in matches:
                start_pos = match.start()
                
                # Find the closing tag of this LinearLayout
                open_count = 1
                end_pos = start_pos + len(match.group())
                
                while open_count > 0 and end_pos < len(content):
                    if content[end_pos] == '<':
                        if content[end_pos:end_pos+2] == '</':
                            if content[end_pos:end_pos+13] == '</LinearLayout':
                                open_count -= 1
                            end_pos += 1
                        elif content[end_pos:end_pos+12] == '<LinearLayout':
                            open_count += 1
                    end_pos += 1
                
                if open_count == 0:
                    # Extract the LinearLayout content
                    linear_layout_content = content[start_pos:end_pos]
                    
                    # Check if this LinearLayout contains footer elements
                    if ('topgrade_logo' in linear_layout_content or 
                        'powered_by_topgrade_software' in linear_layout_content):
                        
                        # Update LinearLayout gravity
                        if 'android:gravity=' in linear_layout_content:
                            linear_layout_content = re.sub(
                                r'android:gravity="[^"]*"',
                                'android:gravity="center_vertical|center_horizontal"',
                                linear_layout_content
                            )
                        else:
                            # Add gravity attribute
                            linear_layout_content = re.sub(
                                r'(<LinearLayout[^>]*android:orientation="horizontal"[^>]*)>',
                                r'\1\n        android:gravity="center_vertical|center_horizontal">',
                                linear_layout_content
                            )
                        
                        # Update ImageView layout_gravity
                        if 'android:src="@drawable/topgrade_logo"' in linear_layout_content:
                            if 'android:layout_gravity=' not in linear_layout_content:
                                linear_layout_content = re.sub(
                                    r'(<ImageView[^>]*android:src="@drawable/topgrade_logo"[^>]*>)',
                                    r'\1\n        android:layout_gravity="center_vertical"',
                                    linear_layout_content
                                )
                        
                        # Update TextView gravity and layout_gravity
                        if 'android:text="@string/powered_by_topgrade_software"' in linear_layout_content:
                            # Update existing gravity
                            if 'android:gravity=' in linear_layout_content:
                                linear_layout_content = re.sub(
                                    r'android:gravity="[^"]*"',
                                    'android:gravity="center_vertical"',
                                    linear_layout_content
                                )
                            else:
                                # Add gravity attribute
                                linear_layout_content = re.sub(
                                    r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*>)',
                                    r'\1\n        android:gravity="center_vertical"',
                                    linear_layout_content
                                )
                            
                            # Add layout_gravity if not present
                            if 'android:layout_gravity=' not in linear_layout_content:
                                linear_layout_content = re.sub(
                                    r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*>)',
                                    r'\1\n        android:layout_gravity="center_vertical"',
                                    linear_layout_content
                                )
                        
                        # Replace the original content
                        content = content[:start_pos] + linear_layout_content + content[end_pos:]
                        updated = True
                        break  # Only process the first matching LinearLayout
        
        # Add missing logo if text exists but logo doesn't
        if 'powered_by_topgrade_software' in content and 'topgrade_logo' not in content:
            # Find TextView with powered_by_topgrade_software
            text_pattern = r'(<TextView[^>]*android:text="@string/powered_by_topgrade_software"[^>]*>)'
            text_match = re.search(text_pattern, content)
            
            if text_match:
                # Find the parent LinearLayout
                text_pos = text_match.start()
                linear_start = content.rfind('<LinearLayout', 0, text_pos)
                
                if linear_start != -1:
                    # Find the end of LinearLayout opening tag
                    linear_end = content.find('>', linear_start) + 1
                    
                    # Insert ImageView before the TextView
                    image_view = '''        <ImageView
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:layout_marginEnd="8dp"
            android:layout_marginRight="8dp"
            android:layout_gravity="center_vertical"
            android:src="@drawable/topgrade_logo"
            android:contentDescription="TopGrade Software Logo"
            android:scaleType="fitCenter" />

'''
                    
                    content = content[:linear_end] + '\n' + image_view + content[linear_end:]
                    updated = True
        
        if updated:
            # Write back the updated content
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
        
    except Exception as e:
        print(f"Error updating {file_path}: {e}")
        return False

def check_footer_status(file_path):
    """Check the current status of footer alignment in a file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        has_logo = 'topgrade_logo' in content
        has_text = 'powered_by_topgrade_software' in content
        has_correct_gravity = 'android:gravity="center_vertical|center_horizontal"' in content
        has_layout_gravity = 'android:layout_gravity="center_vertical"' in content
        
        return {
            'has_logo': has_logo,
            'has_text': has_text,
            'has_correct_gravity': has_correct_gravity,
            'has_layout_gravity': has_layout_gravity,
            'needs_update': (has_text and (not has_correct_gravity or not has_layout_gravity))
        }
        
    except Exception as e:
        print(f"Error checking {file_path}: {e}")
        return None

def main():
    """Main function to update all footer layouts."""
    print("Comprehensive Footer Alignment Update Script")
    print("=" * 50)
    
    # Find all layout files with footer content
    layout_files = find_layout_files()
    print(f"Found {len(layout_files)} layout files with footer content:")
    
    # Check current status
    print("\nChecking current footer status...")
    needs_update = []
    
    for file_path in layout_files:
        status = check_footer_status(file_path)
        if status:
            print(f"  {file_path}:")
            print(f"    - Has logo: {status['has_logo']}")
            print(f"    - Has text: {status['has_text']}")
            print(f"    - Has correct gravity: {status['has_correct_gravity']}")
            print(f"    - Has layout gravity: {status['has_layout_gravity']}")
            print(f"    - Needs update: {status['needs_update']}")
            
            if status['needs_update']:
                needs_update.append(file_path)
    
    if not needs_update:
        print("\n✓ All footers are already properly aligned!")
        return
    
    print(f"\nFound {len(needs_update)} files that need updating:")
    for file_path in needs_update:
        print(f"  - {file_path}")
    
    print("\nUpdating footer alignments...")
    
    updated_count = 0
    for file_path in needs_update:
        print(f"Processing: {file_path}")
        if update_footer_alignment(file_path):
            print(f"  ✓ Updated: {file_path}")
            updated_count += 1
        else:
            print(f"  ✗ Failed to update: {file_path}")
    
    print(f"\nUpdate complete! {updated_count} files were updated.")
    print("All footers now have consistent horizontal and vertical alignment.")

if __name__ == "__main__":
    main()
