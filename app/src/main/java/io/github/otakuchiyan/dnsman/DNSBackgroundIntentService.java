package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;

import io.github.otakuchiyan.dnsman.DNSManager;

public class DNSBackgroundIntentService extends IntentService{
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
		DNSManager.setDNSViaSetprop(dnss.getString("dns1"),
			dnss.getString("dns2"),
			dnss.getBoolean("use_su"));
		
	}
}
