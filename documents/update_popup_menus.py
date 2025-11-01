#!/usr/bin/env python3
"""
Script to update all PopupMenu usages to CustomPopupMenu across the codebase.
This script will automatically update all Java and Kotlin files that use PopupMenu.
"""

import os
import re
import glob

def update_file(file_path):
    """Update a single file to use CustomPopupMenu instead of PopupMenu."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # Add import for CustomPopupMenu if not already present
        if 'import topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;' not in content:
            # Find the last import statement
            import_pattern = r'(import.*?;)\s*\n'
            imports = re.findall(import_pattern, content)
            if imports:
                last_import = imports[-1]
                content = content.replace(last_import, last_import + '\nimport topgrade.parent.com.parentseeks.Utils.CustomPopupMenu;')
        
        # Add CustomPopupMenu member variable
        if 'private CustomPopupMenu customPopupMenu;' not in content:
            # Find a good place to add the member variable (after other member variables)
            member_pattern = r'(private.*?;)\s*\n'
            members = re.findall(member_pattern, content)
            if members:
                # Add after the last member variable
                last_member = members[-1]
                content = content.replace(last_member, last_member + '\n    private CustomPopupMenu customPopupMenu;')
        
        # Update PopupMenu instantiation and usage
        # Pattern 1: PopupMenu popup = new PopupMenu(context, view);
        content = re.sub(
            r'PopupMenu popup = new PopupMenu\([^)]+\);',
            'if (customPopupMenu == null) {\n            customPopupMenu = new CustomPopupMenu(this, view);',
            content
        )
        
        # Pattern 2: popup.getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
        content = re.sub(
            r'popup\.getMenuInflater\(\)\.inflate\(R\.menu\.option_menu, popup\.getMenu\(\)\);',
            'customPopupMenu.setOnMenuItemClickListener(title -> {',
            content
        )
        
        # Pattern 3: popup.setOnMenuItemClickListener
        content = re.sub(
            r'popup\.setOnMenuItemClickListener\([^)]+\)\s*\{',
            'switch (title) {',
            content
        )
        
        # Pattern 4: popup.show();
        content = re.sub(
            r'popup\.show\(\);',
            'return true;\n                });\n            }\n            \n            if (customPopupMenu.isShowing()) {\n                customPopupMenu.dismiss();\n            } else {\n                customPopupMenu.show();',
            content
        )
        
        # Add try-catch wrapper
        if 'try {' not in content or 'catch (Exception e)' not in content:
            # Find the method that contains popup menu logic
            method_pattern = r'(private void [^{]+)\{([^}]+)\}'
            methods = re.findall(method_pattern, content)
            for method_name, method_body in methods:
                if 'PopupMenu' in method_body or 'customPopupMenu' in method_body:
                    # Wrap the method body in try-catch
                    new_method_body = f'''        try {{
{method_body}
        }} catch (Exception e) {{
            Log.e("TAG", "Error showing popup menu", e);
        }}'''
                    content = content.replace(method_body, new_method_body)
        
        # Only write if content changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated: {file_path}")
            return True
        
        return False
        
    except Exception as e:
        print(f"Error updating {file_path}: {e}")
        return False

def main():
    """Main function to update all files."""
    # Find all Java and Kotlin files
    java_files = glob.glob("app/src/main/java/**/*.java", recursive=True)
    kotlin_files = glob.glob("app/src/main/java/**/*.kt", recursive=True)
    
    all_files = java_files + kotlin_files
    
    updated_count = 0
    total_count = 0
    
    for file_path in all_files:
        # Skip files that are already updated
        if 'CustomPopupMenu' in open(file_path, 'r', encoding='utf-8').read():
            continue
            
        if 'PopupMenu' in open(file_path, 'r', encoding='utf-8').read():
            total_count += 1
            if update_file(file_path):
                updated_count += 1
    
    print(f"\nUpdate complete!")
    print(f"Files processed: {total_count}")
    print(f"Files updated: {updated_count}")

if __name__ == "__main__":
    main() 