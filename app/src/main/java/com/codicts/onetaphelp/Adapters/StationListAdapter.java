package com.codicts.onetaphelp.Adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codicts.onetaphelp.Models.DM_Station;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.ui.StationBottomSheetFragment;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationListViewHolder> {

    Context mContext;
    FragmentActivity activity;
    private ArrayList<DM_Station> stationList;
    FragmentManager fragmentManager;
    Boolean enableTracking;
    String signal_id;

    private int CALL_PERMISSION;

    public class StationListViewHolder extends RecyclerView.ViewHolder {
        public TextView stationName, stationPhNo, distance, latitude, longitude;
        public ImageView callBtnImage;


        public StationListViewHolder(View view) {
            super(view);
            stationName = (TextView) view.findViewById(R.id.stationName);
            stationPhNo = (TextView) view.findViewById(R.id.stationNumber);
            distance = (TextView) view.findViewById(R.id.distance);
            callBtnImage = (ImageView) view.findViewById(R.id.btn_call);
        }
    }


    public StationListAdapter(ArrayList<DM_Station> stationList, FragmentActivity activity, Context context, FragmentManager fragmentManager) {
        this.stationList = stationList;
        this.mContext = context;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
    }

    public StationListAdapter(ArrayList<DM_Station> stationList, FragmentActivity activity, Context context, FragmentManager fragmentManager, Boolean tracking) {
        this.stationList = stationList;
        this.mContext = context;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.enableTracking = tracking;
    }

    @NonNull
    @Override
    public StationListAdapter.StationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_item, parent, false);
        return new StationListViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull StationListAdapter.StationListViewHolder holder, int position) {
        final DM_Station currentStation = stationList.get(position);
        holder.stationName.setText(currentStation.getStationName());
        holder.stationPhNo.setText(currentStation.getStationPhone());
        holder.distance.setText(String.format("Approx %sKMs", currentStation.getDistance()));
        holder.callBtnImage.setImageResource(R.drawable.ic_phone_black_24dp);
        holder.callBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + currentStation.getStationPhone()));
                    if (checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PermissionChecker.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION);
                    } else {
                        mContext.startActivity(intent);
                    }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StationBottomSheetFragment stationSheet = new StationBottomSheetFragment(stationList.get(position), mContext, activity, enableTracking);
                stationSheet.show(fragmentManager, stationSheet.getTag());
            }
        });

    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }
}
