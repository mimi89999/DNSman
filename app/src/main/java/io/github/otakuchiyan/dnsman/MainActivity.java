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
import android.content.DialogInterface;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import io.github.otakuchiyan.dnsman.SettingsActivity;
import io.github.otakuchiyan.dnsman.IPChecker;
import io.github.otakuchiyan.dnsman.IPCheckerComponent;
import io.github.otakuchiyan.dnsman.DNSConfActivity;
import io.github.otakuchiyan.dnsman.GetNetwork;
import io.github.otakuchiyan.dnsman.LocalDNSDetecter;
import io.github.otakuchiyan.dnsman.DNSEditText;

import android.widget.*;
import android.widget.Toolbar.*;
import android.view.*;

public class MainActivity extends Activity {
    final private String ACTION_GETDNS = "io.github.otakuchiyan.dnsman.ACTION_GETDNS";
    
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;

    //Layouts
    private LinearLayout mainActivity;
	private LinearLayout.LayoutParams edittext_params = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.WRAP_CONTENT,
            1.0f);
    private TextView cdnstext;
	private TextView cdns1;
	private TextView cdns2;
    private TextView global_category;
    private EditText gdns1;
    private EditText gdns2;
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
		if(i.getAction().equals(DNSBackgroundIntentService.ACTION_SETDNS_DONE)){
		    if(i.getBooleanExtra("result", false)){
			(new getDNSAsync()).execute();
		    }
		}
	    }
	};

    private BroadcastReceiver dnscryptDetected = new BroadcastReceiver(){
        @Override
        public void onReceive(Context c, Intent i){
	    if(i.getAction().equals(LocalDNSDetecter.ACTION_DNSCRYPT_RUNNING) &&
	       LocalDNSDetecter.isEnabled(c) &&
	       LocalDNSDetecter.isGlobalDNSSetted(c)){
		Toast.makeText(c, "DNS detected", Toast.LENGTH_LONG).show();
	    }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		sped = sp.edit();

        GetNetwork.init(this);
		mainActivity = new LinearLayout(this);
		mainActivity.setOrientation(LinearLayout.VERTICAL);

        LinearLayout cdnsView = new LinearLayout(this);
        cdnsView.setOrientation(LinearLayout.HORIZONTAL);
        cdnstext = new TextView(this);
        cdnstext.setText(R.string.current_dnss);
        cdns1 = new TextView(this);
		cdns2 = new TextView(this);
        cdns1.setLayoutParams(edittext_params);
        cdns2.setLayoutParams(edittext_params);
        
		cdnsView.addView(cdnstext);
        cdnsView.addView(cdns1);
        cdnsView.addView(cdns2);
        mainActivity.addView(cdnsView);

        global_category = new TextView(this);
        global_category.setText(R.string.global_category);
        mainActivity.addView(global_category);
        mainActivity.addView(setDNSTwopane(gdns1, gdns2, "g"));

        if(GetNetwork.isSupportWiFi()){
            wifi_category = new TextView(this);
            wifi_category.setText(R.string.wifi_category);
            mainActivity.addView(wifi_category);
            mainActivity.addView(setDNSTwopane(wdns1, wdns2, "w"));
        }

        if(GetNetwork.isSupportMobile()){
            mobile_category = new TextView(this);
            mobile_category.setText(R.string.mobile_category);
            mainActivity.addView(mobile_category);
            mainActivity.addView(setDNSTwopane(mdns1, mdns2, "m"));
        }

        if(GetNetwork.isSupportBluetooth()){
            bt_category = new TextView(this);
            bt_category.setText(R.string.bt_category);
            mainActivity.addView(bt_category);
            mainActivity.addView(setDNSTwopane(bdns1, bdns2, "b"));
        }

        if(GetNetwork.isSupportEthernet()){
            eth_category = new TextView(this);
            eth_category.setText(R.string.eth_category);
            mainActivity.addView(eth_category);
            mainActivity.addView(setDNSTwopane(edns1, edns2, "e"));
        }
       
        if(GetNetwork.isSupportWiMax()){
            wimax_category = new TextView(this);
            wimax_category.setText(R.string.wimax_category);
            mainActivity.addView(wimax_category);
            mainActivity.addView(setDNSTwopane(widns1, widns2, "wi"));
        }

		
		if(!sp.getBoolean("firstbooted", false)){
			showWelcomeDialog();
			sped.putBoolean("firstbooted", true);
			sped.commit();
		}

		IntentFilter setFilter = new IntentFilter();
		setFilter.addAction(DNSBackgroundIntentService.ACTION_SETDNS_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(dnsSetted, setFilter);

		IntentFilter getFilter = new IntentFilter();
		getFilter.addAction(ACTION_GETDNS);
		LocalBroadcastManager.getInstance(this).registerReceiver(getDNSFinished, getFilter);

		setContentView(mainActivity);
		
        (new getDNSAsync()).execute();

	LocalDNSDetecter.detect(this);

	}

    private LinearLayout setDNSTwopane(EditText e1, EditText e2, String keyprefix){
        LinearLayout ll = new LinearLayout(this);
	e1 = new DNSEditText(this, keyprefix + "dns1");
	e2 = new DNSEditText(this, keyprefix + "dns2");

        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.addView(e1);
        ll.addView(e2);
        return ll;
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
	
    private BroadcastReceiver getDNSFinished = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		if(i.getAction().equals(ACTION_GETDNS)){
		    cdns1.setText(i.getStringExtra("dns1"));
		    cdns2.setText(i.getStringExtra("dns2"));
		}
	    }
	};

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
