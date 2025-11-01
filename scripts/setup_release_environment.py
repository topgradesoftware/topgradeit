#!/usr/bin/env python3
"""
Release Environment Setup Script

This script helps set up the release environment and validates all requirements
for the automated release workflow.

Usage:
    python setup_release_environment.py --check
    python setup_release_environment.py --setup
    python setup_release_environment.py --fix
"""

import os
import sys
import subprocess
import argparse
from pathlib import Path

class ReleaseEnvironmentSetup:
    def __init__(self):
        self.project_root = Path(__file__).parent.parent
        self.issues = []
        self.fixes_applied = []
        
    def check_python(self):
        """Check Python installation and version"""
        try:
            version = sys.version_info
            if version.major >= 3 and version.minor >= 7:
                print("✅ Python 3.7+ is installed")
                return True
            else:
                self.issues.append(f"Python version {version.major}.{version.minor} is too old. Need 3.7+")
                return False
        except Exception as e:
            self.issues.append(f"Python check failed: {e}")
            return False
    
    def check_git(self):
        """Check Git installation and repository"""
        try:
            result = subprocess.run(['git', '--version'], 
                                  capture_output=True, text=True, check=True)
            print(f"✅ Git is installed: {result.stdout.strip()}")
            
            # Check if we're in a git repository
            result = subprocess.run(['git', 'status'], 
                                  capture_output=True, text=True)
            if result.returncode == 0:
                print("✅ Git repository found")
                return True
            else:
                self.issues.append("Not in a git repository")
                return False
        except (subprocess.CalledProcessError, FileNotFoundError):
            self.issues.append("Git is not installed or not in PATH")
            return False
    
    def check_gradle(self):
        """Check Gradle wrapper and Android setup"""
        gradlew_path = self.project_root / "gradlew"
        if gradlew_path.exists():
            print("✅ Gradle wrapper found")
            
            # Make gradlew executable on Unix systems
            if os.name != 'nt':
                try:
                    os.chmod(gradlew_path, 0o755)
                    print("✅ Gradle wrapper is executable")
                except Exception as e:
                    self.issues.append(f"Cannot make gradlew executable: {e}")
                    return False
            
            return True
        else:
            self.issues.append("Gradle wrapper (gradlew) not found")
            return False
    
    def check_android_files(self):
        """Check Android project files"""
        build_gradle = self.project_root / "app" / "build.gradle"
        if build_gradle.exists():
            print("✅ Android build.gradle found")
            
            # Check if version fields exist
            with open(build_gradle, 'r') as f:
                content = f.read()
                if 'versionName' in content and 'versionCode' in content:
                    print("✅ Version fields found in build.gradle")
                    return True
                else:
                    self.issues.append("versionName or versionCode not found in build.gradle")
                    return False
        else:
            self.issues.append("Android build.gradle not found")
            return False
    
    def check_version_script(self):
        """Check version management script"""
        script_path = self.project_root / "update_version.py"
        if script_path.exists():
            print("✅ Version management script found")
            return True
        else:
            self.issues.append("update_version.py not found")
            return False
    
    def check_ci_configs(self):
        """Check CI/CD configuration files"""
        github_workflow = self.project_root / ".github" / "workflows" / "release.yml"
        gitlab_ci = self.project_root / ".gitlab-ci.yml"
        
        ci_found = False
        if github_workflow.exists():
            print("✅ GitHub Actions workflow found")
            ci_found = True
        if gitlab_ci.exists():
            print("✅ GitLab CI configuration found")
            ci_found = True
            
        if not ci_found:
            self.issues.append("No CI/CD configuration found")
            return False
        return True
    
    def check_keystore(self):
        """Check for release keystore (optional)"""
        keystore_paths = [
            self.project_root / "app" / "release.keystore",
            self.project_root / "app" / "keystore.jks",
            self.project_root / "keystore.jks"
        ]
        
        for keystore_path in keystore_paths:
            if keystore_path.exists():
                print(f"✅ Keystore found: {keystore_path.name}")
                return True
        
        print("⚠️  No keystore found (required for release builds)")
        return False
    
    def create_gitignore_entries(self):
        """Add release-related entries to .gitignore"""
        gitignore_path = self.project_root / ".gitignore"
        
        entries_to_add = [
            "",
            "# Release files",
            "app/release.keystore",
            "app/keystore.jks",
            "keystore.jks",
            "google-services.json",
            "*.aab",
            "*.apk",
            "build/",
            ".gradle/",
            "",
            "# IDE files",
            ".idea/",
            "*.iml",
            ".vscode/",
            "",
            "# OS files",
            ".DS_Store",
            "Thumbs.db"
        ]
        
        if gitignore_path.exists():
            with open(gitignore_path, 'r') as f:
                existing_content = f.read()
        else:
            existing_content = ""
        
        new_entries = []
        for entry in entries_to_add:
            if entry not in existing_content:
                new_entries.append(entry)
        
        if new_entries:
            with open(gitignore_path, 'a') as f:
                f.write('\n'.join(new_entries))
            self.fixes_applied.append("Added release-related entries to .gitignore")
            print("✅ Updated .gitignore")
        else:
            print("✅ .gitignore is up to date")
    
    def create_release_scripts(self):
        """Create release helper scripts"""
        scripts_dir = self.project_root / "scripts"
        scripts_dir.mkdir(exist_ok=True)
        
        # Make shell scripts executable on Unix systems
        if os.name != 'nt':
            for script in ["release.sh"]:
                script_path = scripts_dir / script
                if script_path.exists():
                    try:
                        os.chmod(script_path, 0o755)
                        print(f"✅ Made {script} executable")
                    except Exception as e:
                        self.issues.append(f"Cannot make {script} executable: {e}")
    
    def run_checks(self):
        """Run all checks"""
        print("🔍 Checking release environment...")
        print("=" * 50)
        
        checks = [
            ("Python", self.check_python),
            ("Git", self.check_git),
            ("Gradle", self.check_gradle),
            ("Android Files", self.check_android_files),
            ("Version Script", self.check_version_script),
            ("CI/CD Configs", self.check_ci_configs),
            ("Keystore", self.check_keystore)
        ]
        
        for check_name, check_func in checks:
            print(f"\n📋 Checking {check_name}:")
            try:
                check_func()
            except Exception as e:
                self.issues.append(f"{check_name} check failed: {e}")
                print(f"❌ {check_name} check failed: {e}")
        
        print("\n" + "=" * 50)
        return len(self.issues) == 0
    
    def apply_fixes(self):
        """Apply automatic fixes"""
        print("🔧 Applying automatic fixes...")
        print("=" * 50)
        
        self.create_gitignore_entries()
        self.create_release_scripts()
        
        if self.fixes_applied:
            print(f"\n✅ Applied {len(self.fixes_applied)} fixes:")
            for fix in self.fixes_applied:
                print(f"  - {fix}")
        else:
            print("\n✅ No fixes needed")
    
    def show_summary(self):
        """Show summary of issues and recommendations"""
        print("\n" + "=" * 50)
        
        if not self.issues:
            print("🎉 All checks passed! Your release environment is ready.")
            print("\n📋 Quick start:")
            print("  python update_version.py --show")
            print("  python update_version.py --patch --git --changelog")
            print("  ./scripts/release.sh --type patch --git --build")
        else:
            print(f"❌ Found {len(self.issues)} issues:")
            for i, issue in enumerate(self.issues, 1):
                print(f"  {i}. {issue}")
            
            print("\n🔧 Recommendations:")
            print("  1. Run: python setup_release_environment.py --fix")
            print("  2. Install missing dependencies")
            print("  3. Configure CI/CD secrets")
            print("  4. Set up release keystore")
            print("  5. Re-run: python setup_release_environment.py --check")

def main():
    parser = argparse.ArgumentParser(
        description="Setup and validate release environment",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python setup_release_environment.py --check     # Check environment
  python setup_release_environment.py --setup     # Setup and check
  python setup_release_environment.py --fix       # Apply fixes only
        """
    )
    
    parser.add_argument('--check', action='store_true',
                       help='Check release environment')
    parser.add_argument('--setup', action='store_true',
                       help='Setup and check release environment')
    parser.add_argument('--fix', action='store_true',
                       help='Apply automatic fixes')
    
    args = parser.parse_args()
    
    if not any([args.check, args.setup, args.fix]):
        parser.print_help()
        sys.exit(1)
    
    setup = ReleaseEnvironmentSetup()
    
    if args.fix:
        setup.apply_fixes()
    
    if args.check or args.setup:
        success = setup.run_checks()
        
        if args.setup and not success:
            print("\n🔧 Applying fixes and re-checking...")
            setup.apply_fixes()
            setup.issues.clear()
            setup.fixes_applied.clear()
            success = setup.run_checks()
        
        setup.show_summary()
        
        if not success:
            sys.exit(1)

if __name__ == "__main__":
    main()

