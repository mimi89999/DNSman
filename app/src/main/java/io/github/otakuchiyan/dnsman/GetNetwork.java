package io.github.otakuchiyan.dnsman;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Build;

public class GetNetwork
{
    private ConnectivityManager cm;

    public NetworkInfo wifiNetInfo;
    public NetworkInfo mobileNetInfo;
    public NetworkInfo bluetoothNetInfo;
    public NetworkInfo etherNetInfo;
    public NetworkInfo wimaxNetInfo;
    public String wifiName;
    public String mobileName;
    public String bluetoothName;
    public String etherName;
    public String wimaxName;
    public boolean isSupportWifi;
    public boolean isSupportMobile;
    public boolean isSupportBluetooth;
    public boolean isSupportEthernet;
    public boolean isSupportWimax;

    public GetNetwork(Context c){
		cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        wifiNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobileNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		bluetoothNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
		etherNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        wimaxNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        isSupportWifi = wifiNetInfo != null;
        isSupportMobile = mobileNetInfo != null;
        isSupportBluetooth = bluetoothNetInfo != null;
        isSupportEthernet = etherNetInfo != null;
        isSupportWimax = wimaxNetInfo != null;

        wifiName = wifiNetInfo != null ? wifiNetInfo.getTypeName() : null;
        mobileName = mobileNetInfo != null ? mobileNetInfo.getTypeName() : null;
        bluetoothName = bluetoothNetInfo != null ? bluetoothNetInfo.getTypeName() : null;
        etherName = etherNetInfo != null ? etherNetInfo.getTypeName() : null;
        wimaxName = wimaxNetInfo != null ? wimaxNetInfo.getTypeName() : null;
    }

    public String getNetId(){
        String netId = "";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //>=5.0
            try {
                Network[] ns = cm.getAllNetworks();
                for (Network i : ns) {
                    netId = i.getClass().getDeclaredField("netId").get(i).toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return netId;
    }
}
