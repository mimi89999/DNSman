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

import java.util.List;

import io.github.otakuchiyan.dnsman.SettingsActivity;
import io.github.otakuchiyan.dnsman.IPChecker;
import io.github.otakuchiyan.dnsman.IPCheckerOnFocusChangeListener;
import io.github.otakuchiyan.dnsman.DNSConfActivity;

public class MainActivity extends Activity {
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
   
	private BroadcastReceiver dnsSetted = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(DNSBackgroundIntentService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
				getCurrentDNS();
		}
		}
	    }
	};


	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();
		
		if(!sp.getBoolean("firstbooted", false)){
			sped.putBoolean("distinguish", false);
			showWelcomeDialog();
			sped.putBoolean("firstbooted", true);
			sped.commit();
		}
	
		setContentView(R.layout.main_activity);
		
		wdns1 = (EditText) findViewById(R.id.wdns1);
		wdns2 = (EditText) findViewById(R.id.wdns2);
		mdns1 = (EditText) findViewById(R.id.mdns1);
		mdns2 = (EditText) findViewById(R.id.mdns2);
		cdns1 = (TextView) findViewById(R.id.cdns1);
		cdns2 = (TextView) findViewById(R.id.cdns2);
		wifi_category = (TextView) findViewById(R.id.wifi_category);
		mobile_category = (TextView) findViewById(R.id.mobile_category);
		
		wdns1.setText(sp.getString("wdns1", ""));
		wdns2.setText(sp.getString("wdns2", ""));
		/*
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(DNSBackgroundIntentService.ACTION_SETDNS_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted, iFilter);
		*/
		
		(new getDNSAsync()).execute();
		
		if(sp.getBoolean("distinguish", false)){
		mdns1.setText(sp.getString("mdns1", ""));
		mdns2.setText(sp.getString("mdns2", ""));
		
		
			mdns1.setOnFocusChangeListener(
				new IPCheckerOnFocusChangeListener(this, mdns1, "mdns1"));
			mdns2.setOnFocusChangeListener(
			new IPCheckerOnFocusChangeListener(this, mdns2, "mdns2"));
			
		}else{
			mdns1.setAlpha(0.0f);
			mdns2.setAlpha(0.0f);
			mobile_category.setText("");
			wifi_category.setText(getText(R.string.global_category));
		}
		
		wdns1.setOnFocusChangeListener(
		new IPCheckerOnFocusChangeListener(this, wdns1, "wdns1"));
		
		wdns2.setOnFocusChangeListener(
		new IPCheckerOnFocusChangeListener(this, wdns2, "wdns2"));
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
			/*
			case R.id.refresh_cdns:
				(new getDNSAsync()).execute();
				break;
				*/
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
	
	private void getCurrentDNS(){
		List<String> currentDNSs = DNSManager.getCurrentDNS();
		
		cdns1.setText(currentDNSs.get(0));
		cdns2.setText(currentDNSs.get(1));
	}
	
	private class getDNSAsync extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void[] p1)
		{
			getCurrentDNS();
			// TODO: Implement this method
			return null;
		}
	}
}
