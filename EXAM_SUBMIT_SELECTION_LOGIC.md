# Exam Submit - Selection Logic and Working Flow

## Overview
The ExamSubmit activity manages exam mark entry with a two-page flow: Selection Page and Data Entry Page.

## Selection Logic Flow

### 1. **Initialization (onCreate)**
```
1. Set layout: activity_staff_exam.xml
2. Initialize UI components (RecyclerView, buttons, spinners, etc.)
3. Show selection page (hide data section)
4. Call Show_Advanced_Option() to initialize spinners
5. Call loadExamSession() to load exam sessions
```

### 2. **Selection Page Components**
- **Exam Session Spinner** - Select academic exam session
- **Class Spinner** - Select class
- **Section Spinner** - Select section  
- **Subject Spinner** - Select subject
- **Exam Spinner** - Select specific exam
- **Load Students Button** - Trigger data loading

### 3. **Selection Workflow**

#### Step 1: Load Exam Sessions
```java
loadExamSession()
├── Read from PaperDB (Constants.exam_session)
├── If null/empty → loadExamSessionsFromAPI()
├── Populate examSessionSpinner
└── Set selection listener → stores session_id
```

#### Step 2: Load Classes (on session selection)
```java
Load_Class()
├── API: load_profile(staff_id, campus_id, session_id)
├── Filter unique class names
├── Populate classSpinner
└── Set selection listener → stores class_id
    └── Triggers setupSectionAdapter() and filters subjects
```

#### Step 3: Load Sections (on class selection)
```java
setupSectionAdapter(filteredSections, list)
├── Filter sections for selected class
├── Populate sectionSpinner
└── Set selection listener → stores sectionId
    └── Triggers setupSubjectAdapter()
```

#### Step 4: Load Subjects (on section selection)
```java
setupSubjectAdapter()
├── Filter subjects for selected class & section
├── Populate subjectSpinner
└── Set selection listener → stores subject_id
    └── Triggers loadExams()
```

#### Step 5: Load Exams (on subject selection)
```java
loadExams()
├── API: load_exams(staff_id, campus_id, class_id, subject_id, session_id, section_id)
├── Populate examSpinner with exam names
└── Set selection listener → stores test_id and total_marks
```

### 4. **Load Students (selection_button click)**
```java
loadExamResult()
├── Validate all selections (session, class, section, subject, exam)
├── API: load_exams_results(staff_id, campus_id, class_id, subject_id, section_id, exam_id)
├── Parse results into exam_submit_list
├── Show data section (hide selection page)
├── Set RecyclerView adapter with ExamAdaptor
└── Display total records count
```

## Data Entry Page Flow

### 1. **Components**
- Info bar (Session, Exam, Date, Total Records)
- SMS checkbox
- RecyclerView with student list
- Submit Marks button

### 2. **Mark Entry**
```java
OnMarksEnter(position, obtained_marks)
├── Validate marks ≤ total_marks
├── Update exam_submit_list[position]
└── Set attendance = 1 if marks > 0, else 0
```

### 3. **SMS Toggle**
```java
onCheckedChanged(compoundButton, isChecked)
├── Update all items in exam_submit_list
└── Refresh RecyclerView adapter
```

### 4. **Submit Marks**
```java
Submit_Marks button click
├── Validate all students have marks
├── Show_Pwd_Dialog()
│   ├── Verify staff password from PaperDB
│   └── On success → Submit_Marks_API()
└── Submit_Marks_API()
    ├── Convert exam_submit_list to JSON
    ├── API: save_exam_results(staff_id, campus_id, class_id, subject_id, section_id, exam_id, results)
    └── Show success/error toast
```

## Navigation Flow

### Forward Navigation
```
Selection Page → [Load Students] → Data Entry Page
```

### Backward Navigation
```
Data Entry Page → [Back button] → Selection Page
Selection Page → [Back button] → Close Activity
```

### Back Button Behavior
```java
onBackPressed() / back_icon.onClick()
├── If data_section is VISIBLE
│   └── showSelectionPage()
└── Else
    └── finish() / super.onBackPressed()
```

## Visibility Management

### showSelectionPage()
- selection_page: VISIBLE
- data_section: GONE

### showDataSection()
- selection_page: GONE
- data_section: VISIBLE

## Key Variables

### Selection State
- `session_id` - Selected exam session ID
- `class_id` - Selected class ID
- `sectionId` - Selected section ID
- `subject_id` - Selected subject ID
- `test_id` - Selected exam ID
- `total_marks` - Total marks for selected exam

### Data Lists
- `examSessionslist` - List of exam sessions
- `classForSubjectList` - List of class names
- `sectionList` - List of sections
- `subjectList` - List of subjects
- `examList` - List of exams
- `resultList` - List of students with results
- `exam_submit_list` - List of marks to submit

## API Endpoints Used

1. **load_exam_session_teacher** - Load exam sessions
2. **load_profile** - Load teacher's classes/subjects
3. **load_exams** - Load exams for class/subject
4. **load_exams_results** - Load student list for exam
5. **save_exam_results** - Submit marks

## Error Handling

### Selection Validation
- Shows toast if any selection is missing
- Prevents API call until all fields selected

### API Errors
- Network failures → Toast message
- Empty results → Keep selection page visible
- Invalid responses → Log error and show message

### Mark Entry Validation
- Marks > total_marks → Warning toast
- Empty marks → Cannot submit
- Missing password → Cannot submit

## UI States

### Loading State
- Progress bar: VISIBLE
- User interaction: Disabled

### Success State
- Data section: VISIBLE
- RecyclerView: VISIBLE with data
- Total records: Updated

### Error State
- Selection page: VISIBLE
- Error toast: Displayed
- Previous selections: Retained

## Testing Checklist

- [ ] All spinners populate correctly
- [ ] Sequential selection works (session → class → section → subject → exam)
- [ ] Load Students button validates all selections
- [ ] Student list displays correctly
- [ ] Mark entry updates data
- [ ] SMS toggle affects all students
- [ ] Submit validates all marks entered
- [ ] Password verification works
- [ ] API submission succeeds
- [ ] Back navigation works correctly
- [ ] Error states handle gracefully

