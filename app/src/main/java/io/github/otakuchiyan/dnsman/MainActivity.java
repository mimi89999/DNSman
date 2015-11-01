package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.app.*;
import android.os.AsyncTask;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.TypedValue;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.widget.*;
import android.widget.Toolbar.*;

public class MainActivity extends ListActivity {
    final private String ACTION_GETDNS = "io.github.otakuchiyan.dnsman.ACTION_GETDNS";
	final private String ACTION_DELETE_RULES = "io.github.otakuchiyan.dnsman.ACTION_DELETE_RULES";
    
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
    private LinearLayout currentDNSLayout;
    private TextView currentDNSText;
    private TextView currentDNS1;
    private TextView currentDNS2;

    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(DNSBackgroundService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
                new getDNSTask().execute();
		    }
		}
	    }
	};

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

        ListView mainList = getListView();
        mainList.addHeaderView(currentDNSLayout);

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

		LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted,
                new IntentFilter(ACTION_GETDNS));

        (new getDNSTask()).execute();
	}

	@Override
    public void onResume(){
        super.onResume();
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();
		String current_mode = sp.getString("mode", "0");
		if(!current_mode.equals(sp.getString("last_mode", "0"))){
			sped.putString("last_mode", current_mode);
			sped.apply();
			finish();
			startActivity(getIntent());
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()){
			case R.id.resolv_edit:
				startActivity(new Intent(this, DNSConfActivity.class));
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
        protected List<String> doInBackground(Void[] p1){
            return DNSManager.getCurrentDNS();
        }

        protected void onPostExecute(List<String> dnss){
            currentDNS1.setText(dnss.get(0));
            currentDNS2.setText(dnss.get(1));
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
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.net_item, parent, false);
            TextView netText = (TextView) rowView.findViewById(R.id.net_text);
            Button button = (Button) rowView.findViewById(R.id.button);
            DNSEditText dns1 = (DNSEditText) rowView.findViewById(R.id.dns1);
            DNSEditText dns2 = (DNSEditText) rowView.findViewById(R.id.dns2);
            GetNetwork gn = new GetNetwork(context);
            final String dns1key = netNameList.get(position) + "dns1";

            String dns2Suffix = "dns2";
            if(sp.getString("mode", "0").equals("1")) {
                dns2.setFirewallMode();
                dns2Suffix = "port";
            }
            final String dns2key = netNameList.get(position) + dns2Suffix;

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
                    DNSBackgroundService.setByString(context, dns1str, dns2str);
                }
            });

            return rowView;
        }


    }
	
}
