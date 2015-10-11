/*
 - Author: otakuchiyan
 - License: GNU GPLv3
 - Description: The impletion and interface class
 */

package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.app.IntentService;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class DNSManager {
    final static String ACTION_SETDNS_DONE = "io.github.otakuchiyan.dnsman.SETDNS_DONE";
    final static String ACTION_NODNS = "io.github.otakuchiyan.dnsman.NODNS";

    final static String SETPROP_COMMAND_PREFIX = "setprop net.dns";
	final static String GETPROP_COMMAND_PREFIX = "getprop net.dns";
    final static String SETRULE_COMMAND_PREFIX = "iptables -t nat ";
    final static String SETRULE_COMMAND_SUFFIX = " --dport 53 -j DNAT --to-destination ";
    final static String CHECKRULE_COMMAND_PREFIX = "iptables -t nat -L OUTPUT | grep ";

	final static String[] CHECKPROP_COMMANDS = {
        GETPROP_COMMAND_PREFIX + "1",
        GETPROP_COMMAND_PREFIX + "2"
    };

    private static String hijackedLastDNS = "";
	private static String hijackedLastPort = "";
	private static SharedPreferences sp;
	private static SharedPreferences.Editor sped;
	private static Context context;
	private static List<String> dnsList2set = new ArrayList<String>();
    private List<String> commandsResult = new ArrayList<String>();

    private static String mode;
    private static String dns1;
    private static String dns2;
    private static String port;
    private static boolean checkProp;


	private static boolean checkNetType(NetworkInfo ni){
        if(ni != null && ni.isConnected()){
			return true;
		} else {
			return false;
		}
	}

	public static boolean getDNS(){
        GetNetwork gn = new GetNetwork(context);
        NetworkInfo mobi_res = gn.mobileNetInfo;
        NetworkInfo wifi_res = gn.wifiNetInfo;
        NetworkInfo bt_res = gn.bluetoothNetInfo;
        NetworkInfo eth_res = gn.etherNetInfo;
        NetworkInfo wimax_res = gn.wimaxNetInfo;

		getDNSByPrefix("g");
		if (checkNetType(mobi_res)) {
			getDNSByPrefix("m");
        } else if (checkNetType(wifi_res)) {
            getDNSByPrefix("w");
        } else if (checkNetType(bt_res)) {
            getDNSByPrefix("b");
        } else if (checkNetType(eth_res)) {
            getDNSByPrefix("e");
        } else if (checkNetType(wimax_res)) {
            getDNSByPrefix("wi");
        }


        if (dnsList2set.isEmpty()) {
            return false;
        }
        Log.d("DNSManager[DATA]", "dnsList2set " + dnsList2set.get(0));
        Log.d("DNSManager[DATA]", "dnsList2set " + dnsList2set.get(1));

		return true;
	}

	public static void setDNS(Context c){
		context = c;
        sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        getDNS();

        dns1 = dnsList2set.get(0);
        mode = sp.getString("mode", "0");
        checkProp = sp.getBoolean("checkProp", true);
        switch(mode){
            case "0":
                dns2 = dnsList2set.get(1);
            case "1":
                port = dnsList2set.get(1);
                break;
        }

        Intent i = new Intent(context, RunCommandService.class);
        context.startService(i);
	}

	private static void getDNSByPrefix(final String net_prefix){
		List<String> l = new ArrayList<String>();
		String dns2suffix = "dns2";
		String dns1 = sp.getString(net_prefix + "dns1", "");

		if(sp.getString("mode", "1").equals("1")) {
			dns2suffix = "port";
		}
		String dns2 = sp.getString(net_prefix + dns2suffix, "");
		if(!dns1.equals("")|| !dns2.equals("")){
			l.clear();
			l.add(dns1);
			l.add(dns2);
		}

		if(!l.isEmpty()){
			dnsList2set = l;
		}

	}

	public static boolean setDNSViaSetprop(String dns1, String dns2, boolean checkProp) {
		String[] setCommands = {
            SETPROP_COMMAND_PREFIX + "1 \"" + dns1 + "\"",
            SETPROP_COMMAND_PREFIX + "2 \"" + dns2 + "\""
        };

        Log.d("DNSManager[CMD]", setCommands[0]);
        Log.d("DNSManager[CMD]", setCommands[1]);

        if(Shell.SU.available()){
            Shell.SU.run(setCommands);
        }else{
            Shell.SH.run(setCommands);
        }
    
        if(checkProp){
            List<String> result = Shell.SH.run(CHECKPROP_COMMANDS);

            //Check effect
            if(!result.get(0).equals(dns1) ||
                !result.get(1).equals(dns2)){
                return false;
            }
        }
		
        return true;
    }

	public static boolean setDNSViaIPtables(String dns, String port){
        if(isRulesAlivable(context)){
            if(hijackedLastDNS.equals(dns)){
                return true;
            }
        }

        if(!hijackedLastDNS.equals(dns)) {
            deleteRules();
			hijackedLastDNS = dns;
        }

        List<String> cmds = new ArrayList<String>();
        List<String> result;
        String cmd1 = SETRULE_COMMAND_PREFIX + "-A OUTPUT -p udp" + SETRULE_COMMAND_SUFFIX + dns;
        String cmd2 = SETRULE_COMMAND_PREFIX + "-A OUTPUT -p tcp" + SETRULE_COMMAND_SUFFIX + dns;

        if(!port.equals("")){
            cmd1 += ":" + port;
            cmd2 += ":" + port;
        }


        Log.d("DNSManager[CMD]", cmds.get(0));
        Log.d("DNSManager[CMD]", cmds.get(1));

        result = Shell.SU.run(cmds);
        return result.isEmpty();
    }

    private static List<String> deleteRules(){
        List<String> cmds = new ArrayList<String>();

		if(!hijackedLastDNS.equals("")) {
			cmds.add(SETRULE_COMMAND_PREFIX + "-D OUTPUT -p udp" + SETRULE_COMMAND_SUFFIX + hijackedLastDNS + ":" + hijackedLastPort);
			cmds.add(SETRULE_COMMAND_PREFIX + "-D OUTPUT -p tcp" + SETRULE_COMMAND_SUFFIX + hijackedLastDNS + ":" + hijackedLastPort);
			Log.d("DNSManager[CMD]", cmds.get(0));
			Log.d("DNSManager[CMD]", cmds.get(1));
		}
        return Shell.SU.run(cmds);
    }

    private static boolean isRulesAlivable(Context context){
        List<String> cmds = new ArrayList<String>();
		if(!hijackedLastDNS.equals("")) {
			cmds.add(CHECKRULE_COMMAND_PREFIX + hijackedLastDNS + ":" + hijackedLastPort);
            Log.d("DNSManager[CMD]", cmds.get(0));
		}
        return !Shell.SU.run(cmds).isEmpty();
    }
	
	public static List<String> writeResolvConf(String dns1, String dns2, String path){
        List<String> cmds = new ArrayList<String>();
		List<String> result;
        boolean isSystem = true;
		
        if(!path.substring(0, 7).equals("/system") ||
            !path.substring(0, 4).equals("/etc")){
            isSystem = false;
        }
        if(isSystem){
            cmds.add("mount -o remount,rw /system");
        }
		if(!dns1.equals("")){
		    cmds.add("echo nameserver " + dns1 + " > " + path);
		}
		if(!dns2.equals("")){
			cmds.add("echo nameserver " + dns2 + " >> " + path);
		}
        cmds.add("chmod 644 " + path);
        if(isSystem){
            cmds.add("mount -o remount,ro /system");
        }
		return Shell.SU.run(cmds);
	}
	
	public static List<String> removeResolvConf(String path){
        List<String> cmds = new ArrayList<String>();
        boolean isSystem = true;

        if(!path.substring(0, 7).equals("/system") ||
            !path.substring(0, 4).equals("/etc")){
            isSystem = false;
        }

        if(isSystem){
            cmds.add("mount -o remount,rw /system");
        }

		cmds.add("rm " + path);

        if(isSystem){
			cmds.add("mount -o remount,ro /system");
        }

		return Shell.SU.run(cmds);
	}
	
	public static List<String> getCurrentDNS(){
		return Shell.SH.run(CHECKPROP_COMMANDS);
	}

    public static String getResolvConf(String path){
        StringBuilder sb = new StringBuilder();
        for(String s : Shell.SH.run("cat " + path + " | grep 'nameserver'")){
            sb.append(s.replace("nameserver", ""));
            sb.append("\n");
        }
        return sb.toString();
    }

    private class RunCommandService extends IntentService{
        public RunCommandService(){
            super("RunCommandService");
        }

        @Override
        protected void onHandleIntent(Intent i){
            Log.d("DNSManager", "onHandleIntent");
			boolean result = false;
            switch(mode){
                case "0":
                    result = setDNSViaSetprop(dns1, dns2, checkProp);
                    break;
                case "1":
                    result = setDNSViaIPtables(dns1, port);
                    break;
            }
        }
    }
    
}
