package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class InterfacePreference extends EditTextPreference {
    private class AutoCompleteEditText extends AutoCompleteTextView{
        public AutoCompleteEditText(Context c){
            super(c);
        }
        public AutoCompleteEditText(Context c, AttributeSet attrs){
            super(c, attrs);
        }
        public AutoCompleteEditText(Context c, AttributeSet attrs, int defStyle){
            super(c, attrs, defStyle);
        }
    }
    private static AutoCompleteEditText mEditText = null;
    private Context context;

    final static String[] interface_list = {
            "wlan0",
            "rmnet0",
            "eth0",
            "bt-pan",
            "tlwlan"
    };

    public InterfacePreference(Context context) {
        super(context);
        init(context, null, 0);
    }

    public InterfacePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public InterfacePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context c, AttributeSet attrs, int defStyle) {
        context = c;
        mEditText = new AutoCompleteEditText(c, attrs);
        mEditText.setSingleLine();
        List<String> all_interface = getAlivableInterfaces();

        ArrayAdapter<String> interfaces = new ArrayAdapter<>(c,
                android.R.layout.simple_dropdown_item_1line,
                all_interface.toArray(new String[all_interface.size()]));
        mEditText.setAdapter(interfaces);
    }

    private List<String> getAlivableInterfaces(){
        List<String> system_interfaces = new ArrayList<>();
        Collections.addAll(system_interfaces, interface_list);
        try{
            for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();){
                NetworkInterface i = ifaces.nextElement();
                String interface_name = i.getName();
                if(Arrays.asList(interface_list).contains(interface_name)){
                    system_interfaces.remove(interface_name);
                    system_interfaces.add(0, interface_name);
                }
                system_interfaces.add(interface_name);
            }
            system_interfaces.remove("lo");
        }catch (Exception e){
            e.printStackTrace();
        }

        return system_interfaces;
    }


    @Override
    protected void onBindDialogView(View v){
        final AutoCompleteEditText editText = mEditText;
        editText.setText(getText());
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.showDropDown();
            }
        });

        ViewParent oldParent = editText.getParent();
        if(oldParent != v){
            if(oldParent != null){
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(v, editText);
            editText.showDropDown();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult){
        if(positiveResult){
            String v = mEditText.getText().toString();
            if(callChangeListener(v)){
                setText(v);
                setSummary(v);
            }
        }
    }
}
