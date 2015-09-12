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
import android.content.IntentFilter;

import eu.chainfire.libsuperuser.Shell;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.MainActivity;
import io.github.otakuchiyan.dnsman.DNSBackgroundIntentService;
import io.github.otakuchiyan.dnsman.GetNetwork;

import android.app.*;

public class NetworkCheckReceiver extends BroadcastReceiver {
	private Notification n;
	private NotificationManager nm;

    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(DNSBackgroundIntentService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
			Toast.makeText(c, R.string.set_succeed, Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(c, R.string.set_failed, Toast.LENGTH_SHORT).show();
		}
		}
	    }
	};

    private void setDNS(Context c, boolean isMobile) {
	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			c.getApplicationContext());

        String[] wdnss = new String[2];
		String[] mdnss = new String[2];
		String[] dnss = new String[2];
		Bundle dnss_bundle = new Bundle();
		
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(DNSBackgroundIntentService.ACTION_SETDNS_DONE);
		LocalBroadcastManager.getInstance(c).registerReceiver(dnsSetted, iFilter);
		
		for(int i = 0; i <= 1; i++) {
			wdnss[i] = sp.getString("wdns" + (i + 1), "");
			mdnss[i] = sp.getString("mdns" + (i + 1), "");
		}
		
       	//Mobile network
	        if(isMobile){
                if(sp.getBoolean("distinguish", false)) {
                    dnss = mdnss;		
				} else {
                    dnss = wdnss;
				}
        }else{
            dnss = wdnss;
        }
		
		if(dnss[0].equals("") && dnss[1].equals("")){
			/*n = new Notification();
			nm = (NotificationManager) c.getSystemService("NOTIFICATION_SERVICE");
			Intent i = new Intent(c, MainActivity.class);
			
			PendingIntent pi = PendingIntent.getActivity(c, 0, i, 0);
			
			//n.icon = R.mipmap.ic_launcher;
			n.when = System.currentTimeMillis();
			n.setLatestEventInfo(c,
				c.getText(R.string.nodns_noti),
				"test",
				pi);
			nm.notify(0, n);
			*/
			Toast.makeText(c, R.string.nodns_noti, Toast.LENGTH_LONG).show();
			
			return;
		}
		dnss_bundle.putString("dns1", dnss[0]);
		dnss_bundle.putString("dns2", dnss[1]);
		
		DNSBackgroundIntentService.performAction(c, dnss_bundle);
		
    }

    public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			context.getApplicationContext());
		
		if(sp.getBoolean("firstbooted", false)){
			GetNetwork.init(context);
			NetworkInfo mobi_res = GetNetwork.getMobileNetwork();
			NetworkInfo wifi_res = GetNetwork.getWiFiNetwork();
        if(mobi_res != null && mobi_res.isConnected()){
            setDNS(context, true);
        } else if (wifi_res != null && wifi_res.isConnected()){
            setDNS(context, false);
        }
		}
    }
}

