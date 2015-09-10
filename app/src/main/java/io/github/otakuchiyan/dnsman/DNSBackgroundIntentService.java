package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import eu.chainfire.libsuperuser.Shell;

import io.github.otakuchiyan.dnsman.DNSManager;

public class DNSBackgroundIntentService extends IntentService{
	private static Context context;
	
	public static void performAction(Context c, Bundle dnss){
		if(c == null || dnss == null){
			return;
		}
		
		context = c;
		Intent i = new Intent(c, DNSBackgroundIntentService.class);
		i.putExtras(dnss);
		c.startService(i);
	}
	
	public DNSBackgroundIntentService(){
		super("DNSBackgroundIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent i){
		Bundle dnss = i.getExtras();
		if(dnss == null){
			return;
		}
		if(DNSManager.setDNSViaSetprop(dnss.getString("dns1"),
			dnss.getString("dns2"))){
				Log.d("DBGIS", "succeed");
				Toast.makeText(context, R.string.set_succeed, Toast.LENGTH_SHORT).show();
		}else{

			Log.d("DBGIS", "failed");
			Toast.makeText(context, R.string.set_failed, Toast.LENGTH_SHORT).show();
		}
	}
}
