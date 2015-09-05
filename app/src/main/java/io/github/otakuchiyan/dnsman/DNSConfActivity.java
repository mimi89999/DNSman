package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.os.Bundle;
import android.widget.EditText;
import android.content.SharedPreferences;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.IPCheckerOnFocusChangeListener;
import android.view.View.*;
import android.content.*;
import android.view.View;



public class DNSConfActivity extends Activity
{
	private SharedPreferences dnssp;
	private DNSManager d = new DNSManager();
	private EditText rdns1;
	private EditText rdns2;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dnsconf_activity);
		dnssp = getSharedPreferences("dnsconf", Context.MODE_PRIVATE);
		
		rdns1 = (EditText) findViewById(R.id.rdns1);
		rdns2 = (EditText) findViewById(R.id.rdns2);
	
		rdns1.setText(dnssp.getString("rdns1", ""));
		rdns2.setText(dnssp.getString("rdns2", ""));
		
		rdns1.setOnFocusChangeListener(
			new IPCheckerOnFocusChangeListener(this, rdns1, "rdns1"));
		rdns2.setOnFocusChangeListener(
			new IPCheckerOnFocusChangeListener(this, rdns2, "rdns2"));
		}
	
	public void writeConfBtn(View v){
		final String rdns1ip = rdns1.getText().toString();
		final String rdns2ip = rdns2.getText().toString();
		
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setMessage(R.string.write_conf_msg)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface di, int which){
					d.writeResolvConf(rdns1ip, rdns2ip);
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
					d.writeResolvConf("8.8.8.8", "8.8.4.4");
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
				d.removeResolvConf();
			}
		})
		.setNegativeButton(android.R.string.cancel, null);
		adb.create().show();
	}
}
