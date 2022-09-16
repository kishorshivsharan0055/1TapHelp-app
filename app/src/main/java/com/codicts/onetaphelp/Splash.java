package com.codicts.onetaphelp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.codicts.onetaphelp.Services.BackgroundService;
import com.google.firebase.auth.FirebaseAuth;

public class Splash extends AppCompatActivity {
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        String auth_token = preferences.getString("auth_token", "");

        if  (FirebaseAuth.getInstance().getCurrentUser() == null || auth_token.length() < 1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent;
            if (preferences.getBoolean("introDone", false)) {
                intent = new Intent(this, MainActivity.class);
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    Log.d("Extras", "onCreate: Extras" + bundle);
                    intent.putExtras(bundle);
                }
            } else {
                intent = new Intent(this, IntroActivity.class);
            }
            startService(new Intent(this, BackgroundService.class));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }
}