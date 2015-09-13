package io.github.otakuchiyan.dnsman;

import android.util.Log;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by otakuchiyan on 2015/5/1.
 */
public class DNSManager {
    final static String SETDNS_PREFIX = "setprop net.dns";
	final static String GETDNS_PREFIX = "getprop net.dns";
	final static String[] chk_cmds = {
		GETDNS_PREFIX + "1",
		GETDNS_PREFIX + "2"
	};
	
	public static boolean setDNSViaSetprop(String dns1, String dns2) {
		String[] set_cmds = {
			SETDNS_PREFIX + "1 " + dns1,
			SETDNS_PREFIX + "2 " + dns2
		};
		
		List<String> result;
		if(Shell.SU.available()){
			Shell.SU.run(set_cmds);
		}else{
			Shell.SH.run(set_cmds);
		}
		
		//Check effect
		result = Shell.SH.run(chk_cmds);
		if(!result.get(0).equals(dns1) ||
			!result.get(1).equals(dns2)){
				return false;
			}
		
        return true;
    }
	
	public static String writeResolvConf(String dns1, String dns2){
		String[] cmds = new String[4];
		List<String> result;
		
		cmds[0] = "mount -o remount,rw /system";
		if(dns1 != ""){
		cmds[1] = "echo nameserver " + dns1 + " > /etc/resolv.conf";
		}
		if(!dns2.equals("")){
			cmds[2] = "echo nameserver " + dns2 + " >> /etc/resolv.conf";
		}
		cmds[3] = "mount -o remount,ro /system";
		result = Shell.SU.run(cmds);
		if(!result.isEmpty()){
		    return result.get(0);
		}
		return null;
	}
	
	public static String removeResolvConf(){
		String[] cmds = {
			"mount -o remount,rw /system",
			"rm /etc/resolv.conf",
			"mount -o remount,ro /system"
		};
		List<String> result;
		result = Shell.SU.run(cmds);
		if(!result.isEmpty()){
		    return result.get(0);
		}
		return null;
	}
	
	public static List<String> getCurrentDNS(){
		return Shell.SH.run(chk_cmds);
	}

    public static boolean detectDNSCrypt(){
	return Shell.SH.run("ps | grep 'dnscrypt'").isEmpty() ? false : true;
    }
}
