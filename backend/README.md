# 🔧 TopGrade Backend - Leave Application Module

## 📌 Problem Fixed

**Issue:** Android app shows "Failed to submit application" with HTTP 500 error

**Root Cause:** Backend endpoint `api.php?page=teacher/leave_applicaton` was either missing or had errors

**Solution:** Complete working backend implementation with proper error handling, validation, and database integration

---

## 📦 What's Included

```
backend/
├── DEPLOYMENT_GUIDE.md              ← 📖 Complete deployment instructions
├── README.md                        ← 📄 This file
├── test_leave_api.php              ← 🧪 Browser-based API tester
├── includes/
│   └── db_connect.php              ← 🔌 Database connection
├── teacher/
│   └── leave_applicaton.php        ← 🎯 Main API endpoint
└── sql/
    └── leave_applications_table.sql ← 💾 Database schema
```

---

## 🚀 Quick Start

### 1. Configure Database

Edit `includes/db_connect.php`:

```php
$db_config = [
    'host'     => 'localhost',
    'username' => 'YOUR_DB_USERNAME',  ← Change this
    'password' => 'YOUR_DB_PASSWORD',  ← Change this
    'database' => 'YOUR_DB_NAME',      ← Change this
];
```

### 2. Create Database Table

Run the SQL script in `sql/leave_applications_table.sql` using phpMyAdmin or MySQL command line.

### 3. Upload Files

Upload to your server at `https://topgradesoftware.com/`:

- `includes/db_connect.php` → `/public_html/includes/`
- `teacher/leave_applicaton.php` → `/public_html/teacher/`

### 4. Test

**Option A: Use the web tester**
- Upload `test_leave_api.php` to server root
- Visit: `https://topgradesoftware.com/test_leave_api.php`
- Click "Test API"

**Option B: Use the Android app**
- Open TopGrade app
- Login as staff
- Submit a leave application

---

## ✅ Features

### API Operations

| Operation | Description | Required Parameters |
|-----------|-------------|---------------------|
| `add_application` | Submit new leave application | `campus_id`, `staff_id`, `application_title`, `applictaion_body`, `start_date`, `end_date` |
| `get_applications` | Retrieve all applications for a staff | `campus_id`, `staff_id` |
| `delete_application` | Delete an application | `application_id`, `staff_id` |
| `get_application_detail` | Get details of one application | `application_id` |

### Security Features

✅ Prepared statements (SQL injection prevention)  
✅ Input validation and sanitization  
✅ Error logging (not exposed to client)  
✅ Proper HTTP status codes  
✅ CORS headers configured  

### Error Handling

- Detailed server-side logging
- User-friendly error messages
- Proper HTTP status codes (200, 400, 404, 500)
- Graceful failure handling

---

## 📱 Android App Integration

The backend is designed to work seamlessly with your TopGrade Android app.

**Request Example:**
```json
POST api.php?page=teacher/leave_applicaton
Content-Type: application/json

{
  "operation": "add_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_title": "Sick Leave",
  "applictaion_body": "Not feeling well",
  "start_date": "25/11/2025",
  "end_date": "27/11/2025"
}
```

**Success Response:**
```json
{
  "status": {
    "code": "1000",
    "message": "Leave application submitted successfully"
  },
  "data": {
    "application_id": 123
  }
}
```

**Error Response:**
```json
{
  "status": {
    "code": "5002",
    "message": "Failed to submit leave application"
  }
}
```

---

## 🗄️ Database Schema

**Table:** `leave_applications`

| Column | Type | Description |
|--------|------|-------------|
| `id` | INT (PK) | Auto-increment ID |
| `campus_id` | VARCHAR(50) | Campus identifier |
| `staff_id` | VARCHAR(50) | Staff identifier |
| `application_title` | VARCHAR(255) | Leave subject |
| `application_body` | TEXT | Leave details |
| `start_date` | DATE | Leave start date |
| `end_date` | DATE | Leave end date |
| `status` | ENUM | pending/approved/rejected/cancelled |
| `approved_by` | VARCHAR(50) | Approver staff ID |
| `approval_date` | DATETIME | When approved/rejected |
| `rejection_reason` | TEXT | Reason if rejected |
| `created_at` | TIMESTAMP | When submitted |
| `updated_at` | TIMESTAMP | Last modified |

---

## 🐛 Troubleshooting

### Problem: 500 Internal Server Error

**Check:**
1. PHP syntax errors
2. Database connection credentials
3. File paths (include statements)
4. File permissions (644 or 755)
5. Error logs

**Enable debugging:**
```php
// In leave_applicaton.php, line 7:
ini_set('display_errors', 1);
```

### Problem: Database Connection Failed

**Check:**
1. Credentials in `db_connect.php`
2. MySQL service running
3. Database user has permissions
4. Correct host (localhost vs 127.0.0.1)

**Test connection:**
```php
<?php
$conn = new mysqli('localhost', 'user', 'pass', 'db');
echo $conn->connect_error ? "Failed" : "Connected!";
?>
```

### Problem: Table Doesn't Exist

**Solution:**
Run the SQL script: `sql/leave_applications_table.sql`

**Verify:**
```sql
SHOW TABLES LIKE 'leave_applications';
```

### Problem: Empty Response

**Check:**
1. API endpoint path is correct
2. File has proper PHP tags `<?php ... ?>`
3. No syntax errors
4. Headers sent before output

---

## 📊 Testing Checklist

- [ ] Database credentials configured
- [ ] Database table created
- [ ] Files uploaded to correct locations
- [ ] File permissions set (644/755)
- [ ] Web tester returns JSON response
- [ ] Android app can submit successfully
- [ ] Data appears in database
- [ ] Error logs are empty (no PHP errors)

---

## 🔍 Debugging Tools

### 1. Web-Based Tester
Upload `test_leave_api.php` and access via browser to test without Android app.

### 2. Check Error Logs
```bash
# Server error log
tail -f /public_html/error_log

# Custom error log
tail -f /public_html/error_log.txt
```

### 3. Database Query
```sql
-- Check recent submissions
SELECT * FROM leave_applications 
ORDER BY created_at DESC 
LIMIT 10;
```

### 4. Android Logs
```bash
adb logcat -s StaffAddApplication:* -v time
```

---

## 🔒 Security Best Practices

1. **In Production:**
   - Disable error display: `ini_set('display_errors', 0);`
   - Use environment variables for credentials
   - Enable HTTPS only
   - Restrict file access in .htaccess

2. **Database:**
   - Use strong passwords
   - Limit user permissions to specific database
   - Regular backups

3. **Files:**
   - Keep sensitive files outside web root if possible
   - Set proper file permissions (644 for files, 755 for directories)
   - Never commit credentials to version control

---

## 📞 Support

If you need help:

1. Check `DEPLOYMENT_GUIDE.md` for detailed instructions
2. Use `test_leave_api.php` to verify the API
3. Check error logs for specific error messages
4. Verify database connection separately
5. Ensure file paths are correct

---

## 📝 Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| 1000 | Success | Operation completed |
| 4000 | Bad Request | Invalid input |
| 4001 | Bad Request | Invalid operation |
| 4002 | Bad Request | Missing parameters |
| 4003 | Bad Request | Invalid date format |
| 4004 | Bad Request | Missing required params (get) |
| 4005 | Bad Request | Missing required params (delete) |
| 4006 | Not Found | Application not found |
| 4007 | Bad Request | Missing application_id |
| 4008 | Not Found | Application not found (detail) |
| 5000 | Server Error | Database connection failed |
| 5001 | Server Error | Prepare statement failed |
| 5002 | Server Error | Insert failed |
| 5003 | Server Error | Query failed |
| 5004 | Server Error | Delete failed |
| 5005 | Server Error | Delete execution failed |
| 5006 | Server Error | Detail query failed |

---

## 🎯 Next Steps

After successful deployment:

1. ✅ Test thoroughly with Android app
2. ✅ Add admin panel to view/approve applications
3. ✅ Add email notifications when applications are submitted
4. ✅ Add push notifications for approval/rejection
5. ✅ Add analytics/reporting features

---

## 📜 License

This backend module is part of the TopGrade Education Management System.

---

**Last Updated:** November 2025  
**Version:** 1.0.0  
**Compatibility:** TopGrade Android App v3.x+

