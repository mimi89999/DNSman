package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private boolean isFirstConnect = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm;
        NetworkInfo currentNet;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        if (isFirstConnect && sp.getBoolean(ValueConstants.KEY_PREF_AUTO_SETTING, true)) {
            if (!sp.getBoolean("firstboot", false)) {

                //Workaround to deal with multiple broadcast
                cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                currentNet = cm.getActiveNetworkInfo();
                if (currentNet != null) {
                    isFirstConnect = false;
    //                DNSBackgroundService.isDefaultDnsGetted = false;
                    Log.d("NCR", "Start set");
                    String dnsToast = sp.getString("toast", "0");
  //                  GetNetwork gn = new GetNetwork(context);
                    if (ExecuteIntentService.startActionByInfo(context, currentNet)) {
                        if (!dnsToast.equals("2")) {
//                            Toast.makeText(context, R.string.nodns_noti, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }

    }
}
