# Date Sheet Analysis & XML Layouts

## 📋 **Overview**

The Date Sheet functionality in the Topgrade Software App allows students and parents to view exam schedules, dates, subjects, and times. It's a comprehensive exam scheduling system with advanced search capabilities.

## 🏗️ **Architecture**

### **Main Components:**
1. **StudentDateSheet.java** - Main activity for date sheet display
2. **StudentDateSheetAdaptor.java** - RecyclerView adapter for date sheet items
3. **DateSheetData.java** - Data model for date sheet entries
4. **DateSheetResponse.java** - API response model
5. **DateAdaptor.java** - Adapter for date selection

## 📱 **XML Layouts**

### **1. Main Activity Layout: `activity_student_datesheet.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="vertical">

    <!-- Header Section -->
    <RelativeLayout
        android:id="@+id/header"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:layout_alignParentTop="true"
        android:background="@color/colorAccent"
        android:orientation="horizontal">

        <!-- Back Button -->
        <ImageView
            android:id="@+id/back_icon_student_timetable"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerVertical="true"
            android:layout_marginStart="5dp"
            android:layout_marginLeft="5dp"
            android:layout_marginTop="5dp"
            android:layout_marginEnd="5dp"
            android:layout_marginRight="5dp"
            android:layout_marginBottom="5dp"
            app:srcCompat="@drawable/ic_arrow_back" />

        <!-- Title -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerVertical="true"
            android:textSize="@dimen/_20sdp"
            android:layout_marginStart="26dp"
            android:layout_marginLeft="8dp"
            android:layout_marginTop="5dp"
            android:layout_marginEnd="5dp"
            android:layout_marginBottom="5dp"
            android:layout_toEndOf="@+id/back_icon_student_timetable"
            android:layout_toRightOf="@+id/back_icon_student_timetable"
            android:text="@string/date_sheet"
            android:textColor="@color/white"
            android:fontFamily="@font/quicksand_bold" />

        <!-- Advanced Search Button -->
        <TextView
            android:id="@+id/tv_advanced_search"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentEnd="true"
            android:layout_alignParentRight="true"
            android:layout_centerVertical="true"
            android:drawablePadding="@dimen/_10sdp"
            android:gravity="center_vertical"
            android:text="Advanced Search"
            android:textColor="@color/white"
            android:fontFamily="@font/quicksand_bold"
            app:drawableEndCompat="@drawable/ic_arrow_drop_down_white_24dp" />
    </RelativeLayout>

    <!-- Main Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/header"
        android:layout_margin="@dimen/_5sdp"
        android:orientation="vertical">

        <!-- Date Sheet Container -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_marginTop="@dimen/_5sdp"
            android:background="@drawable/style_border_gray"
            android:orientation="vertical"
            android:layout_below="@id/header"
            android:padding="1dp">

            <!-- Student Info Header -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:background="@color/green">

                <!-- Student Name -->
                <TextView
                    android:id="@+id/tv_student_name_timetable"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="start"
                    android:layout_weight="1"
                    android:background="@color/colorAccent"
                    android:padding="@dimen/_4sdp"
                    android:text="@string/name"
                    android:textColor="@color/white"
                    android:textSize="@dimen/_12sdp" />

                <!-- Class Info -->
                <TextView
                    android:id="@+id/tv_class_timetable"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="end"
                    android:layout_weight="1"
                    android:background="@color/colorAccent"
                    android:padding="@dimen/_4sdp"
                    android:text="Class: "
                    android:textColor="@color/white"
                    android:textSize="@dimen/_12sdp" />
            </LinearLayout>

            <!-- Table Header -->
            <LinearLayout
                android:id="@+id/header_rv"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="#800000"
                android:divider="@drawable/style_divider_white"
                android:orientation="horizontal"
                android:showDividers="middle"
                android:weightSum="6">

                <!-- Serial Number -->
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="0.7"
                    android:background="#7EAFEC"
                    android:gravity="center"
                    android:padding="@dimen/_2sdp"
                    android:text="@string/serial_number"
                    android:textColor="@color/white"
                    android:textSize="@dimen/_10sdp"
                    android:fontFamily="@font/quicksand_bold" />

                <!-- Date -->
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="1.8"
                    android:background="#7EAFEC"
                    android:gravity="center"
                    android:padding="@dimen/_2sdp"
                    android:text="@string/date"
                    android:textColor="@color/white"
                    android:textSize="@dimen/_10sdp"
                    android:fontFamily="@font/quicksand_bold" />

                <!-- Subject -->
                <TextView
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="1"
                    android:background="#7EAFEC"
                    android:gravity="center"
                    android:padding="@dimen/_2sdp"
                    android:text="@string/subject"
                    android:textColor="@color/white"
                    android:textSize="@dimen/_10sdp"
                    android:fontFamily="@font/quicksand_bold" />

                <!-- Time/Syllabus -->
                <TextView
                    android:id="@+id/tv_header_time_or_syllabus"
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="2.5"
                    android:background="#7EAFEC"
                    android:gravity="center"
                    android:padding="@dimen/_2sdp"
                    android:text="@string/time"
                    android:textColor="@color/white"
                    android:textSize="@dimen/_10sdp"
                    android:fontFamily="@font/quicksand_bold" />

            </LinearLayout>

            <!-- Divider -->
            <View
                android:id="@+id/line_2"
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="@color/color_gray_dark" />

            <!-- RecyclerView for Date Sheet Items -->
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rv_student_timetable"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
                tools:listitem="@layout/date_sheet_student_item" />

        </LinearLayout>

        <!-- Total Records -->
        <TextView
            android:id="@+id/total_records"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="@dimen/_5sdp"
            android:text="Total Records:"
            android:textColor="@android:color/black"
            android:textSize="@dimen/_10sdp"
            android:fontFamily="@font/quicksand_bold" />
    </LinearLayout>

    <!-- Progress Bar -->
    <ProgressBar
        android:id="@+id/progress_bar_student_timetable"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:visibility="gone" />
</RelativeLayout>
```

### **2. Date Sheet Item Layout: `date_sheet_student_item.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/row_student_timetable"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <!-- Break Time Row -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:id="@+id/linear_break_student_timetable"
        android:background="@color/colorAccent"
        android:divider="@drawable/style_divider_gray"
        android:showDividers="middle"
        android:weightSum="6">
        
        <!-- Break Serial Number -->
        <TextView
            android:id="@+id/tv_serial_numb_student_timetable_break"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="0.7"
            android:gravity="center"
            android:padding="@dimen/_2sdp"
            android:text="@string/serial_number"
            android:textColor="@color/white"
            android:textSize="@dimen/_10ssp"/>

        <!-- Break Time Text -->
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/break_time"
            android:textColor="@color/white"
            android:textSize="@dimen/_18ssp"
            android:gravity="center_horizontal"
            android:layout_weight="5.3"/>
    </LinearLayout>

    <!-- Subject Row -->
    <LinearLayout
        android:id="@+id/header_rv_student"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:divider="@drawable/style_divider_gray"
        android:orientation="horizontal"
        android:showDividers="middle"
        android:weightSum="6">

        <!-- Serial Number -->
        <TextView
            android:id="@+id/tv_serial_no_student_timetable"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="0.7"
            android:gravity="center"
            android:padding="@dimen/_2sdp"
            android:text="@string/serial_number"
            android:textSize="@dimen/_10ssp" />

        <!-- Date/Teacher Name -->
        <TextView
            android:id="@+id/tv_teacher_name_student_timetable"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1.8"
            android:gravity="center"
            android:padding="@dimen/_2sdp"
            android:text="@string/date"
            android:textSize="@dimen/_10ssp" />

        <!-- Subject Name -->
        <TextView
            android:id="@+id/tv_subject_name_student_timetable"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:gravity="center"
            android:padding="@dimen/_2sdp"
            android:text="@string/subject"
            android:textSize="@dimen/_10ssp" />

        <!-- Time -->
        <TextView
            android:id="@+id/tv_time_student_timetable"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="2.5"
            android:gravity="center"
            android:padding="@dimen/_2sdp"
            android:text="@string/time"
            android:textSize="@dimen/_10ssp" />

    </LinearLayout>

    <!-- Divider -->
    <View
        android:id="@+id/line_2"
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="@color/color_gray_dark" />
</LinearLayout>
```

### **3. Date Item Layout: `date_item.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="@dimen/_90sdp"
    android:layout_height="@dimen/_30sdp"
    android:background="@drawable/bg_tiles">

    <!-- Date Text -->
    <TextView
        android:id="@+id/date"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:padding="@dimen/_7sdp"
        android:textColor="@color/white"
        android:textSize="@dimen/_10sdp"
        android:fontFamily="@font/quicksand_bold" />

    <!-- Bottom Border -->
    <View
        android:layout_width="match_parent"
        android:layout_height="@dimen/_1sdp"
        android:layout_alignParentBottom="true"
        android:background="@color/color_gray_dark" />

</RelativeLayout>
```

## 🔧 **Key Features**

### **1. Advanced Search Dialog**
- Student selection dropdown
- Exam session selection
- Filter functionality
- Search button

### **2. Date Sheet Display**
- Student information header
- Tabular format with columns:
  - Serial Number
  - Date
  - Subject
  - Time/Syllabus
- Alternating row colors
- Break time indicators

### **3. Data Models**
- **DateSheetData**: Individual date sheet entry
- **DateSheetResponse**: API response wrapper
- **DateSheetFile**: File attachments
- **HeaderFooter**: Header/footer information

## 🎨 **Design Elements**

### **Colors Used:**
- `@color/colorAccent` - Primary accent color
- `@color/green` - Success/positive color
- `#800000` - Dark red header
- `#7EAFEC` - Light blue column headers
- `@color/white` - Text on dark backgrounds
- `@color/color_gray_dark` - Dividers and borders

### **Typography:**
- `@font/quicksand_bold` - Bold text for headers
- `@font/quicksand_light` - Light text for secondary content

### **Dimensions:**
- `@dimen/_10sdp` - Standard text size
- `@dimen/_12sdp` - Header text size
- `@dimen/_20sdp` - Title text size
- `@dimen/_90sdp` - Date item width
- `@dimen/_30sdp` - Date item height

## 📊 **Layout Structure**

```
activity_student_datesheet.xml
├── Header (Back button + Title + Advanced Search)
├── Main Content
│   ├── Student Info (Name + Class)
│   ├── Table Header (Serial, Date, Subject, Time)
│   ├── RecyclerView (Date Sheet Items)
│   └── Total Records Counter
└── Progress Bar
```

## 🔄 **Data Flow**

1. **User selects student** from dropdown
2. **User selects exam session** from dropdown
3. **API call** to load date sheet data
4. **Data parsed** into DateSheetData objects
5. **RecyclerView populated** with date sheet items
6. **Alternating colors** applied for better readability

## 🎯 **Usage**

The date sheet functionality provides:
- **Exam scheduling** for students
- **Date and time** information for each exam
- **Subject details** for each exam
- **Advanced filtering** by student and session
- **Responsive design** that works on different screen sizes

This comprehensive date sheet system helps students and parents stay informed about exam schedules and important academic dates. 