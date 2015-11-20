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
    private static List<String> dnsList = new ArrayList<>();
    private static boolean checkProp = true;
    private static String mode;
    private static String lastHijackedDNS;
    private static String lastHijackedPort;

    public DNSBackgroundService(){
        super("DNSBackgroundService");
    }

    private static void beforeSet(Context c){
        sp = PreferenceManager.getDefaultSharedPreferences(
                c.getApplicationContext());
        context = c;
        checkProp = sp.getBoolean("checkprop", true);
        mode = sp.getString("mode", "0");
        if(mode.equals("1")){
            lastHijackedDNS = sp.getString("lastHijackedDNS", "");
            lastHijackedPort = sp.getString("lastHijackedPort", "");
        }
    }

    public static boolean setByString(Context c, String dns1, String dns2){
        beforeSet(c);
        if(dns1.equals("") && dns2.equals("")){
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
	    
//		dnsList.clear();
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
        boolean result;
        int result_code = 0;
        final String dns1 = dnsList.get(0);
        final String dns2 = dnsList.get(1);

        switch(mode){
            case "PROP":
                result_code = DNSManager.setDNSViaSetprop(dns1, dns2, checkProp);
                break;
            case "IPTABLES":
                sped = sp.edit();

                if(DNSManager.isRulesAlivable(dns1, dns2)) {
                    return;
                }else if (!lastHijackedDNS.equals("") &&
                        DNSManager.isRulesAlivable(lastHijackedDNS, lastHijackedPort)){
                    DNSManager.deleteRules(lastHijackedDNS, lastHijackedPort);
                }
                sped.putString("lastHijackedDNS", dns1);
                sped.putString("lastHijackedPort", dns2);
                sped.apply();
                result_code = DNSManager.setDNSViaIPtables(dns1, dns2);
                break;
            case "NDC":
                String iface = "w";
                result_code = DNSManager.setDNSViaNdc(iface, dns1, dns2);
                break;
        }

        result = result_code == 0;
        Intent result_intent = new Intent(ACTION_SETDNS_DONE);
        result_intent.putExtra("result", result);
        result_intent.putExtra("result_code", result_code);
        result_intent.putExtra("dns1", dns1);
        result_intent.putExtra("dns2", dns2);
        LocalBroadcastManager.getInstance(context).sendBroadcast(result_intent);
    }

}
