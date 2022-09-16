package com.codicts.onetaphelp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;

import com.codicts.onetaphelp.Models.DM_Station;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.ui.StationTracking;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class StationBottomSheetFragment extends BottomSheetDialogFragment {

    DM_Station station;
    TextView stationID, stationName, stationPhNo, stationDistance;
    Button callStation, locateStation;
    Context context;
    FragmentActivity activity;
    Boolean tracking;
    private int CALL_PERMISSION;
    String signal_id;

    public StationBottomSheetFragment(DM_Station station, Context context, FragmentActivity activity) {
        this.station = station;
        this.context = context;
        this.activity = activity;
    }

    public StationBottomSheetFragment(DM_Station station, Context context, FragmentActivity activity, Boolean enableTracking) {
        this.station = station;
        this.context = context;
        this.activity = activity;
        this.tracking =enableTracking;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_stations, container, false);

        stationName = (TextView) view.findViewById(R.id.bottomSheet_stationName);
        stationName.setText(station.getStationName());

        stationID = (TextView) view.findViewById(R.id.bottomSheet_stationID);
        stationID.setText(String.format("Station ID : %s", station.getStationID()));

        stationPhNo = (TextView) view.findViewById(R.id.bottomSheet_stationPhNo);
        stationPhNo.setText(String.format("Phone No. : %s", station.getStationPhone()));

        stationDistance = (TextView) view.findViewById(R.id.bottomSheet_stationDistance);
        stationDistance.setText(String.format("Approx. %sKMs Away",station.getDistance()));

        callStation = view.findViewById(R.id.bottomSheet_btnCallStation);
        locateStation = view.findViewById(R.id.bottomSheet_btnLocateStation);

        callStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + station.getStationPhone()));
                if (checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PermissionChecker.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION);
                } else {
                    context.startActivity(intent);
                }
            }
        });
        if (tracking != null && tracking == true) {
            locateStation.setText("Track Station");
            locateStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, StationTracking.class);
                    intent.putExtra("latitude", station.getLatitude());
                    intent.putExtra("longitude", station.getLongitude());
                    context.startActivity(intent);
                }
            });
        } else {
            locateStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:0,0?q=" + station.getLatitude() + "," + station.getLongitude() + " (" + station.getStationName() + ")"));
                    context.startActivity(intent);
                }
            });

        }


        return view;
    }


}
