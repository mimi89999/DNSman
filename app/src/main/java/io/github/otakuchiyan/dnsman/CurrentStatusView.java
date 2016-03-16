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

public class CurrentStatusView extends LinearLayout implements ValueConstants {
    private SharedPreferences preferences;

    private TextView currentMethod;
    private TextView currentDnsText1, currentDnsText2;
    private TextView networkDnsText1, networkDnsText2;

    public CurrentStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public CurrentStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CurrentStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.current_status_view, this);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        currentMethod = (TextView) findViewById(R.id.current_mode_text);
        currentDnsText1 = (TextView) findViewById(R.id.currentDnsText1);
        currentDnsText2 = (TextView) findViewById(R.id.currentDnsText2);
        networkDnsText1 = (TextView) findViewById(R.id.networkDnsText1);
        networkDnsText2 = (TextView) findViewById(R.id.networkDnsText2);

        setWillNotDraw(true);
        setOnClickListener(null);
    }

    public void refreshCurrentMode(){
        this.currentMethod.setText(this.preferences.getString(KEY_PREF_METHOD, METHOD_VPN));
    }

    public void refreshNetworkDns() {
        String dns1 = preferences.getString(KEY_NETWORK_DNS1, "");
        String dns2 = preferences.getString(KEY_NETWORK_DNS2, "");
        networkDnsText1.setText(dns1);
        networkDnsText2.setText(dns2);
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
            new getDNSTask().execute(getContext());
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