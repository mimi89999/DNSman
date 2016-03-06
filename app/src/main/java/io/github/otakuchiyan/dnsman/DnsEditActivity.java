package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class DnsEditActivity extends Activity {
    private DnsStorage dnsStorage;
    private DnsEditText dns1;
    private DnsEditText dns2;
    private String mPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_dns_edit);

        dnsStorage = new DnsStorage(this);

        Intent i = getIntent();
        mPrefix = i.getStringExtra("prefix");
        setTitle(i.getStringExtra("label"));
        setEditText(mPrefix);
    }

    private void setEditText(String prefix){
        dns1 = (DnsEditText) findViewById(R.id.dnsEditText1);
        dns2 = (DnsEditText) findViewById(R.id.dnsEditText2);

        String[] dnsEntry = dnsStorage.getDnsByKeyPrefix(prefix);

        dns1.setText(dnsEntry[0]);
        dns2.setText(dnsEntry[1]);
    }

    public void onOkButtonClick(View v){
        String[] dnsEntry = new String[2];
        dnsEntry[0] = dns1.getText().toString();
        dnsEntry[1] = dns2.getText().toString();
        dnsStorage.putDnsByKeyPrefix(mPrefix, dnsEntry);
        finish();
    }

    public void onClearButtonClick(View v){
        String[] dnsEntry = new String[2];
        dnsEntry[0] = "";
        dnsEntry[1] = "";
        dns1.setText("");
        dns2.setText("");
        dnsStorage.putDnsByKeyPrefix(mPrefix, dnsEntry);
        Toast.makeText(this, R.string.toast_dns_cleared, Toast.LENGTH_SHORT).show();
    }

    public void onCancelButtonClick(View v){
        finish();
    }
}
