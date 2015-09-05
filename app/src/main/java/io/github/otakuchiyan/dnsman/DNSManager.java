package io.github.otakuchiyan.dnsman;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
/**
 * Created by otakuchiyan on 2015/5/1.
 */
public class DNSManager {
    String net_dns_prop = "net.dns";
	
	private void runCMD(String[] cmds, boolean use_su){
		Process p = null;
        DataOutputStream dos = null;
        try{
            if(use_su){
                p = Runtime.getRuntime().exec("su");
            }else{
                p = Runtime.getRuntime().exec("sh");
            }

            dos = new DataOutputStream(p.getOutputStream());
            for(int i = 0; i != cmds.length; i++) {
                if(cmds[i].isEmpty()){
                    continue;
                }
                dos.writeBytes(cmds[i] + "\n");
                dos.flush();
            }
            if(use_su){
                dos.writeBytes("exit\n");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (dos != null){
                try {
                    dos.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        
	}

    public int setDNSViaSetprop(String dns1, String dns2, boolean use_su) {
		String[] cmds = {
			"setprop " + net_dns_prop + "1 " + dns1,
			"setprop " + net_dns_prop + "2 " + dns2
		};
		runCMD(cmds, use_su);
        return 0;
    }
	
	public void writeResolvConf(String dns1, String dns2){
		String[] cmds = new String[4];
		
		cmds[0] = "mount -o remount,rw /system";
		if(dns1 != ""){
		cmds[1] = "echo nameserver " + dns1 + " > /etc/resolv.conf";
		}
		if(!dns2.equals("")){
			cmds[2] = "echo nameserver " + dns2 + " >> /etc/resolv.conf";
		}
		cmds[3] = "mount -o remount,ro /system";
		runCMD(cmds, true);
	}
	
	public void removeResolvConf(){
		String[] cmds = {
			"mount -o remount,rw /system",
			"rm /etc/resolv.conf",
			"mount -o remount,ro /system"
		};
		runCMD(cmds, true);
	}
}
