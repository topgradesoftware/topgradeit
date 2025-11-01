<?php
/**
 * Parent Complaint API - Production Ready
 * 
 * Base URL: .../api/parent/complain.php
 * Method: POST
 * Content-Type: application/json
 * 
 * Operations:
 * 1. add_complain - Submit new complaint
 * 2. delete_complain - Delete complaint  
 * 3. read_complain_title - Get complaint categories
 * 4. read_complain - List complaints with filtering
 * 
 * @version 2.1
 * @date 2025-10-30
 */

// Set response headers
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Helper functions FIRST (before includes)
function sendError($code, $message, $httpCode = 400) {
    http_response_code($httpCode);
    echo json_encode(array(
        'status' => array(
            'code' => (string)$code,
            'message' => $message
        )
    ));
    exit;
}

function sendSuccess($message, $additionalData = array()) {
    $response = array(
        'status' => array(
            'code' => '1000',
            'message' => $message
        )
    );
    echo json_encode(array_merge($response, $additionalData));
    exit;
}

function validateRequired($fields, $data) {
    $missing = array();
    foreach ($fields as $field) {
        if (!isset($data[$field]) || trim($data[$field]) === '') {
            $missing[] = $field;
        }
    }
    if (!empty($missing)) {
        sendError('400', 'Missing required fields: ' . implode(', ', $missing));
    }
}

// Include required files
if (file_exists(__DIR__ . '/../db.php')) {
    require_once(__DIR__ . '/../db.php');
} elseif (file_exists('../db.php')) {
    require_once('../db.php');
} else {
    error_log('CRITICAL: db.php not found');
    sendError('500', 'Database configuration file not found', 500);
}

// Functions.php is optional (for email/SMS)
if (file_exists(__DIR__ . '/../functions.php')) {
    require_once(__DIR__ . '/../functions.php');
} elseif (file_exists('../functions.php')) {
    require_once('../functions.php');
} else {
    error_log('WARNING: functions.php not found - email/SMS notifications disabled');
}

// Check database connection
if (!isset($db) || !is_object($db)) {
    error_log('CRITICAL: $db object not available - Check db.php file and database credentials');
    sendError('500', 'Database connection error', 500);
}

error_log('✓ Database connection OK - Type: ' . get_class($db));

// Get POST data
$rawInput = file_get_contents('php://input');
if (empty($rawInput)) {
    sendError('400', 'Empty request body');
}

$data_post = json_decode($rawInput, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    logError('Invalid JSON', array('error' => json_last_error_msg()));
    sendError('400', 'Invalid JSON: ' . json_last_error_msg());
}

// Validate operation
if (!isset($data_post['operation']) || empty($data_post['operation'])) {
    sendError('400', 'Operation parameter is required');
}

$operation = trim($data_post['operation']);

// Log incoming request (detailed)
error_log('========== COMPLAINT API DEBUG ==========');
error_log('Operation: ' . $operation);
error_log('Full Request: ' . $rawInput);
error_log('Parsed Data: ' . json_encode($data_post));
error_log('==========================================');

// ==========================================
// OPERATION 1: ADD COMPLAINT
// ==========================================
if ($operation === 'add_complain') {
    
    // Accept dynamic parameters like other parent APIs
    // parent_parent_id = actual parent ID, parent_id = campus ID (confusing naming!)
    $parent_parent_id = isset($data_post['parent_parent_id']) ? trim($data_post['parent_parent_id']) : '';
    $campus_id = isset($data_post['parent_id']) ? trim($data_post['parent_id']) : trim($data_post['campus_id'] ?? '');
    $student_id = trim($data_post['student_id']);
    $session_id = isset($data_post['session_id']) ? trim($data_post['session_id']) : '';
    $complain_title = trim($data_post['complain_title']);
    $complain_body = trim($data_post['complain_body']);
    $complainant_category = isset($data_post['complainant_category']) ? trim($data_post['complainant_category']) : '';
    $priority = isset($data_post['priority']) ? trim($data_post['priority']) : 'Normal';
    
    // Debug: Log extracted parameters
    error_log('ADD_COMPLAIN - Extracted params:');
    error_log('  parent_parent_id: ' . $parent_parent_id);
    error_log('  parent_id (campus): ' . $campus_id);
    error_log('  student_id: ' . $student_id);
    error_log('  session_id: ' . $session_id);
    error_log('  title: ' . substr($complain_title, 0, 50));
    error_log('  category: ' . $complainant_category);
    error_log('  priority: ' . $priority);
    
    // Validate required fields (flexible to accept both naming conventions)
    if (empty($campus_id) || empty($student_id) || empty($complain_title) || empty($complain_body)) {
        error_log('Validation failed - campus_id: ' . $campus_id . ', student_id: ' . $student_id);
        sendError('400', 'Missing required fields: campus_id, student_id, complain_title, complain_body');
    }
    
    try {
        // Validate campus exists
        error_log('Looking up campus with unique_id: ' . $campus_id);
        $db->where('unique_id', $campus_id);
        $db->where('is_active', 1);
        $db->where('is_delete', 0);
        $camp_info = $db->getOne('user');
        
        if (!$camp_info) {
            error_log('Campus not found for unique_id: ' . $campus_id);
            sendError('404', 'Campus not found', 404);
        }
        
        error_log('✓ Campus found: ' . ($camp_info['full_name'] ?? 'N/A'));
        
        // Load student/parent info
        error_log('Looking up student/parent with unique_id: ' . $student_id);
        $db->where('unique_id', $student_id);
        $db->where('is_delete', 0);
        $student = $db->getOne('employee');
        
        if (!$student) {
            error_log('Student/Parent not found for unique_id: ' . $student_id);
            sendError('404', 'Student/Parent not found', 404);
        }
        
        error_log('✓ Student/Parent found: ' . ($student['full_name'] ?? 'N/A'));
        
        // Generate unique ID
        $complaint_unique_id = uniqid('cmp_', true);
        error_log('Generated complaint_unique_id: ' . $complaint_unique_id);
        
        // Prepare complaint data
        $complaintData = array(
            'unique_id' => $complaint_unique_id,
            'parent_id' => $campus_id,
            'employee_id' => $student_id,
            'full_name' => $complain_title,
            'body' => $complain_body,
            'complainant_category' => $complainant_category,
            'complainant_person' => isset($student['full_name']) ? $student['full_name'] : '',
            'contact' => isset($student['phone']) ? $student['phone'] : '',
            'complain_about' => isset($student['full_name']) ? $student['full_name'] : '',
            'priority' => $priority,
            'created_date' => date('Y-m-d H:i:s'),
            'timestamp' => date('Y-m-d H:i:s'),
            'is_active' => 2, // 2 = Under Discussion
            'is_delete' => 0,
            'response_body' => '',
            'response_date' => null
        );
        
        // Insert into database
        error_log('Attempting to insert complaint...');
        $insert_id = $db->insert('complaint', $complaintData);
        
        if (!$insert_id) {
            error_log('INSERT FAILED - Error: ' . $db->getLastError());
            error_log('Insert Data: ' . json_encode($complaintData));
            sendError('500', 'Failed to submit complaint: ' . $db->getLastError(), 500);
        }
        
        error_log('✓ Complaint inserted successfully - ID: ' . $insert_id . ', unique_id: ' . $complaint_unique_id);
        error_log('✓ Student: ' . $student['full_name']);
        
        // Send email notification (async, non-blocking)
        if (function_exists('send_email') && !empty($camp_info['email'])) {
            try {
                $emailBody = "New Complaint Submitted<br><br>"
                    . "<strong>Title:</strong> " . htmlspecialchars($complain_title) . "<br>"
                    . "<strong>Description:</strong> " . nl2br(htmlspecialchars($complain_body)) . "<br>"
                    . "<strong>Priority:</strong> " . htmlspecialchars($priority) . "<br>"
                    . "<strong>Category:</strong> " . htmlspecialchars($complainant_category) . "<br>"
                    . "<strong>Submitted By:</strong> " . htmlspecialchars($student['full_name']) . "<br>"
                    . "<strong>Contact:</strong> " . htmlspecialchars($student['phone']) . "<br>"
                    . "<strong>Date:</strong> " . date('d/M/y H:i');
                
                send_email('New Complaint - ' . $complain_title, $emailBody, $camp_info['email'], false);
            } catch (Exception $e) {
                logError('Email failed', array('error' => $e->getMessage()));
            }
        }
        
        // Send SMS notification (async, non-blocking)
        if (function_exists('send_sms') && !empty($camp_info['complain_no'])) {
            try {
                $smsMsg = "New Complaint\n"
                    . "Title: " . substr($complain_title, 0, 40) . "\n"
                    . "From: " . $student['full_name'] . "\n"
                    . "Priority: " . $priority . "\n"
                    . "Ph: " . $student['phone'];
                
                $senders = explode(',', $camp_info['sms_sender']);
                $numbers = explode(',', $camp_info['complain_no']);
                
                foreach ($numbers as $number) {
                    $number = trim($number);
                    if (!empty($number)) {
                        send_sms($number, $smsMsg, $senders[0], $camp_info['sms_password'], $campus_id);
                    }
                }
                
                // Also send to landline if configured
                if (!empty($camp_info['landline']) && isset($camp_info['sms_alt_number']) && $camp_info['sms_alt_number'] == 1) {
                    send_sms($camp_info['landline'], $smsMsg, $senders[0], $camp_info['sms_password'], $campus_id);
                }
            } catch (Exception $e) {
                logError('SMS failed', array('error' => $e->getMessage()));
            }
        }
        
        // Success response
        sendSuccess('Complaint submitted successfully', array(
            'complaint_id' => $complaint_unique_id,
            'status' => 'Under Discussion'
        ));
        
    } catch (Exception $e) {
        error_log('========== EXCEPTION IN ADD_COMPLAIN ==========');
        error_log('Error Message: ' . $e->getMessage());
        error_log('Error File: ' . $e->getFile());
        error_log('Error Line: ' . $e->getLine());
        error_log('Error Trace: ' . $e->getTraceAsString());
        error_log('===============================================');
        sendError('500', 'Server error: ' . $e->getMessage(), 500);
    }

// ==========================================
// OPERATION 2: READ COMPLAINTS
// ==========================================
} elseif ($operation === 'read_complain') {
    
    // Accept dynamic parameters like other parent APIs
    $parent_parent_id = isset($data_post['parent_parent_id']) ? trim($data_post['parent_parent_id']) : '';
    $campus_id = isset($data_post['parent_id']) ? trim($data_post['parent_id']) : trim($data_post['campus_id'] ?? '');
    $student_id = trim($data_post['student_id']);
    $session_id = isset($data_post['session_id']) ? trim($data_post['session_id']) : '';
    $filter_type = isset($data_post['filter_type']) ? trim($data_post['filter_type']) : 'all';
    
    // Debug: Log extracted parameters
    error_log('READ_COMPLAIN - Extracted params:');
    error_log('  parent_parent_id: ' . $parent_parent_id);
    error_log('  parent_id (campus): ' . $campus_id);
    error_log('  student_id: ' . $student_id);
    error_log('  session_id: ' . $session_id);
    error_log('  filter_type: ' . $filter_type);
    
    // Validate required fields
    if (empty($campus_id) || empty($student_id)) {
        error_log('Validation failed - campus_id: ' . $campus_id . ', student_id: ' . $student_id);
        sendError('400', 'Missing required fields: campus_id (or parent_id), student_id');
    }
    
    try {
        // Build query
        $db->where('parent_id', $campus_id);
        $db->where('employee_id', $student_id);
        $db->where('is_delete', 0);
        
        // Apply status filter
        if ($filter_type !== 'all' && !empty($filter_type)) {
            switch (strtolower($filter_type)) {
                case 'pending':
                    $db->where('is_active', 0);
                    break;
                case 'under_discussion':
                    $db->where('is_active', 2);
                    break;
                case 'solved':
                    $db->where('is_active', 1);
                    break;
            }
        }
        
        $db->orderBy('created_date', 'DESC');
        $complaints = $db->get('complaint');
        
        error_log('Query executed - Found ' . count($complaints ?? array()) . ' complaints');
        if (empty($complaints)) {
            error_log('No complaints found for campus: ' . $campus_id . ', student: ' . $student_id);
        }
        
        // Format response
        $complaint_list = array();
        if ($complaints) {
            foreach ($complaints as $complaint) {
                // Map is_active to status name
                $is_active = isset($complaint['is_active']) ? (int)$complaint['is_active'] : 0;
                $status_map = array(
                    0 => 'Pending',
                    1 => 'Solved',
                    2 => 'Under Discussion'
                );
                $status_name = isset($status_map[$is_active]) ? $status_map[$is_active] : 'Pending';
                
                // Format dates (dd/MMM/yy)
                $complaint_date = '';
                if (!empty($complaint['created_date'])) {
                    $complaint_date = date('d/M/y', strtotime($complaint['created_date']));
                }
                
                $response_date = '';
                if (!empty($complaint['response_date']) && $complaint['response_date'] !== '0000-00-00 00:00:00') {
                    $response_date = date('d/M/y', strtotime($complaint['response_date']));
                }
                
                $complaint_list[] = array(
                    'complaint_id' => $complaint['unique_id'] ?? '',
                    'complaint_title' => $complaint['full_name'] ?? '',
                    'complaint_description' => $complaint['body'] ?? '',
                    'complaint_status' => $status_name,
                    'complaint_date' => $complaint_date,
                    'student_id' => $complaint['employee_id'] ?? '',
                    'student_name' => $complaint['complain_about'] ?? '',
                    'response' => $complaint['response_body'] ?? '',
                    'response_date' => $response_date,
                    'category_id' => $complaint['complainant_category'] ?? '',
                    'contact' => $complaint['contact'] ?? '',
                    'priority' => $complaint['priority'] ?? ''
                );
            }
        }
        
        sendSuccess('Success', array(
            'data' => $complaint_list,
            'total_count' => count($complaint_list),
            'filter_applied' => $filter_type
        ));
        
    } catch (Exception $e) {
        logError('Exception in read_complain', array('error' => $e->getMessage()));
        sendError('500', 'Server error: ' . $e->getMessage(), 500);
    }

// ==========================================
// OPERATION 3: READ COMPLAINT TITLES
// ==========================================
} elseif ($operation === 'read_complain_title') {
    
    // Accept dynamic parameters like other parent APIs
    $parent_parent_id = isset($data_post['parent_parent_id']) ? trim($data_post['parent_parent_id']) : '';
    $campus_id = isset($data_post['parent_id']) ? trim($data_post['parent_id']) : trim($data_post['campus_id'] ?? '');
    $session_id = isset($data_post['session_id']) ? trim($data_post['session_id']) : '';
    
    // Debug: Log extracted parameters
    error_log('READ_COMPLAIN_TITLE - Extracted params:');
    error_log('  parent_parent_id: ' . $parent_parent_id);
    error_log('  parent_id (campus): ' . $campus_id);
    error_log('  session_id: ' . $session_id);
    
    // Validate campus_id is present
    if (empty($campus_id)) {
        error_log('Validation failed - campus_id is empty');
        sendError('400', 'Missing required field: campus_id (or parent_id)');
    }
    
    try {
        $db->where('parent_id', $campus_id);
        $db->where('is_active', 1);
        $db->where('is_delete', 0);
        $db->orderBy('display_order', 'ASC');
        $complain_title_list = $db->get('complain_title');
        
        error_log('Query executed - Found ' . count($complain_title_list ?? array()) . ' complaint titles');
        if (empty($complain_title_list)) {
            error_log('No complaint titles found for campus: ' . $campus_id);
        }
        
        $titles = array();
        if ($complain_title_list) {
            foreach ($complain_title_list as $title) {
                $titles[] = array(
                    'title_id' => $title['title_id'] ?? '',
                    'title' => $title['complain_title'] ?? '',
                    'display_order' => isset($title['display_order']) ? (int)$title['display_order'] : 0,
                    'is_active' => isset($title['is_active']) ? (int)$title['is_active'] : 1
                );
            }
        }
        
        sendSuccess('Success', array(
            'titles' => $titles,
            'count' => count($titles)
        ));
        
    } catch (Exception $e) {
        logError('Exception in read_complain_title', array('error' => $e->getMessage()));
        sendError('500', 'Server error: ' . $e->getMessage(), 500);
    }

// ==========================================
// OPERATION 4: DELETE COMPLAINT
// ==========================================
} elseif ($operation === 'delete_complain') {
    
    // Accept dynamic parameters like other parent APIs
    $unique_id = trim($data_post['unique_id']);
    $parent_parent_id = isset($data_post['parent_parent_id']) ? trim($data_post['parent_parent_id']) : '';
    $campus_id = isset($data_post['parent_id']) ? trim($data_post['parent_id']) : trim($data_post['campus_id'] ?? '');
    $student_id = trim($data_post['student_id']);
    $session_id = isset($data_post['session_id']) ? trim($data_post['session_id']) : '';
    
    // Debug: Log extracted parameters
    error_log('DELETE_COMPLAIN - Extracted params:');
    error_log('  unique_id: ' . $unique_id);
    error_log('  parent_parent_id: ' . $parent_parent_id);
    error_log('  parent_id (campus): ' . $campus_id);
    error_log('  student_id: ' . $student_id);
    error_log('  session_id: ' . $session_id);
    
    // Validate required fields
    if (empty($unique_id) || empty($campus_id) || empty($student_id)) {
        error_log('Validation failed - unique_id: ' . $unique_id . ', campus_id: ' . $campus_id . ', student_id: ' . $student_id);
        sendError('400', 'Missing required fields: unique_id, campus_id (or parent_id), student_id');
    }
    
    try {
        // Verify complaint exists and belongs to this user
        $db->where('unique_id', $unique_id);
        $db->where('parent_id', $campus_id);
        $db->where('employee_id', $student_id);
        $db->where('is_delete', 0);
        $complaint = $db->getOne('complaint');
        
        if (!$complaint) {
            sendError('404', 'Complaint not found or already deleted', 404);
        }
        
        // Soft delete
        $db->where('unique_id', $unique_id);
        $db->where('parent_id', $campus_id);
        $db->where('employee_id', $student_id);
        $result = $db->update('complaint', array(
            'is_delete' => 1,
            'timestamp' => date('Y-m-d H:i:s')
        ));
        
        if (!$result) {
            logError('Failed to delete', array('error' => $db->getLastError()));
            sendError('500', 'Failed to delete complaint', 500);
        }
        
        logError('Complaint deleted', array('id' => $unique_id));
        sendSuccess('Complaint deleted successfully');
        
    } catch (Exception $e) {
        logError('Exception in delete_complain', array('error' => $e->getMessage()));
        sendError('500', 'Server error: ' . $e->getMessage(), 500);
    }

// ==========================================
// INVALID OPERATION
// ==========================================
} else {
    logError('Invalid operation', array('operation' => $operation));
    sendError('400', 'Invalid operation: ' . $operation);
}
?>
