package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends ListActivity implements ValueConstants {
    private DnsStorage dnsStorage;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private SimpleAdapter adapter;
    private List<Map<String, String>> mDnsEntryList;

    private void initVariable(){
        dnsStorage = new DnsStorage(this);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVariable();






        setTitle();
        firstBoot();

        setListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DNS_CHANGE || resultCode == RESULT_OK){
            mDnsEntryList.clear();
            mDnsEntryList.addAll(buildList());
            adapter.notifyDataSetChanged();
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //List part START
    private List<Map<String, String>> buildList(){

        List<Map<String, String>> dnsEntryList = new ArrayList<>();

        dnsEntryList.add(getGlobalDnsEntry());

        if(mPreferences.getBoolean(KEY_PREF_INDIVIDUAL_MODE, false)) {
            for (NetworkInfo info : DnsStorage.supportedNetInfoList) {
                dnsEntryList.add(getNetworkDnsEntry(info));
            }
        }

        return dnsEntryList;
    }

    private Map<String, String> getNetworkDnsEntry(NetworkInfo info){
        return getDnsEntry(info.getTypeName(), DnsStorage.info2resMap.get(info));
    }

    private Map<String, String> getGlobalDnsEntry(){
        return getDnsEntry("g", R.string.category_global);
    }

    private Map<String, String> getDnsEntry(String prefix, int resource){
        Map<String, String> dnsEntry = new HashMap<>();
        dnsEntry.put("prefix", prefix);
        dnsEntry.put("label", getText(resource).toString());

        String[] dnsData = dnsStorage.getDnsByKeyPrefix(prefix);
        String dnsEntryString = "";
        boolean isNoDns = false;

        if(dnsData[0].isEmpty() && dnsData[1].isEmpty()){
            dnsEntryString = getText(R.string.notify_no_dns).toString();
            isNoDns = true;
        }
        if(!isNoDns) {
            if (!dnsData[0].isEmpty()) {
                dnsEntryString += dnsData[0] + ' ';
            }
            dnsEntryString += dnsData[1];
        }

        dnsEntry.put("dnsText", dnsEntryString);
        return dnsEntry;
    }

    private void setListView(){
        mDnsEntryList = buildList();
        adapter = new SimpleAdapter(this, mDnsEntryList,
                android.R.layout.simple_list_item_2,
                new String[] {"label", "dnsText"},
                new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> dnsEntry = (Map<String, String>) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), DnsEditActivity.class);
                i.putExtra("label", dnsEntry.get("label"));
                i.putExtra("prefix", dnsEntry.get("prefix"));
                startActivityForResult(i, ValueConstants.REQUEST_DNS_CHANGE);
            }
        });
    }
    //List part END

    private void firstBoot(){
        dnsStorage.initDnsMap(this);

        Set<String> toSavedDNS = new HashSet<>(Arrays.asList(DEFAULT_DNS_LIST));
        mEditor.putStringSet(KEY_DNS_LIST, toSavedDNS);
        mEditor.apply();
    }
}



