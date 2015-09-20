package io.github.otakuchiyan.dnsman;

import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import io.github.otakuchiyan.dnsman.DNSManager;

public class LocalDNSDetecter{
    final public static String ACTION_DNSCRYPT_RUNNING = "io.github.otakuchiyan.dnsman.ACTION_DNSCRYPT_RUNNING";

    public static void detect(Context c){
        if(DNSManager.detectDNSCrypt()){
            Intent i = new Intent(ACTION_DNSCRYPT_RUNNING);
            LocalBroadcastManager.getInstance(c.getApplicationContext()).sendBroadcast(i);
        }
    }


    public static boolean isGlobalDNSSetted(Context c){
	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
	
	if(sp.getString("gdns1", "").equals("127.0.0.1") ||
           sp.getString("gdns2", "").equals("127.0.0.1")){
        return true;
	}
        Log.d("Detecter", "No global DNS");
	    return false;
    }

    public static boolean isEnabled(Context c){
	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

	return sp.getBoolean("detectLocalDNS", true);
    }

};
