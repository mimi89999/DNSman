package io.github.otakuchiyan.dnsman;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.ListPreference;
import android.content.SharedPreferences;



public class SettingsActivity extends PreferenceActivity{
	@Override
	public void onCreate(final Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getActionBar().setHomeButtonEnabled(true);
		//			SharedPreference sp = PreferenceManager.getDefaultPreference();
				
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new AppPref())
			.commit();

	}

    public static class AppPref extends PreferenceFragment{
        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.pref);
	    //	    ListPreference dnsToastPref = getPreference("dns
	    
        }
    }
}
