<?php
/**
 * Leave Application API for Staff/Teachers
 * Endpoint: api.php?page=teacher/leave_applicaton
 * 
 * This file handles:
 * - add_application: Submit new leave application
 * - get_applications: Retrieve leave applications list
 * - delete_application: Delete a leave application
 * - get_application_detail: Get details of a specific application
 */

// Enable error reporting for debugging (disable in production)
error_reporting(E_ALL);
ini_set('display_errors', 0); // Set to 0 in production
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/../../error_log.txt');

// Set JSON header
header('Content-Type: application/json');

// CORS headers (if needed for web access)
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Include database connection
// Adjust this path to match your actual database connection file
require_once __DIR__ . '/../../includes/db_connect.php';

// If db_connect.php doesn't exist, use this template:
if (!isset($conn)) {
    // Database configuration
    $servername = "localhost";
    $username = "your_db_username";  // CHANGE THIS
    $password = "your_db_password";  // CHANGE THIS
    $dbname = "your_db_name";        // CHANGE THIS
    
    try {
        $conn = new mysqli($servername, $username, $password, $dbname);
        
        if ($conn->connect_error) {
            throw new Exception("Connection failed: " . $conn->connect_error);
        }
        
        $conn->set_charset("utf8mb4");
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode([
            'status' => [
                'code' => '5000',
                'message' => 'Database connection failed'
            ]
        ]);
        error_log("DB Connection Error: " . $e->getMessage());
        exit;
    }
}

/**
 * Read JSON input from request body
 */
function getJsonInput() {
    $input = file_get_contents('php://input');
    error_log("Raw Input: " . $input);
    
    if (empty($input)) {
        return null;
    }
    
    $decoded = json_decode($input, true);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        error_log("JSON Decode Error: " . json_last_error_msg());
        return null;
    }
    
    return $decoded;
}

/**
 * Send JSON response
 */
function sendResponse($code, $message, $data = null) {
    $response = [
        'status' => [
            'code' => $code,
            'message' => $message
        ]
    ];
    
    if ($data !== null) {
        $response['data'] = $data;
    }
    
    echo json_encode($response);
    exit;
}

/**
 * Validate required parameters
 */
function validateParams($data, $required) {
    $missing = [];
    foreach ($required as $param) {
        if (!isset($data[$param]) || empty($data[$param])) {
            $missing[] = $param;
        }
    }
    return $missing;
}

// Get JSON input
$data = getJsonInput();

if ($data === null) {
    http_response_code(400);
    sendResponse('4000', 'Invalid JSON input or empty request body');
}

// Log the operation for debugging
error_log("Operation: " . ($data['operation'] ?? 'not specified'));
error_log("Request Data: " . json_encode($data));

// Get operation type
$operation = $data['operation'] ?? '';

// Route to appropriate handler
switch ($operation) {
    case 'add_application':
        addApplication($conn, $data);
        break;
        
    case 'get_applications':
        getApplications($conn, $data);
        break;
        
    case 'delete_application':
        deleteApplication($conn, $data);
        break;
        
    case 'get_application_detail':
        getApplicationDetail($conn, $data);
        break;
        
    default:
        http_response_code(400);
        sendResponse('4001', 'Invalid or missing operation parameter: ' . $operation);
}

/**
 * Add new leave application
 */
function addApplication($conn, $data) {
    // Validate required parameters
    $required = ['campus_id', 'staff_id', 'application_title', 'start_date', 'end_date'];
    $missing = validateParams($data, $required);
    
    if (!empty($missing)) {
        http_response_code(400);
        sendResponse('4002', 'Missing required parameters: ' . implode(', ', $missing));
    }
    
    // Extract parameters
    $campus_id = $conn->real_escape_string($data['campus_id']);
    $staff_id = $conn->real_escape_string($data['staff_id']);
    $application_title = $conn->real_escape_string($data['application_title']);
    // Note: parameter name has typo "applictaion_body" (missing 'a')
    $application_body = $conn->real_escape_string($data['applictaion_body'] ?? $data['application_body'] ?? '');
    $start_date = $conn->real_escape_string($data['start_date']);
    $end_date = $conn->real_escape_string($data['end_date']);
    
    // Convert date format from dd/mm/yyyy to yyyy-mm-dd for MySQL
    $start_date = convertDateFormat($start_date);
    $end_date = convertDateFormat($end_date);
    
    if (!$start_date || !$end_date) {
        http_response_code(400);
        sendResponse('4003', 'Invalid date format. Expected: dd/mm/yyyy');
    }
    
    // Insert into database
    // Adjust table name and columns according to your database schema
    $sql = "INSERT INTO leave_applications 
            (campus_id, staff_id, application_title, application_body, start_date, end_date, status, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, 'pending', NOW())";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        error_log("Prepare failed: " . $conn->error);
        http_response_code(500);
        sendResponse('5001', 'Database error: Failed to prepare statement');
    }
    
    $stmt->bind_param("ssssss", $campus_id, $staff_id, $application_title, $application_body, $start_date, $end_date);
    
    if ($stmt->execute()) {
        $application_id = $stmt->insert_id;
        error_log("Application submitted successfully. ID: " . $application_id);
        
        http_response_code(200);
        sendResponse('1000', 'Leave application submitted successfully', [
            'application_id' => $application_id
        ]);
    } else {
        error_log("Execute failed: " . $stmt->error);
        http_response_code(500);
        sendResponse('5002', 'Failed to submit leave application: ' . $stmt->error);
    }
    
    $stmt->close();
}

/**
 * Get leave applications list
 */
function getApplications($conn, $data) {
    $required = ['campus_id', 'staff_id'];
    $missing = validateParams($data, $required);
    
    if (!empty($missing)) {
        http_response_code(400);
        sendResponse('4004', 'Missing required parameters: ' . implode(', ', $missing));
    }
    
    $campus_id = $conn->real_escape_string($data['campus_id']);
    $staff_id = $conn->real_escape_string($data['staff_id']);
    
    $sql = "SELECT * FROM leave_applications 
            WHERE campus_id = ? AND staff_id = ? 
            ORDER BY created_at DESC";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        http_response_code(500);
        sendResponse('5003', 'Database error: Failed to prepare statement');
    }
    
    $stmt->bind_param("ss", $campus_id, $staff_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $applications = [];
    while ($row = $result->fetch_assoc()) {
        // Convert date format back to dd/mm/yyyy for display
        $row['start_date'] = date('d/m/Y', strtotime($row['start_date']));
        $row['end_date'] = date('d/m/Y', strtotime($row['end_date']));
        $applications[] = $row;
    }
    
    http_response_code(200);
    sendResponse('1000', 'Applications retrieved successfully', [
        'applications' => $applications
    ]);
    
    $stmt->close();
}

/**
 * Delete leave application
 */
function deleteApplication($conn, $data) {
    $required = ['application_id', 'staff_id'];
    $missing = validateParams($data, $required);
    
    if (!empty($missing)) {
        http_response_code(400);
        sendResponse('4005', 'Missing required parameters: ' . implode(', ', $missing));
    }
    
    $application_id = $conn->real_escape_string($data['application_id']);
    $staff_id = $conn->real_escape_string($data['staff_id']);
    
    $sql = "DELETE FROM leave_applications 
            WHERE id = ? AND staff_id = ?";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        http_response_code(500);
        sendResponse('5004', 'Database error: Failed to prepare statement');
    }
    
    $stmt->bind_param("is", $application_id, $staff_id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            http_response_code(200);
            sendResponse('1000', 'Application deleted successfully');
        } else {
            http_response_code(404);
            sendResponse('4006', 'Application not found or already deleted');
        }
    } else {
        http_response_code(500);
        sendResponse('5005', 'Failed to delete application');
    }
    
    $stmt->close();
}

/**
 * Get application detail
 */
function getApplicationDetail($conn, $data) {
    $required = ['application_id'];
    $missing = validateParams($data, $required);
    
    if (!empty($missing)) {
        http_response_code(400);
        sendResponse('4007', 'Missing required parameters: ' . implode(', ', $missing));
    }
    
    $application_id = $conn->real_escape_string($data['application_id']);
    
    $sql = "SELECT * FROM leave_applications WHERE id = ?";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        http_response_code(500);
        sendResponse('5006', 'Database error: Failed to prepare statement');
    }
    
    $stmt->bind_param("i", $application_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($row = $result->fetch_assoc()) {
        // Convert date format
        $row['start_date'] = date('d/m/Y', strtotime($row['start_date']));
        $row['end_date'] = date('d/m/Y', strtotime($row['end_date']));
        
        http_response_code(200);
        sendResponse('1000', 'Application retrieved successfully', [
            'application' => $row
        ]);
    } else {
        http_response_code(404);
        sendResponse('4008', 'Application not found');
    }
    
    $stmt->close();
}

/**
 * Convert date from dd/mm/yyyy to yyyy-mm-dd
 */
function convertDateFormat($date) {
    // Handle dd/mm/yyyy format
    if (preg_match('/^(\d{2})\/(\d{2})\/(\d{4})$/', $date, $matches)) {
        return $matches[3] . '-' . $matches[2] . '-' . $matches[1];
    }
    
    // If already in yyyy-mm-dd format
    if (preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
        return $date;
    }
    
    return false;
}

// Close database connection
if (isset($conn)) {
    $conn->close();
}

