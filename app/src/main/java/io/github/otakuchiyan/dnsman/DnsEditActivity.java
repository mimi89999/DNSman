package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DnsEditActivity extends Activity {
    private DnsStorage dnsStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_dns_edit);

        dnsStorage = new DnsStorage(this);

        Intent i = getIntent();
        setTitle(i.getStringExtra("label"));
        setEditText(i.getStringExtra("prefix"));
    }

    private void setEditText(String prefix){
        DnsEditText dns1 = (DnsEditText) findViewById(R.id.dnsEditText1);
        DnsEditText dns2 = (DnsEditText) findViewById(R.id.dnsEditText2);

        String[] dnsEntry = dnsStorage.getDnsByKeyPrefix(prefix);

        dns1.setText(dnsEntry[0]);
        dns2.setText(dnsEntry[1]);
    }
}
