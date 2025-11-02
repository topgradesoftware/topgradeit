@echo off
echo ========================================
echo Testing with NEW DATES (not submitted before)
echo ========================================
echo.

curl -X POST "https://topgradesoftware.com/api.php?page=teacher/leave_applicaton" ^
-H "Content-Type: application/json" ^
-d "{\"operation\":\"add_application\",\"campus_id\":\"5c67f03e5c3da\",\"staff_id\":\"6876c43fd910b\",\"application_title\":\"Testing Fixed API\",\"applictaion_body\":\"This is a test with new dates\",\"start_date\":\"28/11/2025\",\"end_date\":\"29/11/2025\"}" ^
-v

echo.
echo.
echo ========================================
echo Expected Response:
echo   {"status":{"code":"1000","message":"Application Submit."}}
echo ========================================
pause

