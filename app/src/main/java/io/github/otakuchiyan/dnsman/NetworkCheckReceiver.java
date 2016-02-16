package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;

public class NetworkCheckReceiver extends BroadcastReceiver {
    static boolean isFirstConnect = true;
    private SharedPreferences sp;

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm;
        NetworkInfo currentNet;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        if (isFirstConnect) {
            if (!sp.getBoolean("firstboot", false)) {

                //Workaround to deal with multiple broadcast
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                currentNet = cm.getActiveNetworkInfo();
                if (currentNet != null) {
                    isFirstConnect = false;
                    Log.d("NCR", "Start set");
                    String dnsToast = sp.getString("toast", "0");
                    GetNetwork gn = new GetNetwork(context);
                    if (!DNSBackgroundService.setByNetworkInfo(context, currentNet, gn.getNetId())) {
                        if (!dnsToast.equals("2")) {
                            Toast.makeText(context, R.string.nodns_noti, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }
}

