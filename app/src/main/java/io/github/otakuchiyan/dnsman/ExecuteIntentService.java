package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ExecuteIntentService extends IntentService implements ValueConstants{
    public ExecuteIntentService() {
        super("ExecuteIntentService");
    }

    public static void startActionByInfo(Context context, NetworkInfo info) {
        DnsStorage dnsStorage = new DnsStorage(context);

        String extra_dns[] = dnsStorage.getDnsByKeyPrefix(info.getTypeName());

        //Fallback to global
        if(extra_dns[0].equals("") && extra_dns[1].equals("")) {
            extra_dns = dnsStorage.getGlobalDns();
        }

        startActionByString(context, extra_dns);
    }

    public static void startActionByString(Context c, String[] dnsEntry){
        Intent intent = new Intent(c, ExecuteIntentService.class);
        intent.putExtra(EXTRA_DNS1, dnsEntry[0]);
        intent.putExtra(EXTRA_DNS2, dnsEntry[1]);
        c.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);

            final String dns1 = intent.getStringExtra(EXTRA_DNS1);
            final String dns2 = intent.getStringExtra(EXTRA_DNS2);

            switch(method){
                case METHOD_VPN:
                    handleActionVpn(dns1, dns2);
                    break;
                case METHOD_ACCESSIBILITY:
                    break;
                case METHOD_NDC:
                    break;
                case METHOD_IPTABLES:
                    break;
                case METHOD_MODULE:
                    break;
                case METHOD_SETPROP:
                    break;
            }
        }
    }

    private void handleActionVpn(String dns1, String dns2) {
        Intent i = new Intent(this, VpnWrapperActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(EXTRA_DNS1, dns1);
        i.putExtra(EXTRA_DNS2, dns2);
        startActivity(i);
    }
}
