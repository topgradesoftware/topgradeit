package topgrade.parent.com.parentseeks.Parent.Utils;

import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;

/**
 * API utility class that uses consolidated constants from ApiConstants
 * This class maintains backward compatibility while using the new centralized constants
 */
public class API {

    // ==================== IMAGE BASE URLs (Backward Compatibility) ====================
    public static String image_base_url = ApiConstants.IMAGE_BASE_URL;
    public static String parent_image_base_url = ApiConstants.PARENT_IMAGE_BASE_URL;
    public static String employee_image_base_url = ApiConstants.EMPLOYEE_IMAGE_BASE_URL;
    
    // ==================== PARENT API ENDPOINTS (Backward Compatibility) ====================
    public static String list_campus = ApiConstants.buildApiUrl(ApiConstants.Parent.LIST_CAMPUS);
    public static String login = ApiConstants.buildApiUrl(ApiConstants.Parent.LOGIN);
    public static String load_profile = ApiConstants.buildApiUrl(ApiConstants.Parent.LOAD_PROFILE);
    public static String load_challan = ApiConstants.buildApiUrl(ApiConstants.Parent.LOAD_CHALLAN);
    public static String update_password = ApiConstants.buildApiUrl(ApiConstants.Parent.UPDATE_PASSWORD);
    public static String update_picture = ApiConstants.buildApiUrl(ApiConstants.Parent.UPDATE_PICTURE);
    public static String update_picture_student = ApiConstants.buildApiUrl(ApiConstants.Parent.UPDATE_PICTURE_STUDENT);
    public static String load_attendance = ApiConstants.buildApiUrl(ApiConstants.Parent.LOAD_ATTENDANCE);
    public static String load_attendance_subject_wise = ApiConstants.buildApiUrl(ApiConstants.Parent.LOAD_ATTENDANCE_SUBJECT_WISE);
    public static String load_attendance_subjectwise = ApiConstants.buildApiUrl(ApiConstants.Parent.LOAD_ATTENDANCE_SUBJECTWISE);
    
    // ==================== STAFF API ENDPOINTS (Backward Compatibility) ====================
    public static String update_picture_staff = ApiConstants.buildApiUrl(ApiConstants.Teacher.UPDATE_PICTURE);
    public static String staff_login = ApiConstants.buildApiUrl(ApiConstants.Teacher.LOGIN);
    public static String staff_update_password = ApiConstants.buildApiUrl(ApiConstants.Teacher.UPDATE_PASSWORD);
    public static String staff_load_attendance = ApiConstants.buildApiUrl(ApiConstants.Teacher.LOAD_ATTENDANCE);
    
    // ==================== STUDENT API ENDPOINTS (Backward Compatibility) ====================
    public static String student_update_password = ApiConstants.buildApiUrl(ApiConstants.Student.UPDATE_PASSWORD);
    
    // ==================== APP VERSION (Backward Compatibility) ====================
    public static final String app_version = ApiConstants.buildApiUrl(ApiConstants.Parent.APP_VERSION);
    public static final String app_version_Name = ApiConstants.SharedPrefs.APP_VERSION_NAME;
    
    // ==================== BASE URL (Backward Compatibility) ====================
    public static String base_url = ApiConstants.BASE_URL;

    /**
     * Get API service using the consolidated base URL
     */
    public static BaseApiService getAPIService() {
        return RetrofitClient.getClient(base_url).create(BaseApiService.class);
    }

}
