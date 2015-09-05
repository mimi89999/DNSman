package io.github.otakuchiyan.dnsman;

import android.widget.EditText;
import android.view.View.*;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

public class IPCheckerOnFocusChangeListener implements View.OnFocusChangeListener
{
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
	
	EditText e;
	String key;
	Context c;
		
	public IPCheckerOnFocusChangeListener(Context c, EditText e, String key){
		this.c = c;
		this.e = e;
		this.key = key;
		sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		sped = sp.edit();
	}
	
	@Override
	public void onFocusChange(View v, boolean hasFocus){
		String s = this.e.getText().toString();
		if(!hasFocus){
			if(!s.equals("")){
				if(IPChecker.IPv4Checker(s)){
					sped.putString(this.key, s);
					sped.commit();
				}else{
					showInvaildDNSDialog();
				}
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
