package io.github.otakuchiyan.dnsman;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class DNSManager {
    final static String SETDNS_PREFIX = "setprop net.dns";
	final static String GETDNS_PREFIX = "getprop net.dns";
    final static String RULES_PREFIX = "iptables -t nat ";
    final static String RULES_SUFFIX = " --dport 53 -j DNAT --to-destination ";
    final static String CHKRULES_PREFIX = "iptables -t nat -L OUTPUT | grep ";

    static String hijackedLastDNS = "";
	static String hijackedLastPort = "";

	final static String[] chk_cmds = {
		GETDNS_PREFIX + "1",
		GETDNS_PREFIX + "2"
	};
	
	public static boolean setDNSViaSetprop(String dns1, String dns2) {
		if(dns1.equals("")){
			dns1 = "";
		}
		if(dns2.equals("")){
			dns2 = "";
		}
		String[] set_cmds = {
			SETDNS_PREFIX + "1 \"" + dns1 + "\"",
			SETDNS_PREFIX + "2 \"" + dns2 + "\""
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

	public static boolean setDNSViaIPtables(String dns, String port){
        if(isRulesAlivable() && hijackedLastDNS.equals(dns)){
            return true;
        }

		String usedPort = !port.equals("") ? port : "53";

        if(!hijackedLastDNS.equals(dns)) {
            hijackedLastDNS = dns;
			hijackedLastPort = usedPort;
            deleteRules();
        }

        List<String> cmds = new ArrayList<String>();
        List<String> result;
        cmds.add(RULES_PREFIX + "-A OUTPUT -p udp" + RULES_SUFFIX + dns + ":" + usedPort);
        cmds.add(RULES_PREFIX + "-A OUTPUT -p tcp" + RULES_SUFFIX + dns + ":" + usedPort);

        result = Shell.SU.run(cmds);
        return result.isEmpty();
    }

    private static List<String> deleteRules(){
        List<String> cmds = new ArrayList<String>();
        cmds.add(RULES_PREFIX + "-D OUTPUT -p udp" + RULES_SUFFIX + hijackedLastDNS + ":" + hijackedLastPort);
        cmds.add(RULES_PREFIX + "-D OUTPUT -p tcp" + RULES_SUFFIX + hijackedLastDNS + ":" + hijackedLastPort);
        return Shell.SU.run(cmds);
    }

    private static boolean isRulesAlivable(){
        List<String> cmds = new ArrayList<String>();
		if(!hijackedLastDNS.equals("")) {
			cmds.add(CHKRULES_PREFIX + hijackedLastDNS + ":" + hijackedLastPort);
			return !Shell.SU.run(cmds).isEmpty();
		}
        return false;
    }
	
	public static String writeResolvConf(String dns1, String dns2){
        List<String> cmds = new ArrayList<String>();
		List<String> result;
		
		cmds.add("mount -o remount,rw /system");
		if(!dns1.equals("")){
		    cmds.add("echo nameserver " + dns1 + " > /etc/resolv.conf");
		}
		if(!dns2.equals("")){
			cmds.add("echo nameserver " + dns2 + " >> /etc/resolv.conf");
		}
        cmds.add("chmod 644 /etc/resolv.conf");
        cmds.add("mount -o remount,ro /system");
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

    public static String getResolvConf(){
	StringBuilder sb = new StringBuilder();
	for(String s : Shell.SH.run("cat /etc/resolv.conf | grep 'nameserver'")){
	    sb.append(s.replace("nameserver", ""));
	    sb.append("\n");
	}
	return sb.toString();
    }
    
}
