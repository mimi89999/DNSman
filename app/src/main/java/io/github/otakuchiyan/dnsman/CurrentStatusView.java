package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CurrentStatusView  implements ValueConstants {
    private SharedPreferences preferences;
    private Context context;

    private LinearLayout linearLayout;
    private TextView currentMethod;
    private TextView currentDnsText1, currentDnsText2;
    private TextView networkDnsText1, networkDnsText2;

    public CurrentStatusView(Context context) {
        linearLayout = new LinearLayout(context);
        linearLayout.inflate(context, R.layout.current_status_view, linearLayout);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

        currentMethod = (TextView) linearLayout.findViewById(R.id.current_mode_text);
        currentDnsText1 = (TextView) linearLayout.findViewById(R.id.currentDnsText1);
        currentDnsText2 = (TextView) linearLayout.findViewById(R.id.currentDnsText2);
        networkDnsText1 = (TextView) linearLayout.findViewById(R.id.networkDnsText1);
        networkDnsText2 = (TextView) linearLayout.findViewById(R.id.networkDnsText2);

    }

    public void refreshCurrentMode(){
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);
        currentMethod.setText(method);
    }

    public void refreshNetworkDns() {
        String dns1 = preferences.getString(KEY_NETWORK_DNS1, "");
        String dns2 = preferences.getString(KEY_NETWORK_DNS2, "");
        networkDnsText1.setText(dns1);
        networkDnsText2.setText(dns2);
    }

    public LinearLayout getLayout(){
        return linearLayout;
    }

    public void setCurrentDns(){
        setCurrentDns("", "");
    }

    public void setCurrentDns(String dns1, String dns2){
        //Not need AsyncTask
        if(!dns1.equals("") || !dns2.equals("")){
            currentDnsText1.setText(dns1);
            currentDnsText2.setText(dns2);
        }else{
            new getDNSTask().execute(context);
        }
    }

    private class getDNSTask extends AsyncTask<Context, Void, List<String>> {
        protected List<String> doInBackground(Context... contexts) {
            List<String> currentDNSData = new ArrayList<>();
            boolean haveRules = false;

            preferences = PreferenceManager.getDefaultSharedPreferences(contexts[0]);

            //Check firewall rules
            String currentMethod = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);
            if(preferences.getBoolean(KEY_IS_ROOT, false)) {
                String dns = preferences.getString(KEY_HIJACKED_LAST_DNS, "");
                if (!dns.equals("") && NativeCommandUtils.isRulesAlivable(dns)) {
                    haveRules = true;
                    currentDNSData.add(dns);
                }
            }

            //Check system properties
            List<String> prop_dns = NativeCommandUtils.getCurrentPropDNS();
            if (!prop_dns.isEmpty()) {
                //ALERT USER
                if (haveRules && !currentMethod.equals(METHOD_IPTABLES)) {
                } else if (!haveRules) {
                    currentDNSData.addAll(prop_dns);
                }
            }
            for (int i = 0; i != currentDNSData.size(); i++) {
                Log.d("MainActivity", "data = " + currentDNSData.get(i));
            }
            return currentDNSData;
        }

        protected void onPostExecute(List<String> data) {
            currentDnsText1.setText(data.get(0));
            currentDnsText2.setText(data.get(1));
        }
    }
}