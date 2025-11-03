<?php
/**
 * Test Script for Leave Application API
 * 
 * This script allows you to test the leave application API without using the Android app.
 * Upload this file to your server root and access it via browser.
 * 
 * URL: https://topgradesoftware.com/test_leave_api.php
 */

// Set error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leave Application API Tester</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }
        
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            padding: 30px;
        }
        
        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 28px;
        }
        
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 14px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 5px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }
        
        input[type="text"],
        textarea,
        select {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        
        input[type="text"]:focus,
        textarea:focus,
        select:focus {
            outline: none;
            border-color: #667eea;
        }
        
        textarea {
            resize: vertical;
            min-height: 100px;
        }
        
        .btn-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }
        
        button {
            flex: 1;
            padding: 15px 30px;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn-submit {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        
        .btn-get {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
        }
        
        .btn-get:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(245, 87, 108, 0.4);
        }
        
        .response {
            margin-top: 30px;
            padding: 20px;
            border-radius: 5px;
            display: none;
        }
        
        .response.success {
            background: #d4edda;
            border: 2px solid #c3e6cb;
            color: #155724;
        }
        
        .response.error {
            background: #f8d7da;
            border: 2px solid #f5c6cb;
            color: #721c24;
        }
        
        .response h3 {
            margin-bottom: 10px;
            font-size: 18px;
        }
        
        .response pre {
            background: rgba(0,0,0,0.05);
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            white-space: pre-wrap;
            word-wrap: break-word;
            font-family: 'Courier New', monospace;
            font-size: 12px;
        }
        
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #2196F3;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        
        .info-box h3 {
            color: #1976D2;
            margin-bottom: 10px;
            font-size: 16px;
        }
        
        .info-box p {
            color: #555;
            font-size: 14px;
            line-height: 1.6;
        }
        
        .loading {
            display: none;
            text-align: center;
            padding: 20px;
        }
        
        .spinner {
            border: 4px solid #f3f3f3;
            border-top: 4px solid #667eea;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧪 Leave Application API Tester</h1>
        <p class="subtitle">Test your backend API without using the Android app</p>
        
        <div class="info-box">
            <h3>📋 Instructions</h3>
            <p>
                Fill in the form below with your test data and click "Submit Application" to test the API.
                This simulates what the Android app sends to your server.
            </p>
        </div>
        
        <form id="testForm">
            <div class="form-group">
                <label for="operation">Operation:</label>
                <select id="operation" name="operation">
                    <option value="add_application">Add Application</option>
                    <option value="get_applications">Get Applications</option>
                    <option value="delete_application">Delete Application</option>
                </select>
            </div>
            
            <div class="form-group">
                <label for="campus_id">Campus ID:</label>
                <input type="text" id="campus_id" name="campus_id" value="5c67f03e5c3da" required>
            </div>
            
            <div class="form-group">
                <label for="staff_id">Staff ID:</label>
                <input type="text" id="staff_id" name="staff_id" value="6876c43fd910b" required>
            </div>
            
            <div class="form-group app-only">
                <label for="application_title">Application Title:</label>
                <input type="text" id="application_title" name="application_title" value="Test Leave Application">
            </div>
            
            <div class="form-group app-only">
                <label for="application_body">Application Body:</label>
                <textarea id="application_body" name="application_body">This is a test leave application submitted via the API tester.</textarea>
            </div>
            
            <div class="form-group app-only">
                <label for="start_date">Start Date (dd/mm/yyyy):</label>
                <input type="text" id="start_date" name="start_date" value="<?php echo date('d/m/Y'); ?>">
            </div>
            
            <div class="form-group app-only">
                <label for="end_date">End Date (dd/mm/yyyy):</label>
                <input type="text" id="end_date" name="end_date" value="<?php echo date('d/m/Y', strtotime('+3 days')); ?>">
            </div>
            
            <div class="btn-group">
                <button type="submit" class="btn-submit">🚀 Test API</button>
            </div>
        </form>
        
        <div class="loading" id="loading">
            <div class="spinner"></div>
            <p>Testing API...</p>
        </div>
        
        <div class="response" id="response"></div>
    </div>
    
    <script>
        const form = document.getElementById('testForm');
        const loading = document.getElementById('loading');
        const response = document.getElementById('response');
        const operationSelect = document.getElementById('operation');
        const appOnlyFields = document.querySelectorAll('.app-only');
        
        // Show/hide fields based on operation
        operationSelect.addEventListener('change', function() {
            const operation = this.value;
            appOnlyFields.forEach(field => {
                field.style.display = operation === 'add_application' ? 'block' : 'none';
            });
        });
        
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            // Show loading
            loading.style.display = 'block';
            response.style.display = 'none';
            
            // Collect form data
            const formData = new FormData(form);
            const data = {};
            
            formData.forEach((value, key) => {
                // Only include fields that are visible or always needed
                if (key === 'operation' || key === 'campus_id' || key === 'staff_id') {
                    data[key] = value;
                } else if (operationSelect.value === 'add_application') {
                    // Note the typo in the parameter name to match Android app
                    if (key === 'application_body') {
                        data['applictaion_body'] = value;
                    } else {
                        data[key] = value;
                    }
                }
            });
            
            try {
                // Make API request
                const apiResponse = await fetch('api.php?page=teacher/leave_applicaton', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data)
                });
                
                const responseText = await apiResponse.text();
                let jsonResponse;
                
                try {
                    jsonResponse = JSON.parse(responseText);
                } catch (e) {
                    jsonResponse = { error: 'Invalid JSON response', raw: responseText };
                }
                
                // Hide loading
                loading.style.display = 'none';
                
                // Show response
                response.style.display = 'block';
                
                if (apiResponse.ok && jsonResponse.status && jsonResponse.status.code === '1000') {
                    response.className = 'response success';
                    response.innerHTML = `
                        <h3>✅ Success!</h3>
                        <p><strong>Status Code:</strong> ${apiResponse.status}</p>
                        <p><strong>Message:</strong> ${jsonResponse.status.message}</p>
                        <pre>${JSON.stringify(jsonResponse, null, 2)}</pre>
                    `;
                } else {
                    response.className = 'response error';
                    response.innerHTML = `
                        <h3>❌ Error</h3>
                        <p><strong>HTTP Status:</strong> ${apiResponse.status}</p>
                        <p><strong>Message:</strong> ${jsonResponse.status ? jsonResponse.status.message : 'Unknown error'}</p>
                        <pre>${JSON.stringify(jsonResponse, null, 2)}</pre>
                    `;
                }
            } catch (error) {
                // Hide loading
                loading.style.display = 'none';
                
                // Show error
                response.style.display = 'block';
                response.className = 'response error';
                response.innerHTML = `
                    <h3>❌ Request Failed</h3>
                    <p><strong>Error:</strong> ${error.message}</p>
                    <p>Make sure the backend files are uploaded correctly and the API endpoint is accessible.</p>
                `;
            }
        });
    </script>
</body>
</html>

