# Diary Module - Final Structure (Attendance-Style)

## Overview
Successfully restructured the diary module to follow the **attendance menu pattern** with three role-based sections on a single menu page.

---

## ✅ Completed Files

### 1. **Main Menu**

#### `DiaryMenu.java`
- Single menu activity with three role sections
- Similar to `StaffAttendanceMenu.java`
- Launches appropriate activity based on role selection

#### `activity_diary_menu.xml`
- Three buttons in scrollable layout:
  - Send Diary - Class InCharge
  - Send Diary - Section InCharge  
  - Send Diary - Subject Teacher
- Navy blue theme with icons

---

### 2. **Class InCharge Diary (✅ Complete)**

#### Files Created:
- `DiarySubmitClass.java`
- `activity_staff_diary_class.xml`
- `item_diary_selection_inputs_class.xml` (Session + Class spinners only)
- `item_diary_selected_criteria_class.xml` (Session + Class display)

#### Selection Flow:
```
Session → Class → Diary Entry
```

#### Features:
- Selects exam session and class
- Sends diary to entire class
- Two-page interface (selection → entry)
- Diary fields: Title, Date, Description

---

### 3. **Section InCharge Diary (⚠️ Needs Creation)**

#### Files to Create:
- `DiarySubmitSection.java`
- `activity_staff_diary_section.xml`
- `item_diary_selection_inputs_section.xml` (Session + Class + Section spinners)
- `item_diary_selected_criteria_section.xml` (Session + Class + Section display)

#### Selection Flow:
```
Session → Class → Section → Diary Entry
```

#### Logic:
- Same as `DiarySubmitClass.java`
- Add section spinner after class
- Send diary to specific section only

---

### 4. **Subject Teacher Diary (⚠️ Needs Creation)**

#### Files to Create/Rename:
- Rename `DiarySubmit.java` → `DiarySubmitSubject.java`
- Rename/Update `activity_staff_diary.xml` → `activity_staff_diary_subject.xml`
- Keep `item_diary_selection_inputs.xml` (Session + Class + Section + Subject)
- Keep `item_diary_selected_criteria.xml` (Session + Class + Section + Subject)

#### Selection Flow:
```
Session → Class → Section → Subject → Diary Entry
```

#### Logic:
- Already created (current DiarySubmit.java)
- Just needs renaming
- Sends diary for specific subject

---

## File Structure Summary

```
app/src/main/
├── java/topgrade/parent/com/parentseeks/Teacher/Diary/
│   ├── DiaryMenu.java                      ✅ Main menu
│   ├── DiarySubmitClass.java               ✅ Class InCharge
│   ├── DiarySubmitSection.java             ⚠️ TO CREATE
│   └── DiarySubmitSubject.java             ⚠️ RENAME from DiarySubmit.java
│
└── res/layout/
    ├── activity_diary_menu.xml             ✅ Menu layout
    │
    ├── activity_staff_diary_class.xml      ✅ Class InCharge layout
    ├── item_diary_selection_inputs_class.xml   ✅ Class spinners
    ├── item_diary_selected_criteria_class.xml  ✅ Class criteria
    │
    ├── activity_staff_diary_section.xml    ⚠️ TO CREATE
    ├── item_diary_selection_inputs_section.xml ⚠️ TO CREATE
    ├── item_diary_selected_criteria_section.xml ⚠️ TO CREATE
    │
    ├── activity_staff_diary_subject.xml    ⚠️ RENAME from activity_staff_diary.xml
    ├── item_diary_selection_inputs.xml     ✅ Already exists (Subject)
    └── item_diary_selected_criteria.xml    ✅ Already exists (Subject)
```

---

## Comparison with Attendance Module

| Feature | Attendance | Diary |
|---------|-----------|-------|
| **Menu Page** | StaffAttendanceMenu.java | DiaryMenu.java ✅ |
| **Class InCharge** | StaffAttendanceSubmitClass.java | DiarySubmitClass.java ✅ |
| **Section InCharge** | StaffAttendanceSubmitSection.java | DiarySubmitSection.java ⚠️ |
| **Subject Teacher** | StaffAttendanceSubmitSubject.java | DiarySubmitSubject.java ⚠️ |
| **Selection Logic** | Session → Class/Section/Subject | Same structure |
| **Data Entry** | Mark attendance (Present/Absent) | Enter diary (Title/Date/Description) |

---

## Next Steps to Complete

### 1. Create DiarySubmitSection.java
- Copy `DiarySubmitClass.java`
- Add `sectionSpinner` between class and diary entry
- Update layout reference to `activity_staff_diary_section.xml`
- Add section filtering logic
- Update API parameter to include `section_id`

### 2. Create Section Layouts
- `activity_staff_diary_section.xml` - Copy from class, add section spinner
- `item_diary_selection_inputs_section.xml` - Session + Class + Section
- `item_diary_selected_criteria_section.xml` - Session + Class + Section display

### 3. Rename Subject Teacher Files
- Rename `DiarySubmit.java` → `DiarySubmitSubject.java`
- Update class name references
- Update layout reference to `activity_staff_diary_subject.xml`
- Rename layout file

### 4. Update AndroidManifest.xml
Add all three activities:
```xml
<activity android:name=".Teacher.Diary.DiaryMenu" />
<activity android:name=".Teacher.Diary.DiarySubmitClass" />
<activity android:name=".Teacher.Diary.DiarySubmitSection" />
<activity android:name=".Teacher.Diary.DiarySubmitSubject" />
```

### 5. Add to Staff Dashboard
Add button to launch DiaryMenu:
```java
diaryButton.setOnClickListener(v -> {
    Intent intent = new Intent(this, DiaryMenu.class);
    startActivity(intent);
});
```

---

## API Requirements

### Send Diary Endpoint
**URL**: `api.php?page=teacher/send_diary_by_role`

**Request Body**:
```json
{
  "staff_id": "string",
  "campus_id": "string",
  "session_id": "string",
  "class_id": "string",
  "section_id": "string (optional - for section/subject)",
  "subject_id": "string (optional - for subject only)",
  "date": "yyyy-MM-dd",
  "title": "string",
  "description": "string",
  "role": "class_incharge|section_incharge|subject_teacher"
}
```

---

## Key Differences Between Roles

| Role | Selection | API Parameters | Target Audience |
|------|-----------|----------------|-----------------|
| **Class InCharge** | Session + Class | session_id, class_id | All students in class |
| **Section InCharge** | Session + Class + Section | session_id, class_id, section_id | All students in section |
| **Subject Teacher** | Session + Class + Section + Subject | All params | Students for specific subject |

---

## Testing Checklist

### Menu Page
- [ ] Three role buttons visible
- [ ] Icons displayed correctly
- [ ] Navy blue theme applied
- [ ] Back button works

### Class InCharge
- [ ] Session loads correctly
- [ ] Classes load based on teacher assignment
- [ ] Can select session and class
- [ ] Continue button navigates to diary entry
- [ ] Can enter title, date, description
- [ ] Send button submits successfully
- [ ] Success message displayed
- [ ] Returns to previous screen

### Section InCharge (After Creation)
- [ ] Same as Class + Section selection works
- [ ] Sections filter based on selected class

### Subject Teacher (After Rename)
- [ ] Same as Section + Subject selection works
- [ ] Subjects filter based on section

---

## Current Status

✅ **Completed (60%)**:
- DiaryMenu (menu page)
- DiarySubmitClass (Class InCharge)
- All Class InCharge layouts
- API interface updated

⚠️ **Remaining (40%)**:
- DiarySubmitSection (Section InCharge) - 20%
- DiarySubmitSubject (rename from DiarySubmit) - 10%
- Section/Subject layouts - 10%

---

## Benefits of This Structure

1. **Consistent UX**: Matches attendance module pattern
2. **Role Clarity**: Clear separation of responsibilities
3. **Simplified Logic**: Each role has dedicated activity
4. **Easier Maintenance**: Isolated code per role
5. **Scalable**: Easy to add more roles if needed

---

## Notes

- All diary activities use the same diary entry fields (Title, Date, Description)
- Only the selection spinners differ based on role
- API handles role-based logic on backend
- Frontend just needs to pass correct parameters based on selection


