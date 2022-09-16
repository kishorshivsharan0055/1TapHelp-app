package com.codicts.onetaphelp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codicts.onetaphelp.Services.BackgroundFrontImage;
import com.codicts.onetaphelp.BuildConfig;
import com.codicts.onetaphelp.Utils.PictureCapturingListener;
import com.codicts.onetaphelp.Utils.BottomSheetActions;
import com.codicts.onetaphelp.Utils.createChecksum;
import com.codicts.onetaphelp.Models.DM_Station;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.Adapters.StationListAdapter;
import com.codicts.onetaphelp.Services.captureBackgroundImage;
import com.codicts.onetaphelp.Services.PowerButtonReceiver;
import com.codicts.onetaphelp.Services.APictureCapturingService;
import com.codicts.onetaphelp.Services.PictureCapturingServiceImpl;
import com.codicts.onetaphelp.ui.MarkMeSafeBottomsheetFragment;
import com.codicts.onetaphelp.ui.StationTracking;
import com.codicts.onetaphelp.ui.VolunteerBottomSheet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kinda.alert.KAlertDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Color.argb;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class HomeFragment extends Fragment implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback, BottomSheetActions {
    private static final int APP_PERMISSION_REQUEST = 1;
    ExtendedFloatingActionButton btnAlert;
    Button btnCapture;
    MaterialCardView cardSignalSent, cardSignalAccepted;
    LinearLayout capturedImages;
    TextView signalReceiversTitle, acceptingSignalName, acceptingSignalDistance;

    //capturing image
    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;
    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private APictureCapturingService pictureService;
    private LocationManager locationManager;
    private FirebaseAuth mAuth;
    String lat, log;
    ConnectivityManager cm;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    String url;
    private String storeCount = "com.codicts.onetaphelp";

    public int count;
    private String[] numbers = new String[10];
    String[] names = new String[10];
    String keyNumbers = "number";
    String keyName = "name";
    String user_id;
    String channel_id = null, title, body;
    int notification_count = 0;
    String keyTitle = "title";
    String keyBody = "body";
    String keyDate = "date";
    private Handler handler;
    private int LOCATION_PERMISSION;
    String token;
    String str_frontImage, str_backImage;
    Bitmap frontImage = null, backImage = null, third_BackImage = null;
    public static final int RequestPermissionCode = 1;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    private RecyclerView stationsList;
    private RecyclerView.Adapter stationsListAdapter;
    private RecyclerView.LayoutManager listLayoutManager;

    MaterialButtonToggleGroup signalTypeToggles;
    Button selectedSignalType;
    String signalType;


    Button acceptedStationCall;
    Button acceptedStationTrack;
    JSONArray stationData;
    private ArrayList<DM_Station> stationArrayList = new ArrayList<>();
    String SHOWCASE_ID = "some_random_id";
    PowerButtonReceiver myRec;
    private MarkMeSafeBottomsheetFragment markSafeSheet;
    private VolunteerBottomSheet volunteerSheet;
    private BottomSheetActions bottomSheetActionsCallback;

    @SuppressLint({"WrongConstant", "MissingPermission"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        preferences = requireContext().getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        editor = preferences.edit();

        ShowcaseConfig showcaseConfig = new ShowcaseConfig();
        showcaseConfig.setDelay(500);

        MaterialShowcaseSequence showcaseSequence = new MaterialShowcaseSequence(requireActivity(), SHOWCASE_ID);

        showcaseSequence.setConfig(showcaseConfig);

        showcaseSequence.addSequenceItem(root.findViewById(R.id.fab_sendSignal), "Click this button whenever you need help.", "GOT IT");

        showcaseSequence.addSequenceItem(root.findViewById(R.id.btn_captureImage), "Click this button when someone else needs help.", "GOT IT");

        showcaseSequence.start();

        acceptedStationCall = root.findViewById(R.id.signalAccepted_Callbtn);
        acceptedStationTrack = root.findViewById(R.id.signalAccepted_Trackbtn);

        preferences = getContext().getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        url = BuildConfig.SERVER_URL + "api/user/verify";
        getFirebaseId();

        bottomSheetActionsCallback = this;

        myRec = new PowerButtonReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.");
//        registerReceiver(myRec, intentFilter);

        getNotifications();

        uploadBackPhoto = (ImageView) root.findViewById(R.id.imgview);
        uploadFrontPhoto = (ImageView) root.findViewById(R.id.imgview2);
        pictureService = PictureCapturingServiceImpl.getInstance(getActivity());
        mAuth = FirebaseAuth.getInstance();
        try {
            user_id = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            Log.e("HomeFragment", "onCreateView: " + e);
        }
        EnableRuntimePermissionToAccessCamera();


        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }

        //check whether location service is not or not
        checkLocationIsEnabled();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, new MyLocationListener());
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, new MyLocationListener());

        getLocation();
        btnAlert = root.findViewById(R.id.fab_sendSignal);
        btnCapture = root.findViewById(R.id.btn_captureImage);

        signalTypeToggles = root.findViewById(R.id.toggleButton_signalType);

        capturedImages = (LinearLayout) root.findViewById(R.id.capturedImages);

        signalReceiversTitle = (TextView) root.findViewById(R.id.signalReceiversTitle);
        cardSignalSent = (MaterialCardView) root.findViewById(R.id.card_sent);
        cardSignalAccepted =  (MaterialCardView) root.findViewById(R.id.card_accepted);

        acceptingSignalName = (TextView) root.findViewById(R.id.card_accepted_text_name);
        acceptingSignalDistance = (TextView) root.findViewById(R.id.card_accepted_text_distance);

        stationsList = (RecyclerView) root.findViewById(R.id.receivingStationsList);

        listLayoutManager = new LinearLayoutManager(getActivity());
        stationsList.setLayoutManager(listLayoutManager);
        stationsList.setHasFixedSize(true);
        stationsListAdapter = new StationListAdapter(stationArrayList, getActivity(), getContext(), getParentFragmentManager(), true);
        stationsList.setAdapter(stationsListAdapter);

        handler = new Handler();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel("MyNotifications", "MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
        getcontacts();

        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendSignalInitialize();
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent1, 0);
                capturedImages.setVisibility(View.VISIBLE);

            }
        });

        signalTypeToggles.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                selectedSignalType = root.findViewById(group.getCheckedButtonId());
                if (selectedSignalType.getText().equals("Ambulance")) {
                    signalType = "Ambulance";
                } else if (selectedSignalType.getText().equals("Fire")) {
                    signalType = "Fire";
                } else if (selectedSignalType.getText().equals("Police")) {
                    signalType = "Police";
                }
                sendSignalInitialize();
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        channel_id = getActivity().getIntent().getStringExtra("channel_id");
        if(channel_id != null){
            Log.e("oncreate  ", channel_id);
            getNotifications();
        }

        if (checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        }
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            check_user();
        }

        Bundle arguments = getArguments();
        if (arguments != null) {
            String NotifType = arguments.getString("NotifType");
            if (NotifType != null) {
                if (NotifType.equals("MarkMeSafe")) {
                    String eventID = arguments.getString("event_id");
                    String eventName = arguments.getString("name");
                    markSafeSheet = new MarkMeSafeBottomsheetFragment(eventID.toString(), eventName, bottomSheetActionsCallback,getContext(), getActivity());
                    markSafeSheet.show(getParentFragmentManager(), getTag());
                } else if (NotifType.equals("Nearby_Volunteer")) {
                    String name = arguments.getString("name");
                    String phno = arguments.getString("phno");
                    String lat = arguments.getString("lat");
                    String lng = arguments.getString("long");
                    String img = arguments.getString("image");
                    volunteerSheet = new VolunteerBottomSheet(name, phno, lat, lng, img, bottomSheetActionsCallback,getContext(), getActivity());
                    volunteerSheet.show(getParentFragmentManager(), getTag());
                } else if (NotifType.equals("Signal_Accepted") || NotifType.equals("Station_Reached")) {
                    final KAlertDialog dialog = new KAlertDialog(getActivity(), KAlertDialog.SUCCESS_TYPE);
                    String name = arguments.getString("name");
                    String phno = arguments.getString("phno");
                    String station_id = arguments.getString("station_id");
                    dialog.setTitleText("Your signal was accepted by " + name);
                    dialog.setContentText("Help is on the way");

                    String stationLatitude, stationLongitude;
                    stationLatitude = arguments.getString("lat");
                    stationLongitude = arguments.getString("long");
                    acceptedStationTrack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), StationTracking.class);
                            intent.putExtra("latitude", stationLatitude);
                            intent.putExtra("longitude", stationLongitude);
                            getContext().startActivity(intent);
                        }
                    });

                    acceptingSignalName.setText("Your signal was accepted by " + name);
                    acceptedStationCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("tel:" + phno));
                            getContext().startActivity(intent);
                        }
                    });

                    dialog.show();
                    cardSignalSent.setVisibility(View.GONE);
                    cardSignalAccepted.setVisibility(View.VISIBLE);
                }
            }
        }




//        String args = getArguments().getString("action");
//        if (args != null && args.equals("sendSignal")) {
//            Toast.makeText(getContext(), "Sending Signal", Toast.LENGTH_SHORT).show();
//            sendSignalInitialize();
//        }
//
//        if (args != null && args.equals("markMeSafe")) {
//            Toast.makeText(getContext(), "mark", Toast.LENGTH_SHORT).show();
//            String eventID = getArguments().getString("event_id");
//            String eventName = getArguments().getString("event_name");
//
//        }
        return root;
    }

    @Override
    public void primaryAction(){
        Toast.makeText(getContext(), "Sending Signal", Toast.LENGTH_SHORT).show();
        markSafeSheet.dismiss();
        sendSignalInitialize();
    }

    @Override
    public void secondaryAction() {
        if (markSafeSheet != null) {
            markSafeSheet.dismiss();
        } else if (volunteerSheet != null) {
            volunteerSheet.dismiss();
        }
    }


    private void checkLocationIsEnabled() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ignored) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ignored) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(requireActivity())
                    .setMessage("Location is OFF")
                    .setMessage("Please click on Enable to turn ON the location")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();
        }
    }


    private void setListener() {
        pictureService.startCapturing(this);
    }


    private void getBackgroundFrontImage() {
        Intent front_translucent = new Intent(getActivity(), BackgroundFrontImage.class);
        front_translucent.putExtra("Front_Request", true);
        requireActivity().startService(front_translucent);

        Intent back_translucent = new Intent(getActivity(), captureBackgroundImage.class);
        back_translucent.putExtra("Front_Request", true);
        requireActivity().startService(back_translucent);
    }


    @SuppressLint("WrongConstant")
    protected void getLocation() {
        if (checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        }
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            lat = String.valueOf(location.getLatitude());
            log = String.valueOf(location.getLongitude());
        }
    }


    public void sendSignalInitialize(){
        if (checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        }
        if(third_BackImage == null){
            Log.e(TAG, "onClick: third_backimage");
            if(Build.VERSION.SDK_INT <= 23){
                setListener();
            }
            else {
                getBackgroundFrontImage();
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLocation();
                sendSignal();
            }
        }, 6000);
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                lat = String.valueOf(location.getLatitude());
                log = String.valueOf(location.getLongitude());
            } else {
                Toast.makeText(getContext(), "Null Location", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "GPS Turned Off", Toast.LENGTH_LONG).show();
            checkLocationIsEnabled();
        }
    }


    //Requesting runtime permission to access camera
    private void EnableRuntimePermissionToAccessCamera() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))
        {
            Toast.makeText(getActivity(),"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            third_BackImage = (Bitmap)data.getExtras().get("data");
            uploadBackPhoto.setImageBitmap(third_BackImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getcontacts() {
        try {
            if (preferences.contains("count")) {
                count = preferences.getInt("count", 0);
            }

            for (int i = 1; i <= count; i++) {
                numbers[i] = preferences.getString(keyNumbers + i, "1");
                names[i] = preferences.getString(keyName + i, "1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendSignalRequest(JSONObject jsonObject) throws IOException {
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
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                e.printStackTrace();

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

                        String signal_id = responseData.getString("signal_id");
                        editor.putString("signal_id", signal_id);
                        editor.apply();

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
                                Log.e("list", "onResponse: "+ stationArrayList.get(i));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            stationsList.setLayoutManager(listLayoutManager);
//                            stationsListAdapter = new StationListAdapter(stationArrayList, getActivity(), getContext());
//                            stationsList.setAdapter(stationsListAdapter);
                            cardSignalSent.setVisibility(View.VISIBLE);
                            stationsList.setVisibility(View.VISIBLE);
                            signalReceiversTitle.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), "Signal Sent", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }


    private void sendSignal() {
        createChecksum cs = new createChecksum();

        if (lat != null && log != null) {

            String myText = cs.checksumGeneration(lat, log);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", lat);
                jsonObject.put("longitude", log);
                jsonObject.put("user_id", user_id);
                jsonObject.put("checksum", myText);

                if (signalType != null) {
                    jsonObject.put("request_type", signalType);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("SendRequest", "sendSignal Data: " + jsonObject);
            Log.d("SendRequest", "Signal Type: " + signalType);

            url = BuildConfig.SERVER_URL + "SOS";

            cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {

                if(third_BackImage == null){
                    str_frontImage = preferences.getString("front_image", "");
                    str_backImage = preferences.getString("back_image", "");
                }
                else {
                    str_frontImage = preferences.getString("front_image","");
                    str_backImage = getFileToByte(third_BackImage);
                }
                try {
                    jsonObject.put("front_image", str_frontImage);
                    jsonObject.put("back_image", str_backImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Toast.makeText(getContext(), "Sending Signal to Server, Please wait", Toast.LENGTH_SHORT).show();
                    sendSignalRequest(jsonObject);

                    String messageText = "I need help \nThis is current Location\n" + "https://www.google.com/maps/search/?api=1&query=" + lat + "," + log;
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

                    uploadBackPhoto.setImageBitmap(null);
                    uploadFrontPhoto.setImageBitmap(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Please make sure that you are connected to internet", Toast.LENGTH_LONG).show();
                String messageText = "[1TH] {\"user\": \"" + user_id + "\", \"lat\": "+lat+", \"lng\": "+log+"}";
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(BuildConfig.SMS_SERVER_PHNO, null, String.valueOf(Uri.parse(messageText)), null, null );
                Log.d("SigalSent", "sendSignal: Signal sent to SMS Server");
                Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_LONG).show();
                uploadBackPhoto.setImageBitmap(null);
                uploadFrontPhoto.setImageBitmap(null);
            }
        } else {
            Toast.makeText(getContext(), "Null Location Returned", Toast.LENGTH_LONG).show();
        }

        backImage = null;
        frontImage = null;
        third_BackImage = null;
        str_backImage = null;
        str_frontImage = null;
    }

    public String getFileToByte(Bitmap bitmap){
        ByteArrayOutputStream bos = null;
        byte[] bt = null;
        String encodeString = null;
        try{
            bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bt = bos.toByteArray();
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
        }
        return encodeString;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStart() {
        super.onStart();
        //Check if Location Permission is provided

        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION);
        }

    }

    private void getFirebaseId() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(getContext(), "getInstance failed!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        token = Objects.requireNonNull(task.getResult()).getToken();
                        Log.e("Token ", token);
                    }
                });
    }


    protected void check_user(){
        JSONObject jsonObject = new JSONObject();
        url = BuildConfig.SERVER_URL + "api/user/verify";
        final String PhoneNumber1 = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().substring(1);
        try {
            jsonObject.put("phno",PhoneNumber1);
            jsonObject.put("token",token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), JSON);
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
                        editor.putString("points", user.getString("points"));

                        editor.putBoolean("volunteer", user.getBoolean("volunteer"));
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


    private void getNotifications() {
        if (channel_id != null) {
            title = getActivity().getIntent().getStringExtra("title");
            body = getActivity().getIntent().getStringExtra("body");
            Log.e("channel id", channel_id);
            if (channel_id.equals("Signal_Accepted")) {
                final KAlertDialog dialog = new KAlertDialog(getActivity(), KAlertDialog.SUCCESS_TYPE);
                dialog.setTitleText(title);
                dialog.setContentText(body);
                acceptingSignalName.setText(body);
                dialog.show();
                cardSignalSent.setVisibility(View.GONE);
                cardSignalAccepted.setVisibility(View.VISIBLE);
            }
            else if(channel_id.equals("Disaster_Notification")){
                notification_count = preferences.getInt("notification_count", 0);
                notification_count = notification_count + 1;
                editor.putInt("notification_count", notification_count);
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String not_date = df.format(c);
                if(notification_count > 0){
                    editor.putString(keyTitle+notification_count, title);
                    editor.putString(keyBody+notification_count, body);
                    editor.putString(keyDate+notification_count, not_date);
                }
                editor.commit();
                channel_id = null;
            }  else if(channel_id.equals("Mark_Me_Safe")){
                new AlertDialog.Builder(getContext())
                        .setTitle("Mark Me Safe")
                        .setMessage("If you are not safe then click on 'Not Safe' else click on 'Safe'")
                        .setPositiveButton("Safe", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                markMeSafeResponse(true);
                            }
                        })
                        .setNegativeButton("Not Safe", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                markMeSafeResponse(false);
                            }
                        });
            }
            channel_id = null;
        }
    }


    private void markMeSafeResponse(boolean b) {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("safe", b);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(String.valueOf(jsonObject), JSON);
        String api_url = BuildConfig.SERVER_URL + "MMS";
        final Request request = new Request.Builder()
                .url(api_url)
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
    @Override
    public void onCaptureDone(String cameraId, byte[] pictureData) {
        Log.e(TAG, "picture : "+cameraId);
        if (pictureData != null) {
            getActivity().runOnUiThread(() -> {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                if(cameraId.equals("0")){
                    uploadBackPhoto.setImageBitmap(scaled);
                    backImage = scaled;
                }
                else if(cameraId.equals("1")){
                    uploadFrontPhoto.setImageBitmap(scaled);
                    frontImage = scaled;
                }
            });
        }
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            showToast("Done capturing all photos!");
            return;
        }
        showToast("No camera detected!");
    }

    private void showToast(final String text) {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkPermissions();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }
}