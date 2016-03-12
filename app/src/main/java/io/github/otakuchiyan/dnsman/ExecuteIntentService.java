package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class ExecuteIntentService extends IntentService implements ValueConstants{
    public ExecuteIntentService() {
        super("ExecuteIntentService");
    }

    public static boolean startActionByInfo(Context context, NetworkInfo info) {
        DnsStorage dnsStorage = new DnsStorage(context);

        String extra_dns[] = dnsStorage.getDnsByKeyPrefix(info.getTypeName());

        //Fallback to global
        if(extra_dns[0].equals("") && extra_dns[1].equals("")) {
            extra_dns = dnsStorage.getGlobalDns();
        }

        if(extra_dns[0].equals("") && extra_dns[1].equals("")){
            return false;
        }

        startActionByString(context, extra_dns);
        return true;
    }

    //Array will be transform to two variable at here
    public static void startActionByString(Context c, String[] dnsEntry){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);
        Intent intent = new Intent(c, ExecuteIntentService.class);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DNS1, dnsEntry[0]);
        intent.putExtra(EXTRA_DNS2, dnsEntry[1]);
        c.startService(intent);
    }

//Need completing
    //Always can be used, because delete rules and disconnect vpn needn't default dns
    public static void restore(Context c){
        boolean isNeedDns = true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String method = preferences.getString(KEY_PREF_METHOD, METHOD_VPN);

        switch (method) {
            case METHOD_IPTABLES:
                method = METHOD_DELETE_RULES;
                isNeedDns = false;
                break;
            case METHOD_VPN:
                int code = new DnsVpnService().disconnect();
                sendResult(c, code);
                return;
        }


        String dns1 = "";
        String dns2 = "";

        if(isNeedDns) {
            dns1 = preferences.getString(KEY_NETWORK_DNS1, "");
            dns2 = preferences.getString(KEY_NETWORK_DNS2, "");

            if(dns1.equals("") && dns2.equals("")) {
                sendResult(c, ValueConstants.ERROR_NO_DNS);
                return;
            }
        }

        Intent intent = new Intent(c, ExecuteIntentService.class);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DNS1, dns1);
        intent.putExtra(EXTRA_DNS2, dns2);
        c.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();

            int resultCode = 0;

            final String method = intent.getStringExtra(EXTRA_METHOD);
            final String dns1 = intent.getStringExtra(EXTRA_DNS1);
            final String dns2 = intent.getStringExtra(EXTRA_DNS2);
            //4 firewall rules mode
            final String lastHijackedDns = preferences.getString(KEY_HIJACKED_LAST_DNS, "");

            switch(method){
                case METHOD_VPN:
                    Intent i = new Intent(this, VpnWrapperActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(EXTRA_DNS1, dns1);
                    i.putExtra(EXTRA_DNS2, dns2);
                    startActivity(i);
                    break;
                case METHOD_ACCESSIBILITY:
                    break;
                case METHOD_NDC:
                    resultCode = NativeCommandUtils.setDnsViaNdc(context, dns1, dns2);
                    break;
                case METHOD_IPTABLES:
                    if(NativeCommandUtils.isRulesAlivable(dns1)) {
                        return;
                    }else if (!dns1.equals(lastHijackedDns) && //IP was changed
                        NativeCommandUtils.isRulesAlivable(lastHijackedDns)){
                        NativeCommandUtils.deleteRules(lastHijackedDns);
                    }
                    editor.putString(KEY_HIJACKED_LAST_DNS, dns1);
                    resultCode = NativeCommandUtils.setDnsViaIptables(dns1);
                    break;
                case METHOD_DELETE_RULES:
                    NativeCommandUtils.deleteRules(lastHijackedDns);
                    editor.putString(KEY_HIJACKED_LAST_DNS, "");
                    break;
                case METHOD_MODULE:
                    break;
                case METHOD_SETPROP:
                    resultCode = NativeCommandUtils.setDnsViaSetprop(dns1, dns2);
                    break;
            }
            editor.apply();


            if(resultCode <= 1000 && intent.getBooleanExtra(KEY_IS_RESTORE, false)) {
                resultCode = ValueConstants.RESTORE_SUCCEED;
            }

            if(resultCode <= 1000 && preferences.getBoolean(KEY_PREF_AUTO_FLUSH, true)) {
                
            }

            sendResultWithDns(context, resultCode, dns1, dns2);
        }
    }

    private static void sendResult(Context c, int result_code){
        sendResultWithDns(c, result_code, "", "");
    }

    private static void sendResultWithDns(Context c, int result_code, String dns1, String dns2){
        Intent result_intent = new Intent(ACTION_SET_DNS);
        result_intent.putExtra(EXTRA_RESULT_CODE, result_code);
        result_intent.putExtra(EXTRA_DNS1, dns1);
        result_intent.putExtra(EXTRA_DNS1, dns2);
        c.sendBroadcast(result_intent);
    }
}
