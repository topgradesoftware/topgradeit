# Activity Configuration Notes

## Required Activity Configuration

To complete the layout optimizations, add the following to your Activity's manifest entry or in the Activity class:

### 1. Keyboard & ScrollView Overlap Fix
Add to AndroidManifest.xml in the activity declaration:
```xml
<activity
    android:name=".Parent.Activity.EditProfileParent"
    android:windowSoftInputMode="adjustResize"
    android:fitsSystemWindows="true"
    ... />
```

Or programmatically in the Activity:
```java
getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
```

### 2. Benefits of These Changes:
- **Keyboard Handling**: Fields won't be hidden by the keyboard
- **Material Design**: TextInputLayout provides consistent Material styling
- **Validation Support**: Built-in error handling and validation (`app:errorEnabled="true"`)
- **Accessibility**: Better screen reader support with `labelFor` attributes
- **Icons**: Visual indicators for each field type
- **Floating Labels**: Modern Material Design floating label behavior
- **Dark Mode Ready**: Uses theme attributes (`?attr/colorOnSurface`, `?attr/colorPrimary`)
- **Tablet Optimized**: Responsive layout with packed chains for wide screens
- **Touch Targets**: Proper 48dp minimum touch target for back button
- **Multi-line Address**: Address field shows 3 lines by default with scrollbars
- **MaterialToolbar**: Professional header with built-in ripple effects and accessibility
- **Error Layout Stability**: `app:helperText=" "` prevents layout shift when errors appear
- **Expandable Address**: Address field can grow beyond 3 lines as user types
- **Material Typography**: Header (20sp) and button (16sp) follow Material Design guidelines

### 3. TextInputLayout Features Added:
- Outlined box style for modern appearance
- Start icons for each field type (person, email, phone, location)
- Proper hint text handling
- Error state support (can be added later)
- Consistent theming with dark brown accent color

### 4. Icon Requirements:
Make sure these icons exist in your drawable resources:
- `@drawable/ic_person` - for name field
- `@drawable/ic_email` - for email field  
- `@drawable/ic_phone` - for phone/landline fields
- `@drawable/ic_location` - for address field

If these icons don't exist, you can:
1. Use Material Design icons from Google's icon library
2. Create simple vector drawables
3. Remove the `app:startIconDrawable` attributes temporarily
