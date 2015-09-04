package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;

import java.util.UnknownFormatFlagsException;

import io.github.otakuchiyan.dnsman.DNSManager;
import java.net.UnknownHostException;

public class NetworkCheckReceiver extends BroadcastReceiver {
    final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private void setDNS(Context c, boolean isMobile) {
        DNSManager dns = new DNSManager();

		SharedPreferences dnssp = c.getSharedPreferences("dnsconf", Context.MODE_PRIVATE);
        SharedPreferences appsp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());

        String[] dnss = new String[3];

        Boolean use_su = appsp.getBoolean("use_su", false);
        //Mobile network
        if(isMobile){
            for(int i = 0; i != 2; i++) {
                if(appsp.getBoolean("same_dns", false)) {
                    dnss[i] = dnssp.getString("wdns" + (i + 1), null);
                } else {
                    dnss[i] = dnssp.getString("mdns" + (i + 1), null);
                }
            }
        }else{
            for(int i = 0; i != 2; i++) {
                dnss[i] = dnssp.getString("wdns" + (i + 1), "");
            }
        }

        if (dns.setDNSViaSetprop(dnss[0], dnss[1], use_su) == 1) {
                Toast.makeText(c, R.string.set_failed, Toast.LENGTH_LONG).show();
        } else {
                Toast.makeText(c, R.string.set_succeed, Toast.LENGTH_LONG).show();
            }
    }

    public void onReceive(Context context, Intent intent) {

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

