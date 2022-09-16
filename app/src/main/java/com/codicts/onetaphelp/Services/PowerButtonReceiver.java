package com.codicts.onetaphelp.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.codicts.onetaphelp.BuildConfig;
import com.codicts.onetaphelp.Utils.createChecksum;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class PowerButtonReceiver extends BroadcastReceiver {
    static int cnt = 0;

    LocationManager locationManager;
    ConnectivityManager cm;
    Context c;
    SharedPreferences preferences;
    Handler handler;

    String user_id;
    double lat;
    double log;
    String url = BuildConfig.SERVER_URL + "SOS";
    FirebaseAuth mAuth;

    Vibrator vibration;

    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {
        c =  context;
        handler = new Handler();

        vibration = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        preferences = context.getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);

        Intent service = new Intent(context, BackgroundService.class);
        context.startService(service);

        Log.d("1TapHelp.ScreenReceiver", "onReceive: ");
        preferences = context.getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
            cnt = cnt + 1;
            Log.e("1TapHelp.ScreenReceiver", "Screen State: OFF");
            Log.e("1TapHelp.ScreenReceiver", "POWER_BUTTON_PRESS_COUNT :" + String.valueOf(cnt));
            if(cnt == 1){
                handler.postDelayed(runnable, 4000);
            }
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            cnt = cnt + 1;
            Log.e("1TapHelp.ScreenReceiver", "Screen State: ON");
            Log.e("1TapHelp.ScreenReceiver", "POWER_BUTTON_PRESS_COUNT :" + String.valueOf(cnt));
            if(cnt == 1){
                handler.postDelayed(runnable, 4000);
            }
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        mAuth = FirebaseAuth.getInstance();
        try {
            user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(cnt >= 4){
                vibration.vibrate(500);
                getLocation(c);
            }
            cnt = 0;
        }
    };

    public void getLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location NetworkLocation, GPSLocation;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            NetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            GPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (GPSLocation != null) {
                lat = GPSLocation.getLatitude();
                log = GPSLocation.getLongitude();
                sendSignal(context);
            } else if (NetworkLocation != null) {
                lat = NetworkLocation.getLatitude();
                log = NetworkLocation.getLongitude();
                sendSignal(context);
            } else {
                Log.e("1TapHelp.CallReceiver", "getLocation: Null Location Returned");
            }
        } else {
            Log.d("1TapHelp.CallReceiver", "getLocation: Location Permission Denied");
        }
    }

    @SuppressLint("MissingPermission")
    private void sendSignal(Context context) {
        Log.d("1TapHelp.CallReceiver", "sendSignal: Lat : " + lat + ", Lng : " + log);
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(log);

        createChecksum checksumGenerator = new createChecksum();
        String checksum = checksumGenerator.checksumGeneration(latitude, longitude);
        JSONObject postData = new JSONObject();
        try {
            postData.put("latitude", lat);
            postData.put("longitude", log);
            postData.put("user_id", user_id);
            postData.put("checksum", checksum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(String.valueOf(postData), JSON);
            String auth_token = preferences.getString("auth_token", "");
            final Request request = new Request.Builder()
                    .addHeader("Cookie", "auth_token=" + auth_token)
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    call.cancel();
                    e.printStackTrace();

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String strResponse = Objects.requireNonNull(response.body()).string();
                    JSONObject res;
                    try {
                        res = new JSONObject(strResponse);

                        if (response.code() == 200 ){

                            Log.d("Signal", "onResponse: SignalSent, Response:" + res.getJSONArray("stations"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            String messageText = "I need help \nThis is current Location\n" + "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            StringBuilder messageBody = new StringBuilder();
            messageBody.append(Uri.parse(messageText));
            SmsManager smsManager = SmsManager.getDefault();

            String contactListString = preferences.getString("contacts", null);
            JSONArray contactsData = new JSONArray();
            if (contactListString != null) {
                try {
                    JSONObject contactListJSON = new JSONObject(contactListString);
                    contactsData = contactListJSON.getJSONArray("contacts");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            for (int i=0; i< contactsData.length(); i++){
                try {
                    String Phno = contactsData.getJSONObject(i).getString("contactPhNo");
                    smsManager.sendTextMessage(Phno, null, messageBody.toString(), null, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } else {
            String messageText = "[1TH] {\"user\": \"" + user_id + "\", \"lat\": "+latitude+", \"lng\": "+longitude+"}";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(BuildConfig.SMS_SERVER_PHNO, null, String.valueOf(Uri.parse(messageText)), null, null );
            Log.d("SigalSent", "sendSignal: Signal sent to SMS Server");
        }
    }




}