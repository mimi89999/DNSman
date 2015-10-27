package io.github.otakuchiyan.dnsman;

import android.util.AttributeSet;
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

    private Context context;
    private String key;
    private boolean isPort = false;


    public DNSEditText(Context c){
        this(c, null);
    }

    public DNSEditText(Context c, AttributeSet attr){
        super(c, attr);
        context = c;
        sp = PreferenceManager.getDefaultSharedPreferences(c);
        setSingleLine(true);

        setRawInputType(InputType.TYPE_CLASS_NUMBER);
        setLayoutParams(edittext_params);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
    }

    public void setKey(String key){
        setText(sp.getString(key, ""));
        this.key = key;
    }

    public void setFirewallMode(){
        setHint(context.getText(R.string.default_port) + " 53");
        isPort = true;
    }

    public void setIPChecker() {
        addTextChangedListener(new IPCheckerComponent(context, this, key, isPort));
    }
}
