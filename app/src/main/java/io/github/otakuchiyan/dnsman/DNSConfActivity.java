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

import java.lang.Void;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.IPCheckerComponent;
import android.view.View.*;
import android.content.*;
import android.view.View;


public class DNSConfActivity extends Activity{
    final static String ACTION_CONFOPERATION = "io.github.otakuchiyan.dnsman.ACTION_CONFOPERATION";
	private SharedPreferences sp;
	private EditText rdns1;
	private EditText rdns2;

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
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dnsconf_activity);
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		rdns1 = (EditText) findViewById(R.id.rdns1);
		rdns2 = (EditText) findViewById(R.id.rdns2);
	
		rdns1.setText(sp.getString("rdns1", ""));
		rdns2.setText(sp.getString("rdns2", ""));
		
		rdns1.addTextChangedListener(
			new IPCheckerComponent(this, rdns1, "rdns1"));
		rdns2.addTextChangedListener(
			new IPCheckerComponent(this, rdns2, "rdns2"));

		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(ACTION_CONFOPERATION);
		LocalBroadcastManager.getInstance(this).registerReceiver(confOperation, iFilter);
		
		}
	
	public void writeConfBtn(View v){
		final String rdns1ip = rdns1.getText().toString();
		final String rdns2ip = rdns2.getText().toString();
		
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.write_conf_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface di, int which){
				    (new writeConfAsync()).execute(rdns1ip, rdns2ip);
				}
			})
			.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
	
	public void defaultConfBtn(View v){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.default_conf_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface di, int which){
				    (new writeConfAsync()).execute("8.8.8.8", "8.8.4.4");
					}
			})
			.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
	
	public void deleteConfBtn(View v){
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
	    LocalBroadcastManager.getInstance(getApplicationContext())
		.sendBroadcast(i);
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
	    LocalBroadcastManager.getInstance(getApplicationContext())
		.sendBroadcast(i);
		    return null;
	}
    }   


	
}
