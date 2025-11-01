package topgrade.parent.com.parentseeks.Teacher.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import topgrade.parent.com.parentseeks.BuildConfig;

public class Util {

    private static final String TAG_WHATSAPP = "Util_WhatsApp";
    private static final String TAG_SMS = "Util_SMS";

    // -----------------------------
    // 🔹 DATE FORMATTER
    // -----------------------------
    public static String formatDate(String inputPattern, String outputPattern, String time) {
        if (time == null || inputPattern == null || outputPattern == null) return "";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    // -----------------------------
    // 🔹 APP VERSION
    // -----------------------------
    public static int getVersion() {
        return BuildConfig.VERSION_CODE;
    }

    // -----------------------------
    // 🔹 SHARE GENERIC TEXT
    // -----------------------------
    public static void share(Context context, String message) {
        if (context == null || message == null) return;

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            context.startActivity(Intent.createChooser(shareIntent, "Choose")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "Application Not Found");
        }
    }

    // -----------------------------
    // 🔹 SHARE WITH PHONE NUMBER
    // -----------------------------
    public static void shareWithPhoneNumber(Context context, String message, String phoneNumber) {
        if (context == null || message == null || phoneNumber == null) return;

        try {
            String messageWithPhone = "📱 Phone: " + phoneNumber + "\n\n" + message;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, messageWithPhone);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Timetable for " + phoneNumber);
            context.startActivity(Intent.createChooser(shareIntent, "Choose")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "Application Not Found");
        }
    }

    // -----------------------------
    // 🔹 WHATSAPP SHARE (GENERIC)
    // -----------------------------
    public static void shareToWhatsApp(Context context, String message, String packageName) {
        if (context == null || message == null || packageName == null) return;

        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage(packageName);
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);
            whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(whatsappIntent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "WhatsApp not installed");
        }
    }

    // -----------------------------
    // 🔹 WHATSAPP WITH NUMBER
    // -----------------------------
    public static void shareToWhatsAppWithNumber(Context context, String message, String phoneNumber, String packageName) {
        if (context == null || phoneNumber == null || packageName == null) return;

        try {
            Log.d(TAG_WHATSAPP, "Original phone number: " + phoneNumber);
            String formattedPhone = formatPhoneNumberForWhatsApp(phoneNumber);
            Log.d(TAG_WHATSAPP, "Formatted phone: " + formattedPhone);

            if (formattedPhone.isEmpty()) {
                showToast(context, "Invalid phone number");
                return;
            }

            // Handle long messages separately
            if (message != null && message.length() > 1500) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Timetable", message);
                    clipboard.setPrimaryClip(clip);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "https://wa.me/" + formattedPhone;
                intent.setData(Uri.parse(url));
                intent.setPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                showToast(context, "Message copied to clipboard. Paste it in WhatsApp.");
            } else {
                String encodedMessage = Uri.encode(message);
                String url = "https://api.whatsapp.com/send?phone=" + formattedPhone + "&text=" + encodedMessage;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "WhatsApp not installed");
        }
    }

    // -----------------------------
    // 🔹 FORMAT NUMBER FOR WHATSAPP
    // -----------------------------
    private static String formatPhoneNumberForWhatsApp(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.w(TAG_WHATSAPP, "Phone number is null or empty");
            return "";
        }

        String cleaned = phoneNumber.replaceAll("[^\\d]", "");
        if (cleaned.isEmpty()) return "";

        String withoutLeadingZeros = cleaned.replaceFirst("^0+", "");

        if (withoutLeadingZeros.length() >= 11) {
            return withoutLeadingZeros;
        }

        if (withoutLeadingZeros.length() == 10 && withoutLeadingZeros.startsWith("3")) {
            return "92" + withoutLeadingZeros;
        }

        if (withoutLeadingZeros.length() == 11 && cleaned.startsWith("0")) {
            return "92" + withoutLeadingZeros;
        }

        return withoutLeadingZeros;
    }

    // -----------------------------
    // 🔹 SMS INTENT
    // -----------------------------
    public static void showSmsIntent(Context context, String message, String number) {
        if (context == null || number == null) return;

        try {
            Log.d(TAG_SMS, "Phone number for SMS: " + number);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.putExtra("sms_body", message);
            intent.setData(Uri.parse("sms:" + number));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "Application Not Found");
        }
    }

    // -----------------------------
    // 🔹 EMAIL SHARE (OPTIONAL)
    // -----------------------------
    public static void shareEmail(Context context, String subject, String message, String email) {
        if (context == null || email == null) return;

        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, "Email app not found");
        }
    }

    // -----------------------------
    // 🔹 TOAST HELPER
    // -----------------------------
    private static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
