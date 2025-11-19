# Staff Login Page Layout - Detailed Analysis

## Overview
The Staff Login page (`activity_staff_login.xml`) is implemented as an Android XML layout using ConstraintLayout. The page uses a dark blue (navy blue) and white color scheme with a wave-shaped header design.

---

## 1. STATUS BAR (System Status Bar)

### Configuration (from `TeacherLogin.kt`)
```kotlin
// Edge-to-edge display
WindowCompat.setDecorFitsSystemWindows(window, false)

// Transparent status bar to allow header wave to cover it
window.statusBarColor = android.graphics.Color.TRANSPARENT
window.navigationBarColor = ContextCompat.getColor(this, R.color.navy_blue)

// White status bar icons on dark background (Android M+)
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
    val flags = window.decorView.systemUiVisibility
    window.decorView.systemUiVisibility = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
}

// Android R+ configuration
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
    window.insetsController?.setSystemBarsAppearance(
        0, // No light icons (white icons on dark background)
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or 
        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
    )
}
```

### Visual Elements:
- **Background**: Transparent (allows header wave to extend behind it)
- **Icons**: White (time, Wi-Fi, battery, etc.)
- **Height**: System-defined (typically 24dp on most devices)
- **Coverage**: Header wave extends behind status bar for seamless design

---

## 2. HEADER SECTION

### Layout Structure
Located at lines **12-103** in `activity_staff_login.xml`

### Components:

#### A. Header Wave Background (`header_wave`)
```12:22:app/src/main/res/layout/activity_staff_login.xml
<!-- Header with Wave Background - Extended to cover status bar -->
<ImageView
    android:id="@+id/header_wave"
    android:layout_width="0dp"
    android:layout_height="180dp"
    android:scaleType="fitXY"
    android:src="@drawable/bg_wave_navy_blue"
    android:contentDescription="@string/header_background"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

- **Height**: 180dp
- **Background**: Navy blue wave shape (`bg_wave_navy_blue.xml`)
- **Wave Shape**: Curved path from top-left to top-right, creating an arc
- **Position**: Constrained to top of parent, extends behind status bar
- **Color**: `@color/navy_blue`

#### B. Staff Login Icon (`header_icon`)
```24:34:app/src/main/res/layout/activity_staff_login.xml
<!-- Staff Login Icon -->
<ImageView
    android:id="@+id/header_icon"
    android:layout_width="45dp"
    android:layout_height="45dp"
    android:src="@drawable/ic_staff_login"
    android:contentDescription="@string/staff_login_icon"
    app:layout_constraintTop_toTopOf="@+id/header_wave"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_marginTop="55dp" />
```

- **Size**: 45dp × 45dp
- **Icon**: `ic_staff_login` (three stylized human icons: two male figures, one female figure)
- **Position**: Centered horizontally, 55dp from top of header wave
- **Content**: Represents staff/users

#### C. Header Title (`header_title`)
```36:56:app/src/main/res/layout/activity_staff_login.xml
<!-- Dynamic Dashboard Title -->
<TextView
    android:id="@+id/header_title"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:fontFamily="@font/quicksand_bold"
    android:text="@string/staff_login"
    android:textSize="24sp"
    android:textStyle="bold"
    android:textColor="@android:color/white"
    android:lineSpacingExtra="2dp"
    android:textAlignment="center"
    android:shadowColor="@android:color/transparent"
    android:shadowDx="0"
    android:shadowDy="0"
    android:shadowRadius="0"
    app:layout_constraintTop_toBottomOf="@+id/header_icon"
    app:layout_constraintBottom_toBottomOf="@+id/header_wave"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    android:layout_marginTop="-20dp" />
```

- **Text**: "Staff Login"
- **Font**: Quicksand Bold
- **Size**: 24sp
- **Color**: White
- **Position**: Centered, below icon with -20dp margin (overlaps slightly)
- **Alignment**: Center

#### D. Back Button (`back_button`)
```73:87:app/src/main/res/layout/activity_staff_login.xml
<!-- Back Button -->
<ImageView
    android:id="@+id/back_button"
    android:layout_width="36dp"
    android:layout_height="36dp"
    android:src="@drawable/ic_arrow_back"
    app:tint="@android:color/white"
    android:layout_marginStart="16dp"
    android:focusable="true"
    android:clickable="true"
    android:contentDescription="@string/back_button"
    tools:ignore="TouchTargetSize"
    app:layout_constraintTop_toTopOf="@+id/header_wave"
    app:layout_constraintBottom_toBottomOf="@+id/header_wave"
    app:layout_constraintStart_toStartOf="parent" />
```

- **Size**: 36dp × 36dp
- **Icon**: White left arrow (`ic_arrow_back`)
- **Position**: Left side, 16dp from start, vertically centered in header
- **Functionality**: Navigates back (finishes activity)
- **Touch Target**: Slightly below recommended 48dp (36dp)

#### E. More Options Button (`more_option`)
```89:103:app/src/main/res/layout/activity_staff_login.xml
<!-- More Options Button -->
<ImageView
    android:id="@+id/more_option"
    android:layout_width="@dimen/_40sdp"
    android:layout_height="@dimen/_40sdp"
    android:src="@drawable/ic_more_vert_black_24dp"
    app:tint="@android:color/white"
    android:layout_marginEnd="4dp"
    android:clickable="true"
    android:focusable="true"
    android:background="?attr/selectableItemBackground"
    app:layout_constraintTop_toTopOf="@+id/header_wave"
    app:layout_constraintBottom_toBottomOf="@+id/header_wave"
    app:layout_constraintEnd_toEndOf="parent" 
    android:contentDescription="@string/more_options"/>
```

- **Size**: 40dp × 40dp
- **Icon**: White three vertical dots (`ic_more_vert_black_24dp`)
- **Position**: Right side, 4dp from end, vertically centered
- **Functionality**: Shows popup menu (Share, Rate, Change Password, Logout)
- **Background**: Selectable ripple effect

#### F. Progress Bar (`progress_bar`)
```115:131:app/src/main/res/layout/activity_staff_login.xml
<!-- Progress Bar -->
<ProgressBar
    android:id="@+id/progress_bar"
    style="?android:attr/progressBarStyleHorizontal"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp"
    android:layout_marginBottom="8dp"
    android:indeterminateOnly="true"
    android:visibility="gone"
    android:progressTint="@android:color/white"
    android:indeterminateTint="@android:color/white"
    android:focusable="false"
    app:layout_constraintBottom_toBottomOf="@+id/header_wave"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

- **Visibility**: Hidden by default (shown during login)
- **Color**: White
- **Position**: Bottom of header wave, 16dp margins on sides, 8dp from bottom
- **Style**: Horizontal indeterminate progress bar

---

## 3. MAIN CONTENT AREA (Login Form Card)

### Layout Structure
Located at lines **133-399** in `activity_staff_login.xml`

### Container: MaterialCardView
```133:148:app/src/main/res/layout/activity_staff_login.xml
<!-- Login Form Card with Navy Blue Background -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/login_form_card"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:layout_marginTop="16dp"
    android:layout_marginBottom="32dp"
    android:layout_marginStart="@dimen/_16sdp"
    android:layout_marginEnd="@dimen/_16sdp"
    app:cardBackgroundColor="@color/navy_blue"
    app:cardCornerRadius="12dp"
    app:cardElevation="6dp"
    app:layout_constraintTop_toBottomOf="@+id/header_wave"
    app:layout_constraintBottom_toTopOf="@+id/footer_container"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">
```

- **Background**: Navy blue
- **Corner Radius**: 12dp (rounded corners)
- **Elevation**: 6dp (shadow effect)
- **Margins**: 16dp horizontal, 16dp top, 32dp bottom
- **Layout**: Constrained between header wave and footer

### Form Fields (inside LinearLayout with 16dp padding):

#### A. Campus ID Field
```157:187:app/src/main/res/layout/activity_staff_login.xml
<!-- Campus ID Field -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/_8sdp"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/_4sdp"
        android:fontFamily="@font/quicksand_bold"
        android:text="@string/campus_id"
        android:textColor="@color/white"
        android:textSize="12sp" />

    <EditText
        android:id="@+id/Campus_ID"
        android:layout_width="match_parent"
        android:layout_height="36dp"
        android:background="@drawable/unified_input_background"
        android:hint="@string/enter_campus_id_hint"
        android:inputType="number"
        android:padding="@dimen/_8sdp"
        android:textColor="@color/black"
        android:textColorHint="@color/navy_blue"
        android:textSize="12sp"
        android:gravity="center_vertical"
        android:autofillHints="username" />

</LinearLayout>
```

- **Label**: "Campus ID" (white, 12sp, Quicksand Bold)
- **Input**: White background, navy blue hint, 36dp height
- **Input Type**: Number only
- **Spacing**: 8dp margin bottom, 4dp between label and input

#### B. Login ID Field
```189:219:app/src/main/res/layout/activity_staff_login.xml
<!-- Login ID Field -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/_8sdp"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/_4sdp"
        android:fontFamily="@font/quicksand_bold"
        android:text="@string/login_id"
        android:textColor="@color/white"
        android:textSize="12sp" />

    <EditText
        android:id="@+id/user_name"
        android:layout_width="match_parent"
        android:layout_height="36dp"
        android:background="@drawable/unified_input_background"
        android:hint="@string/enter_login_id_hint"
        android:inputType="number"
        android:padding="@dimen/_8sdp"
        android:textColor="@color/black"
        android:textColorHint="@color/navy_blue"
        android:textSize="12sp"
        android:gravity="center_vertical"
        android:autofillHints="username" />

</LinearLayout>
```

- **Label**: "Login ID" (white, 12sp, Quicksand Bold)
- **Input**: White background, navy blue hint, 36dp height
- **Input Type**: Number only
- **Spacing**: 8dp margin bottom, 4dp between label and input

#### C. Password Field
```221:266:app/src/main/res/layout/activity_staff_login.xml
<!-- Password Field with Toggle -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/_8sdp"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/_4sdp"
        android:fontFamily="@font/quicksand_bold"
        android:text="@string/password"
        android:textColor="@color/white"
        android:textSize="12sp" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/password_input_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:endIconMode="password_toggle"
        app:endIconTint="@color/navy_blue"
        app:boxBackgroundColor="@color/student_input_background"
        app:boxCornerRadiusTopStart="4dp"
        app:boxCornerRadiusTopEnd="4dp"
        app:boxCornerRadiusBottomStart="4dp"
        app:boxCornerRadiusBottomEnd="4dp"
        app:boxStrokeWidth="0dp"
        app:boxStrokeWidthFocused="0dp"
        app:hintEnabled="false">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/user_enter_password"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/enter_password_hint"
            android:textColorHint="@color/navy_blue"
            android:inputType="textPassword"
            android:textColor="@color/black"
            android:textSize="12sp"
            android:padding="@dimen/_8sdp"
            android:autofillHints="password" />

    </com.google.android.material.textfield.TextInputLayout>

</LinearLayout>
```

- **Label**: "Password" (white, 12sp, Quicksand Bold)
- **Input**: Material TextInputLayout with password toggle
- **Toggle Icon**: Eye icon (crossed when hidden, open when visible)
- **Icon Color**: Navy blue
- **Input Type**: Password (dots by default)
- **Custom Behavior**: Programmatically ensures password starts hidden with crossed eye icon

#### D. Privacy Policy Checkbox
```269:296:app/src/main/res/layout/activity_staff_login.xml
<!-- Privacy Policy Section -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/_12sdp"
    android:layout_marginTop="@dimen/_2sdp"
    android:orientation="horizontal">

    <com.google.android.material.checkbox.MaterialCheckBox
        android:id="@+id/privacy_policy_checkbox"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/i_agree_to_the"
        android:textColor="@color/white"
        android:textSize="12sp"
        android:buttonTint="@color/checkbox_color" />

    <TextView
        android:id="@+id/link"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:fontFamily="@font/quicksand_bold"
        android:text="@string/privacy_policy"
        android:textColor="@color/accent_yellow"
        android:textSize="12sp" />

</LinearLayout>
```

- **Checkbox**: Material checkbox with white text "I agree to the"
- **Privacy Policy Link**: Yellow text ("Privacy Policy"), clickable
- **Link Action**: Opens privacy policy URL in browser
- **Spacing**: 12dp margin bottom, 8dp between checkbox and link

#### E. Login Buttons
```299:395:app/src/main/res/layout/activity_staff_login.xml
<!-- Login Options -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical">

    <!-- Login Button -->
    <androidx.cardview.widget.CardView
        android:id="@+id/login_user"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginEnd="@dimen/_8sdp"
        android:foreground="?attr/selectableItemBackground"
        app:cardBackgroundColor="@color/transparent"
        app:cardCornerRadius="12dp"
        app:cardElevation="3dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/white"
            android:gravity="center"
            android:padding="@dimen/_10sdp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:fontFamily="@font/quicksand_bold"
                android:text="@string/sign_in"
                android:textColor="@color/navy_blue"
                android:textSize="20sp" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Biometric Login Button -->
    <androidx.cardview.widget.CardView
        android:id="@+id/biometric_login_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        android:foreground="?attr/selectableItemBackground"
        app:cardBackgroundColor="@color/transparent"
        app:cardCornerRadius="12dp"
        app:cardElevation="3dp">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@color/white"
            android:gravity="center"
            android:padding="@dimen/_12sdp">

            <ImageView
                android:layout_width="@dimen/_24sdp"
                android:layout_height="@dimen/_24sdp"
                android:src="@drawable/ic_fingerprint"
                app:tint="@color/navy_blue"
                android:contentDescription="@string/biometric_login" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

    <!-- Biometric Setup Button -->
    <androidx.cardview.widget.CardView
        android:id="@+id/biometric_setup_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        android:foreground="?attr/selectableItemBackground"
        app:cardBackgroundColor="@color/transparent"
        app:cardCornerRadius="12dp"
        app:cardElevation="3dp">

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@color/white"
            android:gravity="center"
            android:padding="@dimen/_12sdp">

            <ImageView
                android:layout_width="@dimen/_24sdp"
                android:layout_height="@dimen/_24sdp"
                android:src="@drawable/ic_fingerprint"
                app:tint="@color/navy_blue"
                android:contentDescription="@string/biometric_setup" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

</LinearLayout>
```

**Sign In Button:**
- **Text**: "Sign In" (navy blue, 20sp, Quicksand Bold)
- **Background**: White CardView with 12dp corner radius
- **Size**: Flexible width (weight=1), 8dp margin end
- **Elevation**: 3dp
- **Action**: Validates and submits login form

**Biometric Login Button:**
- **Icon**: Fingerprint icon (24dp, navy blue)
- **Visibility**: Hidden by default, shown when biometric is available and configured
- **Size**: Square button, 12dp padding
- **Action**: Triggers biometric authentication

**Biometric Setup Button:**
- **Icon**: Fingerprint icon (24dp, navy blue)
- **Visibility**: Hidden by default, shown when biometric is available but not configured
- **Size**: Square button, 12dp padding
- **Action**: Shows setup dialog

---

## 4. FOOTER SECTION

### Layout Structure
Located at lines **401-451** in `activity_staff_login.xml`

```401:451:app/src/main/res/layout/activity_staff_login.xml
<!-- Footer Container -->
<LinearLayout
    android:id="@+id/footer_container"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginTop="32dp"
    android:orientation="vertical"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">

    <!-- Footer Card -->
    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:cardCornerRadius="0dp"
        app:cardElevation="6dp"
        app:cardBackgroundColor="@color/navy_blue">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp"
            android:gravity="center_vertical|center_horizontal">

            <ImageView
                android:layout_width="18dp"
                android:layout_height="18dp"
                android:layout_marginEnd="8dp"
                android:layout_gravity="center_vertical"
                android:src="@drawable/topgrade_logo"
                android:contentDescription="@string/topgrade_logo"
                android:scaleType="fitCenter" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:fontFamily="@font/quicksand_bold"
                android:gravity="center_vertical"
                android:layout_gravity="center_vertical"
                android:text="@string/powered_by_topgrade_software"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:textStyle="bold" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

</LinearLayout>
```

### Components:

#### A. Footer Container
- **Layout**: LinearLayout (vertical)
- **Position**: Constrained to bottom of parent
- **Margin**: 32dp top margin
- **Width**: Match parent (0dp with constraints)

#### B. Footer Card
- **Background**: Navy blue CardView
- **Corner Radius**: 0dp (square corners)
- **Elevation**: 6dp (shadow)
- **Padding**: 16dp internal padding

#### C. Footer Content
- **Logo**: TopGrade logo (18dp × 18dp, circular with "TG" text, yellow background)
- **Text**: "Powered By TopGrade Software" (white, 12sp, Quicksand Bold)
- **Layout**: Horizontal LinearLayout, centered
- **Spacing**: 8dp between logo and text

---

## 5. NAVIGATION BAR (System Navigation Bar)

### Configuration (from `TeacherLogin.kt`)
```kotlin
// Navy blue navigation bar
window.navigationBarColor = ContextCompat.getColor(this, R.color.navy_blue)

// Window insets handling
setupWindowInsets() // Ensures footer is visible above navigation bar
```

### Window Insets Setup
```664:686:app/src/main/java/topgrade/parent/com/parentseeks/Teacher/Activity/TeacherLogin.kt
private fun setupWindowInsets() {
    val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root_layout)
    
    ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
        try {
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomPadding = if (systemInsets.bottom > 20) systemInsets.bottom else 0
            
            // Only apply bottom padding for navigation bar, no top padding to allow header wave to cover status bar
            view.updatePadding(
                left = 0,   // no left padding to avoid touch interference
                top = 0,    // no top padding to allow header wave to cover status bar
                right = 0,  // no right padding to avoid touch interference
                bottom = bottomPadding // only bottom padding for navigation bar
            )
            
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        } catch (e: Exception) {
            Log.e("TeacherLogin", "Error in window insets listener: ${e.message}")
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }
    }
}
```

### Visual Elements:
- **Background**: Navy blue (matches app theme)
- **Icons**: System navigation icons (back, home, recent apps)
- **Height**: System-defined (typically 48dp on most devices)
- **Padding**: Root layout applies bottom padding to prevent content overlap
- **Position**: Fixed at bottom of screen

---

## 6. OVERALL LAYOUT STRUCTURE

### Root Container
```2:10:app/src/main/res/layout/activity_staff_login.xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white"
    android:paddingBottom="0dp"
    android:fitsSystemWindows="true">
```

- **Layout Type**: ConstraintLayout
- **Background**: White
- **System Windows**: `fitsSystemWindows="true"` (allows content to extend behind system bars)
- **Padding**: No bottom padding (handled by window insets)

### Layout Hierarchy:
```
ConstraintLayout (root_layout)
├── ImageView (header_wave) - 180dp height, navy blue wave
│   ├── ImageView (header_icon) - 45dp, centered
│   ├── TextView (header_title) - "Staff Login", white
│   ├── ImageView (back_button) - 36dp, left side
│   ├── ImageView (more_option) - 40dp, right side
│   └── ProgressBar (progress_bar) - bottom of header
├── MaterialCardView (login_form_card) - navy blue, 12dp radius
│   └── LinearLayout (vertical)
│       ├── Campus ID Field
│       ├── Login ID Field
│       ├── Password Field
│       ├── Privacy Policy Checkbox
│       └── Login Buttons (Sign In + Biometric)
└── LinearLayout (footer_container) - bottom
    └── CardView (navy blue)
        └── LinearLayout (horizontal)
            ├── ImageView (logo)
            └── TextView ("Powered By TopGrade Software")
```

### Constraints:
- **Header Wave**: Top of parent, full width
- **Login Card**: Below header wave, above footer, 16dp margins
- **Footer**: Bottom of parent, full width

---

## 7. COLOR SCHEME

### Primary Colors:
- **Navy Blue** (`@color/navy_blue`): Header, login card, footer, navigation bar
- **White** (`@color/white`): Text on dark backgrounds, input fields, buttons
- **Yellow** (`@color/accent_yellow`): Privacy policy link text
- **Black** (`@color/black`): Input text

### Usage:
- **Dark Backgrounds**: Navy blue (header, card, footer, navigation bar)
- **Light Backgrounds**: White (input fields, buttons)
- **Text on Dark**: White
- **Text on Light**: Black/Navy blue
- **Accent**: Yellow (links)

---

## 8. TYPOGRAPHY

### Font Family:
- **Primary**: Quicksand Bold (`@font/quicksand_bold`)
- Used for: Labels, titles, buttons, footer text

### Font Sizes:
- **Header Title**: 24sp
- **Button Text**: 20sp
- **Labels**: 12sp
- **Input Text**: 12sp
- **Footer Text**: 12sp

---

## 9. SPACING & MARGINS

### Standard Spacings:
- **Card Margins**: 16dp horizontal, 16dp top, 32dp bottom
- **Field Spacing**: 8dp between fields, 4dp between label and input
- **Button Spacing**: 8dp between Sign In and Biometric buttons
- **Footer Margin**: 32dp top margin
- **Header Elements**: 16dp side margins, 55dp top margin for icon

---

## 10. INTERACTIVE ELEMENTS

### Clickable Elements:
1. **Back Button**: Finishes activity
2. **More Options**: Shows popup menu (Share, Rate, Change Password, Logout)
3. **Privacy Policy Link**: Opens browser
4. **Sign In Button**: Validates and submits login
5. **Biometric Button**: Triggers fingerprint/face authentication
6. **Biometric Setup Button**: Shows setup dialog

### Form Validation:
- Campus ID: Required, numeric
- Login ID: Required, numeric
- Password: Required
- Privacy Policy: Must be checked

---

## 11. RESPONSIVE DESIGN

### Edge-to-Edge Display:
- Uses `WindowCompat.setDecorFitsSystemWindows(window, false)` for modern Android edge-to-edge display
- Header wave extends behind status bar
- Footer respects navigation bar with window insets

### Window Insets Handling:
- Top: No padding (header wave covers status bar)
- Bottom: Dynamic padding based on navigation bar height
- Left/Right: No padding (full width)

---

## 12. ACCESSIBILITY

### Content Descriptions:
- All ImageViews have `contentDescription` attributes
- Icons are properly labeled for screen readers

### Touch Targets:
- Most buttons meet 48dp minimum (back button is 36dp, slightly below)
- Selectable backgrounds provide visual feedback

---

## Summary

The Staff Login page features:
- **Modern Design**: Wave-shaped header, rounded cards, elevation shadows
- **Consistent Theme**: Navy blue and white color scheme throughout
- **Edge-to-Edge**: Extends behind system bars for immersive experience
- **Biometric Support**: Optional fingerprint/face authentication
- **Responsive**: Handles different screen sizes and system bar configurations
- **Accessible**: Proper content descriptions and touch targets

The layout is well-structured using ConstraintLayout for efficient rendering and proper constraint relationships between all elements.

