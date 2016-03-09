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
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    
    // TODO: Rename parameters
    private static final String EXTRA_DNS1 = "extra.DNS1";
    private static final String EXTRA_DNS2 = "extra.DNS2";

    public ExecuteIntentService() {
        super("ExecuteIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
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
            switch(method){
                case METHOD_VPN:
                    final String dns1 = intent.getStringExtra(EXTRA_DNS1);
                    final String dns2 = intent.getStringExtra(EXTRA_DNS2);
                    handleActionVpn(dns1, dns2);
                    break;
            }
        }
    }

    private void handleActionVpn(String dns1, String dns2) {
    }
}
