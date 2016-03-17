package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ResultCodeReceiver extends BroadcastReceiver implements ValueConstants{
    public ResultCodeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int result_code = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        String dns1 = intent.getStringExtra(EXTRA_DNS1);
        String dns2 = intent.getStringExtra(EXTRA_DNS2);

        String dnsToast = preferences.getString(KEY_PREF_TOAST, TOAST_SHOW);
        boolean isShowNotify = preferences.getBoolean(KEY_PREF_NOTIFICATION, true);


        //Succeed
        if(result_code <= 1000){
            //Toast
            if (dnsToast.equals(TOAST_SHOW)) {
                showToastByCodeWithDns(context, result_code, dns1, dns2);
            }
            if(isShowNotify) {
                ControlNotification.notify(context, dns1, dns2);
            }
            if(preferences.getString(KEY_PREF_METHOD, METHOD_VPN).equals(METHOD_VPN)){
                new CurrentStatusView(context).setCurrentDns(dns1, dns2);
            }else{
                new CurrentStatusView(context).setCurrentDns();
            }

        } else {
            //Not never show
            if (!dnsToast.equals(TOAST_NEVER)) {
                showToastByCode(context, result_code);
            }

        }
    }

    private void showToastByCode(Context c, int code){
        showToastByCodeWithDns(c, code, "", "");
    }

    private void showToastByCodeWithDns(Context context, int code, String dns1, String dns2){
        String toastString;

        switch (code) {
            case 0:
                toastString = context.getText(R.string.toast_set_succeed).toString();
                toastString += !dns1.equals("") ? "\nDNS: " + dns1 : "";
                toastString += !dns2.equals("") ? "\nDNS: " + dns2 : "";
                break;
            case ValueConstants.RESTORE_SUCCEED:
                toastString = context.getText(R.string.toast_restored).toString();
                break;
            case ValueConstants.ERROR_NO_DNS:
                toastString = context.getText(R.string.toast_no_dns_to_restore).toString();
                break;
            case ERROR_BAD_ADDRESS:
                toastString = context.getString(R.string.toast_bad_address);
                break;
            default:
                toastString = context.getText(R.string.toast_set_failed).toString();
                toastString += "\n" + context.getText(R.string.toast_unknown_error).toString();
        }

        Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show();
    }


}
