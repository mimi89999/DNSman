
package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.app.*;
import android.os.AsyncTask;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Switch;
import android.widget.CompoundButton;

import java.util.List;

import io.github.otakuchiyan.dnsman.SettingsActivity;
import io.github.otakuchiyan.dnsman.IPChecker;
import io.github.otakuchiyan.dnsman.IPCheckerOnFocusChangeListener;
import io.github.otakuchiyan.dnsman.DNSConfActivity;
import io.github.otakuchiyan.dnsman.GetNetwork;
import android.widget.*;
import android.widget.CompoundButton.*;

public class MainActivity extends Activity {
    final private String ACTION_GETDNS = "io.github.otakuchiyan.dnsman.ACTION_GETDNS";
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
	
	private EditText wdns1;
	private EditText wdns2;
	private EditText mdns1;
	private EditText mdns2;
	private TextView cdns1;
	private TextView cdns2;
	private TextView wifi_category;
	private TextView mobile_category;
    private Switch distinguish_swh;


    private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(DNSBackgroundIntentService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
			(new getDNSAsync()).execute();
		    }
		}
	    }
	};

    private BroadcastReceiver getDNSFinished = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(ACTION_GETDNS)){
		    cdns1.setText(i.getStringExtra("dns1"));
		    cdns2.setText(i.getStringExtra("dns2"));
		}
	    }
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();
		
		wdns1 = (EditText) findViewById(R.id.wdns1);
		wdns2 = (EditText) findViewById(R.id.wdns2);
		mdns1 = (EditText) findViewById(R.id.mdns1);
		mdns2 = (EditText) findViewById(R.id.mdns2);
		cdns1 = (TextView) findViewById(R.id.cdns1);
		cdns2 = (TextView) findViewById(R.id.cdns2);
		wifi_category = (TextView) findViewById(R.id.wifi_category);
		mobile_category = (TextView) findViewById(R.id.mobile_category);
		distinguish_swh = (Switch) findViewById(R.id.distinguish);
		
		if(!sp.getBoolean("firstbooted", false)){
		    GetNetwork.init(this);
		    if(GetNetwork.getMobileNetwork() == null){
				distinguish_swh.setEnabled(false);
		    }
			sped.putBoolean("distinguish", false);
			showWelcomeDialog();
			sped.putBoolean("firstbooted", true);
			sped.commit();
		}

		if(sp.getBoolean("distinguish", false)){
		    enableMobileDNSView();
		}else{
		    		    disableMobileDNSView();

		}


		wdns1.setText(sp.getString("wdns1", ""));
		wdns2.setText(sp.getString("wdns2", ""));
		wdns1.setOnFocusChangeListener(
		new IPCheckerOnFocusChangeListener(this, wdns1, "wdns1"));
		wdns2.setOnFocusChangeListener(
		new IPCheckerOnFocusChangeListener(this, wdns2, "wdns2"));
        mdns1.setText(sp.getString("mdns1", ""));
        mdns2.setText(sp.getString("mdns2", ""));
	    mdns1.setOnFocusChangeListener(
					   new IPCheckerOnFocusChangeListener(this, mdns1, "mdns1"));
	    mdns2.setOnFocusChangeListener(
			new IPCheckerOnFocusChangeListener(this, mdns2, "mdns2"));
	    (new getDNSAsync()).execute();
		distinguish_swh.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
				public void onCheckedChanged(CompoundButton cb, boolean state){
				    if(state){
						disableMobileDNSView();
				    }else{
						enableMobileDNSView();
					}
				}
			});


		IntentFilter setFilter = new IntentFilter();
		setFilter.addAction(DNSBackgroundIntentService.ACTION_SETDNS_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted, setFilter);

		IntentFilter getFilter = new IntentFilter();
		getFilter.addAction(ACTION_GETDNS);
		LocalBroadcastManager.getInstance(this).registerReceiver(getDNSFinished, getFilter);
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

    private void enableMobileDNSView(){
	mdns1.setEnabled(true);
	mdns2.setEnabled(true);
	mobile_category.setEnabled(true);
	wifi_category.setText(getText(R.string.wifi_category));
    }

    private void disableMobileDNSView(){
        mdns1.setEnabled(false);
	mdns2.setEnabled(false);
	mobile_category.setEnabled(false);
        wifi_category.setText(getText(R.string.global_category));
    }
	
	private void getCurrentDNS(){
		List<String> currentDNSs = DNSManager.getCurrentDNS();

		Intent i = new Intent(ACTION_GETDNS);
		i.putExtra("dns1", currentDNSs.get(0));
		i.putExtra("dns2", currentDNSs.get(1));
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}
	
	private class getDNSAsync extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void[] p1)
		{
			getCurrentDNS();
			return null;
		}
	}
}
