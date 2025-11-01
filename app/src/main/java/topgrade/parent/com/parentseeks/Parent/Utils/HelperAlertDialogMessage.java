package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;

import topgrade.parent.com.parentseeks.Parent.Interface.OnClickOneButtonAlertDialog;
import topgrade.parent.com.parentseeks.Parent.Interface.OnClickTwoButtonsAlertDialog;
import topgrade.parent.com.parentseeks.R;

public class HelperAlertDialogMessage {
    private static OnClickTwoButtonsAlertDialog click_two_btn_dialog_listener;
    private static OnClickOneButtonAlertDialog click_one_btn_dialog_listener;

    /**
     * Check if context is still valid before showing dialog
     */
    private static boolean isContextValid(Context context) {
        return context != null && !(context instanceof android.app.Activity && ((android.app.Activity) context).isFinishing());
    }

    public static void showAlertMessage(Context context, String message) {
        if (!isContextValid(context)) return;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setMessage(message);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            android.util.Log.e("HelperAlertDialogMessage", "Error showing alert message", e);
        }
    }

    public static void showAlertMessageWithTitle(Context context, String title, String message) {
        if (!isContextValid(context)) return;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setMessage(message);
            builder.setTitle(title);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            android.util.Log.e("HelperAlertDialogMessage", "Error showing alert message with title", e);
        }
    }

    public static void showAlertMessageWithTitle_NoCancel(Context context, String title, String message) {
        if (!isContextValid(context)) return;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setMessage(message);
            builder.setTitle(title);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            android.util.Log.e("HelperAlertDialogMessage", "Error showing alert message with title no cancel", e);
        }
    }

    public static void showAlertMessageWithTwoButtons(Context context,
                                                      OnClickTwoButtonsAlertDialog click_two_btn_listener,
                                                      final String dialog_name, String title, String message,
                                                      String btn_pos_name, String btn_neg_name) {
        if (!isContextValid(context)) return;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setMessage(message);
            builder.setTitle(title);

            click_two_btn_dialog_listener = click_two_btn_listener;

            builder.setPositiveButton(btn_pos_name, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (click_two_btn_dialog_listener != null) {
                        click_two_btn_dialog_listener.clickPositiveDialogButton(dialog_name);
                    }
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(btn_neg_name, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (click_two_btn_dialog_listener != null) {
                        click_two_btn_dialog_listener.clickNegativeDialogButton(dialog_name);
                    }
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            android.util.Log.e("HelperAlertDialogMessage", "Error showing alert message with two buttons", e);
        }
    }

    public static void showAlertMessageWithOneButtons(Context context,
                                                      OnClickOneButtonAlertDialog click_one_btn_listener,
                                                      final String dialog_name, String title, String message,
                                                      String btn_pos_name) {
        if (!isContextValid(context)) return;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setMessage(message);
            builder.setTitle(title);

            click_one_btn_dialog_listener = click_one_btn_listener;

            builder.setPositiveButton(btn_pos_name, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (click_one_btn_dialog_listener != null) {
                        click_one_btn_dialog_listener.clickPositiveDialogButton(dialog_name);
                    }
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            android.util.Log.e("HelperAlertDialogMessage", "Error showing alert message with one button", e);
        }
    }
}


