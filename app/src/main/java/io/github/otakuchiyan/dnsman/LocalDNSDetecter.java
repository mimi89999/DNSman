package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import io.github.otakuchiyan.dnsman.DNSManager;

public class LocalDNSDetecter extends IntentService{
    final static String ACTION_DNSCRYPT_DETECTED = "io.github.otakuchiyan.dnsman.ACTION_DNSCRYPT_DETECTED";

    public static void perfromAction(Context c){
        if(c == null){
            return;
        }
        Intent i = new Intent(c, LocalDNSDetecter.class);
        c.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent i){
        if(DNSManager.detectDNSCrypt() && isEnabled() && !isGlobalDNSSetted()){
            Log.d("LDD", "detected");
            Intent result_intent = new Intent(ACTION_DNSCRYPT_DETECTED);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(result_intent);
        }
    }

    public LocalDNSDetecter(){
        super("LocalDNSDetecter");
    }


    public boolean isGlobalDNSSetted(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String dns1 = sp.getString("gdns1", "");
        String dns2 = sp.getString("gdns2", "");
        boolean dns1alivable = false;
        boolean dns2alivable = false;

        if(!dns1.equals("") || !dns2.equals("")) {
            if (!dns1.equals("")) {
                dns1alivable = dns1.substring(0, 4).equals("127.");
            }

            if (!dns2.equals("")) {
                dns2alivable = dns2.substring(0, 4).equals("127.");
            }

            if (dns1alivable || dns2alivable){
                return true;
            }

        }
	    return false;
    }

    public boolean isEnabled(){
	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

	return sp.getBoolean("detectLocalDNS", true);
    }

};
