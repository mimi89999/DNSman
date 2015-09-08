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

import java.util.UnknownFormatFlagsException;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.MainActivity;
import java.net.UnknownHostException;
import android.app.*;

public class NetworkCheckReceiver extends BroadcastReceiver {
    final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private Notification n;
	private NotificationManager nm;

    private void setDNS(Context c, boolean isMobile) {
        DNSManager dns = new DNSManager();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			c.getApplicationContext());

        String[] wdnss = new String[2];
		String[] mdnss = new String[2];
		String[] dnss = new String[2];
		
		
		for(int i = 0; i <= 1; i++) {
			wdnss[i] = sp.getString("wdns" + (i + 1), "");
			mdnss[i] = sp.getString("mdns" + (i + 1), "");
		}
		
        Boolean use_su = sp.getBoolean("use_su", false);
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
		
        if (dns.setDNSViaSetprop(dnss[0], dnss[1], use_su) == 1) {
                Toast.makeText(c, R.string.set_failed, Toast.LENGTH_LONG).show();
        } else {
                Toast.makeText(c, R.string.set_succeed, Toast.LENGTH_LONG).show();
            }
    }

    public void onReceive(Context context, Intent intent) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
			context.getApplicationContext());
		
		if(sp.getBoolean("firstbooted", false)){
        ConnectivityManager connmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobi_res = connmgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi_res = connmgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mobi_res != null && mobi_res.isConnected()){
            setDNS(context, true);
        } else if (wifi_res != null && wifi_res.isConnected()){
            setDNS(context, false);
        }
		}
    }
}

