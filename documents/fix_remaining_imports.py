#!/usr/bin/env python3
"""
Script to fix remaining import issues after the main refactoring
"""

import os
import re
import glob

def fix_imports(file_path):
    """Fix import statements in a single file"""
    print(f"Fixing imports: {file_path}")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Fix wrong import paths
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Teacher\.Model\.API\.ParentAttendanceModel;',
        'import topgrade.parent.com.parentseeks.Parent.Model.ParentAttendanceModel;',
        content
    )
    
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Teacher\.Model\.API\.SharedStatus;',
        'import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;',
        content
    )
    
    content = re.sub(
        r'import topgrade\.parent\.com\.parentseeks\.Teacher\.Model\.API\.AttendanceModel;',
        'import topgrade.parent.com.parentseeks.Teacher.Model.API.TeacherAttendanceModel;',
        content
    )
    
    # Add missing SharedStatus imports for files that use it but don't import it
    if 'SharedStatus' in content and 'import topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;' not in content:
        # Find the last import statement and add SharedStatus import after it
        import_pattern = r'(import [^;]+;)\s*'
        matches = list(re.finditer(import_pattern, content))
        if matches:
            last_import = matches[-1]
            insert_pos = last_import.end()
            content = content[:insert_pos] + '\nimport topgrade.parent.com.parentseeks.Shared.Models.SharedStatus;' + content[insert_pos:]
    
    # Write back if changed
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✅ Fixed imports: {file_path}")
        return True
    else:
        print(f"  ⏭️  No import changes needed: {file_path}")
        return False

def main():
    """Main function to fix import issues"""
    print("🔧 Starting import fixes...")
    
    # Files that need import fixes based on the error messages
    files_to_fix = [
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/StaffComplainModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/StaffApplicationModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/FeedbackModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/News_Model.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/Event_Model.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Parent/Model/GeneralModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/UpdateProfilModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/CityModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/StateModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/TimetableSmsModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/TimetableSessionModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/TimetableModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/LegderModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Parent/Model/SubjectAttendeceModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/AdvancedSalaryModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/SalaryModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Parent/Model/SessionModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Parent/Model/New/FeeChalanModel.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Parent/services/MyFirebaseMessaging.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Activity/UpdateAttendanceUpdate_Class.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Activity/UpdateAttendanceUpdate_Section.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Adaptor/AttendanceUpdateAdaptor.java",
        "app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Model/teacher_load_profile/TeacherProfileResponse.java"
    ]
    
    fixed_count = 0
    for file_path in files_to_fix:
        if os.path.exists(file_path):
            if fix_imports(file_path):
                fixed_count += 1
        else:
            print(f"  ❌ File not found: {file_path}")
    
    print(f"\n🎉 Fixed imports in {fixed_count} files")

if __name__ == "__main__":
    main()
