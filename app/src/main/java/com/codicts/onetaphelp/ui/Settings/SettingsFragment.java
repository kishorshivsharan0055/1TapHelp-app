package com.codicts.onetaphelp.ui.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.codicts.onetaphelp.BuildConfig;
import com.codicts.onetaphelp.LoginActivity;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.SignupActivity;
import com.codicts.onetaphelp.ui.EmergencyContacts.EmergencyContacts;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.RECEIVER_VISIBLE_TO_INSTANT_APPS;

public class SettingsFragment extends Fragment {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    LinearLayout logoutButton, contacts, volunteerMode, language;
    Switch volunteerToggle;
    TextView username, phno, points;

    AlertDialog languageDialog;
    boolean isVolunteer;
    String[] languagesList = {"English", "Hindi", "Marathi", "Urdu"};
    int currentLocaleSelection = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        preferences = getContext().getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);

        String lang = preferences.getString("lang", "en");
        switch (lang) {
            case "en":
                currentLocaleSelection = 0;
                break;
            case "hi":
                currentLocaleSelection = 1;
                break;
            case "mr-rIN":
                currentLocaleSelection = 2;
                break;
            case "ur-rIN":
                currentLocaleSelection = 3;
                break;
        }

        logoutButton = (LinearLayout) root.findViewById(R.id.logoutButtonSettings);
        contacts = (LinearLayout) root.findViewById(R.id.emergencyContactsButton);
        language = (LinearLayout) root.findViewById(R.id.languageButtonSettings);
        username = (TextView) root.findViewById(R.id.text_username_settings);
        phno = (TextView) root.findViewById(R.id.text_phno_settings);
        points = (TextView) root.findViewById(R.id.text_points_settings);
        volunteerToggle = (Switch) root.findViewById(R.id.toggle_volunteerMode_settings);
        volunteerMode = (LinearLayout) root.findViewById(R.id.volunteerTextSettings);

        fetchUserDetails();

        username.setText(preferences.getString("name", ""));
        phno.setText(preferences.getString("phno", ""));
        editor = preferences.edit();

        isVolunteer = preferences.getBoolean("volunteer", false);

        contacts.setOnClickListener(v -> {
            Intent emergencyContacts = new Intent(getActivity(), EmergencyContacts.class);
            startActivity(emergencyContacts);
        });

        volunteerMode.setOnClickListener(v -> volunteerToggle.toggle());
        if (isVolunteer) {
            points.setVisibility(View.VISIBLE);
            points.setText(String.format("Points: %s", preferences.getString("points", String.valueOf(0))));
            volunteerToggle.setChecked(isVolunteer);
        }

        volunteerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVolunteer = isChecked;
            JSONObject profileData = new JSONObject();
            try {
                profileData.put("volunteer", isChecked);
                updateProfile(profileData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.putBoolean("volunteer", isChecked);
            editor.apply();
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
        });

language.setOnClickListener(v -> {
    languageDialog = new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Language")
            .setNeutralButton("Cancel", (dialog, which) -> {

            }).setSingleChoiceItems(languagesList, currentLocaleSelection, (dialog, which) -> {

                currentLocaleSelection = which;
                switch (which){
                    case 0:
                        setLocale("en");
                        requireActivity().recreate();
                        break;

                    case 1:
                        setLocale("hi");
                        requireActivity().recreate();
                        break;

                    case 2:
                        setLocale("mr-rIN");
                        requireActivity().recreate();
                        break;

                    case 3:
                        setLocale("ur-rIN");
                        requireActivity().recreate();
                        break;
                }
                languageDialog.dismiss();
            }).show();
});

        return root;

    }


    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("com.codicts.onetaphelp", Context.MODE_PRIVATE).edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    private void fetchUserDetails() {
            JSONObject postData = new JSONObject();
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(String.valueOf(postData), JSON);

            String url = BuildConfig.SERVER_URL + "api/user/verify";
            OkHttpClient client = new OkHttpClient();
            String auth_token = preferences.getString("auth_token", "");
            final Request request = new Request.Builder()
                    .addHeader("Cookie", "auth_token=" + auth_token)
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = response.body().string();
                    if (response.code() != 200) {
                        Log.e("UserError", "onResponse: User Verification Error " + result);
                    } else if (response.code() == 200) {
                        try {
                            JSONObject res = new JSONObject(result);
                            JSONObject user = res.getJSONObject("user");
                            editor.putString("points", user.getString("points"));
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    e.printStackTrace();
                }
            });
    }

    private void updateProfile(JSONObject profileData) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(String.valueOf(profileData), JSON);

        String url = BuildConfig.SERVER_URL + "api/user/update";
        OkHttpClient client = new OkHttpClient();
        String auth_token = preferences.getString("auth_token", "");
        final Request request = new Request.Builder()
                .addHeader("Cookie", "auth_token=" + auth_token)
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                if (response.code() != 200) {
                    Log.e("UserError", "onResponse: User Verification Error " + result);
                } else if (response.code() == 200) {
                    try {
                        JSONObject res = new JSONObject(result);
                        JSONObject user = res.getJSONObject("user");
                        editor.putString("name", user.getString("name"));
                        editor.putString("phno", user.getString("phno"));
                        editor.putBoolean("volunteer", user.getBoolean("volunteer"));
                        editor.putString("points", user.getString("points"));
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();

            }
        });
    }
}