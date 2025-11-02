@echo off
echo ========================================
echo Testing EXACT Leave Application API Call
echo (Same as Android app sends)
echo ========================================
echo.

curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" ^
-H "Content-Type: application/json" ^
-d "{\"operation\":\"add_application\",\"campus_id\":\"5c67f03e5c3da\",\"staff_id\":\"6876c43fd910b\",\"application_title\":\"Ggg\",\"applictaion_body\":\"ggfffff\",\"start_date\":\"19/11/2025\",\"end_date\":\"21/11/2025\"}" ^
-v

echo.
echo.
echo ========================================
echo Test Complete!
echo ========================================
echo Look for:
echo   - HTTP/2 200 = SUCCESS
echo   - HTTP/2 500 = SERVER ERROR
echo   - Check the response body for error details
echo ========================================
pause

