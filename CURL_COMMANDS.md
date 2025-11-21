# cURL Commands for Date Sheet API

## 1. Load Date Sheet API (Main Call)

### Endpoint
```
POST https://topgradesoftware.com/api.php?page=parent/load_datesheet
```

### Parameters Being Sent

The app sends the following parameters in the JSON body:

1. **campus_id** (required) - Campus ID from Paper storage
2. **student_id** (required) - Selected child/student ID
3. **exam_session_id** (optional) - Selected exam session ID (if available)
4. **parent_id** (optional) - Parent ID from Paper storage
5. **session_id** (optional) - Current session ID from Constant.current_session
6. **student_class_id** (required) - Student's class ID from student profile

### cURL Command Format

```bash
curl --location 'https://topgradesoftware.com/api.php?page=parent%2Fload_datesheet' \
--header 'Content-Type: application/json' \
--header 'Cookie: PHPSESSID=YOUR_SESSION_ID' \
--data '{
  "campus_id": "YOUR_CAMPUS_ID",
  "student_id": "YOUR_STUDENT_ID",
  "exam_session_id": "YOUR_EXAM_SESSION_ID",
  "parent_id": "YOUR_PARENT_ID",
  "session_id": "YOUR_SESSION_ID",
  "student_class_id": "YOUR_STUDENT_CLASS_ID"
}'
```

### Example with Actual Values

Replace the following placeholders with actual values from your app:
- `YOUR_CAMPUS_ID` - Value from `Paper.book().read("campus_id", "")`
- `YOUR_STUDENT_ID` - Value from `selectedChildId` (selected student's unique_id)
- `YOUR_EXAM_SESSION_ID` - Value from `selectedExamSession` (selected exam session's unique_id)
- `YOUR_PARENT_ID` - Value from `Paper.book().read("parent_id", "")`
- `YOUR_SESSION_ID` - Value from `Constant.current_session`
- `YOUR_STUDENT_CLASS_ID` - Value from `selectedStudentClassId` (from student profile)
- `YOUR_SESSION_ID` (Cookie) - PHP session ID if needed

### Example cURL Command

```bash
curl --location 'https://topgradesoftware.com/api.php?page=parent%2Fload_datesheet' \
--header 'Content-Type: application/json' \
--header 'Cookie: PHPSESSID=b6hdu650vmd3s048s8f6peibrl' \
--data '{
  "campus_id": "5c67f03e5c3da",
  "student_id": "6876c4336e129",
  "exam_session_id": "692022db7e6d0",
  "parent_id": "6876c4336beb4",
  "session_id": "CURRENT_SESSION_ID",
  "student_class_id": "67efd871861be"
}'
```

## 2. Load Exam Session API

### Endpoint
```
POST https://topgradesoftware.com/api.php?page=parent/load_exam_session
```

### Parameters Being Sent

The app sends only:
1. **parent_id** (required) - Campus ID (stored as parent_id in Paper)

### cURL Command Format

```bash
curl --location 'https://topgradesoftware.com/api.php?page=parent%2Fload_exam_session' \
--header 'Content-Type: application/json' \
--header 'Cookie: PHPSESSID=YOUR_SESSION_ID' \
--data '{
  "parent_id": "YOUR_CAMPUS_ID"
}'
```

### Example cURL Command

```bash
curl --location 'https://topgradesoftware.com/api.php?page=parent%2Fload_exam_session' \
--header 'Content-Type: application/json' \
--header 'Cookie: PHPSESSID=b6hdu650vmd3s048s8f6peibrl' \
--data '{
  "parent_id": "5c67f03e5c3da"
}'
```

## 3. Alternative Date Sheet Call (Fallback)

If the main call fails, the app tries an alternative call without `student_class_id`:

### Parameters
1. **student_id** (required)
2. **campus_id** (required)
3. **exam_session_id** (required)
4. **parent_id** (optional)
5. **session_id** (optional)

### cURL Command Format

```bash
curl --location 'https://topgradesoftware.com/api.php?page=parent%2Fload_datesheet' \
--header 'Content-Type: application/json' \
--header 'Cookie: PHPSESSID=YOUR_SESSION_ID' \
--data '{
  "student_id": "YOUR_STUDENT_ID",
  "campus_id": "YOUR_CAMPUS_ID",
  "exam_session_id": "YOUR_EXAM_SESSION_ID",
  "parent_id": "YOUR_PARENT_ID",
  "session_id": "YOUR_SESSION_ID"
}'
```

## Notes

1. **All parameters are sent in JSON format** in the request body
2. **Content-Type header** must be `application/json`
3. **Cookie header** may be required for session management (PHPSESSID)
4. **URL encoding**: The `%2F` in the URL is the encoded form of `/` (parent/load_datesheet)
5. **Dynamic filtering**: The app now accepts all data from the API response without client-side filtering

## How to Get Actual Values

To see the actual values being sent, check the Logcat output with filter `StudentDateSheet`. The logs show:
- All parameter values
- JSON body being sent
- Full URL
- Response details

Look for these log tags:
- `[DEBUG] JSON Body being sent:`
- `[DEBUG] Added campus_id:`
- `[DEBUG] Added student_id:`
- etc.

