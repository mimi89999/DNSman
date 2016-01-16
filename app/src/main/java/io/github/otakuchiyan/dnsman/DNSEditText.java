package io.github.otakuchiyan.dnsman;

import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.text.InputType;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;

import java.util.HashSet;
import java.util.Set;

import io.github.otakuchiyan.dnsman.IPCheckerComponent;

public class DNSEditText extends AutoCompleteTextView{
    private SharedPreferences sp;

    private Context context;
    private String key = "";
    private boolean isPort;

    public DNSEditText(Context c){
        this(c, null);
    }

    public DNSEditText(Context c, AttributeSet attr){
        super(c, attr);

        context = c;
        sp = PreferenceManager.getDefaultSharedPreferences(c);
        setSingleLine(true);
        int input_type = InputType.TYPE_CLASS_NUMBER;
        int max_length = 15;
        if(sp.getBoolean("enable_ipv6", false)) {
            input_type = InputType.TYPE_CLASS_TEXT;
            max_length = 39;
        }
        setRawInputType(input_type);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length)});
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDown();
            }
        });
        Set<String> dnslist = sp.getStringSet("dnslist", new HashSet<String>());
        ArrayAdapter<String> dnsListAdapter = new ArrayAdapter<>(c,
                android.R.layout.simple_dropdown_item_1line,
                dnslist.toArray(new String[dnslist.size()]));
        setAdapter(dnsListAdapter);
    }

    public void setKeyAndText(String key){
        this.key = key;
        setText(sp.getString(key, ""));
    }

    public void setFirewallMode() {
        setHint(context.getText(R.string.port));
        isPort = true;
        setRawInputType(InputType.TYPE_CLASS_NUMBER);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        //Escape completing
        setThreshold(7);
        setOnClickListener(null);
    }
    public void setIPChecker() {
        addTextChangedListener(new IPCheckerComponent(context, this, key, isPort));
    }
/*
    @Override
    public boolean enoughToFilter(){
        return true;
    }
    */
}
