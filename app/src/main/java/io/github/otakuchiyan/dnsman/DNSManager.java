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
    final static String SETPROP_COMMAND_PREFIX = "setprop net.dns";
	final static String GETPROP_COMMAND_PREFIX = "getprop net.dns";
    final static String SETRULE_COMMAND_PREFIX = "iptables -t nat ";
    final static String SETRULE_COMMAND_SUFFIX = " --dport 53 -j DNAT --to-destination ";
    final static String CHECKRULE_COMMAND_PREFIX = "iptables -t nat -L OUTPUT | grep ";

	final static String[] CHECKPROP_COMMANDS = {
        GETPROP_COMMAND_PREFIX + "1",
        GETPROP_COMMAND_PREFIX + "2"
    };

	private static Context context;

    private static String mode;
    private static String dns1;
    private static String dns2;
    private static String port;
    private static boolean checkProp;

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
        List<String> cmds = new ArrayList<String>();
        List<String> result;
        String cmd1 = SETRULE_COMMAND_PREFIX + "-A OUTPUT -p udp" + SETRULE_COMMAND_SUFFIX + dns;
        String cmd2 = SETRULE_COMMAND_PREFIX + "-A OUTPUT -p tcp" + SETRULE_COMMAND_SUFFIX + dns;
        if(!port.equals("")){
            cmd1 += ":" + port;
            cmd2 += ":" + port;
        }

        cmds.add(cmd1);
        cmds.add(cmd2);

        Log.d("DNSManager[CMD]", cmd1);
        Log.d("DNSManager[CMD]", cmd2);

        return Shell.SU.run(cmds).isEmpty();
    }
	public static boolean isRulesAlivable(String dns, String port){
		String cmd = CHECKRULE_COMMAND_PREFIX + dns;

		if(!port.equals("")){
			cmd += ":" + port;
		}

		Log.d("DNSManager[CMD]", cmd);
		return !Shell.SU.run(cmd).isEmpty();
	}

    public static List<String> deleteRules(String dns, String port){
        List<String> cmds = new ArrayList<String>();
        String cmd1 = SETRULE_COMMAND_PREFIX + "-D OUTPUT -p udp" + SETRULE_COMMAND_SUFFIX + dns;
        String cmd2 = SETRULE_COMMAND_PREFIX + "-D OUTPUT -p tcp" + SETRULE_COMMAND_SUFFIX + dns;
        if(!port.equals("")){
            cmd1 += ":" + port;
            cmd2 += ":" + port;
        }

        cmds.add(cmd1);
        cmds.add(cmd2);

        Log.d("DNSManager[CMD]", cmd1);
        Log.d("DNSManager[CMD]", cmd2);
        return Shell.SU.run(cmds);
    }
	
	public static String writeResolvConfig(String dns1, String dns2, String path){
        List<String> cmds = new ArrayList<String>();
        boolean isSystem = false;
		
        if(path.substring(0, 7).equals("/system") ||
            path.substring(0, 4).equals("/etc")){
            isSystem = true;
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

        StringBuilder sb = new StringBuilder();
		for(String s : Shell.SU.run(cmds)){
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
	}
	
	public static String removeResolvConfig(String path){
        List<String> cmds = new ArrayList<String>();
        boolean isSystem = false;

        if(path.substring(0, 7).equals("/system") ||
            path.substring(0, 4).equals("/etc")){
            isSystem = true;
        }

        if(isSystem){
            cmds.add("mount -o remount,rw /system");
        }

		cmds.add("rm " + path);

        if(isSystem){
			cmds.add("mount -o remount,ro /system");
        }

        StringBuilder sb = new StringBuilder();
		for(String s : Shell.SU.run(cmds)){
            sb.append(s);
            sb.append("\n");
        }
        return sb.toString();
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

    
}
