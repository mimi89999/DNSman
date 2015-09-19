package io.github.otakuchiyan.dnsman;

import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.text.InputType;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;

import io.github.otakuchiyan.dnsman.IPCheckerComponent;

public class DNSEditText extends EditText{
    private SharedPreferences sp;

    	private LinearLayout.LayoutParams edittext_params = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.WRAP_CONTENT,
            1.0f);

    public DNSEditText(Context c, String key){
	super(c);
	sp = PreferenceManager.getDefaultSharedPreferences(c);
        setSingleLine(true);
        setText(sp.getString(key, ""));
	setRawInputType(InputType.TYPE_CLASS_NUMBER);
        setLayoutParams(edittext_params);
	setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });
        addTextChangedListener(
			       new IPCheckerComponent(c, this, key));

    }
}
