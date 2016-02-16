package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

public class VpnWrapperActivity extends Activity {
    private String dns1;
    private String dns2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        dns1 = data.getStringExtra("dns1");
        dns2 = data.getStringExtra("dns2");

        Intent i = VpnService.prepare(this);
        if (i != null) {
            startActivityForResult(i, DNSmanConstants.VPN_REQUEST);
        } else {
            Intent s = new Intent(this, DNSVpnService.class);
            startService(s);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if(reqCode == DNSmanConstants.VPN_REQUEST || resCode == RESULT_OK){
            Intent s = new Intent(this, DNSVpnService.class);
            startService(s);
        }
    }
}
