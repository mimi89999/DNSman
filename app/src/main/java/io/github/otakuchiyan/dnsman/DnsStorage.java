package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DnsStorage{
    /*
    Preference example:
      key       value
      "WIFI" + "dns1" => ["127.0.0.1", "127.0.0.2"]
      "*port" was not used
     */

    public static ArrayList<NetworkInfo> supportedNetInfoList = new ArrayList<>();
    public static boolean isSupportedNetInfoListBuilded = false;
    public static HashMap<NetworkInfo, Integer> info2resMap = new HashMap<>();
    public static boolean isInfo2resMapBuilded = false;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;

    public DnsStorage(Context c){
        preferences = PreferenceManager.getDefaultSharedPreferences(c);
        preferenceEditor = preferences.edit();
    }

    public void initDnsMap(Context c){
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        int[] netTypeList = {
                ConnectivityManager.TYPE_WIFI,
                ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_BLUETOOTH,
                ConnectivityManager.TYPE_ETHERNET,
                ConnectivityManager.TYPE_WIMAX
        };
        int[] resources = {
                R.string.category_wifi,
                R.string.category_mobile,
                R.string.category_bluetooth,
                R.string.category_ethernet,
                R.string.category_wimax
        };

        for(int i = 0; i != netTypeList.length; i++){
            NetworkInfo info = manager.getNetworkInfo(netTypeList[i]);
            if(!isInfo2resMapBuilded) {
                info2resMap.put(info, resources[i]);
            }
            if(info != null){
                if(!isSupportedNetInfoListBuilded){
                    supportedNetInfoList.add(info);
                }
            }
        }

        //Keep build one time
        isSupportedNetInfoListBuilded = true;
        isInfo2resMapBuilded = true;
    }


    public String[] getDnsByNetInfo(NetworkInfo info){
        return getDnsByKeyPrefix(info.getTypeName());
    }

    public String[] getGlobalDns(){
        return getDnsByKeyPrefix("g");
    }

    //To compatible old version
    public String[] getDnsByKeyPrefix(String keyPrefix){
        String[] dnsEntry = new String[2];
        for(int i = 0; i != 2; i++) {
            dnsEntry[i] = preferences.getString(keyPrefix + Integer.toString(i), "");
        }
        return dnsEntry;
    }

    public void putDns(NetworkInfo info, String[] dnsEntry){
        putDnsByKeyPrefix(info.getTypeName(), dnsEntry);
    }

    public void putGlobalDns(String[] dnsEntry){
        putDnsByKeyPrefix("g", dnsEntry);
    }

    private void putDnsByKeyPrefix(String keyPrefix, String[] dnsEntry){
        for(int i = 0; i != 2; i++){
            preferenceEditor.putString(keyPrefix + Integer.toString(i),
                    dnsEntry[i]);
        }
        preferenceEditor.apply();
    }
}