package topgrade.parent.com.parentseeks.Parent.Utils;

/**
 * Constants utility class that uses consolidated constants from ApiConstants
 * This class maintains backward compatibility while using the new centralized constants
 */
public class Constants {

    // ==================== SHARED PREFERENCES KEYS (Backward Compatibility) ====================
    public static final String campus_list = ApiConstants.SharedPrefs.CAMPUS_LIST;
    public static String is_login = ApiConstants.SharedPrefs.IS_LOGIN;
    public static String User_Type = ApiConstants.SharedPrefs.USER_TYPE;
    public static String PREFERENCE_EXTRA_REGISTRATION_ID = ApiConstants.SharedPrefs.PREFERENCE_EXTRA_REGISTRATION_ID;
    
    // ==================== APP VERSION (Backward Compatibility) ====================
    public static final String app_version = ApiConstants.buildCampusApiUrl(ApiConstants.Campus.APP_VERSION);

    // ==================== EXAM RELATED KEYS (Backward Compatibility) ====================
    public static final String exam_session = ApiConstants.SharedPrefs.EXAM_SESSION;

    // Report Paper Key
    public static final String exam_key = ApiConstants.SharedPrefs.EXAM_KEY;
    public static final String month_key = ApiConstants.SharedPrefs.MONTH_KEY;
    public static final String cp_key = ApiConstants.SharedPrefs.CP_KEY;

}
