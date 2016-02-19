package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends ListActivity {
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
    private String current_mode;
    private Intent dnsWatchingServiceIntent;

    private Menu menu;
    private Context context;
    private TextView currentDns;
    private SimpleAdapter adapter;
    private ListView mainList;

    private boolean isRegistered = false;
    BroadcastReceiver dnsSetted = new BroadcastReceiver(){
        @Override
        public void onReceive(Context c, Intent i){
            if(i.getAction().equals(DNSmanConstants.ACTION_SETDNS_DONE)){
                boolean result = i.getBooleanExtra("result", false);
                int result_code = i.getIntExtra("result_code", 0);
                String dns1 = i.getStringExtra("dns1");
                String dns2 = i.getStringExtra("dns2");
                if(result){
                    //For MainActivity
                    new getDNSTask().execute();

                    //Toast
                    String dnsToast = sp.getString("toast", "0");
                    if (result) {
                        if (dnsToast.equals("0")) {
                            String str = context.getText(R.string.set_succeed).toString();
                            str += !dns1.equals("") ? "\n DNS:\t" + dns1 : "";
                            str += !dns2.equals("") ? "\n DNS:\t" + dns2 : "";
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!dnsToast.equals("2")) {
                            String error_str = context.getText(R.string.set_failed).toString();
                            switch(result_code){
                                case DNSManager.ERROR_SETPROP_FAILED:
                                    error_str += "\n" + context.getText(R.string.error_setprop_failed).toString();
                                    break;
                                default:
                                    error_str += "\n" + context.getText(R.string.error_unknown).toString();
                            }
                            Toast.makeText(context, error_str, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        dnsWatchingServiceIntent = new Intent(this, DNSMonitorService.class);
        context = this;

        mainList = getListView();

        List<Map<String, String>> dnsList = new ArrayList<>();
        Map<String, String> dnsEntryData = new HashMap<>();

        current_mode = sp.getString("mode", "PROP");

        String dns1key = "dns1";
        String dns2Suffix = "dns2";
        if(current_mode.equals("IPTABLES")) {
            dns2Suffix = "port";
        }

        //Constructing list
        dnsEntryData.put("label", getText(R.string.global_category).toString());
        String globalDns1Key = "g" + dns1key;
        String globalDns2Key = "g" + dns2Suffix;
        String global_dns_data = sp.getString(globalDns1Key, "") + "\t" + sp.getString(globalDns2Key, "");
        dnsEntryData.put("dns1key", globalDns1Key);
        dnsEntryData.put("dns2key", globalDns2Key);        
        dnsEntryData.put("dns_data", global_dns_data);
        dnsList.add(dnsEntryData);
        GetNetwork gn = new GetNetwork(this);

        ArrayList<Boolean> netSupportingList = new ArrayList<>();
        ArrayList<Integer> netLabelList = new ArrayList<>();
        ArrayList<String> netNameList = new ArrayList<>();
        netSupportingList.add(gn.isSupportWifi);
        netSupportingList.add(gn.isSupportMobile);
        netSupportingList.add(gn.isSupportBluetooth);
        netSupportingList.add(gn.isSupportEthernet);
        netSupportingList.add(gn.isSupportWimax);
        netLabelList.add(R.string.wifi_category);
        netLabelList.add(R.string.mobile_category);
        netLabelList.add(R.string.bt_category);
        netLabelList.add(R.string.eth_category);
        netLabelList.add(R.string.wimax_category);
        netNameList.add(gn.wifiName);
        netNameList.add(gn.mobileName);
        netNameList.add(gn.bluetoothName);
        netNameList.add(gn.etherName);
        netNameList.add(gn.wimaxName);

        for(int i = 0; i != netSupportingList.size(); i++){
            if(netSupportingList.get(i)){
                Map<String, String> netEntryData = new HashMap<>();
                netEntryData.put("label", getText(netLabelList.get(i)).toString());
                        String dns1Key = netNameList.get(i) + dns1key;
                String dns2Key = netNameList + dns2Suffix;
                netEntryData.put("dns1key", dns1Key);
                netEntryData.put("dns2key", dns2Key);
                String dns_data = sp.getString(dns1Key, "") + "\t"
                        + sp.getString(dns2Key, "");
                netEntryData.put("dns_data", dns_data);
                dnsList.add(netEntryData);
            }
        }

		if(sp.getBoolean("firstboot", true)) {
            setDNSCompletingList();
            sped.putBoolean("firstboot", false);
            sped.apply();
        }

        //construecting header
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setPadding(10, 10, 10, 10);
        headerLayout.setOnClickListener(null);
        TextView currentDNSText = new TextView(this);
        currentDNSText.setText(R.string.cdnstext);
        currentDns = new TextView(this);
        headerLayout.addView(currentDNSText);
        headerLayout.addView(currentDns);
        if(sp.getBoolean("auto_setting", true)) {
            TextView priorityText = new TextView(this);
            priorityText.setText(R.string.priority_text);
            headerLayout.addView(priorityText);
        }
        mainList.addHeaderView(headerLayout);

        adapter = new SimpleAdapter(this, dnsList,
                android.R.layout.simple_list_item_2,
                new String[] {"label", "dns_data"},
                new int[]{ android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);

        //listener
        mainList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> data = (Map<String, String>) parent.getItemAtPosition(position);
                Intent i = new Intent(context, DNSEntryActivity.class);
                i.putExtra("label", data.get("label"));
                i.putExtra("dns1key", data.get("dns1key"));
                i.putExtra("dns2key", data.get("dns2key"));
                startActivity(i);
            }
        });



        registerReceiver(dnsSetted, new IntentFilter(DNSmanConstants.ACTION_SETDNS_DONE));

        setDNSWatchingService();

        checkRoot();
        (new getDNSTask()).execute();
	}

	@Override
    public void onResume(){
        super.onResume();
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();

        String last_mode = sp.getString("last_mode", "PROP");
        current_mode = sp.getString("mode", "PROP");
		if(!current_mode.equals(last_mode)){
            sped.putString("last_mode", current_mode);
            sped.apply();
            adapter.notifyDataSetChanged();
        }
        if(!sp.getBoolean("pref_dns_monitor", true)){
            stopService(dnsWatchingServiceIntent);
        }

        //no root
        if(!sp.getBoolean("rooted", true)) {
            if (!current_mode.equals("VPN")) {
                Toast.makeText(this, R.string.toast_noroot, Toast.LENGTH_LONG).show();
                sped.putString("mode", "VPN");
                sped.apply();
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.resolv_edit:
				startActivity(new Intent(this, DNSConfActivity.class));
				break;
            case R.id.delete_rule:
                DNSBackgroundService.deleteLastRules(this);
                break;
			case R.id.settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
		
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dnsSetted);
    }

    private void setDNSCompletingList(){
        Set<String> toSavedDNS = new HashSet<>(Arrays.asList(DNSmanConstants.DEFAULT_LIST));
        sped.putStringSet("dnslist", toSavedDNS);
        sped.apply();
    }

    private void setDNSWatchingService(){
        if(sp.getBoolean("pref_dns_moniter", true)) {
            startService(dnsWatchingServiceIntent);
        }
    }

    private class checkRootTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void[] p1) {
            sped.putBoolean("rooted",Shell.SU.available());
            sped.apply();
            return null;
        }
    }

    private void checkRoot(){
        new checkRootTask().execute();
    }

    private class getDNSTask extends AsyncTask<Void, Void, List<String>>{
        boolean haveRules = false;
        protected List<String> doInBackground(Void[] p1) {
            List<String> currentDNSData = new ArrayList<>();

            //Check firewall rules
            String entry;
            String ip = sp.getString("lastHijackedDNS", "");
            String port = sp.getString("lastHijackedPort", "");
            if (!ip.equals("") && DNSManager.isRulesAlivable(ip, port)) {
                haveRules = true;

                if (!port.equals("")) {
                    entry = ip + ":" + port;
                } else {
                    entry = ip;
                }

                currentDNSData.add(entry);
            }

            //Check system properties
            List<String> prop_dns = DNSManager.getCurrentPropDNS();
            if(!prop_dns.isEmpty()){
                //ALERT USER
                if(haveRules && !current_mode.equals("IPTABLES")){
                    currentDNSData.add(getText(R.string.firewall_rules_available).toString());
                } else if(!haveRules) {
                    currentDNSData.addAll(prop_dns);
                }
            }
            for(int i = 0; i != currentDNSData.size(); i++){
                Log.d("MainActivity", "data = " + currentDNSData.get(i));
            }
            return currentDNSData;
        }

        protected void onPostExecute(List<String> data){
            String dnsString = "";
            for(int i = 0; i != data.size(); i++){
                dnsString += data.get(i) + "\n";
            }
            currentDns.setText(dnsString);
            if(haveRules){
                //Escaping crash when it faster than menu creates
                if(menu != null) {
                    MenuItem itemDelete = menu.findItem(R.id.delete_rule);
                    itemDelete.setEnabled(true);
                }
            }
        }

    }
}
