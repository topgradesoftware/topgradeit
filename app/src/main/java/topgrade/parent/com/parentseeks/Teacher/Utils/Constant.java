package topgrade.parent.com.parentseeks.Teacher.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import io.paperdb.Paper;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Utils.API;

public class Constant {

    public static final BaseApiService mApiService = API.getAPIService();

    public static String staff_id = "";
    public static String campus_id = "";
    public static String current_session = "";
    public static String parent_id = "";

    public static String Credit = "Credit (Received/Inflow)";
    public static String Debit = "Debit (Given/Outflow)";
    
    /**
     * Load constants from Paper database
     * This should be called when the app starts or when user data is needed
     */
    public static void loadFromPaper() {
        try {
            staff_id = Paper.book().read("staff_id", "");
            campus_id = Paper.book().read("campus_id", "");
            current_session = Paper.book().read("current_session", "");
            parent_id = Paper.book().read("parent_id", "");
            
            android.util.Log.d("Constant", "Constants loaded from Paper:");
            android.util.Log.d("Constant", "staff_id: " + staff_id);
            android.util.Log.d("Constant", "campus_id: " + campus_id);
            android.util.Log.d("Constant", "current_session: " + current_session);
            android.util.Log.d("Constant", "parent_id: " + parent_id);
        } catch (Exception e) {
            android.util.Log.e("Constant", "Error loading constants from Paper: " + e.getMessage());
        }
    }
    
    // API Parameter Keys - prevents typos and improves maintainability
    public static final String KEY_OPERATION = "operation";
    public static final String KEY_STAFF_ID = "staff_id";
    public static final String KEY_CAMPUS_ID = "campus_id";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_IS_COMPLETED = "is_completed";
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_STATUS = "status";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";
    
    // Operation Values
    public static final String OPERATION_READ = "read";
    public static final String OPERATION_UPDATE = "update";
    
    // Status Values
    public static final String STATUS_ALL = "all";
    public static final String STATUS_PENDING = "0";
    public static final String STATUS_COMPLETED = "1";
    public static final String STATUS_INCOMPLETE = "0";
    
    // Filter Types for Task Menu
    public static final String FILTER_ALL = "all";
    public static final String FILTER_PENDING = "pending";
    public static final String FILTER_INCOMPLETE = "incomplete";
    public static final String FILTER_COMPLETED = "completed";
    
    // Filter Types for Leave Application Menu
    public static final String FILTER_LEAVE_ALL = "all_leaves";
    public static final String FILTER_LEAVE_PENDING = "pending_leaves";
    public static final String FILTER_LEAVE_APPROVED = "approved_leaves";
    public static final String FILTER_LEAVE_REJECTED = "rejected_leaves";
    
    // Filter Types for Complaint Menu
    public static final String FILTER_COMPLAINT_ALL = "all_complaints";
    public static final String FILTER_COMPLAINT_PENDING = "pending_complaints";
    public static final String FILTER_COMPLAINT_DISCUSSION = "discussion_complaints";
    public static final String FILTER_COMPLAINT_SOLVED = "solved_complaints";
    
    public static void Log(String msg) {
        System.out.println("Testing:    " + msg);
    }

    public static String[] getConvertedNotificationData(String data) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] converted_data = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                converted_data[i] = jsonArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return converted_data;
    }
}
