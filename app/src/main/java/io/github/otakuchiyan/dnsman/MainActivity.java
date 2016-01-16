package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
    private LinearLayout currentDNSLayout;
    private TextView currentDNSText;
    private TextView currentDNS1;
    private TextView currentDNS2;
    private String current_mode;
    private Menu menu;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();


        GetNetwork gn = new GetNetwork(this);

        currentDNSLayout = new LinearLayout(this);
        currentDNSLayout.setOrientation(LinearLayout.VERTICAL);
        currentDNSText = new TextView(this);
        currentDNSText.setText(R.string.cdnstext);
        currentDNSText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        currentDNS1 = new TextView(this);
        currentDNS2 = new TextView(this);

        currentDNSLayout.addView(currentDNSText);
        currentDNSLayout.addView(currentDNS1);
        currentDNSLayout.addView(currentDNS2);

        final ListView mainList = getListView();
        mainList.addHeaderView(currentDNSLayout);
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


        ArrayList<String> netLabelList = new ArrayList<>();
        ArrayList<String> netNameList = new ArrayList<>();
        netLabelList.add(getText(R.string.global_category).toString());
        netNameList.add("g");


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

		
		if(!sp.getBoolean("firstbooted", false)) {
            showWelcomeDialog();
            sped.putBoolean("firstbooted", true);
            sped.apply();
        }

        final ArrayAdapter<String> adapter = new CustomArrayAdapter(this, netLabelList, netNameList);
        setListAdapter(adapter);

        BroadcastReceiver dnsSetted = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i){
                if(i.getAction().equals(DNSBackgroundService.ACTION_SETDNS_DONE)){
                    if(i.getBooleanExtra("result", false)){
                        new getDNSTask().execute();

                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted,
                new IntentFilter(DNSBackgroundService.ACTION_SETDNS_DONE));

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
			.setPositiveButton(android.R.string.ok, null);
		adb.create().show();
	}

    private class getDNSTask extends AsyncTask<Void, Void, List<String>>{
        boolean haveRules = false;
        protected List<String> doInBackground(Void[] p1){
            current_mode = sp.getString("mode", "PROP");
            if(current_mode.equals("IPTABLES")){
                List<String> dnss = new ArrayList<>();
                String entry;
                String ip = sp.getString("lastHijackedDNS", "");
                String port = sp.getString("lastHijackedPort", "");
                if(DNSManager.isRulesAlivable(ip, port)){
                    haveRules = true;

                    if(!port.equals("")){
                        entry = ip + ":" + port;
                    }else{
                        entry = ip;
                    }

                    dnss.add(entry);
                } else{ //Fill the list
                    dnss.add("");
                }
                dnss.add("");
                return dnss;
            }
            return DNSManager.getCurrentDNS();
        }

        protected void onPostExecute(List<String> dnss){
            currentDNS1.setText(dnss.get(0));
            currentDNS2.setText(dnss.get(1));
            if(haveRules){
                MenuItem itemDelete = menu.findItem(R.id.delete_rule);
                itemDelete.setEnabled(true);
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
