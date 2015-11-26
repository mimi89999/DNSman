package io.github.otakuchiyan.dnsman;
import android.app.*;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.lang.Void;

import io.github.otakuchiyan.dnsman.DNSManager;
import io.github.otakuchiyan.dnsman.IPCheckerComponent;
import io.github.otakuchiyan.dnsman.DNSEditText;
import android.view.View.*;
import android.content.*;
import android.view.*;
import android.util.Log;

public class DNSConfActivity extends Activity{
    final static String ACTION_CONFOPERATION = "io.github.otakuchiyan.dnsman.ACTION_CONFOPERATION";
    final static String ACTION_CONF_GETTED = "io.github.otakuchiyan.dnsman.ACTION_CONF_GETTED";
    
	private SharedPreferences sp;
    private LinearLayout dnsConfActivity;
    private TextView configPathText;
    private TextView configDNS;
	private DNSEditText rdns1;
	private DNSEditText rdns2;
    private String configPath;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.action_edit_resolv);
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        configPath = sp.getString("config_path", "/etc/resolv.conf");
        if(configPath.equals("")){
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setMessage(R.string.no_conf);
            adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface di, int choice){
                    finish();
                }
            });
			adb.show();
            return;
        }
        dnsConfActivity = new LinearLayout(this);
        dnsConfActivity.setOrientation(LinearLayout.VERTICAL);
        configPathText = new TextView(this);
        configDNS = new TextView(this);
        configPathText.setText(configPath);

        rdns1 = new DNSEditText(this);
        rdns2 = new DNSEditText(this);
        rdns1.setKeyAndText("rdns1");
        rdns2.setKeyAndText("rdns2");
        rdns1.setIPChecker();
        rdns2.setIPChecker();

        dnsConfActivity.addView(configPathText);
        dnsConfActivity.addView(configDNS);
        dnsConfActivity.addView(rdns1);
        dnsConfActivity.addView(rdns2);

		IntentFilter operationFilter = new IntentFilter();
		operationFilter.addAction(ACTION_CONFOPERATION);
		LocalBroadcastManager.getInstance(this).registerReceiver(confOperation, operationFilter);

        IntentFilter confFilter = new IntentFilter();
        confFilter.addAction(ACTION_CONF_GETTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(gettedConf, confFilter);

        (new getConfigTask()).execute();
		
        setContentView(dnsConfActivity);
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dnsconf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.write_conf:
                onClickWriteConfig();
                break;
            case R.id.default_conf:
                onClickDefaultConfig();
                break;
            case R.id.delete_conf:
                onClickDeleteConfig();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver confOperation = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
		String err = i.getStringExtra("err");
		if(i.getAction().equals(ACTION_CONFOPERATION)){
		    if(err != null){
				Toast.makeText(c, err, Toast.LENGTH_LONG).show();
		    }else{
	    Toast.makeText(c, R.string.operation_succeed, Toast.LENGTH_SHORT).show();
		    }
		}
	    }
	};

    private BroadcastReceiver gettedConf = new BroadcastReceiver(){
	    @Override
	    public void onReceive(Context c, Intent i){
            if(i.getAction().equals(ACTION_CONF_GETTED)){
                (new getConfigTask()).execute();
            }
	    }
	};

    private class getConfigTask extends AsyncTask<Void, Void, String>{
        protected String doInBackground(Void... p1){
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(configPath.equals("")){
                return "";
            }
            return DNSManager.getResolvConf(configPath);
        }

        protected void onPostExecute(String data){
            configDNS.setText(data);
        }
    }

    private class writeConfigTask extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... dnss){
            return DNSManager.writeResolvConfig(dnss[0], dnss[1], configPath);
        }
        protected void onPostExecuted(String error){
            if(!error.equals("")){
                AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                adb.setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }else{
                new getConfigTask().execute();
            }
        }
    }

    private class deleteConfigTask extends AsyncTask<Void, Void, String>{
        protected String doInBackground(Void... p1){
            return DNSManager.removeResolvConfig(configPath);
        }
        protected void onPostExecuted(String error){
            if(!error.equals("")){
                AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                adb.setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }
        }
    }

    private void onClickConfigCore(int titleId, int messageId, DialogInterface.OnClickListener listener){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(titleId)
		.setMessage(messageId)
		.setPositiveButton(android.R.string.ok, listener)
		.setNegativeButton(android.R.string.cancel, null)
        .show();
    }

    private void onClickWriteConfig(){
        onClickConfigCore(R.string.write_conf, R.string.write_conf_msg, 
            new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface di, int choice){
                    String dns1 = rdns1.getText().toString();
                    String dns2 = rdns2.getText().toString();
                    (new writeConfigTask()).execute(dns1, dns2);
                }
            });
    }

    private void onClickDefaultConfig(){
        onClickConfigCore(R.string.default_conf, R.string.default_conf_msg, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface di, int choice){
                (new writeConfigTask()).execute("8.8.8.8", "8.8.4.4");
            }
        });
    }

    private void onClickDeleteConfig(){
        onClickConfigCore(R.string.delete_conf, R.string.delete_conf_msg, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface di, int choice){
                (new deleteConfigTask()).execute();
            }
        });
    }
}
