package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.app.IntentService;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import io.github.otakuchiyan.dnsman.DNSManager;

public class DNSBackgroundService extends IntentService{
    final public static String ACTION_SETDNS_DONE = "io.github.otakuchiyan.dnsman.SETDNS_DONE";

    private static Context context;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor sped;
    private static List<String> dnsList = new ArrayList<String>();
    private static boolean checkProp = true;
    private static String mode;

    public DNSBackgroundService(){
        super("DNSBackgroundService");
    }

    private static void beforeSet(Context c){
        sp = PreferenceManager.getDefaultSharedPreferences(
                c.getApplicationContext());
        context = c;
        checkProp = sp.getBoolean("checkprop", true);
        mode = sp.getString("mode", "0");
    }

    public static boolean setByString(Context c, String dns1, String dns2){
        beforeSet(c);
        if(dns1.equals("") || dns2.equals("")){
            return false;
        }

        dnsList.add(dns1);
        dnsList.add(dns2);
        Intent i = new Intent(c, DNSBackgroundService.class);
        c.startService(i);
        return true;
    }

    public static boolean setByNetworkInfo(Context c, NetworkInfo info){
        beforeSet(c);
	getDNSByNetType(info);
	if(dnsList.isEmpty()){
		return false;
	}
	Log.d("DNSBackgroundService", "dnsList.0 " + dnsList.get(0));
	Log.d("DNSBackgroundService", "dnsList.1 " + dnsList.get(1));

	Intent i = new Intent(c, DNSBackgroundService.class);
	c.startService(i);
	return true;
    }

    private static void getDNSByNetType(NetworkInfo info){
	    String dns1 = sp.getString(info.getTypeName() + "dns1", "");
	    String dns2suffix = "dns2";
	    //dns2 was used for port when mode is 1
	    if(mode.equals("1")){
		    dns2suffix = "port";
	    }
	    String dns2 = sp.getString(info.getTypeName() + dns2suffix, "");
	    
		dnsList.clear();
	    if(!dns1.equals("") || !dns2.equals("")){
		    dnsList.add(dns1);
		    dnsList.add(dns2);
	    }else{
			//Fallback to global DNS
			dns1 = sp.getString("gdns1", "");
			dns2 = sp.getString("g" + dns2suffix, "");
			dnsList.add(dns1);
			dnsList.add(dns2);
		}
    }

    @Override
    protected void onHandleIntent(Intent i){
        boolean result = false;
        switch(mode){
            case "0":
                result = DNSManager.setDNSViaSetprop(dnsList.get(0), dnsList.get(1), checkProp);
                break;
            case "1":
                result = DNSManager.setDNSViaIPtables(dnsList.get(0), dnsList.get(1));
                break;
        }
        Intent result_intent = new Intent(ACTION_SETDNS_DONE);
        i.putExtra("result", result);
        LocalBroadcastManager.getInstance(context).sendBroadcast(result_intent);
    }

}
