# Parent Profile View and Update API Summary

## Overview
Both projects (KMU-Final and Topgradeit) use the **SAME working API endpoints** for parent profile view and update functionality.

## API Endpoints Used

### 1. **View Parent Profile**
- **Method**: Direct data loading from Paper DB (No API call)
- **Location**: `ParentProfile.java`
- **Implementation**: 
  - Reads data directly from local storage using Paper.book()
  - Keys: `full_name`, `email`, `phone`, `landline`, `address`, `picture`
  - Image URL: `API.parent_image_base_url + picture`

```java
// Data loading in ParentProfile.java (line 213-270)
private void loadDataDirectly() {
    final String full_name_ = Paper.book().read("full_name");
    String email_ = Paper.book().read("email");
    String phone_ = Paper.book().read("phone");
    String landline_ = Paper.book().read("landline");
    String address_ = Paper.book().read("address");
    final String picture_ = Paper.book().read("picture");
    
    // Set text data to UI
    Name.setText(full_name_);
    email.setText(email_);
    phone.setText(phone_);
    landline.setText(landline_);
    address.setText(address_);
    
    // Load image
    String imageUrl = API.parent_image_base_url + picture_;
    EnhancedImageLoader imageLoader = new EnhancedImageLoader(this);
    imageLoader.loadImage(image, imageUrl, callback);
}
```

### 2. **Update Parent Profile**
- **API Endpoint**: `api.php?page=parent/update_profile`
- **HTTP Method**: POST
- **Content-Type**: application/json
- **Location**: `BaseApiService.java` (line 164-166)

```java
@Headers("Content-Type:application/json")
@POST("api.php?page=parent/update_profile")
Call<GeneralModel> update_profile_(@Body RequestBody body);
```

#### Request Parameters:
```json
{
  "parent_id": "string",
  "campus_id": "string",
  "full_name": "string",
  "email": "string",
  "phone": "string",
  "landline": "string",
  "address": "string"
}
```

#### Response Model: `GeneralModel`
```java
// Response structure
{
  "status": {
    "code": "1000",  // Success code
    "message": "Profile updated successfully"
  }
}
```

#### Implementation in Edit_ProfileParent.java (line 523-589):
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

    progress_bar.setVisibility(View.VISIBLE);
    RequestBody body = RequestBody.create(
        (new JSONObject(postParam)).toString(), 
        MediaType.parse("application/json; charset=utf-8")
    );
    
    Constant.mApiService.update_profile_(body).enqueue(new Callback<>() {
        @Override
        public void onResponse(@NonNull Call<GeneralModel> call, 
                             @NonNull Response<GeneralModel> response) {
            if (response.body() != null) {
                if (response.body().getStatus().getCode().equals("1000")) {
                    // Success - Update Paper DB
                    Paper.book().write("full_name", Name_);
                    Paper.book().write("email", email_);
                    Paper.book().write("phone", phone_);
                    Paper.book().write("landline", landline_);
                    Paper.book().write("address", address_);
                    
                    // If picture was changed, update it separately
                    if (picture_str != null && !picture_str.isEmpty()) {
                        update_picture(picture_str);
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        finish();
                    }
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<GeneralModel> call, @NonNull Throwable e) {
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 3. **Update Parent Profile Picture (Separate API)**
- **API Endpoint**: `API.update_picture` (Volley-based, not Retrofit)
- **HTTP Method**: POST
- **Location**: `Edit_ProfileParent.java` (line 447-521)

```java
private void update_picture(final String file) {
    progress_bar.setVisibility(View.VISIBLE);
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    
    final StringRequest jsonObjectRequest = new StringRequest(
        Request.Method.POST,
        API.update_picture,
        response -> {
            JSONObject respone = new JSONObject(response);
            JSONObject status = respone.getJSONObject("status");
            if (status.getString("code").equals("1000")) {
                String new_picture = respone.getString("data");
                Paper.book().write("picture", new_picture);
                Toast.makeText(context, "Profile Image Updated", Toast.LENGTH_SHORT).show();
            }
        },
        error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    ) {
        @Override
        public byte[] getBody() {
            HashMap<String, String> postParam = new HashMap<>();
            postParam.put("parent_id", Constant.parent_id);
            postParam.put("campus_id", Constant.campus_id);
            postParam.put("file", file);  // Base64 encoded image
            return new JSONObject(postParam).toString().getBytes();
        }
    };
    
    requestQueue.add(jsonObjectRequest);
}
```

## Activities Flow

### ParentProfile.java
- **Purpose**: View parent profile (read-only)
- **Data Source**: Paper DB (local storage)
- **Features**:
  - Displays: Name, Email, Phone, Landline, Address, Profile Picture
  - Click profile picture → Opens ZoomImage activity
  - Click "Edit Profile" button → Opens Edit_ProfileParent activity
  - Reloads data in `onResume()` to reflect updates

### Edit_ProfileParent.java
- **Purpose**: Edit and update parent profile
- **Data Source**: Paper DB for initial load, API for updates
- **Features**:
  - Editable fields: Name, Email, Phone, Landline, Address
  - Profile picture edit (camera/gallery with crop)
  - Save button → Calls `update_profile_()` API
  - Updates Paper DB after successful API response
  - Separate picture update if new image selected

## Data Flow

```
1. LOGIN
   ↓
   API saves data to Paper DB
   ↓
2. VIEW PROFILE (ParentProfile.java)
   ↓
   Read from Paper DB → Display
   ↓
3. EDIT PROFILE (Edit_ProfileParent.java)
   ↓
   Read from Paper DB → Populate form
   ↓
4. USER MAKES CHANGES
   ↓
5. CLICK SAVE
   ↓
   API: update_profile_() → Server
   ↓
6. SUCCESS RESPONSE
   ↓
   Update Paper DB with new values
   ↓
7. IF PICTURE CHANGED
   ↓
   API: update_picture() → Server
   ↓
8. RETURN TO PROFILE VIEW
   ↓
   onResume() reloads data from Paper DB
```

## Key Constants Used

```java
// From Constant.java (Teacher.Utils)
Constant.parent_id     // Current parent ID
Constant.campus_id     // Current campus ID
Constant.mApiService   // Retrofit API service instance

// From API.java (Parent.Utils)
API.parent_image_base_url   // Base URL for parent images
API.update_picture          // Picture update endpoint URL
```

## Theme Support

Both activities support dual themes:
- **Parent Theme**: Dark brown
- **Student Theme**: Teal (when student accesses parent profile)

Theme is determined by checking `Paper.book().read(Constants.User_Type, "")`

## Current Status

✅ **Both projects use the SAME working API**
✅ **API is properly implemented in BaseApiService.java**
✅ **Edit_ProfileParent.java correctly uses the API**
✅ **ParentProfile.java correctly reads from Paper DB**
✅ **Profile picture update works separately**

## Verification

To verify the API is working:

1. **Check BaseApiService.java** → Line 164-166 has `update_profile_()`
2. **Check Edit_ProfileParent.java** → Line 545 calls `Constant.mApiService.update_profile_()`
3. **Check ParentProfile.java** → Line 213-270 loads data from Paper DB
4. **Test flow**:
   - Login as parent
   - Navigate to profile
   - Click "Edit Profile"
   - Make changes
   - Click "Save"
   - Verify API call in logs
   - Check if Paper DB is updated
   - Return to profile view
   - Verify changes are displayed

## API Success Criteria

- Response code: `"1000"`
- Response has valid `status` object with `code` and `message`
- After success, Paper DB is updated with new values
- UI reflects the changes after returning to profile view

## Notes

1. **No separate "view profile" API** - Data is read from Paper DB which is populated during login
2. **Picture update is separate** - Uses Volley instead of Retrofit
3. **Paper DB is the source of truth** for profile display
4. **API only called during updates**, not during view
5. **onResume() refresh** ensures profile view is always up-to-date after edits

---

**Conclusion**: The Topgradeit project already has the complete working API implementation from KMU-Final. No changes are needed to the API layer.

