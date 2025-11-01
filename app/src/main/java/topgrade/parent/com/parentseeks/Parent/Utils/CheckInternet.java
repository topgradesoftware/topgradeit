package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import androidx.appcompat.app.AlertDialog;
import android.os.Build;

public class CheckInternet {

    public boolean isConnectionAvailable(final Context context) {

        boolean netCon = false;

        try {

            //Internet & network information "Object" initialization
            ConnectivityManager connectivityManager = context.getSystemService(ConnectivityManager.class);
            if (connectivityManager != null) {
                // minSdk is 26, so M (API 23) check is unnecessary
android.net.Network network = connectivityManager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                        if (capabilities != null &&
                                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                            netCon = true;
                    }
                } else {
                    // Fallback for older devices
                    android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        netCon = true;
                    }
                }
            }
        } catch (Exception e) {
            new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("No Network Connection!")
                    .setMessage("Please connect your device to either WiFi or switch on Mobile Data, operator charges may apply!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
        }
        return netCon;
    }

}
