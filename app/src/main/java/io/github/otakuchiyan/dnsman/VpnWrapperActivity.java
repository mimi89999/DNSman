package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.VpnService;
import android.os.Bundle;

public class VpnWrapperActivity extends Activity implements ValueConstants{
    private String dns1, dns2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent dnsData = getIntent();
        dns1 = dnsData.getStringExtra(ValueConstants.EXTRA_DNS1);
        dns2 = dnsData.getStringExtra(ValueConstants.EXTRA_DNS2);

        Intent i = VpnService.prepare(this);
        if (i != null) {
            startActivityForResult(i, ValueConstants.REQUEST_VPN);
        } else {
            DnsVpnService.perform(this, dns1, dns2);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if(reqCode == ValueConstants.REQUEST_VPN || resCode == RESULT_OK){
            DnsVpnService.perform(this, dns1, dns2);
        }
    }
}
