package io.github.otakuchiyan.dnsman;

import android.widget.EditText;
import android.view.View.*;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.Selection;

public class IPCheckerComponent implements TextWatcher
{
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;

	private EditText e;
	private String key;
	private Context c;
	private boolean isPort;

	public IPCheckerComponent(Context c, EditText e, String key, boolean isPort){
		this.c = c;
		this.e = e;
		this.key = key;
		this.isPort = isPort;
		sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		sped = sp.edit();
	}

	private void putStringWithoutBlank(String rawString){
		String s = rawString.replace(" ", "");
		sped.putString(this.key, s);
		sped.apply();
	}
	
	@Override
	public void afterTextChanged(Editable eable){
		String s = this.e.getText().toString();
		if(s.equals("")){
			putStringWithoutBlank(s);
		}else if(!s.equals("")){
			if(!isPort) {
				if (IPChecker.isIPv4(s) && IPChecker.IPv4Checker(s)) {
					putStringWithoutBlank(s);
				} else if (IPChecker.IPv6Checker(s)) {
					putStringWithoutBlank(s);
				} else {
					this.e.setError(c.getText(R.string.invalid_dns));
				}
			}
			putStringWithoutBlank(s);
		}else{
			this.e.setError(c.getText(R.string.invalid_dns));
		}
	}
	
	@Override
	public void onTextChanged(CharSequence cs, int start, int end, int count){
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}
	
}
