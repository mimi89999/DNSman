package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

public class DNSEntryActivity extends Activity {
    private String dns1Key;
    private String dns2Key;
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;
    private DNSEditText dns1;
    private DNSEditText dns2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnsentry);

        ActionBar actionBar = getActionBar();
        Intent i = getIntent();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sped = sp.edit();
        String current_mode = sp.getString("mode", "PROP");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(i.getStringExtra("label"));

        dns1 = (DNSEditText) findViewById(R.id.dns1);
        dns2 = (DNSEditText) findViewById(R.id.dns2);

        dns1Key = i.getStringExtra("dns1key");
        dns2Key = i.getStringExtra("dns2key");

        dns1.setKeyAndText(dns1Key);
        dns2.setKeyAndText(dns2Key);
        dns1.setIPChecker();
        dns2.setIPChecker();

        if(current_mode.equals("IPTABLES")){
            dns2.setFirewallMode();
        }
    }

    public void onClickApplyButton(View v){
        DNSBackgroundService.setByString(this, sp.getString(dns1Key, ""), sp.getString(dns2Key, ""));
    }

    public void onClickClearButton(View v){
        dns1.setText("");
        dns2.setText("");
        sped.putString(dns1Key, "");
        sped.putString(dns2Key, "");
        sped.apply();
        Toast.makeText(this, R.string.toast_clear, Toast.LENGTH_SHORT).show();
    }
}
