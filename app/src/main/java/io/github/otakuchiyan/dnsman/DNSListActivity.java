package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DNSListActivity extends ListActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;
    private ArrayList<String> dnsList;
    private ArrayAdapter<String> adapter;

    private String[] default_list = {
            "8.8.8.8",
            "8.8.4.4"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.pref_dns_list);
        dnsList = new ArrayList<>(sp.getStringSet("dnslist", new HashSet<String>()));
        if(dnsList.size() == 0){
            dnsList.addAll(Arrays.asList(default_list));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dnsList);
        setListAdapter(adapter);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dnslist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_add:
                addItem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause(){
        super.onPause();
        sped = sp.edit();
        Set<String> toSavedDNS = new HashSet<>(dnsList);
        sped.putStringSet("dnslist", toSavedDNS);
        sped.apply();
    }

    private void addItem(){
        AlertDialog.Builder dnsDialog = new AlertDialog.Builder(this);
        final DNSEditText dnsEditText = new DNSEditText(this);
        dnsEditText.setIPChecker();
        dnsDialog.setView(dnsEditText);
        dnsDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dns = dnsEditText.getText().toString();
                if (!dns.equals("") &&
                        (IPChecker.IPv4Checker(dns)
                                || IPChecker.IPv6Checker(dns))) {
                    adapter.add(dns);
                }
            }
        });
        dnsDialog.setNegativeButton(android.R.string.cancel, null);
        dnsDialog.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }
}
