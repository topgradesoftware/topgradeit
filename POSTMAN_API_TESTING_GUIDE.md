# 📮 Postman API Testing Guide - Load Diary API

## 🎯 API Endpoint Information

**Endpoint:** `api.php?page=parent/load_diary`  
**Base URL:** `https://topgradesoftware.com/`  
**Full URL:** `https://topgradesoftware.com/api.php?page=parent/load_diary`  
**Method:** `POST`  
**Content-Type:** `application/json`

---

## 📋 Step-by-Step Postman Setup

### Step 1: Create a New Request

1. Open **Postman**
2. Click **"New"** → **"HTTP Request"**
3. Name it: `Load Diary - Parent`

### Step 2: Configure Request Method and URL

1. Set method to **POST** (dropdown on the left)
2. Enter the URL:
   ```
   https://topgradesoftware.com/api.php?page=parent/load_diary
   ```

### Step 3: Set Headers

1. Go to the **"Headers"** tab
2. Add the following header:
   - **Key:** `Content-Type`
   - **Value:** `application/json`

   Or click **"Presets"** → **"Content-Type: application/json"**

### Step 4: Set Request Body

1. Go to the **"Body"** tab
2. Select **"raw"** radio button
3. Select **"JSON"** from the dropdown (on the right)
4. Paste the following JSON:

```json
{
  "parent_parent_id": "YOUR_PARENT_PARENT_ID",
  "parent_id": "YOUR_CAMPUS_ID",
  "employee_id": "YOUR_STUDENT_ID",
  "date": "2025-11-19",
  "date2": "2025-11-19"
}
```

### Step 5: Replace Placeholder Values

Replace the placeholder values with actual data:

- **`parent_parent_id`**: Your actual parent ID (e.g., `"6876c43fd910b"`)
- **`parent_id`**: Your campus ID (e.g., `"5c67f03e5c3da"`)
- **`employee_id`**: The student/child ID (e.g., `"6876c4336beb4"`)
- **`date`**: Date in format `YYYY-MM-DD` (e.g., `"2025-11-19"`)
- **`date2`**: Same date as `date` (e.g., `"2025-11-19"`)

**Example with real values:**
```json
{
  "parent_parent_id": "6876c43fd910b",
  "parent_id": "5c67f03e5c3da",
  "employee_id": "6876c4336beb4",
  "date": "2025-11-19",
  "date2": "2025-11-19"
}
```

### Step 6: Send the Request

1. Click the **"Send"** button
2. Wait for the response

---

## ✅ Expected Response Format

### Success Response (Status Code: 200)

```json
{
  "status": {
    "code": "1000",
    "message": "Success"
  },
  "title": {
    "0": {
      "subject_name": "Mathematics",
      "diary_entry": "Today we learned about algebra...",
      "created_date": "2025-11-19"
    }
  }
}
```

### Error Response Examples

**Missing Required Fields:**
```json
{
  "status": {
    "code": "400",
    "message": "Missing required fields"
  }
}
```

**No Data Found:**
```json
{
  "status": {
    "code": "1001",
    "message": "No records found"
  }
}
```

---

## 🔍 Understanding the Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `parent_parent_id` | The actual parent user ID | `"6876c43fd910b"` |
| `parent_id` | The campus/school ID (confusing naming!) | `"5c67f03e5c3da"` |
| `employee_id` | The student/child ID | `"6876c4336beb4"` |
| `date` | Start date for diary entries (YYYY-MM-DD) | `"2025-11-19"` |
| `date2` | End date for diary entries (YYYY-MM-DD) | `"2025-11-19"` |

**Note:** Based on the codebase, `parent_id` in this API actually represents the `campus_id`, not the parent ID. The actual parent ID is `parent_parent_id`.

---

## 🛠️ Troubleshooting

### Issue: "400 Bad Request"
- **Solution:** Check that all required fields are present and have valid values
- Verify JSON syntax is correct (no trailing commas, proper quotes)

### Issue: "500 Internal Server Error"
- **Solution:** Check server logs or contact backend team
- Verify the API endpoint is accessible

### Issue: "No records found"
- **Solution:** 
  - Verify the `employee_id` (student ID) is correct
  - Check that diary entries exist for the specified date range
  - Ensure the date format is `YYYY-MM-DD`

### Issue: "CORS Error"
- **Solution:** This shouldn't happen with Postman, but if testing from browser, ensure CORS headers are set on the server

---

## 📸 Postman Collection Export

You can save this request as part of a Postman Collection:

1. Click **"Save"** button
2. Create a new collection: `TopGrade APIs`
3. Save the request

### Import/Export Collection

To share with your team:
1. Click **"..."** (three dots) on the collection
2. Select **"Export"**
3. Share the JSON file

---

## 🔄 Testing Different Scenarios

### Test Case 1: Single Date
```json
{
  "parent_parent_id": "6876c43fd910b",
  "parent_id": "5c67f03e5c3da",
  "employee_id": "6876c4336beb4",
  "date": "2025-11-19",
  "date2": "2025-11-19"
}
```

### Test Case 2: Date Range
```json
{
  "parent_parent_id": "6876c43fd910b",
  "parent_id": "5c67f03e5c3da",
  "employee_id": "6876c4336beb4",
  "date": "2025-11-01",
  "date2": "2025-11-30"
}
```

### Test Case 3: Invalid Student ID (Error Testing)
```json
{
  "parent_parent_id": "6876c43fd910b",
  "parent_id": "5c67f03e5c3da",
  "employee_id": "INVALID_ID",
  "date": "2025-11-19",
  "date2": "2025-11-19"
}
```

---

## 📝 Quick Reference: cURL Equivalent

If you prefer using cURL, here's the equivalent command:

```bash
curl -X POST "https://topgradesoftware.com/api.php?page=parent/load_diary" \
  -H "Content-Type: application/json" \
  -d '{
    "parent_parent_id": "YOUR_PARENT_PARENT_ID",
    "parent_id": "YOUR_CAMPUS_ID",
    "employee_id": "YOUR_STUDENT_ID",
    "date": "2025-11-19",
    "date2": "2025-11-19"
  }'
```

---

## 🎓 Additional Tips

1. **Use Environment Variables:** Create a Postman environment with variables like `{{base_url}}`, `{{parent_id}}`, etc., to make testing easier

2. **Save Responses:** Use Postman's "Save Response" feature to keep examples of successful responses

3. **Test Scripts:** Add test scripts in the "Tests" tab to automatically validate responses:
   ```javascript
   pm.test("Status code is 200", function () {
       pm.response.to.have.status(200);
   });
   
   pm.test("Response has status code 1000", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.status.code).to.eql("1000");
   });
   ```

4. **Pre-request Scripts:** Use pre-request scripts to set dynamic values (like current date):
   ```javascript
   const today = new Date().toISOString().split('T')[0];
   pm.environment.set("today", today);
   ```

---

## 📚 Related APIs

Other APIs in the TopGrade system that use similar parameters:

- `api.php?page=parent/load_attendance`
- `api.php?page=parent/load_exam`
- `api.php?page=parent/complain`
- `api.php?page=parent/load_profile`

---

**Last Updated:** 2025-11-19  
**API Version:** Based on TopGrade Android App Codebase

