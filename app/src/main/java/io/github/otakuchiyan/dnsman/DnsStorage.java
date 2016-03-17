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

public class DnsStorage implements ValueConstants{
    /*
    Preference example:
      key       value
      "WIFI" + "dns1" => ["127.0.0.1", "127.0.0.2"]
      "*port" was not used
     */

    public static ArrayList<NetworkInfo> supportedNetInfoList = new ArrayList<>();
    public static HashMap<NetworkInfo, Integer> info2resMap = new HashMap<>();
    public static HashMap<NetworkInfo, String> info2interfaceMap = new HashMap<>();
    public static HashMap<String, Integer> method2resMap = new HashMap<>();

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;
    private Context context;

    public DnsStorage(Context c){
        preferences = PreferenceManager.getDefaultSharedPreferences(c);
        preferenceEditor = preferences.edit();
        context = c;
    }

    public void refreshInfo2InterfaceMap(){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        for(int i = 0; i != ValueConstants.NET_TYPE_LIST.length; i++){
            NetworkInfo info = manager.getNetworkInfo(ValueConstants.NET_TYPE_LIST[i]);
            info2interfaceMap.put(info,
                    preferences.getString(
                            ValueConstants.KEY_CUSTOM_INTERFACES[i],
                            ValueConstants.NETWORK_INTERFACES[i]));
        }

    }

    public void initResourcesMap(){
        for(int i = 0; i != METHODS.length; i++){
            method2resMap.put(METHODS[i], METHOD_RESOURCES[i]);
        }
    }

    //Keep build one time
    public void initDnsMap(){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        for(int i = 0; i != ValueConstants.NET_TYPE_LIST.length; i++){
            NetworkInfo info = manager.getNetworkInfo(ValueConstants.NET_TYPE_LIST[i]);
            info2resMap.put(info, ValueConstants.NET_TYPE_RESOURCES[i]);
            if(info != null){
                 supportedNetInfoList.add(info);
            }
        }

        refreshInfo2InterfaceMap();
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

    public void putGlobalDns(String[] dnsEntry){
        putDnsByKeyPrefix("g", dnsEntry);
    }

    public void putDnsByKeyPrefix(String keyPrefix, String[] dnsEntry){
        for(int i = 0; i != 2; i++){
            preferenceEditor.putString(keyPrefix + Integer.toString(i),
                    dnsEntry[i]);
        }
        preferenceEditor.apply();
    }
}