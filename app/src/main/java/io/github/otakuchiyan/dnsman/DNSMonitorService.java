package io.github.otakuchiyan.dnsman;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DNSMonitorService extends Service {
    static final String TAG = "DNSMonitorService";

    private final IBinder mBinder = new DNSWatchingBinder();

    public DNSMonitorService() {
    }

    public class DNSWatchingBinder extends Binder {
        DNSMonitorService getService() {
            return DNSMonitorService.this;
        }
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "Running");
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId){
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "Stopping");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
