package io.github.otakuchiyan.dnsman;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

public class GetNetwork
{
	private static ConnectivityManager cm;


    public static void init(Context c){
		cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
	
	public static NetworkInfo getMobileNetwork(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }
	
	public static NetworkInfo getWiFiNetwork(){
		return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}
}
