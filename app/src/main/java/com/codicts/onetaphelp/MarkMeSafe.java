package com.codicts.onetaphelp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.codicts.onetaphelp.Utils.createChecksum;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MarkMeSafe extends Service {
    LocationManager locationManager;
    FirebaseAuth mAuth;
    String user_id, lat, log;
    private SharedPreferences preferences;
    private String url;
    private ConnectivityManager cm;
    String response;

    public MarkMeSafe() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(3);
        int response = intent.getIntExtra("safe", 1);
        Log.e("****", "onStartCommand: " + response);
        if(response == 0){
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new MyLocationListener());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new MyLocationListener());
            getLocation();
            sendSignal();
            mAuth = FirebaseAuth.getInstance();
            try {
                user_id = mAuth.getCurrentUser().getUid();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }


    protected void getLocation() {
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            lat = String.valueOf(location.getLatitude());
            log = String.valueOf(location.getLongitude());
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                lat = String.valueOf(location.getLatitude());
                log = String.valueOf(location.getLongitude());
            } else {
                Toast.makeText(getApplicationContext(), "Null Location", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "GPS Turned Off", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void sendSignal() {
        getLocation();
        createChecksum cs = new createChecksum();
        if (lat != null && log != null) {

            String myText = cs.checksumGeneration(lat, log);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", lat);
                jsonObject.put("longitude", log);
                jsonObject.put("user_id", user_id);
                jsonObject.put("checksum", myText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            url = BuildConfig.SERVER_URL + "SOS";

            cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                try {
                    post_okhttp(jsonObject);
                    Toast.makeText(getApplicationContext(), "Signal Sent", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please make sure that you are connected to internet", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Null Location Returned", Toast.LENGTH_LONG).show();
        }
    }

    private void post_okhttp(JSONObject jsonObject) throws IOException {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), JSON);
        String auth_token = preferences.getString("auth_token", "");
        final Request request = new Request.Builder()
                .addHeader("Cookie", "auth_token="+auth_token)
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();

            }
        });
    }
}
