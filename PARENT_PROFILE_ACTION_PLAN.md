# Parent Profile API - Action Plan (Backend Unchanged)

## ✅ Current Status

### Your Setup:
- ✅ API endpoint exists: `api.php?page=parent/update_profile`
- ✅ Android model is correct (`GeneralModel` + `SharedStatus`)
- ✅ Code structure matches KMU-Final (working version)
- ❌ Backend **cannot be changed**
- ⚠️ Backend ignores `phone` and `landline` (commented out)

### The Problem:
Getting `IllegalStateException: Expected BEGIN_OBJECT` error during profile update

---

## 🎯 Quick Action Steps

### Step 1: Add Logging to See What Server Returns

Open: `Edit_ProfileParent.java`  
Find: `update_profile()` method (line ~523)  
Add: Enhanced logging in `onFailure()` callback

```java
@Override
public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
    progress_bar.setVisibility(View.GONE);
    
    // 🔍 LOG THE ERROR
    Log.e("PROFILE_UPDATE", "════════════════════════════════");
    Log.e("PROFILE_UPDATE", "Error Type: " + e.getClass().getName());
    Log.e("PROFILE_UPDATE", "Error Message: " + e.getMessage());
    Log.e("PROFILE_UPDATE", "════════════════════════════════");
    e.printStackTrace();
    
    // Show user-friendly message
    String msg = "Update failed";
    if (e instanceof com.google.gson.JsonSyntaxException) {
        msg = "Server response error - contact support";
        Log.e("PROFILE_UPDATE", "⚠️ Server returned invalid JSON!");
    }
    
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
}
```

### Step 2: Test and Check Logcat

1. Run app
2. Go to Edit Profile
3. Change name/email/address
4. Click Save
5. **Check Logcat** for `PROFILE_UPDATE` tag

---

## 🔍 What to Look For

### Scenario A: JsonSyntaxException
```
Error Type: com.google.gson.JsonSyntaxException
Error Message: Expected BEGIN_OBJECT but was STRING at line 1
```
**Cause:** Server returned plain text or HTML instead of JSON  
**Fix:** See "Solution A" below

### Scenario B: Network Error
```
Error Type: java.net.UnknownHostException
```
**Cause:** Can't reach server  
**Fix:** Check internet connection, verify API URL

### Scenario C: Success (No error)
```
Status Code: 1000
Status Message: Success.
```
**Result:** It's working! 🎉

---

## 🔧 Solutions

### Solution A: Server Returns HTML/Plain Text (Most Common)

The backend might be returning PHP error or HTML.

**Add Raw Response Logger:**

```java
// Add this ABOVE your current Retrofit call
// Temporary debug version - use ResponseBody instead of GeneralModel

import okhttp3.ResponseBody;

// In update_profile() method, ADD THIS TEMPORARILY:
Constant.mApiService.update_profile_raw(body).enqueue(new Callback<ResponseBody>() {
    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, 
                         @NonNull Response<ResponseBody> response) {
        try {
            String rawResponse = response.body().string();
            Log.d("PROFILE_UPDATE", "════════════════════════════════");
            Log.d("PROFILE_UPDATE", "RAW RESPONSE FROM SERVER:");
            Log.d("PROFILE_UPDATE", rawResponse);
            Log.d("PROFILE_UPDATE", "════════════════════════════════");
            
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, "Check logcat for response", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("PROFILE_UPDATE", "Error reading response", e);
        }
    }
    
    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable e) {
        Log.e("PROFILE_UPDATE", "Request failed", e);
        progress_bar.setVisibility(View.GONE);
    }
});
```

**But first, ADD this to BaseApiService.java:**

```java
// In BaseApiService.java, add this method (temporary debug):
@Headers("Content-Type:application/json")
@POST("api.php?page=parent/update_profile")
Call<ResponseBody> update_profile_raw(@Body RequestBody body);
```

This will show you EXACTLY what the server returns!

### Solution B: Backend Database Issue

Your PHP has:
```php
$db->where('(parent_id="' . $dataa_post['campus_id'] . '")');
```

This might be wrong and causing update to fail.

**Can't fix PHP, but can verify:**
- Check if `parent_id` in Paper DB matches database
- Check if `campus_id` in Paper DB matches database

```java
// Add logging before API call
Log.d("PROFILE_UPDATE", "Sending parent_id: " + Constant.parent_id);
Log.d("PROFILE_UPDATE", "Sending campus_id: " + Constant.campus_id);
```

### Solution C: Phone/Landline Causing Issues

Since backend ignores them, **try removing them**:

```java
HashMap<String, String> postParam = new HashMap<>();
postParam.put("parent_id", Constant.parent_id);
postParam.put("campus_id", Constant.campus_id);
postParam.put("full_name", Name_);
postParam.put("email", email_);
// Don't send phone/landline since backend ignores them anyway
// postParam.put("phone", phone_);
// postParam.put("landline", landline_);
postParam.put("address", address_);
```

---

## 📱 Recommended Testing Flow

### Test 1: Check Raw Response
1. Add `update_profile_raw()` to BaseApiService
2. Use it temporarily in Edit_ProfileParent
3. Try to update profile
4. Check Logcat for "RAW RESPONSE FROM SERVER"
5. **Share that output** - it will tell us exactly what's wrong

### Test 2: Verify Model Matches Response
Expected server response:
```json
{
  "status": {
    "code": "1000",
    "message": "Success."
  }
}
```

Your model should match (and it does!):
```java
GeneralModel {
    status: SharedStatus {
        code: "1000",
        message: "Success."
    }
}
```

### Test 3: Try Without Phone/Landline
If raw response looks good but still fails, remove phone/landline from request.

---

## 💾 Paper DB Handling

After successful update, make sure to update Paper DB:

```java
if ("1000".equals(code)) {
    // Update Paper DB with new values
    Paper.book().write("full_name", Name_);
    Paper.book().write("email", email_);
    Paper.book().write("address", address_);
    
    // Optional: Update phone/landline in Paper DB even if backend doesn't save
    // This keeps UI consistent with what user entered
    Paper.book().write("phone", phone.getText().toString());
    Paper.book().write("landline", landline.getText().toString());
    
    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show();
    finish(); // Return to profile view
}
```

---

## 📋 Quick Checklist

**Immediate Actions:**
- [ ] Add error logging to `onFailure()` in Edit_ProfileParent.java
- [ ] Add `update_profile_raw()` to BaseApiService.java
- [ ] Test update and check Logcat
- [ ] Share Logcat output if still failing

**Validation:**
- [ ] Verify parent_id is correct (check Paper DB)
- [ ] Verify campus_id is correct (check Paper DB)
- [ ] Verify internet connection works
- [ ] Verify API URL is correct

**If Still Failing:**
- [ ] Try removing phone/landline from request
- [ ] Check if Picture update works (uses different API)
- [ ] Test with Postman/curl directly

---

## 🎯 Expected Outcome

After adding logging, you'll see one of these:

### ✅ Success:
```
PROFILE_UPDATE: Status Code: 1000
PROFILE_UPDATE: Status Message: Success.
```
→ It works! Profile is updated.

### ⚠️ Server Error:
```
PROFILE_UPDATE: RAW RESPONSE FROM SERVER:
<!DOCTYPE html>
<html>...PHP Warning...</html>
```
→ Backend has PHP error. Can't fix without backend access.

### ⚠️ Wrong Format:
```
PROFILE_UPDATE: RAW RESPONSE FROM SERVER:
{"success": true, "message": "Updated"}
```
→ Server returns different format. Need to create custom model or contact backend team.

### ⚠️ Database Error:
```
PROFILE_UPDATE: Status Code: 1001
PROFILE_UPDATE: Status Message: BaceNd Issue.
```
→ Database query failed (probably the parent_id/campus_id mismatch). Can't fix without backend access.

---

## 🚀 Next Step

**Add the raw response logger and test.** Share the Logcat output here and I'll help you fix it based on what the server actually returns! 🔍

The error message will tell us exactly what's wrong.

