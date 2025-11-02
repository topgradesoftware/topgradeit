<?php
// Quick test script to verify if API changes are live
// Place this in the same directory as your api.php

header('Content-type: application/json');

echo json_encode([
    'test' => 'API file check',
    'timestamp' => date('Y-m-d H:i:s'),
    'message' => 'If you see this, PHP files are being updated correctly',
    'php_version' => phpversion()
]);
?>

