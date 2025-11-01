# Parent Profile API - Android-Only Fix (No Backend Changes)

## 🎯 Situation
- ✅ You have the working API from KMU-Final
- ✅ Android model structure is correct
- ❌ **Backend cannot be changed**
- ⚠️ Backend has phone/landline **commented out** (won't update)

---

## 📋 What This Means

### Backend Currently Updates:
- ✅ `full_name`
- ✅ `email`
- ✅ `address`

### Backend Does NOT Update:
- ❌ `phone` (commented in PHP line 31)
- ❌ `landline` (commented in PHP line 32)

**Impact:** When you update profile, phone and landline changes will be **ignored by server** but still stored locally in Paper DB.

---

## 🔧 Android Fix Options

### Option 1: Keep Sending All Fields (Recommended)

**Advantage:** When backend is eventually updated, no code changes needed  
**Disadvantage:** Phone/Landline won't update on server (only locally)

**No changes needed!** Your current code already does this.

### Option 2: Remove Phone/Landline from Update

**Advantage:** Honest - doesn't pretend to update what it can't  
**Disadvantage:** Users can't change phone/landline at all

If you choose this, modify `Edit_ProfileParent.java`:

```java
private void update_profile() {
    final String Name_ = Name.getText().toString();
    final String email_ = email.getText().toString();
    final String address_ = address.getText().toString();
    
    // Don't send phone and landline since backend ignores them
    HashMap<String, String> postParam = new HashMap<>();
    postParam.put("parent_id", Constant.parent_id);
    postParam.put("campus_id", Constant.campus_id);
    postParam.put("full_name", Name_);
    postParam.put("email", email_);
    // postParam.put("phone", phone_);      // Removed - backend doesn't update
    // postParam.put("landline", landline_); // Removed - backend doesn't update
    postParam.put("address", address_);
    
    // ... rest of code
}
```

And make phone/landline fields non-editable:

```java
// In onCreate()
phone.setEnabled(false);
phone.setFocusable(false);
landline.setEnabled(false);
landline.setFocusable(false);

// Optional: Add hint
phone.setHint("Cannot edit (contact admin)");
landline.setHint("Cannot edit (contact admin)");
```

---

## 🐛 Fixing the "Expected BEGIN_OBJECT" Error

Since your model is correct, the error might be from:

### Cause 1: PHP Returning HTML Error
**Check:** Does your server have PHP errors/warnings enabled?

**Solution:** Add error handling in Android to detect HTML responses:

```java
// In Edit_ProfileParent.java, modify onFailure:
@Override
public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
    progress_bar.setVisibility(View.GONE);
    
    Log.e("UPDATE_PROFILE", "API Failure", e);
    
    String errorMsg;
    if (e instanceof com.google.gson.JsonSyntaxException) {
        // Server returned HTML or invalid JSON
        errorMsg = "Server configuration error. Please contact support.";
        Log.e("UPDATE_PROFILE", "JSON Parse Error - Server might be returning HTML/PHP error page");
        
        // Try to log the raw response if available
        try {
            okhttp3.Response rawResponse = call.execute();
            if (rawResponse.errorBody() != null) {
                String rawError = rawResponse.errorBody().string();
                Log.e("UPDATE_PROFILE", "Raw Error Response: " + rawError);
            }
        } catch (Exception ex) {
            // Ignore
        }
    } else if (e instanceof java.net.UnknownHostException) {
        errorMsg = "Network error. Check internet connection.";
    } else if (e instanceof java.net.SocketTimeoutException) {
        errorMsg = "Request timeout. Try again.";
    } else {
        errorMsg = "Update failed: " + e.getMessage();
    }
    
    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
}
```

### Cause 2: Backend Database Query Fails

The database query in your PHP has this suspicious line:
```php
$db->where('(parent_id="' . $dataa_post['campus_id'] . '")');
```

This might fail and return an error response.

**Solution:** Add logging to see what's actually returned:

```java
@Override
public void onResponse(@NonNull Call<GeneralModel> call, 
                     @NonNull Response<GeneralModel> response) {
    
    // Log everything
    Log.d("UPDATE_PROFILE", "Response Code: " + response.code());
    Log.d("UPDATE_PROFILE", "Response Success: " + response.isSuccessful());
    
    // Try to read raw response first
    try {
        if (response.errorBody() != null) {
            String errorBody = response.errorBody().string();
            Log.e("UPDATE_PROFILE", "Error Body: " + errorBody);
        }
    } catch (Exception e) {
        // Continue
    }
    
    if (response.body() != null) {
        try {
            SharedStatus status = response.body().getStatus();
            if (status != null) {
                String code = status.getCode();
                String message = status.getMessage();
                
                Log.d("UPDATE_PROFILE", "Status Code: " + code);
                Log.d("UPDATE_PROFILE", "Status Message: " + message);
                
                if ("1000".equals(code)) {
                    // Success
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    
                    // Update Paper DB
                    Paper.book().write("full_name", Name_);
                    Paper.book().write("email", email_);
                    Paper.book().write("address", address_);
                    
                    // Update phone/landline in Paper DB even though backend doesn't save them
                    // This keeps local data in sync with what user entered
                    final String phone_ = phone.getText().toString();
                    final String landline_ = landline.getText().toString();
                    Paper.book().write("phone", phone_);
                    Paper.book().write("landline", landline_);
                    
                    if (picture_str != null && !picture_str.isEmpty()) {
                        update_picture(picture_str);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        finish();
                    }
                } else {
                    // Error code from server
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            } else {
                // Status is null
                progress_bar.setVisibility(View.GONE);
                Log.e("UPDATE_PROFILE", "Status object is null");
                Toast.makeText(context, "Invalid response from server", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            progress_bar.setVisibility(View.GONE);
            Log.e("UPDATE_PROFILE", "Error parsing response", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    } else {
        // Response body is null
        progress_bar.setVisibility(View.GONE);
        Log.e("UPDATE_PROFILE", "Response body is null");
        Toast.makeText(context, "Empty response from server", Toast.LENGTH_SHORT).show();
    }
}
```

---

## 🧪 Testing with OkHttp Interceptor

Add this to see EXACTLY what the server returns:

### Step 1: Check your Retrofit setup location

Usually in one of these files:
- `Constant.java` (Teacher/Utils)
- `API.java` (Parent/Utils)
- `RetrofitClient.java`

### Step 2: Add OkHttp Interceptor

```java
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Response;
import okio.Buffer;

// Create logging interceptor
HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
    message -> Log.d("API_LOG", message)
);
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

// Create OkHttp client with interceptor
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

// Use this client in Retrofit
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(API.BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

This will log EVERYTHING in Logcat under `API_LOG` tag:
- Request URL
- Request headers
- Request body (JSON you're sending)
- Response code
- Response headers
- **Response body (EXACTLY what server returns)**

---

## 🎯 Recommended Solution

### Keep Current Implementation BUT Add Better Logging

1. **Keep sending all fields** (including phone/landline)
2. **Add enhanced logging** to see what server returns
3. **Update Paper DB for all fields** after success (even if server doesn't save phone/landline)
4. **Show user a note** that phone/landline must be updated by admin

### Add User Notice (Optional)

In `activity_edit_profile_parent.xml`, add a note:

```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Note: Phone and landline changes require admin approval"
    android:textColor="@color/orange"
    android:textSize="12sp"
    android:padding="8dp"
    android:layout_marginTop="8dp"
    android:background="@drawable/border_orange"
    android:visibility="gone"
    android:id="@+id/phone_notice" />
```

Show it when phone/landline are edited:

```java
// In onCreate()
phone.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        findViewById(R.id.phone_notice).setVisibility(View.VISIBLE);
    }
    // ... other methods
});
```

---

## 📝 Summary

### What Works:
✅ Full Name update  
✅ Email update  
✅ Address update  
✅ Profile picture update (separate API)

### What Doesn't Work (Backend Limitation):
❌ Phone update (commented in backend)  
❌ Landline update (commented in backend)

### Recommended Approach:
1. Keep current code as-is
2. Add better logging to debug the "Expected BEGIN_OBJECT" error
3. Update Paper DB for all fields after success (local sync)
4. Optionally add user notice about phone/landline

### To Debug the Error:
1. Add OkHttp interceptor
2. Add detailed logging in onResponse/onFailure
3. Test update and check Logcat for `API_LOG` or `UPDATE_PROFILE`
4. Share the logs if still having issues

---

## 🆘 Next Steps

1. Add the enhanced logging code above
2. Test profile update
3. Check Logcat for these tags:
   - `API_LOG` (OkHttp interceptor)
   - `UPDATE_PROFILE` (your custom logs)
4. Share the output if you still see "Expected BEGIN_OBJECT" error

The error is likely because:
- Server returns HTML error page (PHP error)
- Server returns different JSON structure
- Server returns plain text

The logs will tell us exactly what's happening! 🔍

