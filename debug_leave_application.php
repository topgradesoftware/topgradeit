<?php
// DEBUG SCRIPT - Place this in /public_html/api/teacher/leave_application/
// This will help us see what's happening

error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-type: application/json');

try {
    // Test 1: Check if file exists
    $test_result = array(
        'test' => 'Leave Application Debug',
        'timestamp' => date('Y-m-d H:i:s'),
        'php_version' => phpversion(),
        'current_file' => __FILE__,
        'current_dir' => __DIR__,
    );
    
    // Test 2: Check if leave_applicaton.php exists
    $leave_app_file = __DIR__ . '/leave_applicaton.php';
    $test_result['leave_applicaton_exists'] = file_exists($leave_app_file);
    
    // Test 3: Try to parse a test date
    $test_date = '19/11/2025';
    $date_obj = DateTime::createFromFormat('d/m/Y', $test_date);
    if ($date_obj !== false) {
        $test_result['date_parsing'] = 'SUCCESS';
        $test_result['parsed_date'] = $date_obj->format('Y-m-d');
    } else {
        $test_result['date_parsing'] = 'FAILED';
    }
    
    echo json_encode($test_result, JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    echo json_encode(array(
        'error' => $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine()
    ));
}
?>

