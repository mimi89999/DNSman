package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;
import java.util.ArrayList;

import io.github.otakuchiyan.dnsman.DNSManager;

public class DNSBackgroundService extends IntentService{
        private SharedPreferences sp;
        private SharedPreferences.Editor sped;
        private List<String> dnsList2set = new ArrayList<String>();

        public DNSBackgroundService(){
            super("DNSBackgroundService");
        }

        @Override
        protected void onHandleIntent(Intent i){
			boolean result = false;
            switch(mode){
                case "0":
                    result = DNSManager.setDNSViaSetprop(dns1, dns2, checkProp);
                    break;
                case "1":
                    result = DNSManager.setDNSViaIPtables(dns1, port);
                    break;
            }
			Intent result_intent = new Intent(ACTION_SETDNS_DONE);
            i.putExtra("result", result);
            LocalBroadcastManager.getInstance(context).sendBroadcast(result_intent);
        }

}
