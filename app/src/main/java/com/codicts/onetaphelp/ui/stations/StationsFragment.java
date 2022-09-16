package com.codicts.onetaphelp.ui.stations;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codicts.onetaphelp.BuildConfig;
import com.codicts.onetaphelp.Models.DM_Station;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.Adapters.StationListAdapter;
import com.codicts.onetaphelp.ui.StationBottomSheetFragment;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class StationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_CHECK_SETTINGS = 0;
    private StationsViewModel stationsViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    // lists for permissions
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private String latitude, longitude;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView stationsList;
    private RecyclerView.Adapter stationsListAdapter;
    private RecyclerView.LayoutManager listLayoutManager;

    JSONArray stationData;
    private ArrayList<DM_Station> stationArrayList = new ArrayList<>();


    @Override
    public void onStart() {
        super.onStart();
        createLocationRequest();
    }


    @SuppressLint("ResourceAsColor")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        stationsViewModel =
                ViewModelProviders.of(this).get(StationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stations, container, false);

        stationsList = (RecyclerView) root.findViewById(R.id.stations_fetched_list);
        listLayoutManager = new LinearLayoutManager(getActivity());

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeToRefresh_Stations);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                createLocationRequest();
            }
        });

        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);


        return root;
    }

    private void createLocationRequest(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLocation();
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    // Location Settings are not satisfied, Show a dialog
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(getActivity(),
                                REQUEST_CHECK_SETTINGS);
                        startIntentSenderForResult(((ResolvableApiException) e).getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);

                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getLocation();
        } else {
            Toast.makeText(getContext(), "GPS is required to fetch nearby DM Stations", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {


        // Showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);
        // Check Permissions
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            };
            requestPermissions(PERMISSIONS, ALL_PERMISSIONS_RESULT);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationClient.getLastLocation().addOnSuccessListener((Activity) getContext(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude=String.valueOf(location.getLatitude());
                    longitude=String.valueOf(location.getLongitude());
                    fetchStations();
                } else {
                    // Stopping swipe refresh
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), "Error occurred while getting Location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchStations(){
        try {

            JSONObject postData = new JSONObject();
            JSONObject locationData = new JSONObject();

            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            postData.put("location", locationData);
            postData.put("auto_increment", true);

            MediaType contentType = MediaType.parse("application/json; charset=utf-8");

            OkHttpClient requestClient = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(contentType, postData.toString());
            String API_URL = BuildConfig.SERVER_URL + "api/station/get";
            Request request = new Request.Builder().url(API_URL).post(requestBody).build();

                requestClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Error occured while sending signal to the server", Toast.LENGTH_LONG).show();
                            }
                        });

                        // Stopping swipe refresh
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String strResponse = response.body().string();
                        if (!strResponse.isEmpty()) {
                            JSONObject responseData = null;
                            try {
                                responseData = new JSONObject(strResponse);
                                stationArrayList.clear();
                                stationData = responseData.getJSONArray("stations");

                                for (int i = 0; i < stationData.length(); i++) {
                                    JSONObject stationJSON = null;
                                    try {
                                        stationJSON = stationData.getJSONObject(i);
                                        DM_Station station = new DM_Station();
                                        station.stationID = stationJSON.getString("station_id");
                                        station.stationName = stationJSON.getString("station_name");
                                        station.phoneNo = stationJSON.getString("phno");
                                        station.distance = stationJSON.getString("distance");
                                        station.latitude = stationJSON.getString("latitude");
                                        station.longitude = stationJSON.getString("longitude");
                                        stationArrayList.add(station);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            stationsList.setHasFixedSize(true);
            try {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stationsList.setLayoutManager(listLayoutManager);
                                    stationsListAdapter = new StationListAdapter(stationArrayList, getActivity(), getContext(), getParentFragmentManager());
                                    stationsList.setAdapter(stationsListAdapter);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
            } catch (Exception e ) {
                Log.d("TAG", "onResponse: ");
            }


                        }
                    }
                });


        } catch (JSONException e) {
            e.printStackTrace();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }



    @Override
    public void onRefresh() {
        createLocationRequest();


    }
}