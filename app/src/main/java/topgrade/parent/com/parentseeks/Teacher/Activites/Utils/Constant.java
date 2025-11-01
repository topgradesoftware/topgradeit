package topgrade.parent.com.parentseeks.Teacher.Activites.Utils;

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
            
            Log("Constants loaded from Paper:");
            Log("staff_id: " + staff_id);
            Log("campus_id: " + campus_id);
            Log("current_session: " + current_session);
            Log("parent_id: " + parent_id);
        } catch (Exception e) {
            Log("Error loading constants from Paper: " + e.getMessage());
        }
    }

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
