package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.lang.Void;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.IPCheckerComponent;
import io.github.otakuchiyan.dnsman.DNSEditText;
import android.view.View.*;
import android.content.*;
import android.view.*;


public class DNSConfActivity extends Activity{
    final static String ACTION_CONFOPERATION = "io.github.otakuchiyan.dnsman.ACTION_CONFOPERATION";
    final static String ACTION_CONF_GETTED = "io.github.otakuchiyan.dnsman.ACTION_CONF_GETTED";
    
	private SharedPreferences sp;
    private LinearLayout dnsConfActivity;
    private TextView confPath;
    private TextView confDNS;
	private EditText rdns1;
	private EditText rdns2;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.action_edit_resolv);
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        dnsConfActivity = new LinearLayout(this);
        dnsConfActivity.setOrientation(LinearLayout.VERTICAL);
	confPath = new TextView(this);
	confDNS = new TextView(this);
	confPath.setText("/etc/resolv.conf:");

	dnsConfActivity.addView(confPath);
	dnsConfActivity.addView(confDNS);
        dnsConfActivity.addView(setDNSTwopane());

		IntentFilter operationFilter = new IntentFilter();
		operationFilter.addAction(ACTION_CONFOPERATION);
		LocalBroadcastManager.getInstance(this).registerReceiver(confOperation, operationFilter);

        IntentFilter confFilter = new IntentFilter();
        confFilter.addAction(ACTION_CONF_GETTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(gettedConf, confFilter);
		
        setContentView(dnsConfActivity);
        (new getConfAsync()).execute();
		}

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
	getMenuInflater().inflate(R.menu.dnsconf, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
	switch(item.getItemId()){
	case R.id.write_conf:
	    writeConf();
	    break;
	case R.id.default_conf:
	    defaultConf();
	    break;
	case R.id.delete_conf:
	    deleteConf();
	    break;
	}
	return super.onOptionsItemSelected(item);
    }

    private LinearLayout setDNSTwopane(){
        LinearLayout ll = new LinearLayout(this);
	rdns1 = new DNSEditText(this, "rdns1", false);
	rdns2 = new DNSEditText(this, "rdns2", false);

        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.addView(rdns1);
        ll.addView(rdns2);

        return ll;
    }

    private BroadcastReceiver confOperation = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		String err = i.getStringExtra("err");
		if(i.getAction().equals(ACTION_CONFOPERATION)){
		    if(err != null){
				Toast.makeText(c, err, Toast.LENGTH_LONG).show();
		    }else{
	    Toast.makeText(c, R.string.operation_succeed, Toast.LENGTH_SHORT).show();
		    }
		}
	    }
	};
	
	
	public void writeConf(){
		final String rdns1ip = rdns1.getText().toString();
		final String rdns2ip = rdns2.getText().toString();
		
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.write_conf_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface di, int which){
				    (new writeConfAsync()).execute(rdns1ip, rdns2ip);
                    (new getConfAsync()).execute();
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
	
	public void defaultConf(){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.default_conf_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface di, int which){
				    (new writeConfAsync()).execute("8.8.8.8", "8.8.4.4");
                    (new getConfAsync()).execute();
					}
			})
			.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
	
    public void deleteConf(){
	AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.delete_conf_msg)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface di, int which){
			    (new deleteConfAsync()).execute();
				}
		})
		.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
	
    private class writeConfAsync extends AsyncTask<String, Void, Void>{
	@Override
	protected Void doInBackground(String... dnss){
	    String err = DNSManager.writeResolvConf(dnss[0], dnss[1]);
	    Intent i = new Intent(ACTION_CONFOPERATION);
		if(err != null){
		i.putExtra("err", err);
	    }
	    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);

	    return null;
	}
    }

    private class deleteConfAsync extends AsyncTask<Void, Void, Void>{
	@Override
	protected Void doInBackground(Void[] p1){
	    String err = DNSManager.removeResolvConf();
	    Intent i = new Intent(ACTION_CONFOPERATION);
	    if(err != null){
		i.putExtra("err", err);
	    }
	    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
		    return null;
	}
    }

    private BroadcastReceiver gettedConf = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
            if(i.getAction().equals(ACTION_CONF_GETTED)){
                confDNS.setText(i.getStringExtra("confDNS"));
            }
	    }
	};

    private class getConfAsync extends AsyncTask<Void, Void, Void>{
	@Override
	protected Void doInBackground(Void[] p1){
	    Intent i = new Intent(ACTION_CONF_GETTED);
	    i.putExtra("confDNS", DNSManager.getResolvConf());
	    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
	    return null;
	}
    }

}
