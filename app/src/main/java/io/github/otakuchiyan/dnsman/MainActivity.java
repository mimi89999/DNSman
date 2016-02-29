package io.github.otakuchiyan.dnsman;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();
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
}
