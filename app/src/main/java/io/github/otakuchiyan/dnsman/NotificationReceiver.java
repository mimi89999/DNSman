package io.github.otakuchiyan.dnsman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver implements ValueConstants{
    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int result_code = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        String dns1 = intent.getStringExtra(EXTRA_DNS1);
        String dns2 = intent.getStringExtra(EXTRA_DNS2);

        String dnsToast = preferences.getString("toast", "0");
        //Succeed
        if(result_code <= 1000){
            //Toast
            if (dnsToast.equals("0")) {
                showToastByCodeWithDns(context, result_code, dns1, dns2);
            }
        } else {
            //Not never show
            if (!dnsToast.equals("2")) {
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
                toastString += !dns1.equals("") ? "\n DNS:\t" + dns1 : "";
                toastString += !dns2.equals("") ? "\n DNS:\t" + dns2 : "";
                break;
            case ValueConstants.RESTORE_SUCCEED:
                toastString = context.getText(R.string.toast_restored).toString();
                break;
            case ValueConstants.ERROR_NO_DNS:
                toastString = context.getText(R.string.toast_no_dns_to_restore).toString();
                break;
            default:
                toastString = context.getText(R.string.toast_set_failed).toString();
                toastString += "\n" + context.getText(R.string.toast_unknown_error).toString();
        }

        Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show();
    }
}
