@echo off
REM ========================================
REM Staff Application Submission - Curl Test
REM ========================================
REM 
REM This curl command submits a leave application
REM by a staff member, exactly as the Android app does
REM
REM API Endpoint: api.php?page=teacher/leave_applicaton
REM Method: POST
REM Content-Type: application/json
REM ========================================

echo.
echo ========================================
echo STAFF APPLICATION SUBMISSION TEST
echo ========================================
echo.
echo Submitting Leave Application...
echo.

curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" ^
-H "Content-Type: application/json" ^
-d "{\"operation\":\"add_application\",\"campus_id\":\"5c67f03e5c3da\",\"staff_id\":\"6876c43fd910b\",\"application_title\":\"Medical Leave Request\",\"applictaion_body\":\"I am requesting leave due to medical reasons. I will be visiting the doctor and need time for recovery.\",\"start_date\":\"04/11/2025\",\"end_date\":\"06/11/2025\"}" ^
-v

echo.
echo.
echo ========================================
echo Test Complete!
echo ========================================
echo.
echo Expected Response:
echo   - HTTP/2 200 = SUCCESS
echo   - Status Code: "1000" = Application Submitted
echo   - Status Message: Success message
echo.
echo Error Responses:
echo   - HTTP/2 500 = SERVER ERROR
echo   - Status Code: other = Check message for details
echo.
echo ========================================
pause

