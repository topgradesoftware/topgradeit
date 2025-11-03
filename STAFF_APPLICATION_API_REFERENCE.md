# Staff Application Submission API Reference

## 📋 API Details

### **Endpoint**
```
POST https://topgradesoftware.com/api.php?page=teacher/leave_applicaton
```

### **Content-Type**
```
application/json
```

---

## 📝 Request Parameters

### **Required Fields**

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `operation` | String | Operation type (always "add_application") | `"add_application"` |
| `campus_id` | String | Campus ID from login | `"5c67f03e5c3da"` |
| `staff_id` | String | Staff unique ID from login | `"6876c43fd910b"` |
| `application_title` | String | Subject/Title of the application | `"Medical Leave Request"` |
| `applictaion_body` | String | Detailed description (note typo in API) | `"I need leave for..."` |
| `start_date` | String | Start date (DD/MM/YYYY format) | `"04/11/2025"` |
| `end_date` | String | End date (DD/MM/YYYY format) | `"06/11/2025"` |

**⚠️ Important Note:** The API parameter is `applictaion_body` (with typo), NOT `application_body`

---

## 🔧 cURL Command Examples

### **Example 1: Medical Leave**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{
  "operation": "add_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_title": "Medical Leave Request",
  "applictaion_body": "I am requesting leave due to medical reasons. I will be visiting the doctor and need time for recovery.",
  "start_date": "04/11/2025",
  "end_date": "06/11/2025"
}'
```

### **Example 2: Personal Leave**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{
  "operation": "add_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_title": "Personal Leave",
  "applictaion_body": "I need to attend a family function and request leave for the mentioned dates.",
  "start_date": "10/11/2025",
  "end_date": "12/11/2025"
}'
```

### **Example 3: Emergency Leave**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{
  "operation": "add_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_title": "Emergency Leave",
  "applictaion_body": "Urgent family matter that requires immediate attention.",
  "start_date": "05/11/2025",
  "end_date": "05/11/2025"
}'
```

### **Windows CMD Format** (with escape characters)
```batch
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" ^
-H "Content-Type: application/json" ^
-d "{\"operation\":\"add_application\",\"campus_id\":\"5c67f03e5c3da\",\"staff_id\":\"6876c43fd910b\",\"application_title\":\"Medical Leave\",\"applictaion_body\":\"Need leave for medical checkup\",\"start_date\":\"04/11/2025\",\"end_date\":\"06/11/2025\"}" ^
-v
```

---

## ✅ Success Response

### **HTTP Status Code**
```
200 OK
```

### **Response Body Structure**
```json
{
  "status": {
    "code": "1000",
    "message": "Application submitted successfully"
  },
  "data": {
    // Application details
  }
}
```

### **Success Indicators**
- HTTP Status: `200`
- Status Code: `"1000"` or `"success"`
- Status Message: Success message

---

## ❌ Error Responses

### **Common Errors**

#### 1. **Missing Required Fields**
```json
{
  "status": {
    "code": "1001",
    "message": "Missing required parameters"
  }
}
```

#### 2. **Invalid Campus ID**
```json
{
  "status": {
    "code": "1002",
    "message": "Invalid campus_id"
  }
}
```

#### 3. **Invalid Staff ID**
```json
{
  "status": {
    "code": "1003",
    "message": "Invalid staff_id"
  }
}
```

#### 4. **Invalid Date Format**
```json
{
  "status": {
    "code": "1004",
    "message": "Invalid date format. Use DD/MM/YYYY"
  }
}
```

#### 5. **Server Error**
```
HTTP 500 Internal Server Error
```

---

## 📱 Android Implementation Reference

### **From StaffAddApplication.java**
```java
// Create JSON body
JSONObject jsonBody = new JSONObject();
jsonBody.put("operation", "add_application");
jsonBody.put("campus_id", Constant.campus_id);
jsonBody.put("staff_id", Constant.staff_id);
jsonBody.put("application_title", subject);
jsonBody.put("applictaion_body", body);  // Note: typo in API
jsonBody.put("start_date", startDateStr);
jsonBody.put("end_date", endDateStr);

// Create request
RequestBody requestBody = RequestBody.create(
    MediaType.parse("application/json"), 
    jsonBody.toString()
);

// Make API call
Call<StaffApplicationModel> call = apiService.leave_applicaton(requestBody);
```

---

## 🔍 Testing with Verbose Output

### **Add -v flag for detailed debugging**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{"operation":"add_application","campus_id":"5c67f03e5c3da","staff_id":"6876c43fd910b","application_title":"Test","applictaion_body":"Testing","start_date":"04/11/2025","end_date":"04/11/2025"}' \
-v
```

### **What to look for in verbose output:**
- `> POST /api.php?page=teacher/leave_applicaton HTTP/2` - Request sent
- `> Content-Type: application/json` - Header sent correctly
- `< HTTP/2 200` - Success response
- Response body with status code

---

## 🧪 Quick Test Commands

### **1. Quick Test (One-liner)**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" -H "Content-Type: application/json" -d '{"operation":"add_application","campus_id":"5c67f03e5c3da","staff_id":"6876c43fd910b","application_title":"Test Leave","applictaion_body":"Testing API","start_date":"04/11/2025","end_date":"04/11/2025"}'
```

### **2. Test with Response Saved to File**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{"operation":"add_application","campus_id":"5c67f03e5c3da","staff_id":"6876c43fd910b","application_title":"Test","applictaion_body":"Test","start_date":"04/11/2025","end_date":"04/11/2025"}' \
-o response.json
```

### **3. Test with Full Headers and Response**
```bash
curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" \
-H "Content-Type: application/json" \
-d '{"operation":"add_application","campus_id":"5c67f03e5c3da","staff_id":"6876c43fd910b","application_title":"Test","applictaion_body":"Test","start_date":"04/11/2025","end_date":"04/11/2025"}' \
-i
```

---

## 📊 Field Validation Rules

### **Date Format**
- **Required Format:** `DD/MM/YYYY`
- **Examples:** `04/11/2025`, `31/12/2025`
- **Invalid:** `2025-11-04`, `11/04/2025` (MM/DD/YYYY)

### **Date Range**
- `end_date` must be >= `start_date`
- Both dates are required
- Dates should not be in the past (server-side validation)

### **Text Fields**
- **application_title:** 
  - Min length: 3 characters
  - Max length: 255 characters
  - Cannot be empty
  
- **applictaion_body:** 
  - Min length: 10 characters
  - Max length: 1000 characters
  - Cannot be empty

### **IDs**
- **campus_id:** Must exist in database
- **staff_id:** Must exist and be active
- Format: Alphanumeric string

---

## 🎯 Common Use Cases

### **1. Single Day Leave**
```json
{
  "start_date": "04/11/2025",
  "end_date": "04/11/2025"
}
```

### **2. Multiple Days Leave**
```json
{
  "start_date": "04/11/2025",
  "end_date": "08/11/2025"
}
```

### **3. Weekend Inclusive Leave**
```json
{
  "start_date": "08/11/2025",
  "end_date": "11/11/2025"
}
```

---

## 🔒 Security Notes

### **Authentication**
- This API uses `campus_id` and `staff_id` for authentication
- No JWT or session token required
- Ensure these IDs are kept secure

### **Data Validation**
- Always validate dates on client-side before sending
- Sanitize input to prevent SQL injection
- Use HTTPS for all API calls

---

## 📝 Related Operations

### **Read Applications**
```json
{
  "operation": "read_application_title",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b"
}
```

### **Update Application**
```json
{
  "operation": "update_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_id": "APPLICATION_ID",
  "application_title": "Updated Title",
  "applictaion_body": "Updated body"
}
```

### **Delete Application**
```json
{
  "operation": "delete_application",
  "campus_id": "5c67f03e5c3da",
  "staff_id": "6876c43fd910b",
  "application_id": "APPLICATION_ID"
}
```

---

## 📞 Support & Troubleshooting

### **Common Issues**

1. **"Invalid campus_id"**
   - Verify campus_id from login response
   - Ensure it's stored correctly in Paper DB

2. **"Invalid staff_id"**
   - Check staff_id from login response
   - Verify staff is active in system

3. **"Invalid date format"**
   - Use DD/MM/YYYY format only
   - Ensure dates are zero-padded (e.g., `04/11/2025` not `4/11/2025`)

4. **HTTP 500 Error**
   - Check server logs
   - Verify database connection
   - Check PHP error logs

---

## 🎉 Summary

**API Endpoint:** `POST https://topgradesoftware.com/api.php?page=teacher/leave_applicaton`

**Required Parameters:**
- ✅ `operation` = "add_application"
- ✅ `campus_id` (from login)
- ✅ `staff_id` (from login)
- ✅ `application_title`
- ✅ `applictaion_body` (note typo)
- ✅ `start_date` (DD/MM/YYYY)
- ✅ `end_date` (DD/MM/YYYY)

**Success Response:** `status.code = "1000"`

**Test File:** `test_staff_application_submit.bat`

---

**Created:** November 3, 2025  
**Last Updated:** November 3, 2025  
**Version:** 1.0

