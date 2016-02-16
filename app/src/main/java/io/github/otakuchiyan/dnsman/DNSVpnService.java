package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

public class DNSVpnService extends VpnService {
    private ParcelFileDescriptor fd;
    private Builder vpn = new Builder();
    private static String vdns1;
    private static String vdns2;

    public DNSVpnService() {
    }

    public static void perform(Context c, String dns1, String dns2){
        Intent i = new Intent(c, DNSVpnService.class);
        vdns1 = dns1;
        vdns2 = dns2;
        c.startService(i);
    }

    private String getAddress(){
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                 ifaces.hasMoreElements(); ) {
                Enumeration<InetAddress> addresses = ifaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String addr = addresses.nextElement().getHostAddress();
                    if (!addr.equals("127.0.0.1") &&
                            !addr.equals("0.0.0.0") &&
                            !addr.equals("::1%1")) {
                        return addr;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                String addr = getAddress();
                String real_addr = "";

                //Escaping IPv6 address suffix - "<Real address>%wlan0"
                for(int i = addr.length() - 1; i != 0; i--){
                    if(addr.charAt(i) == '%'){
                        real_addr = addr.substring(0, i);
                    }
                }

                //If no suffix
                if(real_addr.equals("")){
                    real_addr = addr;
                }

                Log.d("DNSVpn", "addr = " + real_addr);
                DatagramChannel tunnel = DatagramChannel.open();

                if(!protect(tunnel.socket())) {
                    throw new IllegalStateException("Cannot protect the tunnel");
                }
                tunnel.connect(new InetSocketAddress(addr, 8087));
                tunnel.configureBlocking(false);

                vpn.setSession("DNSVpnService")
                        .addAddress(real_addr, 24);
                if(!vdns1.equals("")) {
                    vpn.addDnsServer(vdns1);
                }
                if(!vdns2.equals("")) {
                    vpn.addDnsServer(vdns2);
                }
                fd = vpn.establish();

                while(true){
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fd.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            }
        });

        thread.start();
        return START_STICKY;
    }
}
