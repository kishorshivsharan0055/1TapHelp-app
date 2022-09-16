package com.codicts.onetaphelp.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {
    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("1TapHelp", "Service working in background");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver powerButtonReceiver = new PowerButtonReceiver();
        registerReceiver(powerButtonReceiver, filter);

//        IntentFilter filterCall = new IntentFilter("android.intent.action.PHONE_STATE");
//        filterCall.addAction("android.intent.action.NEW_OUTGOING_CALL");
//        BroadcastReceiver callReceiver = new CallCast();
//        registerReceiver(callReceiver, filterCall);
    }

    @Override
    public void onDestroy() {
        Log.d("1TapHelp", "Service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
    }
}
