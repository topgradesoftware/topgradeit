# 🚀 Backend Deployment Guide - TopGrade Leave Application

## 📋 Overview

This guide will help you deploy the Leave Application backend to fix the **HTTP 500 error** you're experiencing.

**Current Issue:**
- App returns: `Server error (500): Please contact administrator`
- API Endpoint: `https://topgradesoftware.com/api.php?page=teacher/leave_applicaton`

---

## 📁 Files to Upload

You need to upload these files to your server at `https://topgradesoftware.com/`:

```
your_server_root/
├── includes/
│   └── db_connect.php          ← Upload this
├── teacher/
│   └── leave_applicaton.php    ← Upload this
└── sql/
    └── leave_applications_table.sql  ← Run this in your database
```

---

## 🔧 Step-by-Step Deployment

### Step 1: Configure Database Connection

1. Open `backend/includes/db_connect.php`
2. Update these lines with your actual database credentials:

```php
$db_config = [
    'host'     => 'localhost',           // Your MySQL host
    'username' => 'your_actual_username', // Your MySQL username
    'password' => 'your_actual_password', // Your MySQL password
    'database' => 'your_actual_dbname',   // Your database name
    'charset'  => 'utf8mb4',
    'port'     => 3306
];
```

**Where to find your credentials:**
- Check your hosting control panel (cPanel, Plesk, etc.)
- Look for "MySQL Databases" or "Database Management"
- Or check your existing backend files for database credentials

---

### Step 2: Create Database Table

1. **Access your database** via phpMyAdmin or MySQL command line
2. **Run the SQL script** from `backend/sql/leave_applications_table.sql`

**Option A: Using phpMyAdmin**
   - Login to phpMyAdmin
   - Select your database
   - Click "SQL" tab
   - Copy and paste the contents of `leave_applications_table.sql`
   - Click "Go"

**Option B: Using MySQL Command Line**
   ```bash
   mysql -u your_username -p your_database < leave_applications_table.sql
   ```

**Option C: Using cPanel File Manager**
   - Upload the SQL file
   - Use phpMyAdmin import feature

---

### Step 3: Upload Backend Files

**Using FTP/SFTP (FileZilla, WinSCP, etc.):**

1. **Connect to your server:**
   - Host: `topgradesoftware.com` or your server IP
   - Username: Your FTP username
   - Password: Your FTP password
   - Port: 21 (FTP) or 22 (SFTP)

2. **Upload files:**
   ```
   Upload: backend/includes/db_connect.php
   To: /public_html/includes/db_connect.php
   
   Upload: backend/teacher/leave_applicaton.php
   To: /public_html/teacher/leave_applicaton.php
   ```

**Using cPanel File Manager:**

1. Login to cPanel
2. Open "File Manager"
3. Navigate to `public_html` (or your web root)
4. Create folders if they don't exist:
   - `includes/`
   - `teacher/`
5. Upload the files to respective folders
6. Set file permissions to **644** (or **755** if needed)

---

### Step 4: Verify File Paths

Your server structure should look like this:

```
/public_html/                          (or /var/www/html/)
├── api.php                            ← Should already exist
├── includes/
│   └── db_connect.php                 ← NEW FILE
├── teacher/
│   └── leave_applicaton.php           ← NEW FILE
```

**Important:** The path in `leave_applicaton.php` references:
```php
require_once __DIR__ . '/../../includes/db_connect.php';
```

This means if `leave_applicaton.php` is at:
- `/public_html/teacher/leave_applicaton.php`

Then `db_connect.php` should be at:
- `/public_html/includes/db_connect.php`

**Adjust the path if your structure is different!**

---

## 🧪 Testing

### Test 1: Check if file is accessible

Visit in browser:
```
https://topgradesoftware.com/teacher/leave_applicaton.php
```

**Expected:** You should see an error like:
```json
{
  "status": {
    "code": "4000",
    "message": "Invalid JSON input or empty request body"
  }
}
```

This is **GOOD** - it means the file is working!

If you see a blank page or 500 error, check error logs.

---

### Test 2: Submit from Android App

1. Open your TopGrade app
2. Login as staff
3. Go to Leave Application
4. Fill the form:
   - Category: Any (e.g., "Sick Leave")
   - Subject: Test
   - Body: Testing
   - Dates: Select any dates
5. Click Submit → Submit to System

**Expected Result:**
- ✅ Success message: "Leave application submitted successfully"
- App closes and returns to previous screen

---

## 🐛 Troubleshooting

### Issue 1: Still Getting 500 Error

**Solution:** Check error logs

1. **Enable error logging in PHP file:**
   ```php
   // In leave_applicaton.php, change line 7:
   ini_set('display_errors', 1); // Enable temporarily
   ```

2. **Check server error log:**
   - cPanel: `Error Log` icon
   - Or file: `/public_html/error_log`
   - Look for recent errors related to leave_applicaton.php

3. **Check app-created log:**
   - File: `/public_html/error_log.txt`
   - This contains detailed error messages

---

### Issue 2: Database Connection Error

**Symptoms:**
```json
{
  "status": {
    "code": "5000",
    "message": "Database connection failed"
  }
}
```

**Solutions:**

1. **Verify credentials in `db_connect.php`:**
   - Correct username?
   - Correct password?
   - Correct database name?
   - Correct host? (usually `localhost`, sometimes `127.0.0.1` or a remote host)

2. **Test database connection separately:**
   Create a test file: `test_db.php`
   ```php
   <?php
   $conn = new mysqli('localhost', 'username', 'password', 'database');
   if ($conn->connect_error) {
       die("Failed: " . $conn->connect_error);
   }
   echo "Connected successfully!";
   ?>
   ```

3. **Check if database user has permissions:**
   ```sql
   GRANT ALL PRIVILEGES ON your_database.* TO 'your_username'@'localhost';
   FLUSH PRIVILEGES;
   ```

---

### Issue 3: Table Doesn't Exist

**Symptoms:**
```
Table 'database.leave_applications' doesn't exist
```

**Solution:**
- Run the SQL script again from `leave_applications_table.sql`
- Make sure you selected the correct database
- Check table name matches exactly (case-sensitive on Linux servers!)

---

### Issue 4: Permission Denied

**Symptoms:**
```
Warning: require_once(...): failed to open stream: Permission denied
```

**Solution:**
1. Set correct file permissions:
   ```bash
   chmod 644 backend/teacher/leave_applicaton.php
   chmod 644 backend/includes/db_connect.php
   ```

2. Or via cPanel File Manager:
   - Right-click file → Change Permissions
   - Set to 644 or 755

---

### Issue 5: Wrong API Endpoint

If your `api.php` handles routing differently, you may need to modify how it includes the file.

**Check your existing `api.php`** file to see how other pages are loaded:

Example pattern:
```php
// In api.php
$page = $_GET['page'] ?? '';

if ($page === 'teacher/leave_applicaton') {
    require_once __DIR__ . '/teacher/leave_applicaton.php';
    exit;
}
```

---

## 📊 Verify Deployment

### Checklist:

- [ ] Database credentials configured in `db_connect.php`
- [ ] SQL table created (`leave_applications`)
- [ ] `db_connect.php` uploaded to `/includes/`
- [ ] `leave_applicaton.php` uploaded to `/teacher/`
- [ ] File permissions set correctly (644 or 755)
- [ ] Test endpoint returns JSON (not 500 error)
- [ ] Android app successfully submits leave application

---

## 🔐 Security Notes

1. **Protect sensitive files:**
   ```apache
   # Add to .htaccess in /includes/ folder
   <Files "db_connect.php">
       Order Allow,Deny
       Deny from all
   </Files>
   ```

2. **Disable error display in production:**
   ```php
   // In leave_applicaton.php, line 7:
   ini_set('display_errors', 0); // Set to 0 in production
   ```

3. **Use prepared statements:** (Already implemented ✅)
   - All queries use prepared statements
   - Prevents SQL injection attacks

4. **Validate input:** (Already implemented ✅)
   - All inputs are validated and sanitized

---

## 📞 Support

If you continue experiencing issues:

1. **Collect these details:**
   - Error message from Android app
   - Server error log entries
   - PHP version (from cPanel or phpinfo())
   - MySQL version
   - Exact file paths on server

2. **Enable detailed logging:**
   ```php
   // Add to top of leave_applicaton.php
   error_reporting(E_ALL);
   ini_set('display_errors', 1);
   ini_set('log_errors', 1);
   ```

3. **Check Android app logs:**
   ```bash
   adb logcat -s StaffAddApplication:* -v time
   ```

---

## ✅ Success Indicators

When everything is working correctly:

1. **Android app shows:**
   ```
   "Leave application submitted successfully"
   ```

2. **Database contains the record:**
   ```sql
   SELECT * FROM leave_applications ORDER BY id DESC LIMIT 1;
   ```

3. **No 500 errors in logs**

4. **API returns:**
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

---

## 🎯 Quick Reference

**File Locations:**
- Backend PHP: `/public_html/teacher/leave_applicaton.php`
- DB Config: `/public_html/includes/db_connect.php`
- Error Log: `/public_html/error_log.txt`

**Database:**
- Table: `leave_applications`
- Required fields: `campus_id`, `staff_id`, `application_title`, `application_body`, `start_date`, `end_date`

**API Endpoint:**
- URL: `https://topgradesoftware.com/api.php?page=teacher/leave_applicaton`
- Method: POST
- Content-Type: application/json

**App Request:**
```json
{
  "operation": "add_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_title": "Subject here",
  "applictaion_body": "Body here",
  "start_date": "25/11/2025",
  "end_date": "28/11/2025"
}
```

---

**Good luck with your deployment! 🚀**

