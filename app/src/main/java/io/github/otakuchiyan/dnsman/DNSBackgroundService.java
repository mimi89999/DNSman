package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DNSBackgroundService extends IntentService{
    private static Context context;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor sped;
    private static List<String> dnsList = new ArrayList<>();
    private static String mode;
    private static String current_netObj;
    private static String lastHijackedDNS;
    private static String lastHijackedPort;

    public DNSBackgroundService(){
        super("DNSBackgroundService");
    }

    private static void beforeSet(Context c){
        sp = PreferenceManager.getDefaultSharedPreferences(
                c.getApplicationContext());
        context = c;

        mode = sp.getString("mode", "PROP");

        if (mode.equals("IPTABLES")) {
            lastHijackedDNS = sp.getString("lastHijackedDNS", "");
            lastHijackedPort = sp.getString("lastHijackedPort", "");
        }
    }

    public static boolean setByString(Context c, String dns1, String dns2){
        beforeSet(c);
        if(dns1.equals("") && dns2.equals("")){
            return false;
        }

        GetNetwork g = new GetNetwork(c);

        dnsList.clear();
        dnsList.add(dns1);
        dnsList.add(dns2);

        current_netObj = g.getNetId();
        Intent i = new Intent(c, DNSBackgroundService.class);
        c.startService(i);
        return true;
    }

    public static void deleteLastRules(Context c){
        beforeSet(c);
        mode = "DELETE_RULES";
        Intent i = new Intent(c, DNSBackgroundService.class);
        c.startService(i);
    }

    public static boolean setByNetworkInfo(Context c, NetworkInfo info, String netId){
        beforeSet(c);
        getDNSByNetType(info);
        if(dnsList.isEmpty()){
            return false;
        }
        if(mode.equals("NDC")){
            if(netId.equals("")) {
                GetNetwork i = new GetNetwork(c);
                Map<String, String> name2pref_map = new HashMap<>();
                name2pref_map.put(i.wifiName, "pref_ndc_wlan");
                name2pref_map.put(i.mobileName, "pref_ndc_rmnet");
                name2pref_map.put(i.bluetoothName, "pref_ndc_bt");
                name2pref_map.put(i.etherName, "pref_ndc_eth");
                current_netObj = sp.getString(name2pref_map.get(info.getTypeName()), "");
            }else {
                current_netObj = netId;
            }
        }

        Intent i = new Intent(c, DNSBackgroundService.class);
        c.startService(i);
        return true;
    }

    private static void getDNSByNetType(NetworkInfo info){
	    String dns1 = sp.getString(info.getTypeName() + "dns1", "");
	    String dns2suffix = "dns2";
        //dns2 was used for port when mode is IPTABLES
        if (mode.equals("IPTABLES")) {
            dns2suffix = "port";
	    }
	    String dns2 = sp.getString(info.getTypeName() + dns2suffix, "");

        //Fallback to global DNS
	    if(dns1.equals("") || dns2.equals("")){
			dns1 = sp.getString("gdns1", "");
			dns2 = sp.getString("g" + dns2suffix, "");
		}
        if(!dns1.equals("") || !dns2.equals("")) {
            dnsList.add(dns1);
            dnsList.add(dns2);
        }
    }

    @Override
    protected void onHandleIntent(Intent i){
        boolean result;
        int result_code = 0;
        String dns1 = "";
        String dns2 = "";
        if(!mode.equals("DELETE_RULES")){
            dns1 = dnsList.get(0);
            dns2 = dnsList.get(1);
            Log.d("DNSBackgroundService", "onHandleIntent.dns1 = " + dns1);
            Log.d("DNSBackgroundService", "onHandleIntent.dns2 = " + dns2);
        }
        sped = sp.edit();

        switch(mode){
            case "PROP":
                boolean checkProp = sp.getBoolean("checkprop", true);
                result_code = DNSManager.setDNSViaSetprop(dns1, dns2, checkProp);
                break;
            case "IPTABLES":
                if(DNSManager.isRulesAlivable(dns1, dns2)) {
                    return;
                    //IP was changed
                }else if (!dns1.equals(lastHijackedDNS) &&
                        DNSManager.isRulesAlivable(lastHijackedDNS, lastHijackedPort)){
                    DNSManager.deleteRules(lastHijackedDNS, lastHijackedPort);
                }
                sped.putString("lastHijackedDNS", dns1);
                sped.putString("lastHijackedPort", dns2);
                sped.apply();
                result_code = DNSManager.setDNSViaIPtables(dns1, dns2);
                break;
            case "DELETE_RULES":
                DNSManager.deleteRules(lastHijackedDNS, lastHijackedPort);
                sped.putString("lastHijackedDNS", "");
                sped.putString("lastHijackedPort", "");
                sped.apply();
                break;
            case "NDC":
                result_code = DNSManager.setDNSViaNdc(current_netObj, dns1, dns2);
                break;
            case "VPN":
                DNSManager.setDNSViaVpn(context, dns1, dns2);
                break;
        }

        result = result_code == 0;
        if(result && sp.getBoolean("autoflush", true)) {
            AirplaneModeUtils.toggle(context, current_netObj);
        }

        Intent result_intent = new Intent(DNSmanConstants.ACTION_SETDNS_DONE);
        result_intent.putExtra("result", result);
        result_intent.putExtra("result_code", result_code);
        result_intent.putExtra("dns1", dns1);
        result_intent.putExtra("dns2", dns2);
        LocalBroadcastManager.getInstance(context).sendBroadcast(result_intent);
    }

}
