package io.github.otakuchiyan.dnsman;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
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
            "127.0.0.1",
            "192.168.0.1",
            "192.168.100.1",
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

        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.item_longclick, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        SparseBooleanArray selectedItems = listView.getCheckedItemPositions();
                        for(int i = 0; i < selectedItems.size(); i++){
                            if(selectedItems.valueAt(i)){
                                String s = adapter.getItem(selectedItems.keyAt(i));
                                adapter.remove(s);
                            }
                        }
                        mode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

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

        final String focusedString = adapter.getItem(position);

        AlertDialog.Builder dnsDialog = new AlertDialog.Builder(this);
        final DNSEditText dnsEditText = new DNSEditText(this);
        dnsEditText.setText(focusedString);

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
                    adapter.remove(focusedString);
                }
            }
        });
        dnsDialog.setNegativeButton(android.R.string.cancel, null);
        dnsDialog.show();

    }



}
