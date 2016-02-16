package io.github.otakuchiyan.dnsman;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import java.lang.Void;



public class DNSConfActivity extends Activity{
	private SharedPreferences sp;
    private Context context;

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
        context = this;

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
        protected void onPostExecute(String error){
            if(!error.equals("")){
                AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                adb.setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }else{
                Toast.makeText(context, R.string.set_succeed, Toast.LENGTH_SHORT).show();
                new getConfigTask().execute();
            }
        }
    }

    private class deleteConfigTask extends AsyncTask<Void, Void, String>{
        protected String doInBackground(Void... p1){
            return DNSManager.removeResolvConfig(configPath);
        }
        protected void onPostExecute(String error){
            if(!error.equals("")){
                AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
                adb.setMessage(error)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }else{
                Toast.makeText(context, R.string.set_succeed, Toast.LENGTH_SHORT).show();
                new getConfigTask().execute();
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
