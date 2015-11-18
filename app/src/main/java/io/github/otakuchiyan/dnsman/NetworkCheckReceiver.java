package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.content.IntentFilter;
import android.content.res.Resources;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.GetNetwork;

import android.app.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

public class NetworkCheckReceiver extends BroadcastReceiver {
    private SharedPreferences sp;

    private ConnectivityManager cm;
    private NetworkInfo currentNet;
    private boolean isFirstConnect = true;

    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
            if(i.getAction().equals(DNSBackgroundService.ACTION_SETDNS_DONE)){
                sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
                String dnsToast = sp.getString("toast", "0");
                // if(sp.getString("mode", "0").equals("0")) {
                    if (i.getBooleanExtra("result", false)) {
                        if (dnsToast.equals("0")) {
                            final String dns1 = i.getStringExtra("dns1");
                            final String dns2 = i.getStringExtra("dns2");
                            String str = c.getText(R.string.set_succeed).toString();
                            str += !dns1.equals("") ? "\n DNS:\t" + dns1 : "";
                            str += !dns2.equals("") ? "\n DNS:\t" + dns2 : "";
                            Toast.makeText(c, str, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!dnsToast.equals("2")) {
                            Toast.makeText(c, R.string.set_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                //}
            }
	    }
	};

    public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			context.getApplicationContext());
		
		if(sp.getBoolean("firstbooted", false)){
            LocalBroadcastManager.getInstance(context).registerReceiver(dnsSetted,
                    new IntentFilter(DNSBackgroundService.ACTION_SETDNS_DONE));

            //Workaround to deal with multiple broadcast
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            currentNet = cm.getActiveNetworkInfo();
            if(currentNet != null) {
                if(isFirstConnect) {
                    isFirstConnect = false;
                        String dnsToast = sp.getString("toast", "0");
			if(!DNSBackgroundService.setByNetworkInfo(context, currentNet)){
				if (!dnsToast.equals("2")) {
					Toast.makeText(context, R.string.nodns_noti, Toast.LENGTH_LONG).show();
				}
			}
	    }
            }else{
                isFirstConnect = true;
            }
        }
    }
}

