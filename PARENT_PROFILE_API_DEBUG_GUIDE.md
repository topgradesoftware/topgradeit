# Parent Profile API Error Debug Guide

## The Error: `IllegalStateException: Expected BEGIN_OBJECT`

This error occurs when Gson tries to parse JSON but receives a different format than expected.

---

## 🔍 Understanding the Error

### What Your Code Expects (from `GeneralModel`)
```json
{
  "status": {
    "code": "1000",
    "message": "Profile updated successfully"
  }
}
```

### What the Server Might Be Sending

❌ **Case 1: JSON Array Instead of Object**
```json
[
  {
    "status": {
      "code": "1000",
      "message": "Profile updated"
    }
  }
]
```

❌ **Case 2: Plain Text**
```
Profile updated successfully
```

❌ **Case 3: HTML Error Page**
```html
<!DOCTYPE html>
<html>
<body>Error: PHP Warning...</body>
</html>
```

❌ **Case 4: Different Structure**
```json
{
  "success": true,
  "data": "Profile updated"
}
```

---

## 🛠️ Debug Steps for Parent Profile API

### Step 1: Add Response Logging to `Edit_ProfileParent.java`

Find the `update_profile()` method (around line 523) and add logging:

```java
private void update_profile() {
    final String phone_ = phone.getText().toString();
    final String email_ = email.getText().toString();
    final String landline_ = landline.getText().toString();
    final String address_ = address.getText().toString();
    final String Name_ = Name.getText().toString();

    HashMap<String, String> postParam = new HashMap<>();
    postParam.put("parent_id", Constant.parent_id);
    postParam.put("campus_id", Constant.campus_id);
    postParam.put("full_name", Name_);
    postParam.put("email", email_);
    postParam.put("phone", phone_);
    postParam.put("landline", landline_);
    postParam.put("address", address_);

    // 🔍 DEBUG: Log request parameters
    Log.d("UPDATE_PROFILE", "=== API REQUEST ===");
    Log.d("UPDATE_PROFILE", "Endpoint: api.php?page=parent/update_profile");
    Log.d("UPDATE_PROFILE", "Parameters: " + new JSONObject(postParam).toString());
    Log.d("UPDATE_PROFILE", "==================");

    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create(
        (new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8")
    );
    
    Constant.mApiService.update_profile_(body).enqueue(new Callback<>() {
        @Override
        public void onResponse(@NonNull Call<GeneralModel> call, 
                             @NonNull Response<GeneralModel> response) {
            
            // 🔍 DEBUG: Log raw response
            Log.d("UPDATE_PROFILE", "=== API RESPONSE ===");
            Log.d("UPDATE_PROFILE", "Response Code: " + response.code());
            Log.d("UPDATE_PROFILE", "Response Message: " + response.message());
            
            if (response.body() != null) {
                // 🔍 DEBUG: Log parsed response
                Log.d("UPDATE_PROFILE", "Status Code: " + response.body().getStatus().getCode());
                Log.d("UPDATE_PROFILE", "Status Message: " + response.body().getStatus().getMessage());
                Log.d("UPDATE_PROFILE", "===================");
                
                if (response.body().getStatus().getCode().equals("1000")) {
                    Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                    Paper.book().write("full_name", Name_);
                    Paper.book().write("email", email_);
                    Paper.book().write("phone", phone_);
                    Paper.book().write("landline", landline_);
                    Paper.book().write("address", address_);
                    
                    if (picture_str != null && !picture_str.isEmpty()) {
                        update_picture(picture_str);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        finish();
                    }
                } else {
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(context, response.body().getStatus().getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                // 🔍 DEBUG: Response body is null - check error body
                progress_bar.setVisibility(View.GONE);
                
                try {
                    String errorBody = response.errorBody() != null ? 
                        response.errorBody().string() : "null";
                    Log.e("UPDATE_PROFILE", "Error Body: " + errorBody);
                    Log.e("UPDATE_PROFILE", "===================");
                    
                    Toast.makeText(context, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("UPDATE_PROFILE", "Error reading error body", e);
                    Toast.makeText(context, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
            // 🔍 DEBUG: Log failure details
            Log.e("UPDATE_PROFILE", "=== API FAILURE ===");
            Log.e("UPDATE_PROFILE", "Error Type: " + e.getClass().getSimpleName());
            Log.e("UPDATE_PROFILE", "Error Message: " + e.getMessage());
            Log.e("UPDATE_PROFILE", "Stack Trace: ", e);
            Log.e("UPDATE_PROFILE", "===================");
            
            progress_bar.setVisibility(View.GONE);
            
            // Better error message for user
            String errorMsg = e.getMessage();
            if (e instanceof com.google.gson.JsonSyntaxException) {
                errorMsg = "Server response format error. Check API logs.";
            } else if (e instanceof java.net.UnknownHostException) {
                errorMsg = "Network error. Check internet connection.";
            } else if (e instanceof java.net.SocketTimeoutException) {
                errorMsg = "Request timeout. Try again.";
            }
            
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
    });
}
```

### Step 2: Test and Check Logcat

Run the app, try to update profile, and check Logcat for:
```
UPDATE_PROFILE: === API REQUEST ===
UPDATE_PROFILE: === API RESPONSE ===
UPDATE_PROFILE: === API FAILURE ===
```

---

## 🔧 Solutions Based on What You Find

### Solution 1: If Server Returns Wrong JSON Structure

**Check your `GeneralModel.java`** - it should match the API response:

```java
package topgrade.parent.com.parentseeks.Parent.Model;

import com.google.gson.annotations.SerializedName;

public class GeneralModel {
    
    @SerializedName("status")
    private Status status;
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public static class Status {
        @SerializedName("code")
        private String code;
        
        @SerializedName("message")
        private String message;
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
```

### Solution 2: If Server Returns Plain String

Modify the API interface to accept `ResponseBody` temporarily:

```java
// In BaseApiService.java - ADD THIS TEMPORARILY
@Headers("Content-Type:application/json")
@POST("api.php?page=parent/update_profile")
Call<ResponseBody> update_profile_raw(@Body RequestBody body);
```

Then in `Edit_ProfileParent.java`:

```java
// Temporary debug version
Constant.mApiService.update_profile_raw(body).enqueue(new Callback<ResponseBody>() {
    @Override
    public void onResponse(@NonNull Call<ResponseBody> call, 
                         @NonNull Response<ResponseBody> response) {
        try {
            String rawResponse = response.body().string();
            Log.d("UPDATE_PROFILE", "Raw Response: " + rawResponse);
            
            // Now you can see EXACTLY what the server returns
            // Then adjust your model accordingly
            
        } catch (Exception e) {
            Log.e("UPDATE_PROFILE", "Error reading response", e);
        }
    }
    
    @Override
    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable e) {
        Log.e("UPDATE_PROFILE", "API Failure", e);
    }
});
```

### Solution 3: Backend Returns HTML/PHP Error

If you see HTML in logs, the backend has a PHP error. Check:
- PHP error logs on server
- Make sure `Content-Type: application/json` header is sent
- Ensure API endpoint URL is correct

### Solution 4: Use OkHttp Interceptor for All Requests

Add a logging interceptor to see ALL API requests/responses:

```java
// In your Retrofit setup (usually in Constant.java or API setup)
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            
            // Log request
            Log.d("API_REQUEST", "===================");
            Log.d("API_REQUEST", "URL: " + request.url());
            Log.d("API_REQUEST", "Method: " + request.method());
            Log.d("API_REQUEST", "Headers: " + request.headers());
            
            if (request.body() != null) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                Log.d("API_REQUEST", "Body: " + buffer.readUtf8());
            }
            Log.d("API_REQUEST", "===================");
            
            // Get response
            Response response = chain.proceed(request);
            
            // Log response
            String responseBody = response.body().string();
            Log.d("API_RESPONSE", "===================");
            Log.d("API_RESPONSE", "URL: " + response.request().url());
            Log.d("API_RESPONSE", "Code: " + response.code());
            Log.d("API_RESPONSE", "Message: " + response.message());
            Log.d("API_RESPONSE", "Body: " + responseBody);
            Log.d("API_RESPONSE", "===================");
            
            // Rebuild response (body can only be read once)
            return response.newBuilder()
                .body(ResponseBody.create(
                    response.body().contentType(), 
                    responseBody
                ))
                .build();
        }
    })
    .build();

// Use this client in Retrofit
Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(API.BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

---

## 🧪 Test with Postman/Curl

Test the API directly to see the actual response:

```bash
curl -X POST "https://yourserver.com/api.php?page=parent/update_profile" \
  -H "Content-Type: application/json" \
  -d '{
    "parent_id": "123",
    "campus_id": "456",
    "full_name": "Test Name",
    "email": "test@test.com",
    "phone": "1234567890",
    "landline": "0987654321",
    "address": "Test Address"
  }'
```

Expected response:
```json
{
  "status": {
    "code": "1000",
    "message": "Profile updated successfully"
  }
}
```

---

## 📋 Checklist

- [ ] Add debug logging to `Edit_ProfileParent.java`
- [ ] Run app and attempt profile update
- [ ] Check Logcat for `UPDATE_PROFILE` logs
- [ ] Identify what server is actually returning
- [ ] Verify `GeneralModel.java` matches response structure
- [ ] Test API with Postman/curl directly
- [ ] Check backend PHP code for errors
- [ ] Verify Content-Type headers are correct
- [ ] Ensure no HTML/PHP warnings in response
- [ ] Test with OkHttp interceptor if needed

---

## 🎯 Quick Reference: Common Error Patterns

| Error Type | Logcat Shows | Solution |
|------------|--------------|----------|
| JSON Array | `[{...}]` | Change model or wrap in array |
| Plain String | `"Success"` | Use `ResponseBody` type |
| HTML Error | `<!DOCTYPE...` | Fix backend PHP errors |
| Wrong Structure | Different keys | Update `GeneralModel` |
| Network Error | `UnknownHostException` | Check internet/API URL |
| Timeout | `SocketTimeoutException` | Increase timeout or check server |

---

## 💡 Pro Tips

1. **Always log the raw response first** before trying to fix model
2. **Test API with Postman** to verify backend works correctly
3. **Use OkHttp interceptor** for production-ready logging
4. **Check PHP error logs** on server if you see HTML
5. **Verify Content-Type header** is `application/json`
6. **Use try-catch** around Gson parsing for better error messages
7. **Add timeout handling** for slow networks

---

## 🔗 Related Files to Check

1. `Edit_ProfileParent.java` - Line 523-589 (update_profile method)
2. `BaseApiService.java` - Line 164-166 (API interface)
3. `GeneralModel.java` - Response model
4. Backend: `api.php?page=parent/update_profile` - Server endpoint
5. `Constant.java` or API setup - Retrofit configuration

---

**Next Steps**: Add the debug logging above, run the app, and share the Logcat output. That will tell us exactly what the server is returning and how to fix the model!

