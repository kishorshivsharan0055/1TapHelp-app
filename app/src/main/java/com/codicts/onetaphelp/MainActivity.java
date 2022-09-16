package com.codicts.onetaphelp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri data = getIntent().getData();
        String action = getIntent().getStringExtra("action");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            Log.d("TEMP", "Extras are NULL");
        } else {
            String NotifType = extras.getString("NotifType");
            Log.d("extras", "onCreate: " + NotifType);
            if (NotifType != null && NotifType.equals("Upcoming_Disaster")) {
                navController.navigate(R.id.navigation_notifications, extras);
            } else {
                navController.navigate(R.id.navigation_home, extras);
            }
        }
//        if (data != null) {
//            Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT).show();
//            List<String> pathSegments  = data.getPathSegments();
//            if (pathSegments.size() > 0) {
//                String actionUri = pathSegments.get(0);
//                if (actionUri != null && actionUri.equals("sendSignal")) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("action", "sendSignal");
//                    navController.navigate(R.id.navigation_home, bundle);
//                }
//            }
//        }
//
//        if (action != null && action.equals("sendSignal")) {
//            Bundle bundle = new Bundle();
//            bundle.putString("action", "sendSignal");
//            navController.navigate(R.id.navigation_home, bundle);
//        }
//
//        if (action != null && action.equals("markMeSafe")) {
//            Toast.makeText(this, "mrak", Toast.LENGTH_SHORT).show();
//            String eventID = getIntent().getStringExtra("event_id");
//            String eventName = getIntent().getStringExtra("event_name");
//            Bundle bundle = new Bundle();
//            bundle.putString("action", "markMeSafe");
//            if (eventID != null) {
//                bundle.putString("event_id", eventID);
//            }
//            if (eventName != null) {
//                bundle.putString("event_name", eventName);
//
//            }
//            navController.navigate(R.id.navigation_home, bundle);
//        }

    }

}