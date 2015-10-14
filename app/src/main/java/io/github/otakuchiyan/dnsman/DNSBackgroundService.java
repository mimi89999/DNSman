package io.github.otakchiyan.dnsman;

import android.context.Content;
import android.preference.PreferenceManager;
import android.context.SharedPreferences;

public class RunCommandService extends IntentService{
        private SharedPreferences sp;
        private SharedPreferences.Editor sped;
        private List<String> dnsList2set = new ArrayList<String>();

        public RunCommandService(){
            super("RunCommandService");
        }

        public boolean setDNS(Context c){
            context = c;
            sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            getDNS();

            if(dnsList2set.isEmpty()){
                return false;
            }
            
            dns1 = dnsList2set.get(0);
            mode = sp.getString("mode", "0");
            checkProp = sp.getBoolean("checkProp", true);
            switch(mode){
                case "0":
                    dns2 = dnsList2set.get(1);
                case "1":
                    port = dnsList2set.get(1);
                    break;
            }

            Intent i = new Intent(context, RunCommandService.class);
            context.startService(i);
            return true;
        }

        @Override
        protected void onHandleIntent(Intent i){
            Log.d("DNSManager", "onHandleIntent");
			boolean result = false;
            switch(mode){
                case "0":
                    result = setDNSViaSetprop(dns1, dns2, checkProp);
                    break;
                case "1":
                    result = setDNSViaIPtables(dns1, port);
                    break;
            }
			Intent result_intent = new Intent(ACTION_SETDNS_DONE);
            i.putExtra("result", result);
            LocalBroadcastManager.getInstance(context).sendBroadcast(result_intent);
        }
