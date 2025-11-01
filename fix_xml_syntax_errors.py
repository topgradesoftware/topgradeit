#!/usr/bin/env python3
"""
Fix XML syntax errors in layout files caused by malformed script conversions.
This script will fix:
1. Duplicate attribute errors
2. Unclosed RelativeLayout tags
3. Malformed LinearLayout conversions
"""

import os
import re
import glob

def fix_duplicate_attributes(file_path):
    """Fix duplicate android:layout_width and android:layout_height attributes."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Pattern to match duplicate attributes in LinearLayout opening tags
        pattern = r'<LinearLayout[^>]*android:layout_width="[^"]*"[^>]*\s+android:layout_width="[^"]*"[^>]*>'
        
        def fix_duplicate_match(match):
            tag_content = match.group(0)
            # Remove the duplicate attributes
            # Keep the first occurrence and remove subsequent ones
            lines = tag_content.split('\n')
            fixed_lines = []
            seen_width = False
            seen_height = False
            
            for line in lines:
                if 'android:layout_width=' in line and not seen_width:
                    fixed_lines.append(line)
                    seen_width = True
                elif 'android:layout_height=' in line and not seen_height:
                    fixed_lines.append(line)
                    seen_height = True
                elif 'android:layout_width=' in line and seen_width:
                    # Skip duplicate
                    continue
                elif 'android:layout_height=' in line and seen_height:
                    # Skip duplicate
                    continue
                else:
                    fixed_lines.append(line)
            
            return '\n'.join(fixed_lines)
        
        # Fix the duplicates
        new_content = re.sub(pattern, fix_duplicate_match, content, flags=re.DOTALL)
        
        if new_content != content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Fixed duplicate attributes in: {file_path}")
            return True
    except Exception as e:
        print(f"Error fixing {file_path}: {e}")
    
    return False

def fix_malformed_linear_layouts(file_path):
    """Fix malformed LinearLayout conversions with extra whitespace and broken attributes."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Fix malformed LinearLayout opening tags with broken attributes
        # Pattern: <LinearLayout ... android:layout_width="..." android:layout_height="..." \n \n android:layout_width="..." android:layout_height="..."
        pattern = r'(<LinearLayout[^>]*android:layout_width="[^"]*"[^>]*android:layout_height="[^"]*"[^>]*)\s*\n\s*\n\s*(android:layout_width="[^"]*"[^>]*android:layout_height="[^"]*"[^>]*>)'
        
        def fix_malformed_match(match):
            first_part = match.group(1)
            second_part = match.group(2)
            
            # Extract the proper attributes from the second part
            width_match = re.search(r'android:layout_width="[^"]*"', second_part)
            height_match = re.search(r'android:layout_height="[^"]*"', second_part)
            background_match = re.search(r'android:background="[^"]*"', second_part)
            orientation_match = re.search(r'android:orientation="[^"]*"', second_part)
            
            # Build the fixed tag
            fixed_tag = first_part
            if background_match:
                fixed_tag += '\n    ' + background_match.group(0)
            if orientation_match:
                fixed_tag += '\n    ' + orientation_match.group(0)
            fixed_tag += '>'
            
            return fixed_tag
        
        content = re.sub(pattern, fix_malformed_match, content, flags=re.DOTALL)
        
        # Fix another pattern where attributes are split across lines incorrectly
        pattern2 = r'(<LinearLayout[^>]*android:layout_width="[^"]*"[^>]*android:layout_height="[^"]*"[^>]*)\s*\n\s*\n\s*(android:layout_width="[^"]*"[^>]*android:layout_height="[^"]*"[^>]*android:background="[^"]*"[^>]*android:orientation="[^"]*"[^>]*>)'
        
        def fix_malformed_match2(match):
            first_part = match.group(1)
            second_part = match.group(2)
            
            # Extract attributes from second part
            background_match = re.search(r'android:background="[^"]*"', second_part)
            orientation_match = re.search(r'android:orientation="[^"]*"', second_part)
            
            # Build the fixed tag
            fixed_tag = first_part
            if background_match:
                fixed_tag += '\n    ' + background_match.group(0)
            if orientation_match:
                fixed_tag += '\n    ' + orientation_match.group(0)
            fixed_tag += '>'
            
            return fixed_tag
        
        content = re.sub(pattern2, fix_malformed_match2, content, flags=re.DOTALL)
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed malformed LinearLayout in: {file_path}")
            return True
    except Exception as e:
        print(f"Error fixing malformed LinearLayout in {file_path}: {e}")
    
    return False

def fix_unclosed_relative_layouts(file_path):
    """Fix unclosed RelativeLayout tags by converting them to ConstraintLayout properly."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Find RelativeLayout tags that weren't properly converted
        relative_pattern = r'<RelativeLayout\s+([^>]*android:id="@\+id/header"[^>]*)>'
        
        def fix_relative_layout(match):
            attributes = match.group(1)
            
            # Extract key attributes
            id_match = re.search(r'android:id="([^"]*)"', attributes)
            width_match = re.search(r'android:layout_width="([^"]*)"', attributes)
            height_match = re.search(r'android:layout_height="([^"]*)"', attributes)
            background_match = re.search(r'android:background="([^"]*)"', attributes)
            
            # Build ConstraintLayout tag
            fixed_tag = '<androidx.constraintlayout.widget.ConstraintLayout'
            if id_match:
                fixed_tag += f' android:id="{id_match.group(1)}"'
            if width_match:
                fixed_tag += f' android:layout_width="{width_match.group(1)}"'
            if height_match:
                fixed_tag += f' android:layout_height="{height_match.group(1)}"'
            if background_match:
                fixed_tag += f' android:background="{background_match.group(1)}"'
            
            fixed_tag += '\n        app:layout_constraintTop_toTopOf="parent"'
            fixed_tag += '\n        app:layout_constraintStart_toStartOf="parent"'
            fixed_tag += '\n        app:layout_constraintEnd_toEndOf="parent">'
            
            return fixed_tag
        
        content = re.sub(relative_pattern, fix_relative_layout, content)
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed unclosed RelativeLayout in: {file_path}")
            return True
    except Exception as e:
        print(f"Error fixing RelativeLayout in {file_path}: {e}")
    
    return False

def main():
    """Main function to fix all XML syntax errors."""
    layout_dir = "app/src/main/res/layout"
    
    if not os.path.exists(layout_dir):
        print(f"Layout directory not found: {layout_dir}")
        return
    
    # Get all XML files
    xml_files = glob.glob(os.path.join(layout_dir, "*.xml"))
    
    fixed_count = 0
    
    for xml_file in xml_files:
        print(f"Processing: {xml_file}")
        
        # Fix duplicate attributes
        if fix_duplicate_attributes(xml_file):
            fixed_count += 1
        
        # Fix malformed LinearLayouts
        if fix_malformed_linear_layouts(xml_file):
            fixed_count += 1
        
        # Fix unclosed RelativeLayouts
        if fix_unclosed_relative_layouts(xml_file):
            fixed_count += 1
    
    print(f"\nFixed {fixed_count} files with XML syntax errors.")

if __name__ == "__main__":
    main()
