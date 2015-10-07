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
    public boolean isSupportWiMax;

    public GetNetwork{
    }

    public static void init(Context c){
		cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
	
	public static NetworkInfo getMobileNetInfo(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isSupportMobile(){
        return getMobileNetInfo() != null ? true : false;
    }
	
	public static NetworkInfo getWiFiNetInfo(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}

    public static boolean isSupportWiFi(){
        return getWiFiNetInfo() != null ? true : false;
    }
	
	public static NetworkInfo getBluetoothNetInfo(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
	}

    public static boolean isSupportBluetooth(){
        return getBluetoothNetInfo() != null ? true : false;
    }
	
	public static NetworkInfo getEthernetNetInfo(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
	}

    public static boolean isSupportEthernet(){
        return getEthernetNetInfo() != null ? true : false;
    }

    public static NetworkInfo getWiMaxNetInfo(){
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
    }

    public static boolean isSupportWiMax(){
        return getWiMaxNetInfo() != null ? true : false;
    }
}
