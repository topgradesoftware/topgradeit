package topgrade.parent.com.parentseeks.Parent.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.ContextCompat;


import topgrade.parent.com.parentseeks.BuildConfig;
import topgrade.parent.com.parentseeks.Parent.Activity.ParentFeedback;
import topgrade.parent.com.parentseeks.R;


public class NotificationHelper extends ContextWrapper {

    static final String chanlle_id = BuildConfig.APPLICATION_ID;
    private static final String chanlle_name = "SEEKS APPS";
    NotificationManager manager;
    Bitmap logo;

    public NotificationHelper(Context base) {
        super(base);
        creatc_chaneels();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void creatc_chaneels() {
        NotificationChannel notificationChannel = new NotificationChannel(chanlle_id,
                chanlle_name, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannel);
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

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }


    @TargetApi(Build.VERSION_CODES.O)
    public void getnotifictaion(String title, String message, Class activity) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted");
            return;
        }
        
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                1, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(),
                chanlle_id)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(notification_sound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
        getManager().notify(1, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void getnotifictaion2(String title, String message, Class activity, String child_id, String examSession) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted");
            return;
        }
        
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.putExtra("child_id", child_id);
        intent.putExtra("examSession", examSession);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(),
                chanlle_id)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(notification_sound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
        getManager().notify(1, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void getnotifictaion_feedback(String title, String message, Class activity, String child_id) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted");
            return;
        }
        
        Intent intent = new Intent(this, activity);
        if (activity == ParentFeedback.class) {
            intent.putExtra("child_id", child_id);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), chanlle_id)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(notification_sound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        getManager().notify(1, builder.build());
    }

}

