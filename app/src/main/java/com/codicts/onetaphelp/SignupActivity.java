package com.codicts.onetaphelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private static final int REQUEST_CHECK_SETTINGS = 0;
    private FusedLocationProviderClient fusedLocationClient;
    // lists for permissions
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private double latitude, longitude;


    Button btnContinue;
    TextInputEditText nameField;
    SharedPreferences prefs;

    SharedPreferences preferences;
    private String url = BuildConfig.SERVER_URL + "api/user/register";
    String token;
    Context context;
    SwitchMaterial volunteerToggle;
    ProgressBar progressBar;
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);
        preferences = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        context = getApplicationContext();
        getFirebaseId();
        progressBar = (ProgressBar) findViewById(R.id.register_progress);
        progressBar.setVisibility(View.INVISIBLE);
        btnContinue = (Button)findViewById(R.id.signup_btn);
        nameField = (TextInputEditText)findViewById(R.id.textField_name);
        volunteerToggle = (SwitchMaterial)findViewById(R.id.register_volunteerToggle);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        createLocationRequest();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameField.getText().toString().isEmpty()){
                    nameField.setError("Enter Your Full Name");
                    nameField.requestFocus();
                    return;
                } else {
                    registerUser();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        createLocationRequest();
    }

    private void getFirebaseId() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "getInstance failed!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        token = Objects.requireNonNull(task.getResult()).getToken();
                        Log.e("Token ", token);
                    }
                });
    }


    private void createLocationRequest(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(SignupActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLocation();
            }
        });

        task.addOnFailureListener(SignupActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    // Location Settings are not satisfied, Show a dialog
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(SignupActivity.this,
                                REQUEST_CHECK_SETTINGS);
                        startIntentSenderForResult(((ResolvableApiException) e).getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);

                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    private void getLocation() {
        // Showing refresh animation before making http call
        // Check Permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                PERMISSIONS = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                };
            }
            requestPermissions(PERMISSIONS, ALL_PERMISSIONS_RESULT);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                } else {
                    Toast.makeText(context, "Error occurred while getting Location.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    private void registerUser(){
        progressBar.setVisibility(View.VISIBLE);
        String strName, strPhone;
        strName = nameField.getText().toString().trim();
        strPhone = getIntent().getStringExtra("PhoneNum");
        boolean isVolunteer = volunteerToggle.isChecked();

        //store user data locally
        prefs = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", strName);
        editor.putString("phone", strPhone);
        editor.apply();

        Log.e("Lat", " "+latitude);
        Log.e("Lng", " "+longitude);
        String userid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userid);
            jsonObject.put("phno", strPhone);
            jsonObject.put("token", token);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("name", strName);
            jsonObject.put("volunteer", isVolunteer);
            sendRequest(jsonObject);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(JSONObject jsonObject) throws IOException {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), JSON);
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!result.isEmpty()) {
                            JSONObject responseData = null;
                                if (response.code() == 200) {
                                    try {
                                        responseData = new JSONObject(result);

                                    String auth_token = responseData.getString("auth_token");
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("auth_token", auth_token);
                                    editor.commit();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(SignupActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent;
                                    if (preferences.getBoolean("introDone", false) == true) {
                                        intent = new Intent(SignupActivity.this, MainActivity.class);
                                    } else {
                                        intent = new Intent(SignupActivity.this, IntroActivity.class);
                                    }
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else if (response.code() == 400) {
                                    Toast.makeText(SignupActivity.this, "Already Registered", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(SignupActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }
                            }

                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
                Toast.makeText(SignupActivity.this, "Registration Failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

}
