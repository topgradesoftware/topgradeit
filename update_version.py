#!/usr/bin/env python3
"""
Android App Version Update Script

This script helps manage versionCode and versionName in build.gradle before releases.
It provides options to increment versions automatically or set custom versions.

Usage:
    python update_version.py --help
    python update_version.py --major          # 1.0.0 -> 2.0.0
    python update_version.py --minor          # 1.0.0 -> 1.1.0  
    python update_version.py --patch          # 1.0.0 -> 1.0.1
    python update_version.py --version 2.0.0  # Set specific version
    python update_version.py --version-code 10 # Set specific version code
"""

import re
import argparse
import os
import sys
import subprocess
import json
from datetime import datetime
from typing import Tuple, Optional, List

class VersionManager:
    def __init__(self, build_gradle_path: str = "app/build.gradle"):
        self.build_gradle_path = build_gradle_path
        self.version_pattern = r'versionName\s+"([^"]+)"'
        self.version_code_pattern = r'versionCode\s+(\d+)'
        self.application_id_pattern = r'applicationId\s+"([^"]+)"'
        
    def run_command(self, command: str, cwd: str = None) -> Tuple[bool, str, str]:
        """Run a shell command and return success status, stdout, stderr"""
        try:
            result = subprocess.run(
                command, 
                shell=True, 
                cwd=cwd or os.getcwd(),
                capture_output=True, 
                text=True,
                check=False
            )
            return result.returncode == 0, result.stdout.strip(), result.stderr.strip()
        except Exception as e:
            return False, "", str(e)
    
    def git_status(self) -> Tuple[bool, str]:
        """Check if git repo is clean and get status"""
        success, stdout, stderr = self.run_command("git status --porcelain")
        if not success:
            return False, "Not a git repository or git not available"
        
        if stdout:
            return False, f"Working directory not clean:\n{stdout}"
        return True, "Working directory is clean"
    
    def git_commit_and_tag(self, version: str, version_code: int, message: str = None) -> Tuple[bool, str]:
        """Create git commit and tag for version update"""
        if not message:
            message = f"Bump version to {version} (versionCode {version_code})"
        
        # Add build.gradle to staging
        success, stdout, stderr = self.run_command(f"git add {self.build_gradle_path}")
        if not success:
            return False, f"Failed to add {self.build_gradle_path}: {stderr}"
        
        # Create commit
        success, stdout, stderr = self.run_command(f'git commit -m "{message}"')
        if not success:
            return False, f"Failed to create commit: {stderr}"
        
        # Create tag
        tag_name = f"v{version}"
        success, stdout, stderr = self.run_command(f'git tag -a {tag_name} -m "Release {version}"')
        if not success:
            return False, f"Failed to create tag: {stderr}"
        
        return True, f"Created commit and tag {tag_name}"
    
    def validate_application_id(self) -> Tuple[bool, str]:
        """Validate that applicationId hasn't changed unexpectedly"""
        try:
            with open(self.build_gradle_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            match = re.search(self.application_id_pattern, content)
            if not match:
                return False, "Could not find applicationId in build.gradle"
            
            app_id = match.group(1)
            expected_app_id = "topgrade.parent.com.parentseeks"
            
            if app_id != expected_app_id:
                return False, f"Application ID mismatch: expected '{expected_app_id}', found '{app_id}'"
            
            return True, f"Application ID valid: {app_id}"
            
        except Exception as e:
            return False, f"Error validating application ID: {e}"
    
    def check_gradle_build(self) -> Tuple[bool, str]:
        """Check if Gradle build is successful"""
        print("🔨 Testing Gradle build...")
        success, stdout, stderr = self.run_command("./gradlew assembleDebug --quiet")
        if success:
            return True, "Gradle build successful"
        else:
            return False, f"Gradle build failed: {stderr}"
    
    def generate_changelog_entry(self, version: str, version_code: int) -> str:
        """Generate a changelog entry for the new version"""
        today = datetime.now().strftime("%Y-%m-%d")
        return f"""## [{version}] - {today}

### Added
- Version bump to {version} (versionCode {version_code})

### Changed
- Updated app version for release

### Technical Details
- Version Code: {version_code}
- Build Date: {today}
- Git Tag: v{version}

"""
        
    def read_current_versions(self) -> Tuple[str, int]:
        """Read current versionName and versionCode from build.gradle"""
        try:
            with open(self.build_gradle_path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            version_match = re.search(self.version_pattern, content)
            version_code_match = re.search(self.version_code_pattern, content)
            
            if not version_match or not version_code_match:
                raise ValueError("Could not find versionName or versionCode in build.gradle")
                
            current_version = version_match.group(1)
            current_version_code = int(version_code_match.group(1))
            
            return current_version, current_version_code
            
        except FileNotFoundError:
            raise FileNotFoundError(f"build.gradle not found at {self.build_gradle_path}")
        except Exception as e:
            raise Exception(f"Error reading build.gradle: {e}")
    
    def parse_version(self, version: str) -> Tuple[int, int, int]:
        """Parse semantic version string (e.g., '1.2.3') into (major, minor, patch)"""
        try:
            parts = version.split('.')
            if len(parts) != 3:
                raise ValueError("Version must be in format X.Y.Z")
            return int(parts[0]), int(parts[1]), int(parts[2])
        except ValueError as e:
            raise ValueError(f"Invalid version format '{version}': {e}")
    
    def increment_version(self, current_version: str, increment_type: str) -> str:
        """Increment version based on type (major, minor, patch)"""
        major, minor, patch = self.parse_version(current_version)
        
        if increment_type == 'major':
            major += 1
            minor = 0
            patch = 0
        elif increment_type == 'minor':
            minor += 1
            patch = 0
        elif increment_type == 'patch':
            patch += 1
        else:
            raise ValueError(f"Invalid increment type: {increment_type}")
            
        return f"{major}.{minor}.{patch}"
    
    def update_versions(self, new_version: str, new_version_code: int) -> None:
        """Update versionName and versionCode in build.gradle"""
        try:
            with open(self.build_gradle_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Update versionName
            content = re.sub(
                self.version_pattern,
                f'versionName "{new_version}"',
                content
            )
            
            # Update versionCode
            content = re.sub(
                self.version_code_pattern,
                f'versionCode {new_version_code}',
                content
            )
            
            # Write back to file
            with open(self.build_gradle_path, 'w', encoding='utf-8') as f:
                f.write(content)
                
            print(f"✅ Successfully updated to version {new_version} (versionCode: {new_version_code})")
            
        except Exception as e:
            raise Exception(f"Error updating build.gradle: {e}")
    
    def run_validations(self, validate_git: bool = True, validate_build: bool = False) -> List[str]:
        """Run validation checks before version update"""
        issues = []
        
        if validate_git:
            print("🔍 Validating Git repository...")
            git_clean, git_message = self.git_status()
            if not git_clean:
                issues.append(f"Git validation failed: {git_message}")
            else:
                print("✅ Git repository is clean")
        
        print("🔍 Validating application ID...")
        app_id_valid, app_id_message = self.validate_application_id()
        if not app_id_valid:
            issues.append(f"Application ID validation failed: {app_id_message}")
        else:
            print(f"✅ {app_id_message}")
        
        if validate_build:
            build_valid, build_message = self.check_gradle_build()
            if not build_valid:
                issues.append(f"Build validation failed: {build_message}")
            else:
                print(f"✅ {build_message}")
        
        return issues
    
    def update_changelog(self, version: str, version_code: int, changelog_path: str = "CHANGELOG.md") -> None:
        """Update or create changelog file"""
        entry = self.generate_changelog_entry(version, version_code)
        
        try:
            # Read existing changelog if it exists
            if os.path.exists(changelog_path):
                with open(changelog_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            else:
                content = "# Changelog\n\nAll notable changes to this project will be documented in this file.\n\n"
            
            # Add new entry after the header
            lines = content.split('\n')
            header_end = 0
            for i, line in enumerate(lines):
                if line.startswith('# Changelog') and i < len(lines) - 1:
                    if lines[i + 1] == '':
                        header_end = i + 2
                        break
            
            # Insert new entry
            new_content = '\n'.join(lines[:header_end]) + '\n' + entry + '\n'.join(lines[header_end:])
            
            with open(changelog_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            
            print(f"✅ Updated {changelog_path}")
            
        except Exception as e:
            print(f"⚠️  Warning: Could not update changelog: {e}")
    
    def run_gradle_build(self, build_type: str = "release") -> Tuple[bool, str]:
        """Run Gradle build command"""
        if build_type == "release":
            command = "./gradlew bundleRelease"
        elif build_type == "debug":
            command = "./gradlew assembleDebug"
        else:
            return False, f"Unknown build type: {build_type}"
        
        print(f"🔨 Running Gradle {build_type} build...")
        success, stdout, stderr = self.run_command(command)
        
        if success:
            return True, f"Gradle {build_type} build completed successfully"
        else:
            return False, f"Gradle {build_type} build failed: {stderr}"
    
    def validate_version(self, version: str) -> None:
        """Validate version format"""
        try:
            self.parse_version(version)
        except ValueError as e:
            raise ValueError(f"Invalid version: {e}")
    
    def get_next_version_code(self, current_version_code: int) -> int:
        """Get next version code"""
        return current_version_code + 1

def main():
    parser = argparse.ArgumentParser(
        description="Update Android app version in build.gradle",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python update_version.py --major          # 1.0.0 -> 2.0.0
  python update_version.py --minor          # 1.0.0 -> 1.1.0  
  python update_version.py --patch          # 1.0.0 -> 1.0.1
  python update_version.py --version 2.0.0  # Set specific version
  python update_version.py --version-code 10 # Set specific version code
  python update_version.py --show           # Show current version
        """
    )
    
    # Version increment options
    increment_group = parser.add_mutually_exclusive_group()
    increment_group.add_argument('--major', action='store_true', 
                               help='Increment major version (1.0.0 -> 2.0.0)')
    increment_group.add_argument('--minor', action='store_true', 
                               help='Increment minor version (1.0.0 -> 1.1.0)')
    increment_group.add_argument('--patch', action='store_true', 
                               help='Increment patch version (1.0.0 -> 1.0.1)')
    
    # Specific version options
    parser.add_argument('--version', type=str, 
                       help='Set specific version (e.g., "2.0.0")')
    parser.add_argument('--version-code', type=int, 
                       help='Set specific version code (e.g., 10)')
    
    # Other options
    parser.add_argument('--show', action='store_true', 
                       help='Show current version information')
    parser.add_argument('--build-gradle', type=str, default='app/build.gradle',
                       help='Path to build.gradle file (default: app/build.gradle)')
    parser.add_argument('--dry-run', action='store_true',
                       help='Show what would be changed without making changes')
    
    # Git integration options
    parser.add_argument('--git', action='store_true',
                       help='Create git commit and tag after version update')
    parser.add_argument('--git-message', type=str,
                       help='Custom git commit message')
    parser.add_argument('--no-git-validation', action='store_true',
                       help='Skip git repository validation')
    
    # Build integration options
    parser.add_argument('--build', action='store_true',
                       help='Run Gradle build after version update')
    parser.add_argument('--build-type', type=str, default='release',
                       choices=['release', 'debug'],
                       help='Type of Gradle build to run (default: release)')
    parser.add_argument('--validate-build', action='store_true',
                       help='Validate Gradle build before version update')
    
    # Changelog options
    parser.add_argument('--changelog', action='store_true',
                       help='Update CHANGELOG.md file')
    parser.add_argument('--changelog-file', type=str, default='CHANGELOG.md',
                       help='Path to changelog file (default: CHANGELOG.md)')
    
    # Validation options
    parser.add_argument('--no-validation', action='store_true',
                       help='Skip all validation checks')
    parser.add_argument('--validate-only', action='store_true',
                       help='Run validation checks only, do not update version')
    
    args = parser.parse_args()
    
    try:
        version_manager = VersionManager(args.build_gradle)
        current_version, current_version_code = version_manager.read_current_versions()
        
        if args.show:
            print(f"Current Version: {current_version}")
            print(f"Current Version Code: {current_version_code}")
            return
        
        # Validate only mode
        if args.validate_only:
            print("🔍 Running validation checks only...")
            issues = version_manager.run_validations(
                validate_git=not args.no_git_validation,
                validate_build=args.validate_build
            )
            
            if issues:
                print("❌ Validation issues found:")
                for issue in issues:
                    print(f"  - {issue}")
                sys.exit(1)
            else:
                print("✅ All validations passed!")
                return
        
        # Determine new version
        if args.version:
            new_version = args.version
            version_manager.validate_version(new_version)
        elif args.major:
            new_version = version_manager.increment_version(current_version, 'major')
        elif args.minor:
            new_version = version_manager.increment_version(current_version, 'minor')
        elif args.patch:
            new_version = version_manager.increment_version(current_version, 'patch')
        else:
            print("❌ Error: Please specify version increment (--major, --minor, --patch) or specific version (--version)")
            parser.print_help()
            sys.exit(1)
        
        # Determine new version code
        if args.version_code:
            new_version_code = args.version_code
        else:
            new_version_code = version_manager.get_next_version_code(current_version_code)
        
        # Show what will be changed
        print(f"Current: {current_version} (versionCode: {current_version_code})")
        print(f"New:     {new_version} (versionCode: {new_version_code})")
        
        if args.dry_run:
            print("🔍 Dry run - no changes made")
            return
        
        # Run validations before update
        if not args.no_validation:
            print("\n🔍 Running validation checks...")
            issues = version_manager.run_validations(
                validate_git=not args.no_git_validation,
                validate_build=args.validate_build
            )
            
            if issues:
                print("❌ Validation issues found:")
                for issue in issues:
                    print(f"  - {issue}")
                print("\nUse --no-validation to skip validation checks")
                sys.exit(1)
            else:
                print("✅ All validations passed!")
        
        # Confirm changes
        if current_version != new_version or current_version_code != new_version_code:
            response = input("\n❓ Proceed with version update? [y/N]: ")
            if response.lower() not in ['y', 'yes']:
                print("❌ Version update cancelled")
                sys.exit(0)
        
        # Update versions
        version_manager.update_versions(new_version, new_version_code)
        
        # Update changelog if requested
        if args.changelog:
            version_manager.update_changelog(new_version, new_version_code, args.changelog_file)
        
        # Git operations if requested
        if args.git:
            git_message = args.git_message or f"Bump version to {new_version} (versionCode {new_version_code})"
            success, git_result = version_manager.git_commit_and_tag(new_version, new_version_code, git_message)
            if success:
                print(f"✅ {git_result}")
            else:
                print(f"⚠️  Git operation failed: {git_result}")
        
        # Gradle build if requested
        if args.build:
            success, build_result = version_manager.run_gradle_build(args.build_type)
            if success:
                print(f"✅ {build_result}")
            else:
                print(f"❌ {build_result}")
                sys.exit(1)
        
        print(f"\n🎉 Version updated successfully!")
        print(f"📱 App version: {new_version}")
        print(f"🔢 Version code: {new_version_code}")
        print(f"📁 Updated file: {args.build_gradle}")
        
        # Show next steps
        if args.git:
            print(f"\n📋 Next steps:")
            print(f"  1. Push changes: git push")
            print(f"  2. Push tags: git push --tags")
        elif args.build:
            print(f"\n📋 Next steps:")
            print(f"  1. Test the build: {args.build_type} AAB/APK created")
            print(f"  2. Upload to Play Store: ./gradlew publishRelease")
        else:
            print(f"\n📋 Next steps:")
            print(f"  1. Test the changes")
            print(f"  2. Commit to git: git add {args.build_gradle} && git commit -m \"Bump version to {new_version}\"")
            print(f"  3. Build release: ./gradlew bundleRelease")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
