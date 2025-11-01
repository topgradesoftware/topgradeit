#!/bin/bash
# Android App Version Update Script for Unix/Linux/macOS
# This script provides easy access to the version update functionality

echo "========================================"
echo "Android App Version Manager"
echo "========================================"
echo

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    if ! command -v python &> /dev/null; then
        echo "Error: Python is not installed or not in PATH"
        echo "Please install Python and try again"
        exit 1
    else
        PYTHON_CMD="python"
    fi
else
    PYTHON_CMD="python3"
fi

# Check if build.gradle exists
if [ ! -f "app/build.gradle" ]; then
    echo "Error: app/build.gradle not found"
    echo "Please run this script from the project root directory"
    exit 1
fi

show_menu() {
    echo "Current options:"
    echo
    echo "1. Show current version"
    echo "2. Increment patch version (1.0.0 -> 1.0.1)"
    echo "3. Increment minor version (1.0.0 -> 1.1.0)"
    echo "4. Increment major version (1.0.0 -> 2.0.0)"
    echo "5. Set custom version"
    echo "6. Exit"
    echo
}

while true; do
    show_menu
    read -p "Enter your choice (1-6): " choice
    
    case $choice in
        1)
            $PYTHON_CMD update_version.py --show
            echo
            read -p "Press Enter to continue..."
            ;;
        2)
            $PYTHON_CMD update_version.py --patch
            echo
            read -p "Press Enter to continue..."
            ;;
        3)
            $PYTHON_CMD update_version.py --minor
            echo
            read -p "Press Enter to continue..."
            ;;
        4)
            $PYTHON_CMD update_version.py --major
            echo
            read -p "Press Enter to continue..."
            ;;
        5)
            read -p "Enter new version (e.g., 2.0.0): " custom_version
            if [ -n "$custom_version" ]; then
                $PYTHON_CMD update_version.py --version "$custom_version"
            else
                echo "No version entered. Operation cancelled."
            fi
            echo
            read -p "Press Enter to continue..."
            ;;
        6)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            echo
            ;;
    esac
done
