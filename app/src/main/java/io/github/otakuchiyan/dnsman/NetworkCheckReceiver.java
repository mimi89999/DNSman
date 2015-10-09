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
import io.github.otakuchiyan.dnsman.MainActivity;
import io.github.otakuchiyan.dnsman.GetNetwork;

import android.app.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.Integer;

public class NetworkCheckReceiver extends BroadcastReceiver {
	private NotificationManager nm;
    private List<String> dnsList2set = new ArrayList<String>();
    private SharedPreferences sp;

    private ConnectivityManager cm;
    private NetworkInfo ni;
    private boolean isFirstConnect = true;

    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
            if(i.getAction().equals(DNSManager.ACTION_SETDNS_DONE)){
                sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
                String dnsToast = sp.getString("toast", "0");
                if(sp.getString("mode", "0").equals("0")) {
                    if (i.getBooleanExtra("result", false)) {
                        if (dnsToast.equals("0")) {
                            Toast.makeText(c, R.string.set_succeed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (dnsToast.equals("2")) {
                            Toast.makeText(c, R.string.set_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
	    }
	};

    public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			context.getApplicationContext());
		
		if(sp.getBoolean("firstbooted", false)){
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(DNSManager.ACTION_SETDNS_DONE);
            LocalBroadcastManager.getInstance(context).registerReceiver(dnsSetted, iFilter);

            //Workaround to deal with multiple broadcast
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            ni = cm.getActiveNetworkInfo();
            if(ni != null) {
                if(isFirstConnect) {
                    isFirstConnect = false;
                    DNSManager.setDNS(context);
                        Toast.makeText(context, R.string.nodns_noti, Toast.LENGTH_LONG).show();
                }
            }else{
                isFirstConnect = true;
            }
        }

    }


}

