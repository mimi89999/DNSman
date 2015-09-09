package io.github.otakuchiyan.dnsman;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import eu.chainfire.libsuperuser.Shell;
/**
 * Created by otakuchiyan on 2015/5/1.
 */
public class DNSManager {
    final static String NETDNS_PROP = "net.dns";
	
	public static int setDNSViaSetprop(String dns1, String dns2, boolean use_su) {
		String[] cmds = {
			"setprop " + NETDNS_PROP + "1 " + dns1,
			"setprop " + NETDNS_PROP + "2 " + dns2
		};
		//runCMD(cmds, use_su);
		if(use_su){
			Shell.SU.run(cmds);
		}else{
			Shell.SH.run(cmds);
		}
        return 0;
    }
	
	public static void writeResolvConf(String dns1, String dns2){
		String[] cmds = new String[4];
		
		cmds[0] = "mount -o remount,rw /system";
		if(dns1 != ""){
		cmds[1] = "echo nameserver " + dns1 + " > /etc/resolv.conf";
		}
		if(!dns2.equals("")){
			cmds[2] = "echo nameserver " + dns2 + " >> /etc/resolv.conf";
		}
		cmds[3] = "mount -o remount,ro /system";
		Shell.SU.run(cmds);       
	}
	
	public static void removeResolvConf(){
		String[] cmds = {
			"mount -o remount,rw /system",
			"rm /etc/resolv.conf",
			"mount -o remount,ro /system"
		};
		Shell.SU.run(cmds);
	}
}
