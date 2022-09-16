package com.codicts.onetaphelp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.codicts.onetaphelp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationTracking extends AppCompatActivity implements OnMapReadyCallback,
        Callback<DirectionsResponse> {

    MapView mapView;
    ProgressBar routeLoading;
    private MapboxMap mapboxMap;
    private NavigationMapRoute navigationMapRoute;
    private StyleCycle styleCycle = new StyleCycle();

    Point originPoint, destinationPoint;
    DirectionsRoute routes;
    String path = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private LatLng latLng;
    private Marker mMarker;
    private boolean isFirstMessage = true;
    List<Point> routeCoodinates;
    CameraPosition cameraPosition;
    private FusedLocationProviderClient fusedLocationProviderClient;
    String signal_id;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.station_tracking_layout);
        mapView = findViewById(R.id.stationTracking);
        mapView.onCreate(savedInstanceState);
        routeCoodinates = new ArrayList<>();
        preferences = getApplicationContext().getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        signal_id = preferences.getString("signal_id", null);

        //object initialization
        routeLoading = findViewById(R.id.routeLoadingProgressBar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StationTracking.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PackageManager.PERMISSION_GRANTED);
            }
        }
        Bundle bundle =getIntent().getExtras();

        //get DM team location
        double lat = Double.valueOf(bundle.getString("latitude"));
        double lng = Double.valueOf(bundle.getString("longitude"));
        destinationPoint = Point.fromLngLat(lng, lat);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Double curlat = location.getLatitude();
                        Double  curlng = location.getLongitude();
                        originPoint = Point.fromLngLat(curlng, curlat);
                        mapView.getMapAsync(StationTracking.this::onMapReady);
                        findRoute(originPoint, destinationPoint);

                    }
                });






        routeLoading.setVisibility(View.VISIBLE);

        getRealTimeLocation();
    }


    private void getRealTimeLocation() {
        Log.e("TAG", "getRealTimeLocation: ");
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("/locations/"+signal_id);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                com.codicts.onetaphelp.LocationFields value = dataSnapshot.getValue(com.codicts.onetaphelp.LocationFields.class);
                if (value != null) {
                    latLng = new LatLng(value.getLatitude(), value.getLongitude());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCamera(latLng);
                            updateMarker();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read value.", error.toException());
            }
        });
    }


    private void updateCamera(LatLng latLng) {
        cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(17)
                .bearing(180)
                .tilt(30)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 7000);
    }

    private void updateMarker() {
        if (!isFirstMessage) {
            mMarker.remove();
        }
        isFirstMessage = false;

        mMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(latLng));
    }

    public void findRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .build()
                .getRoute(this);
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(styleCycle.getStyle(), style -> {
            initializeLocationComponent(mapboxMap);
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap);
//            locationComponent = mapboxMap.getLocationComponent();
        });
    }

    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        if (response.isSuccessful()
                && response.body() != null
                && !response.body().routes().isEmpty()) {
            routes = response.body().routes().get(0);
            List<DirectionsRoute> l_route = response.body().routes();
            navigationMapRoute.addRoutes(l_route);
            routeLoading.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
    }


    private static class StyleCycle {
        private static final String[] STYLES = new String[]{
                Style.MAPBOX_STREETS,
                Style.OUTDOORS,
                Style.LIGHT,
                Style.DARK,
                Style.SATELLITE_STREETS
        };

        private int index;

        private String getNextStyle() {
            index++;
            if (index == STYLES.length) {
                index = 0;
            }
            return getStyle();
        }

        private String getStyle() {
            return STYLES[index];
        }
    }


    private void initializeLocationComponent(MapboxMap mapboxMap) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationComponent.activateLocationComponent(this, mapboxMap.getStyle());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.zoomWhileTracking(10d);
    }
}