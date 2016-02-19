package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import eu.chainfire.libsuperuser.Shell;

public class AirplaneModeUtils {

    public static void toggle(Context context, String netId){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean("rooted", true)) {
            try {
                String flushdns_cmd;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    flushdns_cmd = String.format(DNSmanConstants.FLUSHNET_COMMAND, netId);
                } else { //<=4.4, netId will be ignored
                    flushdns_cmd = DNSmanConstants.FLUSHDEFAULTIF_COMMAND;
                }

                Log.d("AirplaneModeUtils", "cmd = " + flushdns_cmd);
                Shell.SU.run(flushdns_cmd);
            } catch (Exception e) {
                toggleViaCommand();
            }
        } else {
            if (Build.VERSION.SDK_INT >= 17) {
            }else{
                toggleBelowApiLevel17(context);
            }
        }
    }

    private static void toggleViaCommand() {
        List<String> cmds = new ArrayList<>();
        Log.e("AirplaneModeUtils", "failed to flush dns cache via ndc");
        cmds.add("settings put global airplane_mode_on 1");
        cmds.add("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
        cmds.add("settings put global airplane_mode_on 0");
        cmds.add("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
        Shell.SU.run(cmds);
    }

    private static void toggleBelowApiLevel17(Context context) {
        // Android 4.2 below
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 1);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        context.sendBroadcast(intent);
        try {
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", false);
        context.sendBroadcast(intent);
    }
}
