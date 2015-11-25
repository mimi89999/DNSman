package io.github.otakuchiyan.dnsman;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class DNSListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.pref_dns_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dnslist, menu);
        return true;
    }
}
