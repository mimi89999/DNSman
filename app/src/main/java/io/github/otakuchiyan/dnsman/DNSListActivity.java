package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DNSListActivity extends ListActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor sped;
    private ArrayList<String> dnsList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.pref_dns_list);
        dnsList = new ArrayList<>(sp.getStringSet("dnslist", new HashSet<String>()));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dnsList);
        setListAdapter(adapter);
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

}
