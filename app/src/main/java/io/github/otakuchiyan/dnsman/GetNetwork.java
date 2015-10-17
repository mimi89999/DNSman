package io.github.otakuchiyan.dnsman;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

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

        isSupportWifi = wifiNetInfo != null ? true : false;
        isSupportMobile = mobileNetInfo != null ? true : false;
        isSupportBluetooth = bluetoothNetInfo != null ? true : false;
        isSupportEthernet = etherNetInfo != null ? true : false;
        isSupportWimax = wimaxNetInfo != null ? true : false;

	wifiName = wifiNetInfo != null ? wifiNetInfo.getTypeName() : null;
	mobileName = mobileNetInfo != null ? mobileNetInfo.getTypeName() : null;
	bluetoothName = bluetoothNetInfo != null ? bluetoothNetInfo.getTypeName() : null;
	etherName = etherNetInfo != null ? etherNetInfo.getTypeName() : null;
	wimaxName = wimaxNetInfo != null ? wimaxNetInfo.getTypeName() : null;
    }
}
