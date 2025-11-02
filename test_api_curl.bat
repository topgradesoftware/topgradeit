@echo off
echo Testing Leave Application API...
echo.

curl -X POST "https://topgradesoftware.com/api/teacher/leave_application/leave_applicaton.php" ^
-H "Content-Type: application/json" ^
-d "{\"operation\":\"add_application\",\"campus_id\":\"5c67f03e5c3da\",\"staff_id\":\"6876c43fd910b\",\"application_title\":\"Test API\",\"applictaion_body\":\"Testing via curl\",\"start_date\":\"25/11/2025\",\"end_date\":\"26/11/2025\"}" ^
-v

echo.
echo.
echo Test complete! Check the output above for errors.
pause

