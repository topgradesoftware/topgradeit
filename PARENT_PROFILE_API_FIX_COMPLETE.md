# Parent Profile API - Complete Fix Guide

## 🔍 Analysis

### PHP Backend Response (From your code)
```json
{
  "status": {
    "code": "1000",
    "message": "Success."
  }
}
```

### Current Android Model Structure
✅ **CORRECT Structure:**
- `GeneralModel` → has `SharedStatus status`
- `SharedStatus` → has `String code` and `String message`

**Mapping:**
```
JSON: { "status": { "code": "1000", "message": "Success" } }
         ↓
GeneralModel.getStatus() → SharedStatus object
         ↓
SharedStatus.getCode() → "1000"
SharedStatus.getMessage() → "Success"
```

---

## ⚠️ Issues Found in PHP Code

### Issue 1: Phone and Landline Commented Out
```php
// Lines 31-32 are commented out
$data = Array(
    'full_name' => $dataa_post['full_name'],
    'email' => $dataa_post['email'],
    // 'phone' => $dataa_post['phone'],      // ❌ COMMENTED
    // 'landline' => $dataa_post['landline'], // ❌ COMMENTED
    'address' => $dataa_post['address']
);
```

**Fix:** Uncomment these lines if you want to update phone and landline:
```php
$data = Array(
    'full_name' => $dataa_post['full_name'],
    'email' => $dataa_post['email'],
    'phone' => $dataa_post['phone'],        // ✅ UNCOMMENTED
    'landline' => $dataa_post['landline'],  // ✅ UNCOMMENTED
    'address' => $dataa_post['address']
);
```

### Issue 2: Database Query Logic Issue
```php
// Line 37 - This logic looks incorrect
$db->where('(parent_id="' . $dataa_post['campus_id'] . '")');
```

Should probably be:
```php
$db->where('campus_id', $dataa_post['campus_id']);
```

Or if parent_id is the field name:
```php
$db->where('parent_id', $dataa_post['campus_id']);
```

---

## ✅ The Android Model is CORRECT

Your `GeneralModel` and `SharedStatus` are already correctly structured! The issue is likely not the model.

---

## 🐛 Debugging Steps

### Step 1: Add Detailed Logging to Edit_ProfileParent.java

Add this enhanced version with better error handling:

```java
private void update_profile() {
    final String phone_ = phone.getText().toString();
    final String email_ = email.getText().toString();
    final String landline_ = landline.getText().toString();
    final String address_ = address.getText().toString();
    final String Name_ = Name.getText().toString();

    // Validate inputs
    if (Name_.isEmpty() || email_.isEmpty()) {
        Toast.makeText(context, "Name and email are required", Toast.LENGTH_SHORT).show();
        return;
    }

    HashMap<String, String> postParam = new HashMap<>();
    postParam.put("parent_id", Constant.parent_id);
    postParam.put("campus_id", Constant.campus_id);
    postParam.put("full_name", Name_);
    postParam.put("email", email_);
    postParam.put("phone", phone_);
    postParam.put("landline", landline_);
    postParam.put("address", address_);

    // 🔍 DEBUG LOG
    Log.d("UPDATE_PROFILE", "╔════════════════════════════════");
    Log.d("UPDATE_PROFILE", "║ API REQUEST");
    Log.d("UPDATE_PROFILE", "╠════════════════════════════════");
    Log.d("UPDATE_PROFILE", "║ Endpoint: api.php?page=parent/update_profile");
    Log.d("UPDATE_PROFILE", "║ Parent ID: " + Constant.parent_id);
    Log.d("UPDATE_PROFILE", "║ Campus ID: " + Constant.campus_id);
    Log.d("UPDATE_PROFILE", "║ Full Name: " + Name_);
    Log.d("UPDATE_PROFILE", "║ Email: " + email_);
    Log.d("UPDATE_PROFILE", "║ Phone: " + phone_);
    Log.d("UPDATE_PROFILE", "║ Landline: " + landline_);
    Log.d("UPDATE_PROFILE", "║ Address: " + address_);
    Log.d("UPDATE_PROFILE", "╚════════════════════════════════");

    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create(
        (new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8")
    );
    
    Constant.mApiService.update_profile_(body).enqueue(new Callback<GeneralModel>() {
        @Override
        public void onResponse(@NonNull Call<GeneralModel> call, 
                             @NonNull Response<GeneralModel> response) {
            
            // 🔍 DEBUG LOG
            Log.d("UPDATE_PROFILE", "╔════════════════════════════════");
            Log.d("UPDATE_PROFILE", "║ API RESPONSE");
            Log.d("UPDATE_PROFILE", "╠════════════════════════════════");
            Log.d("UPDATE_PROFILE", "║ HTTP Code: " + response.code());
            Log.d("UPDATE_PROFILE", "║ HTTP Message: " + response.message());
            Log.d("UPDATE_PROFILE", "║ Is Successful: " + response.isSuccessful());
            
            if (response.body() != null) {
                try {
                    SharedStatus status = response.body().getStatus();
                    Log.d("UPDATE_PROFILE", "║ Response Body: NOT NULL");
                    Log.d("UPDATE_PROFILE", "║ Status Object: " + (status != null ? "NOT NULL" : "NULL"));
                    
                    if (status != null) {
                        String code = status.getCode();
                        String message = status.getMessage();
                        
                        Log.d("UPDATE_PROFILE", "║ Status Code: " + code);
                        Log.d("UPDATE_PROFILE", "║ Status Message: " + message);
                        Log.d("UPDATE_PROFILE", "╚════════════════════════════════");
                        
                        if ("1000".equals(code)) {
                            // ✅ SUCCESS
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            
                            // Update Paper DB
                            Paper.book().write("full_name", Name_);
                            Paper.book().write("email", email_);
                            Paper.book().write("phone", phone_);
                            Paper.book().write("landline", landline_);
                            Paper.book().write("address", address_);
                            
                            Log.d("UPDATE_PROFILE", "✅ Paper DB updated successfully");
                            
                            // If picture was changed, update it
                            if (picture_str != null && !picture_str.isEmpty()) {
                                Log.d("UPDATE_PROFILE", "📸 Updating picture...");
                                update_picture(picture_str);
                            } else {
                                progress_bar.setVisibility(View.GONE);
                                Log.d("UPDATE_PROFILE", "✅ Update complete, closing activity");
                                finish();
                            }
                        } else {
                            // ❌ Error from server
                            progress_bar.setVisibility(View.GONE);
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            Log.e("UPDATE_PROFILE", "❌ Server returned error code: " + code);
                        }
                    } else {
                        // Status object is null
                        progress_bar.setVisibility(View.GONE);
                        Log.e("UPDATE_PROFILE", "❌ Status object is NULL");
                        Log.e("UPDATE_PROFILE", "╚════════════════════════════════");
                        Toast.makeText(context, "Error: Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    progress_bar.setVisibility(View.GONE);
                    Log.e("UPDATE_PROFILE", "❌ Exception parsing response", e);
                    Log.e("UPDATE_PROFILE", "╚════════════════════════════════");
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                // Response body is null
                progress_bar.setVisibility(View.GONE);
                Log.e("UPDATE_PROFILE", "║ Response Body: NULL");
                
                try {
                    if (response.errorBody() != null) {
                        String errorBody = response.errorBody().string();
                        Log.e("UPDATE_PROFILE", "║ Error Body: " + errorBody);
                        Toast.makeText(context, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("UPDATE_PROFILE", "║ Error Body: NULL");
                        Toast.makeText(context, "Error: Empty response", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("UPDATE_PROFILE", "❌ Error reading error body", e);
                }
                
                Log.e("UPDATE_PROFILE", "╚════════════════════════════════");
            }
        }

        @Override
        public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
            progress_bar.setVisibility(View.GONE);
            
            // 🔍 DEBUG LOG
            Log.e("UPDATE_PROFILE", "╔════════════════════════════════");
            Log.e("UPDATE_PROFILE", "║ API FAILURE");
            Log.e("UPDATE_PROFILE", "╠════════════════════════════════");
            Log.e("UPDATE_PROFILE", "║ Error Type: " + e.getClass().getSimpleName());
            Log.e("UPDATE_PROFILE", "║ Error Message: " + e.getMessage());
            Log.e("UPDATE_PROFILE", "╚════════════════════════════════");
            Log.e("UPDATE_PROFILE", "Stack Trace:", e);
            
            // User-friendly error messages
            String errorMsg;
            if (e instanceof com.google.gson.JsonSyntaxException) {
                errorMsg = "Server response format error. Please contact support.";
                Log.e("UPDATE_PROFILE", "💥 JSON SYNTAX ERROR - Server not returning expected format!");
            } else if (e instanceof com.google.gson.JsonIOException) {
                errorMsg = "JSON parsing error. Please contact support.";
            } else if (e instanceof java.net.UnknownHostException) {
                errorMsg = "Network error. Please check your internet connection.";
            } else if (e instanceof java.net.SocketTimeoutException) {
                errorMsg = "Request timeout. Please try again.";
            } else if (e instanceof java.net.ConnectException) {
                errorMsg = "Cannot connect to server. Please try again later.";
            } else {
                errorMsg = "Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
            
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
    });
}
```

---

## 📱 Testing Procedure

### Step 1: Test with Valid Data
1. Open Edit Profile
2. Change name to "Test User"
3. Change email to "test@example.com"
4. Click Save
5. Check Logcat for `UPDATE_PROFILE` logs

### Step 2: Check Logcat Output

Look for these patterns:

**✅ Success Pattern:**
```
UPDATE_PROFILE: ║ HTTP Code: 200
UPDATE_PROFILE: ║ Status Code: 1000
UPDATE_PROFILE: ║ Status Message: Success.
UPDATE_PROFILE: ✅ Paper DB updated successfully
```

**❌ Error Pattern (JSON Syntax):**
```
UPDATE_PROFILE: ║ API FAILURE
UPDATE_PROFILE: ║ Error Type: JsonSyntaxException
UPDATE_PROFILE: 💥 JSON SYNTAX ERROR
```

**❌ Error Pattern (Network):**
```
UPDATE_PROFILE: ║ API FAILURE
UPDATE_PROFILE: ║ Error Type: UnknownHostException
```

---

## 🔧 Quick Fixes

### Fix 1: If you see "Expected BEGIN_OBJECT but was STRING"

**Problem:** PHP is returning plain text instead of JSON

**Solution:** Check PHP for echo statements before `json_encode()`:
```php
// Remove any echo/print statements before this:
header('Content-type: application/json');
echo json_encode($data);
die();
```

### Fix 2: If you see "Expected BEGIN_OBJECT but was BEGIN_ARRAY"

**Problem:** PHP is returning array instead of object

**Solution:** Verify PHP returns object, not array:
```php
// ✅ CORRECT - Returns object
echo json_encode($data);

// ❌ WRONG - Returns array
echo json_encode([$data]);
```

### Fix 3: If Status Object is NULL

**Problem:** JSON key mismatch

**Solution:** Verify PHP JSON has "status" key:
```php
$data = Array(
    'status' => Array(  // ← Must be "status"
        'code' => '1000',
        'message' => 'Success.',
    )
);
```

---

## 🎯 Complete Working PHP Code

Here's the corrected PHP code:

```php
<?php

if (isset($dataa_post['parent_id'])) {
    
    // Validation
    if (empty($dataa_post['parent_id']) || empty($dataa_post['campus_id'])) {
        $data = Array(
            'status' => Array(
                'code' => '2000',
                'message' => 'Please fill all the fields.',
            )
        );
        header('Content-type: application/json');
        echo json_encode($data);
        die();
    }

    // Prepare update data - UNCOMMENTED phone and landline
    $data = Array(
        'full_name' => $dataa_post['full_name'],
        'email' => $dataa_post['email'],
        'phone' => $dataa_post['phone'],        // ✅ ACTIVE
        'landline' => $dataa_post['landline'],  // ✅ ACTIVE
        'address' => $dataa_post['address']
    );

    // Update query - FIXED
    $db->where('is_delete', 0);
    $db->where('unique_id', $dataa_post['parent_id']);
    $db->where('campus_id', $dataa_post['campus_id']);  // ✅ FIXED
    $result = $db->update('parent', $data);

    if ($result) {
        // Success response
        $data = Array(
            'status' => Array(
                'code' => '1000',
                'message' => 'Profile updated successfully.',
            )
        );
        header('Content-type: application/json');
        echo json_encode($data);
        die();
    } else {
        // Database error
        $data = Array(
            'status' => Array(
                'code' => '1001',
                'message' => 'Database update failed.',
            )
        );
        header('Content-type: application/json');
        echo json_encode($data);
        die();
    }
} else {
    // Missing parent_id
    $data = Array(
        'status' => Array(
            'code' => '2001',
            'message' => 'Invalid request - missing parent_id.',
        )
    );
    header('Content-type: application/json');
    echo json_encode($data);
    die();
}
?>
```

---

## 📋 Checklist

- [ ] PHP returns proper JSON format (use browser/Postman to test)
- [ ] PHP has `Content-Type: application/json` header
- [ ] PHP has no echo/print before `json_encode()`
- [ ] PHP uncommented phone and landline fields
- [ ] PHP fixed database query (`campus_id` not `parent_id`)
- [ ] Android has updated logging code
- [ ] Test update profile in app
- [ ] Check Logcat for detailed logs
- [ ] Verify Paper DB is updated after success
- [ ] Verify ParentProfile shows updated data on return

---

## 🚀 Next Steps

1. **Add the enhanced logging code** to `Edit_ProfileParent.java`
2. **Fix the PHP code** (uncomment phone/landline, fix DB query)
3. **Test the update** and share the Logcat output
4. **If still errors**, share the full log and we'll fix it

The model structure is **ALREADY CORRECT**! The issue is likely in the PHP backend or how the response is being sent.

