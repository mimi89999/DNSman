package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends ListActivity {
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
    private LinearLayout currentDNSLayout;
    private String current_mode;
    private Menu menu;
    private Context context;

    private DNSMonitorService dnsMonitorService;
    private boolean dnsMonitorServiceIsBound;
    private ServiceConnection dnsMonitorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dnsMonitorService = ((DNSMonitorService.DNSWatchingBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dnsMonitorService = null;
        }
    };
    private Intent dnsWatchingServiceIntent;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        dnsWatchingServiceIntent = new Intent(this, DNSMonitorService.class);
        context = this;

        final ListView mainList = getListView();
        ArrayList<String> netLabelList = new ArrayList<>();
        ArrayList<String> netNameList = new ArrayList<>();

        //Constructing list
        netLabelList.add(getText(R.string.global_category).toString());
        netNameList.add("g");
        GetNetwork gn = new GetNetwork(this);
        if(gn.isSupportWifi){
            netLabelList.add(getText(R.string.wifi_category).toString());
            netNameList.add(gn.wifiName);
        }
        if(gn.isSupportMobile){
            netLabelList.add(getText(R.string.mobile_category).toString());
            netNameList.add(gn.mobileName);
        }
        if(gn.isSupportBluetooth){
            netLabelList.add(getText(R.string.bt_category).toString());
            netNameList.add(gn.bluetoothName);
        }
        if(gn.isSupportEthernet){
            netLabelList.add(getText(R.string.eth_category).toString());
            netNameList.add(gn.etherName);
        }
        if(gn.isSupportWimax){
            netLabelList.add(getText(R.string.wimax_category).toString());
            netNameList.add(gn.wimaxName);
        }

		if(sp.getBoolean("firstboot", true)) {
            showWelcomeDialog();
            setDNSCompletingList();
            sped.putBoolean("firstboot", false);
            sped.apply();
        }

        //construecting header
        currentDNSLayout = new LinearLayout(this);
        currentDNSLayout.setOrientation(LinearLayout.VERTICAL);
        mainList.addHeaderView(currentDNSLayout);

        //listener
        mainList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    mainList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                    view.requestFocus();
                } else if (!mainList.isFocused()) {
                    mainList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    mainList.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mainList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });
        mainList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        //broadcast
        BroadcastReceiver dnsSetted = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i){
                if(i.getAction().equals(DNSmanConstants.ACTION_SETDNS_DONE)){
                    if(i.getBooleanExtra("result", false)){
                        new getDNSTask().execute();

                    }
                }
            }
        };

        final ArrayAdapter<String> adapter = new CustomArrayAdapter(this, netLabelList, netNameList);
        setListAdapter(adapter);


        LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted,
                new IntentFilter(DNSmanConstants.ACTION_SETDNS_DONE));
        setDNSWatchingService();

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
            finish();
            startActivity(getIntent());
        }
        boolean enable_ipv6 = sp.getBoolean("enable_ipv6", false);
        if(enable_ipv6 != sp.getBoolean("already_enable_ipv6", false)){
            sped.putBoolean("already_enable_ipv6", enable_ipv6);
            sped.apply();
            finish();
            startActivity(getIntent());
        }
        if(!sp.getBoolean("pref_dnswatching", true)){
            if(dnsMonitorServiceIsBound) {
                stopService(dnsWatchingServiceIntent);
                //unbindService(dnsWatchingConnection);
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
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

	private void showWelcomeDialog(){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(R.string.welcome)
			.setMessage(R.string.welcome_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/otakuchiyan/DNSman/wiki/"));
                    startActivity(i);
                }
            })
                .setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}

    private void setDNSCompletingList(){
        Set<String> toSavedDNS = new HashSet<>(Arrays.asList(DNSmanConstants.DEFAULT_LIST));
        sped.putStringSet("dnslist", toSavedDNS);
        sped.apply();
    }

    private void setDNSWatchingService(){
        if(sp.getBoolean("pref_dnswatching", true)) {
            /*bindService(new Intent(context, DNSMonitorService.class),
                    dnsWatchingConnection, Context.BIND_AUTO_CREATE);
            dnsWatchingServiceIsBound = true;*/
            startService(dnsWatchingServiceIntent);
        }
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
            currentDNSLayout.removeAllViews();
            TextView currentDNSText = new TextView(context);
            currentDNSText.setText(R.string.cdnstext);
            currentDNSText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            currentDNSLayout.addView(currentDNSText);
            for(int i = 0; i != data.size(); i++){
                TextView t = new TextView(context);
                t.setText(data.get(i));
                currentDNSLayout.addView(t);
            }
            if(haveRules){
                //Escaping crash when it faster than menu creates
                if(menu != null) {
                    MenuItem itemDelete = menu.findItem(R.id.delete_rule);
                    itemDelete.setEnabled(true);
                }
            }
        }

    }

    private class CustomArrayAdapter extends ArrayAdapter<String>{
        private Context context;
        private ArrayList<String> netLabelList;
        private ArrayList<String> netNameList;

        public CustomArrayAdapter(Context c, ArrayList<String> netLabelList,
                                  ArrayList<String> netNameList){
            super(c, -1, netLabelList);
            this.context = c;
            this.netLabelList = netLabelList;
            this.netNameList = netNameList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.net_item, parent, false);
            TextView netText = (TextView) rowView.findViewById(R.id.net_text);
            final Button button = (Button) rowView.findViewById(R.id.button);
            DNSEditText dns1 = (DNSEditText) rowView.findViewById(R.id.dns1);
            DNSEditText dns2 = (DNSEditText) rowView.findViewById(R.id.dns2);
            GetNetwork gn = new GetNetwork(context);
            final String currentNetName = netNameList.get(position);
            final String dns1key = currentNetName + "dns1";

            String dns2Suffix = "dns2";
            if(current_mode.equals("IPTABLES")) {
                dns2.setFirewallMode();
                dns2Suffix = "port";
            }
            final String dns2key = currentNetName + dns2Suffix;

            netText.setText(netLabelList.get(position));
            dns1.setKeyAndText(dns1key);
            dns2.setKeyAndText(dns2key);
            dns1.setIPChecker();
            dns2.setIPChecker();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String dns1str = sp.getString(dns1key, "");
                    String dns2str = sp.getString(dns2key, "");

                    if (dns1str.equals("") && dns2str.equals("")) {
                        return;
                    }

                    DNSBackgroundService.setByString(context, dns1str, dns2str);
                }
            });

            return rowView;

        }

    }
	
}
