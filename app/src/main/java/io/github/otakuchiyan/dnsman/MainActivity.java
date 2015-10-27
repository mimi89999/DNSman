package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
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
        getActionBar().setDisplayShowHomeEnabled(false);

	sp = PreferenceManager.getDefaultSharedPreferences(this);
	sped = sp.edit();

        GetNetwork gn = new GetNetwork(this);


        ArrayList<String> netList = new ArrayList<>();
        netList.add(getText(R.string.global_category).toString());


        if(gn.isSupportWifi){
            netList.add(getText(R.string.wifi_category).toString());
//            mainActivity.addView(setDNSTwopane(wdns1, wdns2, gn.wifiName));
        }

        if(gn.isSupportMobile){
            netList.add(getText(R.string.mobile_category).toString());

//            mainActivity.addView(setDNSTwopane(mdns1, mdns2, gn.mobileName));
        }

        if(gn.isSupportBluetooth){
            netList.add(getText(R.string.bt_category).toString());
                    }

        if(gn.isSupportEthernet){
            netList.add(getText(R.string.eth_category).toString());

        }
       
        if(gn.isSupportWimax){

            netList.add(getText(R.string.wimax_category).toString());
        }

		
		if(!sp.getBoolean("firstbooted", false)) {
            showWelcomeDialog();
            sped.putBoolean("firstbooted", true);
            sped.apply();
        }


        final ArrayAdapter<String> adapter = new CustomArrayAdapter(this, netList);

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

    private LinearLayout setDNSTwopane(EditText e1, EditText e2, String keyprefix){
        LinearLayout ll = new LinearLayout(this);
		boolean isPort = false;
		String e2Suffix = "dns2";
		if(sp.getString("mode", "0").equals("1")) {
			isPort = true;
			e2Suffix = "port";
		}
		//e1 = new DNSEditText(this, keyprefix + "dns1", false);
		//e2 = new DNSEditText(this, keyprefix + e2Suffix, isPort);

        ll.setOrientation(LinearLayout.HORIZONTAL);
        //ll.addView(e1);
        //ll.addView(e2);
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

    private class getDNSTask extends AsyncTask<Void, Void, List<String>>{
        protected List<String> doInBackground(Void[] p1){
            return DNSManager.getCurrentDNS();
        }

        protected void onPostExecute(List<String> dnss){
            getActionBar().setTitle(dnss.get(0) + " & " + dnss.get(1));
        }

    }

    private class CustomArrayAdapter extends ArrayAdapter<String>{
        private Context context;
        private ArrayList<String> values;
        public CustomArrayAdapter(Context c, ArrayList<String> values){
            super(c, -1, values);
            this.context = c;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.net_item, parent, false);
            TextView netText = (TextView) rowView.findViewById(R.id.net_text);

            netText.setText(values.get(position));

            return rowView;
        }


    }
	
}
