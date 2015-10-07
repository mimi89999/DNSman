package io.github.otakuchiyan.dnsman;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

public class GetNetwork
{
    private ConnectivityManager cm;

    public NetworkInfo wifiNetInfo;
    public NetworkInfo mobileNetInfo;
    public NetworkInfo btNetInfo;
    public NetworkInfo ethNetInfo;
    public NetworkInfo wimaxNetInfo;
    public boolean isSupportWifi;
    public boolean isSupportMobile;
    public boolean isSupportBluetooth;
    public boolean isSupportEthernet;
    public boolean isSupportWimax;

    public GetNetwork(Context c){
		cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        wifiNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		mobileNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		btNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
		ethNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        wimaxNetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        isSupportWifi = wifiNetInfo != null ? true : false;
        isSupportMobile = mobileNetInfo != null ? true : false;
        isSupportBluetooth = btNetInfo != null ? true : false;
        isSupportEthernet = ethNetInfo != null ? true : false;
        isSupportWimax = wimaxNetInfo != null ? true : false;
    }
}
