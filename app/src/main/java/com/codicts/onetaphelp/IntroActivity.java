package com.codicts.onetaphelp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends OnboarderActivity {
    List<OnboarderPage> introPages;
    SharedPreferences preferences;
    public static final int page1Color = Color.rgb(0,96,100);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);

        introPages = new ArrayList<OnboarderPage>();

        // Pages

        OnboarderPage introPage1 = new OnboarderPage("How it works ?", "When you Send a Signal, it has your location and Pictures of surroundings. The Server alerts the Disaster Management Station nearest to you.");
        OnboarderPage introPage2 = new OnboarderPage("Emergency Contacts", "The Signal is sent via a Text Message to the Emergency Contacts selected by you. So that your close ones can know that you need help.", R.drawable.ic_family);
        OnboarderPage introPage3 = new OnboarderPage("Volunteers", "Volunteers nearby you are also alerted. More helpers = Fast help. Volunteers also receive Reward Points upon helping someone.", R.drawable.ic_volunteer);
        OnboarderPage introPage4 = new OnboarderPage("Disaster Updates", "Whenever there is a Disaster around your area, you will automatically receive an alert notification so that you stay safe.", R.drawable.ic_alert);
        OnboarderPage introPage5 = new OnboarderPage("How to seek help ?", "You can ask for help by the following three ways: \n 1) Pressing the Power button 3 Times Consecutively.\n 2) Press the SOS button in the App  \n 3) Calling 0101 in from phone.", R.drawable.ic_help);


        introPage1.setBackgroundColor(R.color.introPage1);
        introPage2.setBackgroundColor(R.color.introPage2);
        introPage3.setBackgroundColor(R.color.introPage3);
        introPage4.setBackgroundColor(R.color.introPage4);
        introPage5.setBackgroundColor(R.color.introPage5);

        introPages.add(introPage1);
        introPages.add(introPage2);
        introPages.add(introPage3);
        introPages.add(introPage4);
        introPages.add(introPage5);

        this.setSkipButtonHidden();
        this.shouldDarkenButtonsLayout(true);
        this.setInactiveIndicatorColor(android.R.color.darker_gray);

        setOnboardPagesReady(introPages);
    }

    @Override
    public void onFinishButtonPressed() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("introDone", true);
        editor.commit();
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}