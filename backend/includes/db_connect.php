<?php
/**
 * Database Connection Configuration
 * TopGrade Application Backend
 * 
 * IMPORTANT: Update the credentials below with your actual database information
 */

// Prevent direct access
if (!defined('DB_CONNECTION_ALLOWED')) {
    define('DB_CONNECTION_ALLOWED', true);
}

// ============================================
// DATABASE CONFIGURATION
// ============================================

// PRODUCTION SETTINGS - Update these with your actual credentials
$db_config = [
    'host'     => 'localhost',           // Usually 'localhost' or your server IP
    'username' => 'your_db_username',    // Your MySQL username
    'password' => 'your_db_password',    // Your MySQL password
    'database' => 'your_db_name',        // Your database name
    'charset'  => 'utf8mb4',            // Character set (recommended: utf8mb4)
    'port'     => 3306                  // MySQL port (default: 3306)
];

// ============================================
// ALTERNATIVE: Read from environment variables (more secure)
// ============================================
/*
$db_config = [
    'host'     => getenv('DB_HOST') ?: 'localhost',
    'username' => getenv('DB_USERNAME'),
    'password' => getenv('DB_PASSWORD'),
    'database' => getenv('DB_DATABASE'),
    'charset'  => 'utf8mb4',
    'port'     => getenv('DB_PORT') ?: 3306
];
*/

// ============================================
// CREATE CONNECTION
// ============================================

try {
    // Create mysqli connection
    $conn = new mysqli(
        $db_config['host'],
        $db_config['username'],
        $db_config['password'],
        $db_config['database'],
        $db_config['port']
    );
    
    // Check connection
    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }
    
    // Set character set
    if (!$conn->set_charset($db_config['charset'])) {
        throw new Exception("Error setting character set: " . $conn->error);
    }
    
    // Optional: Set timezone (adjust to your timezone)
    // $conn->query("SET time_zone = '+00:00'");
    
    // Connection successful - log only in development
    // error_log("Database connected successfully");
    
} catch (Exception $e) {
    // Log error
    error_log("Database Connection Error: " . $e->getMessage());
    
    // In production, don't expose detailed error messages
    if (defined('ENVIRONMENT') && ENVIRONMENT === 'development') {
        die("Database Error: " . $e->getMessage());
    } else {
        // Generic error for production
        http_response_code(500);
        die(json_encode([
            'status' => [
                'code' => '5000',
                'message' => 'Database connection error. Please try again later.'
            ]
        ]));
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================

/**
 * Close database connection
 */
function closeConnection() {
    global $conn;
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
}

/**
 * Sanitize input for SQL
 * @param string $data
 * @return string
 */
function sanitize($data) {
    global $conn;
    if (!isset($conn)) {
        return htmlspecialchars(strip_tags(trim($data)));
    }
    return $conn->real_escape_string(trim($data));
}

/**
 * Execute a prepared query
 * @param string $sql
 * @param array $params
 * @param string $types
 * @return mysqli_stmt|false
 */
function executeQuery($sql, $params = [], $types = '') {
    global $conn;
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        error_log("Prepare failed: " . $conn->error);
        return false;
    }
    
    if (!empty($params) && !empty($types)) {
        $stmt->bind_param($types, ...$params);
    }
    
    if (!$stmt->execute()) {
        error_log("Execute failed: " . $stmt->error);
        return false;
    }
    
    return $stmt;
}

// Register shutdown function to close connection
register_shutdown_function('closeConnection');

?>

