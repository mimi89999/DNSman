package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.HashSet;
import java.util.Set;

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
        int input_type;
        int max_length;
        if(sp.getBoolean("enable_full_keyboard", false)) {
            input_type = InputType.TYPE_CLASS_TEXT;
            max_length = 39;
        }else{
            input_type = InputType.TYPE_CLASS_NUMBER;
            max_length = 15;
        }
        setRawInputType(input_type);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(max_length)});
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDropDown();
                }
            }
        });
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDown();
            }
        });

        setCompletingList();
    }

    private void setCompletingList(){
        Set<String> dnslist = sp.getStringSet("dnslist", new HashSet<String>());
        ArrayAdapter<String> dnsListAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line,
                dnslist.toArray(new String[dnslist.size()]));
        setAdapter(dnsListAdapter);
    }

    public void setKeyAndText(String key){
        this.key = key;
        setText(sp.getString(key, ""));
    }


    //If the EditText is used to input port, must invoke before setIPChecker
    public void setFirewallMode() {
        setHint(context.getText(R.string.port));
        isPort = true;
        setRawInputType(InputType.TYPE_CLASS_NUMBER);
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        //Escape completing
        setThreshold(7);
        setOnFocusChangeListener(null);
    }
    public void setIPChecker() {
        addTextChangedListener(new IPCheckerComponent(context, this, key, isPort));
    }

}
