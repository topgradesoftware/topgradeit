#!/usr/bin/env python3
"""
Script to fix remaining compilation errors after duplicate file cleanup
"""

import os
import re
import glob

def fix_file(file_path):
    """Fix a single file by updating imports and class references"""
    print(f"Fixing: {file_path}")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Fix imports
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Parent\.Model\.AttendanceModel;',
        'import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;',
        content
    )
    
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Parent\.Model\.Student;',
        'import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;',
        content
    )
    
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Teacher\.Model\.Status;',
        'import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;',
        content
    )
    
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Parent\.Model\.Status;',
        'import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;',
        content
    )
    
    # Fix class references
    content = re.sub(r'\bAttendanceModel\b', 'ParentAttendanceModel', content)
    content = re.sub(r'\bStudent\b(?!\s*\.)', 'SharedStudent', content)  # Avoid replacing Student. in method calls
    content = re.sub(r'\bStatus\b(?!\s*\.)', 'SharedStatus', content)  # Avoid replacing Status. in method calls
    
    # Write back if changed
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✅ Fixed: {file_path}")
        return True
    else:
        print(f"  ⏭️  No changes needed: {file_path}")
        return False

def main():
    """Main function to fix all Java files"""
    print("🔧 Starting systematic fix of remaining compilation errors...")
    
    # Find all Java files that might need fixing
    java_files = []
    
    # Parent Activity files
    java_files.extend(glob.glob("app/src/main/java/topgrade/parent/com/parentseeks/Parent/Activity/*.java"))
    
    # Teacher Model files
    java_files.extend(glob.glob("app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/**/*.java", recursive=True))
    
    # Parent Model files
    java_files.extend(glob.glob("app/src/main/java/topgrade/parent/com/parentseeks/Parent/Model/**/*.java", recursive=True))
    
    # Teacher Activity files
    java_files.extend(glob.glob("app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Activity/*.java"))
    
    print(f"Found {len(java_files)} Java files to check")
    
    fixed_count = 0
    for file_path in java_files:
        if fix_file(file_path):
            fixed_count += 1
    
    print(f"\n🎉 Fixed {fixed_count} files out of {len(java_files)} total files")

if __name__ == "__main__":
    main()
