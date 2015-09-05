package io.github.otakuchiyan.dnsman;

import android.widget.EditText;
import android.view.View.*;
import android.content.SharedPreferences;
import android.view.View;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class IPCheckerOnFocusChangeListener implements View.OnFocusChangeListener
{
	private SharedPreferences dnssp;
	private SharedPreferences.Editor dnssped;
	
	EditText e;
	String key;
	Context c;
		
	public IPCheckerOnFocusChangeListener(Context c, EditText e, String key){
		this.c = c;
		this.e = e;
		this.key = key;
		dnssp = c.getSharedPreferences("dnsconf", Context.MODE_PRIVATE);
		dnssped = dnssp.edit();
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus){
		String s = this.e.getText().toString();
		if(!hasFocus){
			if(!s.equals("") && IPChecker.IPv4Checker(s)){
				dnssped.putString(this.key, s);
				dnssped.commit();
			}else{
				showInvaildDNSDialog();
			}
		}
	}
	
	private void showInvaildDNSDialog(){
		AlertDialog.Builder adb = new AlertDialog.Builder(c);
		adb.setTitle(R.string.badip)
			.setMessage(R.string.badip_msg)
			.setPositiveButton(android.R.string.ok, null);
		adb.create().show();
	}
}
