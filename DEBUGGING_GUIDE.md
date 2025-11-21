# How to Check Logic for Exam Session Loading

## 1. **On-Screen Debug Panel** (Easiest Method)
The app now shows a debug panel on screen that displays:
- Request parameters being sent
- API response received
- Number of exam sessions found
- Any errors

**To check:**
1. Open the Date Sheet activity
2. Look at the debug panel (gray box above the SMS button)
3. You'll see:
   - Request URL and parameters
   - Response status
   - Exam sessions count
   - Session details (ID, Name)

## 2. **Android Logcat** (Detailed Logs)
Check Android Studio Logcat for detailed logs:

**Filter by tag:** `StudentDateSheet`

**Key log messages to look for:**
```
=== LOADING EXAM SESSIONS ===
Parent ID (Campus ID): [your_campus_id]
Exam Session JSON Body: {"parent_id":"..."}
Exam Session Response Code: 200
Exam Sessions received: [number]
Exam Session 0: [name] (ID: [id])
```

**Steps:**
1. Open Android Studio
2. Connect device/emulator
3. Open Logcat window
4. Filter by: `StudentDateSheet`
5. Look for logs starting with `=== LOADING EXAM SESSIONS ===`

## 3. **Check API Response Manually**
Use curl or Postman to test the API directly:

```bash
curl -X POST "https://topgradesoftware.com/api.php?page=parent/load_exam_session" \
  -H "Content-Type: application/json" \
  -d '{"parent_id": "YOUR_CAMPUS_ID"}'
```

**Expected Response:**
```json
{
  "status": {
    "code": "1000",
    "message": "Success."
  },
  "exam_session": [
    {
      "unique_id": "...",
      "full_name": "...",
      "display_order": 1
    }
  ]
}
```

## 4. **Check Data Flow Step by Step**

### Step 1: Verify Request is Sent
Look for in Logcat:
```
[DEBUG] Exam session request parameters displayed on screen
Exam Session JSON Body: {"parent_id":"..."}
```

### Step 2: Verify Response Received
Look for:
```
Exam Session Response Code: 200
Exam Session API Status Code: 1000
Exam Sessions received: [number]
```

### Step 3: Verify Data Parsing
Look for:
```
Exam Session 0: [Session Name] (ID: [Session ID])
Total Sessions: [number]
```

### Step 4: Verify UI Update
Check if spinner is populated:
- Multiple sessions: Spinner should show all sessions
- Single session: Auto-selected, spinner hidden
- No sessions: Toast message "No exam sessions available"

## 5. **Common Issues to Check**

### Issue 1: Empty Exam Session List
**Check:**
- Debug panel shows "Total Sessions: 0"
- Logcat shows: "No exam sessions available for this student"
- **Solution:** Verify `parent_id` is correct and has exam sessions in database

### Issue 2: API Not Called
**Check:**
- Debug panel doesn't show request
- Logcat doesn't show "=== LOADING EXAM SESSIONS ==="
- **Solution:** Check if `campusId` is available and `loadExamSession()` is being called

### Issue 3: Response Parsing Error
**Check:**
- Logcat shows: "Error in loadExamSession onResponse"
- Debug panel shows error message
- **Solution:** Check if response structure matches expected format

### Issue 4: Spinner Not Updating
**Check:**
- Logcat shows sessions received but spinner is empty
- **Solution:** Check if `selectExamSession` is properly initialized

## 6. **Quick Debug Checklist**

- [ ] Debug panel visible on screen?
- [ ] Request shows correct `parent_id`?
- [ ] Response code is 200?
- [ ] API status code is "1000"?
- [ ] Exam sessions count > 0?
- [ ] Session names appear in debug panel?
- [ ] Spinner populated with sessions?
- [ ] No errors in Logcat?

## 7. **Breakpoint Debugging** (Advanced)

Set breakpoints in Android Studio:
1. Line 914: `postParam.put("parent_id", campus_id);` - Check if campus_id is correct
2. Line 978: `if (sessionModel.getStatus().getCode().equals("1000"))` - Check response status
3. Line 979: `examSessionList = sessionModel.getExamSession();` - Check if list is populated
4. Line 991: `if (session != null && session.getFullName() != null)` - Check session data

## 8. **Network Inspection**

Use network monitoring tools:
- **Charles Proxy** or **Fiddler** to intercept HTTP requests
- Check actual request/response
- Verify headers and body

## 9. **Database Check** (Backend)

If you have database access, verify:
```sql
SELECT * FROM exam_session e 
WHERE e.parent_id = 'YOUR_CAMPUS_ID' 
AND e.is_delete = 0 
AND e.is_active = 1 
ORDER BY display_order ASC;
```

This should return the same data the API returns.

