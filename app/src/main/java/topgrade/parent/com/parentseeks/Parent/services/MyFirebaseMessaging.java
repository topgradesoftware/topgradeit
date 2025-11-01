package topgrade.parent.com.parentseeks.Parent.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentFeedback;
import topgrade.parent.com.parentseeks.Parent.Activity.Report;
import topgrade.parent.com.parentseeks.Parent.Interface.BaseApiService;
import topgrade.parent.com.parentseeks.Parent.Model.ReportModel;
import topgrade.parent.com.parentseeks.Shared.Models.SharedStudent;
import topgrade.parent.com.parentseeks.Parent.Utils.API;
import topgrade.parent.com.parentseeks.Parent.Utils.Constants;
import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffTaskMenu;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;


public class MyFirebaseMessaging extends FirebaseMessagingService {


    String parent_id;
    String campus_id;
    String child_name = "";
    String examSession = "";
    String child_id = "";
    List<SharedStudent> students = new ArrayList<>();

    int j = -1; // j= studnet loop

    @Override
    public void onNewToken(@NonNull String tokenRefeshed) {
        super.onNewToken(tokenRefeshed);
        Paper.init(getApplicationContext());
        Paper.book().write(Constants.PREFERENCE_EXTRA_REGISTRATION_ID, tokenRefeshed);
        Paper.book().write("is_generated", true);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> hashMap = remoteMessage.getData();
        String data = hashMap.toString();
        String title = hashMap.get("title");
        String body = hashMap.get("text");
        if (title != null) {
            newMessageReceived(title, body);
        }


    }

    private void newMessageReceived(String title, String message) {
        Paper.init(getApplicationContext());

        switch (title) {

            case "Result Updated": {
                try {
                    JSONObject jsonArray = new JSONObject(message);
                    child_name = jsonArray.getString("child_name");
                    examSession = jsonArray.getString("exam_session");
                    parent_id = Paper.book().read("parent_id", "");
                    campus_id = Paper.book().read("campus_id", "");
                    try {
                        students = Paper.book().read("students", new ArrayList<SharedStudent>());
                    } catch (Exception e) {
                        // If there's a serialization error, clear the corrupted data and start fresh
                        Paper.book().delete("students");
                        students = new ArrayList<>();
                    }
                    if (students.size() > 0) {
                        j = j + 1;
                        load_exam_report(parent_id, campus_id, students.get(j).getUniqueId(),
                                examSession);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            case "feedback_recevied": {
                try {
                    String[] fcmStringAray = Constant.getConvertedNotificationData(message);
                    String message_show = fcmStringAray[0];
                    child_id = fcmStringAray[1];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
                        notificationHelper.getnotifictaion_feedback("New Feedback Recevied",
                                message_show, ParentFeedback.class, child_id);
                    } else {
                        sendNotification("New Feedback Recevied",
                                message_show, ParentFeedback.class);
                    }
                } catch (Exception error) {

                }
            }


            case "New Task": {
                try {
                    // String[] fcmStringAray = Constant.getConvertedNotificationData(message);

                    JSONObject object = new JSONObject(message);
                    String task_title = object.getString("title");
                    String task_msg = object.getString("body");
                    String msg = task_title + " - " + task_msg;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
                        notificationHelper.getnotifictaion("New Task Assign",
                                msg, StaffTaskMenu.class);
                    } else {
                        sendNotification("New Task Assign",
                                msg, StaffTaskMenu.class);
                    }
                } catch (Exception error) {
                    error.printStackTrace();

                }


            }
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
                    notificationHelper.getnotifictaion(title,
                            message, StaffTaskMenu.class);
                } else {
                    sendNotification(title,
                            message, StaffTaskMenu.class);
                }
                break;
        }
    }

    private void load_exam_report(final String parent_id,
                                  final String campus_id,
                                  final String student_id, final String exam_session_id) {


        HashMap<String, String> postParam = new HashMap<String, String>();
        postParam.put("parent_parent_id", parent_id);
        postParam.put("parent_id", campus_id);
        postParam.put("student_id", student_id);
        postParam.put("exam_session_id", exam_session_id);
        RequestBody body = RequestBody.create((new JSONObject(postParam)).toString(), MediaType.parse("application/json; charset=utf-8"));

        BaseApiService mApiService = API.getAPIService();
        mApiService.load_exam(body).enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Call<ReportModel> call, retrofit2.Response<ReportModel> reportModel) {
                if (reportModel.body() != null) {
                    if (reportModel.body().getStatus().getCode().equals("1000")) {
                        String exam_key = exam_session_id + "+" + student_id + "+" + Constants.exam_key;
                        String month_key = exam_session_id + "+" + student_id + "+" + Constants.month_key;
                        String cp_key = exam_session_id + "+" + student_id + "+" + Constants.cp_key;
                        Paper.book().write(exam_key, reportModel.body().getExam());
                        Paper.book().write(month_key, reportModel.body().getMonth());
                        Paper.book().write(cp_key, reportModel.body().getCp());


                        if (j == students.size() - 1) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
                                notificationHelper.getnotifictaion2("Progress Report",
                                        child_name + " Progress Report is Ready", Report.class, student_id,
                                        exam_session_id);
                            } else {
                                sendNotification2("Progress Report",
                                        child_name + " Progress Report is Ready", student_id,
                                        exam_session_id);
                            }

                        } else {
                            j = j + 1;
                            load_exam_report(parent_id, campus_id, students.get(j).getUniqueId(),
                                    exam_session_id);
                        }


                    }
                }
            }

            @Override
            public void onFailure(Call<ReportModel> call, Throwable e) {
                e.printStackTrace();


            }
        });


    }

    /**
     * Check if notification permission is granted
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not required for older Android versions
    }

    private void sendNotification(String title, String message, Class activity) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("MyFIrebasemEssaging", "Notification permission not granted");
            return;
        }

        Intent intent = new Intent(this, activity);
        if (activity == ParentFeedback.class) {
            intent.putExtra("child_id", child_id);
        } else if (activity == Report.class) {
            intent.putExtra("child_id", child_id);
            intent.putExtra("examSession", examSession);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this, "default");
        notification_builder.setSmallIcon(R.mipmap.ic_launcher);
        notification_builder.setContentTitle(title);
        notification_builder.setContentText(message);
        notification_builder.setAutoCancel(true);
        notification_builder.setContentIntent(pendingIntent);
        notification_builder.setSound(notification_sound);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        notification_builder.setStyle(bigTextStyle);
        final int random = new Random().nextInt(10) + 50;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(random, notification_builder.build());
    }

    private void sendNotification2(String title, String message, String child_id, String examSession) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("MyFIrebasemEssaging", "Notification permission not granted");
            return;
        }

        Intent intent = new Intent(this, Report.class);

        intent.putExtra("child_id", child_id);
        intent.putExtra("examSession", examSession);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this, "default");
        notification_builder.setSmallIcon(R.mipmap.ic_launcher);
        notification_builder.setContentTitle(title);
        notification_builder.setContentText(message);
        notification_builder.setAutoCancel(true);
        notification_builder.setContentIntent(pendingIntent);
        notification_builder.setSound(notification_sound);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        notification_builder.setStyle(bigTextStyle);
        final int random = new Random().nextInt(10) + 50;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(random, notification_builder.build());
    }

}