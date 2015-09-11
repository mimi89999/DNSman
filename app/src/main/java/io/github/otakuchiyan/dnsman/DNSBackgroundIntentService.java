package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import android.support.v4.content.LocalBroadcastManager;

import eu.chainfire.libsuperuser.Shell;

import io.github.otakuchiyan.dnsman.DNSManager;

public class DNSBackgroundIntentService extends IntentService{
    final static String ACTION_SETDNS_DONE = "io.github.otakuchiyan.dnsman.SETDNS_DONE";
	
	public static void performAction(Context c, Bundle dnss){
		if(c == null || dnss == null){
			return;
		}
		
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
		boolean result = DNSManager.
			setDNSViaSetprop(dnss.getString("dns1"),
				dnss.getString("dns2"));
		Intent result_intent = new Intent(ACTION_SETDNS_DONE);
		result_intent.putExtra("result", result);
		LocalBroadcastManager.getInstance(getApplicationContext())
		    .sendBroadcast(result_intent);
	}
}
