package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

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

    public DNSVpnService() {
    }

    public static void perform(Context c, String dns1, String dns2){
        Intent i = new Intent(c, DNSVpnService.class);
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
        /*
        try {

        } catch (Exception e){

        }*/


        /*n
        final FileInputStream in = new FileInputStream(fd.getFileDescriptor());
        final FileInputStream out = new FileInputStream(fd.getFileDescriptor());
        final ByteBuffer packet = ByteBuffer.allocate(3200);
*/
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        String addr = getAddress();
                        DatagramChannel tunnel = DatagramChannel.open();

                        if(!protect(tunnel.socket())) {
                            throw new IllegalStateException("Cannot protect the tunnel");
                        }
                        tunnel.connect(new InetSocketAddress(addr, 8087));
                        tunnel.configureBlocking(false);

                        fd = vpn.setSession("DNSVpnService")
                                .addAddress(addr, 24)
                                .addDnsServer("8.8.8.8")
                               // .addRoute("0.0.0.0", 0)
                                .establish();

                        //int n = in.read(packet.array());
                        while(true){
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fd.close();
                        } catch (Exception e) {

                        }
                    }
            }
        });

        thread.start();
        return START_STICKY;
    }
}
