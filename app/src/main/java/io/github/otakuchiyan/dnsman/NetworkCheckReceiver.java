package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Log;

public class NetworkCheckReceiver extends BroadcastReceiver {
    static boolean isFirstConnect = true;

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm;
        NetworkInfo currentNet;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        if (isFirstConnect && sp.getBoolean("pref_auto_setting", true)) {
            if (!sp.getBoolean("firstboot", false)) {

                //Workaround to deal with multiple broadcast
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                currentNet = cm.getActiveNetworkInfo();
                if (currentNet != null) {
                    isFirstConnect = false;
                    DNSBackgroundService.isDefaultDnsGetted = false;
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

