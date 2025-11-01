#!/bin/bash

# JDWP Debugging Troubleshooting Script
# This script helps resolve JDWP broken pipe and debugging issues

echo "=== JDWP Debugging Troubleshooting Script ==="
echo ""

# Function to check if ADB is available
check_adb() {
    if command -v adb &> /dev/null; then
        echo "✓ ADB is available"
        return 0
    else
        echo "✗ ADB is not available. Please install Android SDK Platform Tools"
        return 1
    fi
}

# Function to restart ADB server
restart_adb() {
    echo "Restarting ADB server..."
    adb kill-server
    sleep 2
    adb start-server
    echo "ADB server restarted"
}

# Function to check connected devices
check_devices() {
    echo "Checking connected devices..."
    adb devices
    echo ""
}

# Function to check device memory
check_memory() {
    echo "Checking device memory..."
    adb shell dumpsys meminfo | head -20
    echo ""
}

# Function to check app memory usage
check_app_memory() {
    echo "Checking app memory usage..."
    adb shell dumpsys meminfo com.topgradesoftware.cmtb
    echo ""
}

# Function to clear app data
clear_app_data() {
    echo "Clearing app data..."
    adb shell pm clear com.topgradesoftware.cmtb
    echo "App data cleared"
}

# Function to uninstall and reinstall app
reinstall_app() {
    echo "Uninstalling app..."
    adb uninstall com.topgradesoftware.cmtb
    echo "App uninstalled"
    echo "Please rebuild and install the app from Android Studio"
}

# Function to check network connectivity
check_network() {
    echo "Checking network connectivity..."
    adb shell ping -c 3 8.8.8.8
    echo ""
}

# Function to check USB debugging
check_usb_debugging() {
    echo "Checking USB debugging status..."
    adb shell settings get global adb_enabled
    echo ""
}

# Function to enable wireless debugging
enable_wireless_debugging() {
    echo "Enabling wireless debugging..."
    adb tcpip 5555
    echo "Wireless debugging enabled on port 5555"
    echo "Connect using: adb connect <device_ip>:5555"
}

# Function to check system resources
check_system_resources() {
    echo "Checking system resources..."
    echo "CPU Usage:"
    adb shell top -n 1 | head -10
    echo ""
    echo "Memory Usage:"
    adb shell free
    echo ""
}

# Function to check logcat for errors
check_logcat() {
    echo "Checking recent logcat entries..."
    adb logcat -d | tail -20
    echo ""
}

# Function to clear logcat
clear_logcat() {
    echo "Clearing logcat..."
    adb logcat -c
    echo "Logcat cleared"
}

# Main menu
show_menu() {
    echo "Select an option:"
    echo "1. Check ADB availability"
    echo "2. Restart ADB server"
    echo "3. Check connected devices"
    echo "4. Check device memory"
    echo "5. Check app memory usage"
    echo "6. Clear app data"
    echo "7. Reinstall app"
    echo "8. Check network connectivity"
    echo "9. Check USB debugging"
    echo "10. Enable wireless debugging"
    echo "11. Check system resources"
    echo "12. Check logcat"
    echo "13. Clear logcat"
    echo "14. Run all checks"
    echo "15. Exit"
    echo ""
    read -p "Enter your choice (1-15): " choice
}

# Run all checks
run_all_checks() {
    echo "Running all checks..."
    echo ""
    
    check_adb
    if [ $? -eq 0 ]; then
        restart_adb
        check_devices
        check_memory
        check_app_memory
        check_network
        check_usb_debugging
        check_system_resources
        check_logcat
    fi
}

# Main execution
main() {
    while true; do
        show_menu
        
        case $choice in
            1)
                check_adb
                ;;
            2)
                if check_adb; then
                    restart_adb
                fi
                ;;
            3)
                if check_adb; then
                    check_devices
                fi
                ;;
            4)
                if check_adb; then
                    check_memory
                fi
                ;;
            5)
                if check_adb; then
                    check_app_memory
                fi
                ;;
            6)
                if check_adb; then
                    clear_app_data
                fi
                ;;
            7)
                if check_adb; then
                    reinstall_app
                fi
                ;;
            8)
                if check_adb; then
                    check_network
                fi
                ;;
            9)
                if check_adb; then
                    check_usb_debugging
                fi
                ;;
            10)
                if check_adb; then
                    enable_wireless_debugging
                fi
                ;;
            11)
                if check_adb; then
                    check_system_resources
                fi
                ;;
            12)
                if check_adb; then
                    check_logcat
                fi
                ;;
            13)
                if check_adb; then
                    clear_logcat
                fi
                ;;
            14)
                run_all_checks
                ;;
            15)
                echo "Exiting..."
                exit 0
                ;;
            *)
                echo "Invalid option. Please try again."
                ;;
        esac
        
        echo ""
        read -p "Press Enter to continue..."
        echo ""
    done
}

# Run main function
main 