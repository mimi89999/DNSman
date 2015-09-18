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

public class IPCheckerComponent implements TextWatcher
{
	private SharedPreferences sp;
	private SharedPreferences.Editor sped;
	
	EditText e;
	String key;
	Context c;
		
	public IPCheckerComponent(Context c, EditText e, String key){
		this.c = c;
		this.e = e;
		this.key = key;
		sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		sped = sp.edit();
	}
	
	@Override
	public void afterTextChanged(Editable eable){
		String s = this.e.getText().toString();
				if(s.equals("")){
					sped.putString(this.key, s);
					sped.commit();
				}else if(!s.equals("") && IPChecker.IPv4Checker(s)){
					sped.putString(this.key, s);
					sped.commit();
				}else{
                    this.e.setError(c.getText(R.string.invaild_dns));
				}
		
	}
	
	@Override
	public void onTextChanged(CharSequence cs, int start, int end, int count){
		
	}

	@Override
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		// TODO: Implement this method
	}
	
}
