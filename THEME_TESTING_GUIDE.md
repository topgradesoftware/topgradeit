# Theme Testing Guide 🎨

## Quick Visual Check

Use this guide to visually verify that themes are correctly applied based on user type.

---

## 🟫 **PARENT THEME (Dark Brown)**

### **Color Code:** `#693e02`

### **Where to Check:**
1. **Parent Login Screen**
   - Status bar should be dark brown
   - Navigation bar should be dark brown
   - Header wave should show brown gradient

2. **Parent Dashboard**
   - All headers in dark brown
   - Footer in dark brown
   - Buttons in dark brown

### **Test Steps:**
```
1. Open app
2. Click "Parent Login" from SelectRole
3. ✅ Check: Status bar is dark brown
4. ✅ Check: Navigation bar is dark brown
5. Login with parent credentials
6. ✅ Check: Dashboard headers are dark brown
7. Navigate to Profile
8. ✅ Check: Profile page uses dark brown theme
```

---

## 🟩 **STUDENT THEME (Teal)**

### **Color Code:** `#004d40`

### **Where to Check:**
1. **Student Login Screen**
   - Status bar should be teal
   - Navigation bar should be teal
   - Header wave should show teal gradient

2. **Student Dashboard**
   - All headers in teal
   - Footer in teal
   - Buttons in teal

### **Test Steps:**
```
1. Open app
2. Click "Student Login" from SelectRole
3. ✅ Check: Status bar is teal
4. ✅ Check: Navigation bar is teal
5. Login with student credentials
6. ✅ Check: Dashboard headers are teal
7. Navigate to Profile
8. ✅ Check: Profile page uses teal theme
```

---

## 🟦 **STAFF THEME (Navy Blue)**

### **Color Code:** `#000064`

### **Where to Check:**
1. **Staff Login Screen**
   - Status bar should be navy blue
   - Navigation bar should be navy blue
   - Header wave should show navy gradient

2. **Staff Dashboard**
   - All headers in navy blue
   - Footer in navy blue
   - Buttons in navy blue

### **Test Steps:**
```
1. Open app
2. Click "Staff Login" from SelectRole
3. ✅ Check: Status bar is navy blue
4. ✅ Check: Navigation bar is navy blue
5. Login with staff credentials
6. ✅ Check: Dashboard headers are navy blue
7. Navigate to Profile
8. ✅ Check: Profile page uses navy blue theme
```

---

## 🔍 **Debug Logging**

Add this to any activity to check current theme:

```kotlin
val userType = Paper.book().read(Constants.User_Type, "")
Log.d("THEME_CHECK", "═══════════════════════════════")
Log.d("THEME_CHECK", "User Type: $userType")
Log.d("THEME_CHECK", "Theme: ${ThemeHelper.getThemeDisplayName(userType)}")
Log.d("THEME_CHECK", "Primary Color: ${ThemeHelper.getPrimaryColor(this, userType)}")
Log.d("THEME_CHECK", "═══════════════════════════════")
```

Then check logcat:
```bash
adb logcat | grep "THEME_CHECK"
```

---

## 🎯 **Quick Visual Comparison**

| Element | Parent 🟫 | Student 🟩 | Staff 🟦 |
|---------|-----------|------------|----------|
| **Status Bar** | Dark Brown | Teal | Navy Blue |
| **Nav Bar** | Dark Brown | Teal | Navy Blue |
| **Header** | Brown Gradient | Teal Gradient | Navy Gradient |
| **Footer** | Brown | Teal | Navy |
| **Buttons** | Brown | Teal | Navy |
| **Cards** | White (all) | White (all) | White (all) |

---

## ✅ **Checklist**

### **Parent Theme**
- [ ] Login screen status bar is dark brown
- [ ] Login screen navigation bar is dark brown
- [ ] Dashboard header is dark brown
- [ ] Dashboard footer is dark brown
- [ ] Buttons are dark brown
- [ ] Profile page uses dark brown

### **Student Theme**
- [ ] Login screen status bar is teal
- [ ] Login screen navigation bar is teal
- [ ] Dashboard header is teal
- [ ] Dashboard footer is teal
- [ ] Buttons are teal
- [ ] Profile page uses teal

### **Staff Theme**
- [ ] Login screen status bar is navy blue
- [ ] Login screen navigation bar is navy blue
- [ ] Dashboard header is navy blue
- [ ] Dashboard footer is navy blue
- [ ] Buttons are navy blue
- [ ] Profile page uses navy blue

---

## 🐛 **Common Issues**

### **Issue 1: Theme not applying**
**Solution:**
- Check if `Constant.loadFromPaper()` is called in onCreate()
- Verify user type is stored in Paper DB
- Check logs for theme application errors

### **Issue 2: Wrong theme showing**
**Solution:**
- Clear app data and login again
- Check Paper DB for correct user type
- Verify theme mapping in ThemeHelper

### **Issue 3: Status bar/nav bar wrong color**
**Solution:**
- Check if `ThemeHelper.applySimpleTheme()` is called
- Verify Android version compatibility
- Check for window flag conflicts

---

## 📱 **Test Scenarios**

### **Scenario 1: Fresh Install**
1. Install app
2. Select Parent
3. Login
4. ✅ Verify: Dark Brown theme everywhere

### **Scenario 2: Switch Users**
1. Logout from Parent
2. Login as Student
3. ✅ Verify: Teal theme everywhere
4. Logout from Student
5. Login as Staff
6. ✅ Verify: Navy Blue theme everywhere

### **Scenario 3: Cross-User Access**
1. Login as Parent
2. Access Student profile (if allowed)
3. ✅ Verify: Student profile shows Teal theme
4. Return to parent dashboard
5. ✅ Verify: Parent dashboard shows Brown theme

---

## 🎨 **Visual Reference**

### **Parent (Dark Brown)**
```
Status Bar:     █████████████ #693e02
Header:         █████████████ Dark Brown Gradient
Content:        □□□□□□□□□□□□□ White Cards
Button:         [████ Brown ████]
Footer:         █████████████ Dark Brown
Nav Bar:        █████████████ #693e02
```

### **Student (Teal)**
```
Status Bar:     █████████████ #004d40
Header:         █████████████ Teal Gradient
Content:        □□□□□□□□□□□□□ White Cards
Button:         [████ Teal ████]
Footer:         █████████████ Teal
Nav Bar:        █████████████ #004d40
```

### **Staff (Navy Blue)**
```
Status Bar:     █████████████ #000064
Header:         █████████████ Navy Gradient
Content:        □□□□□□□□□□□□□ White Cards
Button:         [████ Navy ████]
Footer:         █████████████ Navy Blue
Nav Bar:        █████████████ #000064
```

---

**Created:** November 3, 2025  
**Purpose:** Quick visual verification of theme implementation

