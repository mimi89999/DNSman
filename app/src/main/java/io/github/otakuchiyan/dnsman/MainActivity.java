package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();
        firstBoot();

        setListView();
    }

    private void setTitle(){
        final PackageManager pm = getPackageManager();
        try{
            final PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String label = "DNS man " + info.versionName;

            ActionBar actionBar = getActionBar();
            if(actionBar != null){
                actionBar.setTitle(label);
            }
        }catch (PackageManager.NameNotFoundException e){
            throw new AssertionError(e);
        }
    }

    private List<Map<String, String>> buildList(){
        DnsStorage dnsStorage = new DnsStorage(this);
        List<Map<String, String>> dnsEntryList = new ArrayList<>();

        for(NetworkInfo info : DnsStorage.supportedNetInfoList){
            Map<String, String> dnsEntry = new HashMap<>();
            dnsEntry.put("label", getText(DnsStorage.info2resMap.get(info)).toString());
            
            String[] dnsData = dnsStorage.getDnsByNetInfo(info);
            String dnsEntryString = "";
            boolean isNoDns = false;

            if(dnsData[0].isEmpty() && dnsData[1].isEmpty()){
                dnsEntryString = getText(R.string.notify_no_dns).toString();
                isNoDns = true;
            }
            if(isNoDns) {
                if (dnsData[0].isEmpty()) {
                    dnsEntryString += dnsData[0] + '\t';
                }
                dnsEntryString += dnsData[1];
            }

            dnsEntry.put("dnsText", dnsEntryString);
            dnsEntryList.add(dnsEntry);
        }
        return dnsEntryList;
    }


    private void setListView(){
        SimpleAdapter adapter = new SimpleAdapter(this, buildList(),
                android.R.layout.simple_list_item_2,
                new String[] {"label", "dnsText"},
                new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);
    }

    private void firstBoot(){
        DnsStorage dnsStorage = new DnsStorage(this);
        dnsStorage.initDnsMap(this);
    }
}



