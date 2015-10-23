package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences; 
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.List;

import android.widget.*;
import android.widget.Toolbar.*;

public class MainActivity extends Activity {
    final private String ACTION_GETDNS = "io.github.otakuchiyan.dnsman.ACTION_GETDNS";
	final private String ACTION_DELETE_RULES = "io.github.otakuchiyan.dnsman.ACTION_DELETE_RULES";
    
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;

    //Layouts
    private LinearLayout mainActivity;
	private LinearLayout.LayoutParams edittext_params = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.WRAP_CONTENT,
            1.0f);
    private TextView cdnstext;
	private TextView propdnstext;
	private TextView cdns1;
	private TextView cdns2;
    private TextView global_category;
    private EditText gdns1;
    private EditText gdns2;
    private TextView individual_category;
	private TextView wifi_category;
	private EditText wdns1;
	private EditText wdns2;
	private TextView mobile_category;
	private EditText mdns1;
	private EditText mdns2;
    private TextView bt_category;
    private EditText bdns1;
    private EditText bdns2;
    private TextView eth_category;
    private EditText edns1;
    private EditText edns2;
    private TextView wimax_category;
    private EditText widns1;
    private EditText widns2;
	
    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(DNSBackgroundService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
			//(new getDNSAsync()).execute();
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
	mainActivity = new LinearLayout(this);
	mainActivity.setOrientation(LinearLayout.VERTICAL);

        LinearLayout cdnsView = new LinearLayout(this);
        cdnsView.setOrientation(LinearLayout.HORIZONTAL);
        cdnstext = setCategoryText(R.string.cdnstext);
	propdnstext = new TextView(this);
        cdns1 = new TextView(this);
        cdns2 = new TextView(this);
	propdnstext.setText(R.string.pref_mode_prop);
        cdns1.setLayoutParams(edittext_params);
        cdns2.setLayoutParams(edittext_params);

	cdnsView.addView(propdnstext);
        cdnsView.addView(cdns1);
        cdnsView.addView(cdns2);
		mainActivity.addView(cdnstext);
        mainActivity.addView(cdnsView);


        global_category = setCategoryText(R.string.global_category);
        mainActivity.addView(global_category);
        mainActivity.addView(setDNSTwopane(gdns1, gdns2, "g"));

        individual_category = setCategoryText(R.string.individual_category);
        mainActivity.addView(individual_category);

        if(gn.isSupportWifi){
            wifi_category = new TextView(this);
            wifi_category.setText(R.string.wifi_category);
            mainActivity.addView(wifi_category);
            mainActivity.addView(setDNSTwopane(wdns1, wdns2, gn.wifiName));
        }

        if(gn.isSupportMobile){
            mobile_category = new TextView(this);
            mobile_category.setText(R.string.mobile_category);
            mainActivity.addView(mobile_category);
            mainActivity.addView(setDNSTwopane(mdns1, mdns2, gn.mobileName));
        }

        if(gn.isSupportBluetooth){
            bt_category = new TextView(this);
            bt_category.setText(R.string.bt_category);
            mainActivity.addView(bt_category);
            mainActivity.addView(setDNSTwopane(bdns1, bdns2, gn.bluetoothName));
        }

        if(gn.isSupportEthernet){
            eth_category = new TextView(this);
            eth_category.setText(R.string.eth_category);
            mainActivity.addView(eth_category);
            mainActivity.addView(setDNSTwopane(edns1, edns2, gn.etherName));
        }
       
        if(gn.isSupportWimax){
            wimax_category = new TextView(this);
            wimax_category.setText(R.string.wimax_category);
            mainActivity.addView(wimax_category);
            mainActivity.addView(setDNSTwopane(widns1, widns2, gn.wimaxName));
        }

		
		if(!sp.getBoolean("firstbooted", false)){
			showWelcomeDialog();
			sped.putBoolean("firstbooted", true);
			sped.apply();
		}

		IntentFilter setFilter = new IntentFilter();
		setFilter.addAction(DNSBackgroundService.ACTION_SETDNS_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted, setFilter);

        (new getDNSTask()).execute();

		setContentView(mainActivity);
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

    private LinearLayout setDNSTwopane(EditText e1, EditText e2, String keyprefix){
        LinearLayout ll = new LinearLayout(this);
		boolean isPort = false;
		String e2Suffix = "dns2";
		if(sp.getString("mode", "0").equals("1")) {
			isPort = true;
			e2Suffix = "port";
		}
		e1 = new DNSEditText(this, keyprefix + "dns1", false);
		e2 = new DNSEditText(this, keyprefix + e2Suffix, isPort);

        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.addView(e1);
        ll.addView(e2);
        return ll;
    }

    private TextView setCategoryText(int res){
        TextView tv = new TextView(this);
        tv.setText(res);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        return tv;
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
			Log.d("MA", "getDNS");
            return DNSManager.getCurrentDNS();
        }

        protected void onPostExecute(List<String> dnss){
            cdns1.setText(dnss.get(0));
            cdns2.setText(dnss.get(1));
        }

    }
	
}
