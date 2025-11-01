# Timetable Loading Issue - Troubleshooting Guide

## 🔍 **Issue Analysis: Why Timetable is Not Loading**

Based on the code analysis, I've identified several potential issues that could prevent the timetable from loading properly:

## 🚨 **Common Issues Found:**

### **1. API Endpoint Mismatch**
```java
// Current API call in StudentTimeTable.java
topgrade.parent.com.parentseeks.Teacher.Utils.Constant.mApiService.loadStudentTimetable(body)
```

**Problem**: The API endpoint might not be correctly configured or the PHP backend might not be responding properly.

### **2. Parameter Issues**
```java
// Request parameters being sent
postParam.put("student_id", studentId);
postParam.put("parent_id", parentIdParam);
postParam.put("timetable_session_id", sessionId);
```

**Potential Issues**:
- `studentId` might be null or empty
- `parentIdParam` might be incorrect
- `sessionId` might not be properly selected

### **3. Response Model Mismatch**
The API response might not match the expected `StudentTimetableResponse` model structure.

### **4. Network/Server Issues**
- Server might be down
- Network connectivity issues
- API endpoint might have changed

## 🔧 **Immediate Fixes to Implement:**

### **Fix 1: Enhanced Error Logging**
```java
private void loadTimetable(String sessionId) {
    // Add comprehensive logging
    Log.d(TAG, "=== TIMETABLE LOADING DEBUG ===");
    Log.d(TAG, "Session ID: " + sessionId);
    Log.d(TAG, "Student: " + (student != null ? student.getFullName() : "NULL"));
    Log.d(TAG, "Selected Child ID: " + selectedChildId);
    
    // Validate parameters
    if (student == null) {
        Log.e(TAG, "Student is null!");
        Toast.makeText(context, "Student data not available", Toast.LENGTH_SHORT).show();
        return;
    }
    
    if (sessionId == null || sessionId.isEmpty()) {
        Log.e(TAG, "Session ID is null or empty!");
        Toast.makeText(context, "Please select a session", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Continue with existing code...
}
```

### **Fix 2: API Response Validation**
```java
@Override
public void onResponse(Call<StudentTimetableResponse> call, Response<StudentTimetableResponse> response) {
    progressBar.setVisibility(View.GONE);
    
    Log.d(TAG, "=== API RESPONSE DEBUG ===");
    Log.d(TAG, "Response Code: " + response.code());
    Log.d(TAG, "Response Body: " + (response.body() != null ? "NOT NULL" : "NULL"));
    
    if (response.errorBody() != null) {
        try {
            String errorBody = response.errorBody().string();
            Log.e(TAG, "Error Body: " + errorBody);
        } catch (IOException e) {
            Log.e(TAG, "Error reading error body", e);
        }
    }
    
    if (response.body() != null) {
        Log.d(TAG, "Status Code: " + response.body().getStatus().getCode());
        Log.d(TAG, "Status Message: " + response.body().getStatus().getMessage());
        
        if (response.body().getStatus().getCode().equals("1000")) {
            // Success case
            handleSuccessfulResponse(response.body());
        } else {
            // API error
            String errorMessage = response.body().getStatus().getMessage();
            Log.e(TAG, "API Error: " + errorMessage);
            Toast.makeText(context, "API Error: " + errorMessage, Toast.LENGTH_LONG).show();
        }
    } else {
        // Null response body
        Log.e(TAG, "Response body is null!");
        Toast.makeText(context, "Server error: No response received", Toast.LENGTH_LONG).show();
    }
}
```

### **Fix 3: Parameter Validation**
```java
private void validateAndLoadTimetable(String sessionId) {
    // Validate all required parameters
    if (student == null) {
        Toast.makeText(context, "Student information not available", Toast.LENGTH_SHORT).show();
        return;
    }
    
    String studentId = selectedChildId != null && !selectedChildId.isEmpty() ? 
                      selectedChildId : student.getUniqueId();
    
    if (studentId == null || studentId.isEmpty()) {
        Toast.makeText(context, "Student ID not available", Toast.LENGTH_SHORT).show();
        return;
    }
    
    String parentIdParam = student.getParentId() != null ? 
                          student.getParentId() : Paper.book().read("parent_id", "");
    
    if (parentIdParam == null || parentIdParam.isEmpty()) {
        Toast.makeText(context, "Parent ID not available", Toast.LENGTH_SHORT).show();
        return;
    }
    
    if (sessionId == null || sessionId.isEmpty()) {
        Toast.makeText(context, "Session not selected", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // All validations passed, proceed with loading
    loadTimetable(sessionId);
}
```

## 🛠️ **Step-by-Step Troubleshooting:**

### **Step 1: Check API Endpoint**
1. Verify the API endpoint is correct: `api.php?page=parent/load_timetable`
2. Check if the server is responding
3. Test with a simple API call

### **Step 2: Validate Parameters**
1. Ensure `student_id` is not null/empty
2. Ensure `parent_id` is correct
3. Ensure `timetable_session_id` is selected

### **Step 3: Check Response Format**
1. Verify the API response matches `StudentTimetableResponse` model
2. Check if the response contains the expected data structure

### **Step 4: Network Debugging**
1. Check internet connectivity
2. Verify API base URL is correct
3. Check for any network timeouts

## 🔍 **Debug Information to Collect:**

### **Request Parameters Log:**
```java
Log.d(TAG, "Request Parameters:");
Log.d(TAG, "- student_id: " + studentId);
Log.d(TAG, "- parent_id: " + parentIdParam);
Log.d(TAG, "- timetable_session_id: " + sessionId);
```

### **Response Data Log:**
```java
Log.d(TAG, "Response Data:");
Log.d(TAG, "- Status Code: " + response.code());
Log.d(TAG, "- Response Body: " + (response.body() != null ? "Available" : "Null"));
if (response.body() != null) {
    Log.d(TAG, "- API Status: " + response.body().getStatus().getCode());
    Log.d(TAG, "- API Message: " + response.body().getStatus().getMessage());
}
```

## 🚀 **Quick Fix Implementation:**

### **Enhanced StudentTimeTable.java:**
```java
private void loadTimetable(String sessionId) {
    Log.d(TAG, "=== TIMETABLE LOADING STARTED ===");
    
    // Validate inputs
    if (!validateTimetableInputs(sessionId)) {
        return;
    }
    
    // Prepare request
    HashMap<String, String> postParam = new HashMap<>();
    String studentId = selectedChildId != null && !selectedChildId.isEmpty() ? 
                      selectedChildId : student.getUniqueId();
    String parentIdParam = student.getParentId() != null ? 
                          student.getParentId() : Paper.book().read("parent_id", "");
    
    postParam.put("student_id", studentId);
    postParam.put("parent_id", parentIdParam);
    postParam.put("timetable_session_id", sessionId);
    
    // Log request
    Log.d(TAG, "Request JSON: " + new JSONObject(postParam).toString());
    
    progressBar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), 
                                        new JSONObject(postParam).toString());
    
    // Make API call with enhanced error handling
    Constant.mApiService.loadStudentTimetable(body).enqueue(new Callback<StudentTimetableResponse>() {
        @Override
        public void onResponse(Call<StudentTimetableResponse> call, Response<StudentTimetableResponse> response) {
            handleTimetableResponse(response);
        }
        
        @Override
        public void onFailure(Call<StudentTimetableResponse> call, Throwable t) {
            handleTimetableFailure(t);
        }
    });
}

private boolean validateTimetableInputs(String sessionId) {
    if (student == null) {
        Toast.makeText(context, "Student data not available", Toast.LENGTH_SHORT).show();
        return false;
    }
    
    if (sessionId == null || sessionId.isEmpty()) {
        Toast.makeText(context, "Please select a session", Toast.LENGTH_SHORT).show();
        return false;
    }
    
    return true;
}

private void handleTimetableResponse(Response<StudentTimetableResponse> response) {
    progressBar.setVisibility(View.GONE);
    
    Log.d(TAG, "Response Code: " + response.code());
    
    if (response.body() != null && response.body().getStatus().getCode().equals("1000")) {
        // Success case
        list = response.body().getTimetable();
        updateTimetableUI();
    } else {
        // Error case
        String errorMessage = response.body() != null ? 
                             response.body().getStatus().getMessage() : 
                             "Server error: " + response.code();
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }
}

private void handleTimetableFailure(Throwable t) {
    progressBar.setVisibility(View.GONE);
    Log.e(TAG, "Timetable loading failed", t);
    Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
}
```

## 📋 **Checklist for Resolution:**

- [ ] **API Endpoint**: Verify `api.php?page=parent/load_timetable` is accessible
- [ ] **Parameters**: Ensure all required parameters are provided
- [ ] **Session Selection**: Verify session is properly selected
- [ ] **Student Data**: Confirm student information is available
- [ ] **Network**: Check internet connectivity
- [ ] **Server**: Verify backend server is running
- [ ] **Response Format**: Confirm API response matches expected model

## 🎯 **Expected Behavior After Fix:**

1. **Proper Error Messages**: Clear indication of what's wrong
2. **Successful Loading**: Timetable displays correctly
3. **Debug Information**: Detailed logs for troubleshooting
4. **User Feedback**: Appropriate messages for different scenarios

## 🔧 **Next Steps:**

1. **Implement the enhanced error handling**
2. **Add comprehensive logging**
3. **Test with different scenarios**
4. **Monitor the logs for specific issues**
5. **Verify API endpoint functionality**

This troubleshooting guide should help identify and resolve the timetable loading issue. The enhanced error handling and logging will provide clear information about what's causing the problem.
