package com.codicts.onetaphelp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codicts.onetaphelp.Models.DisasterUpdate;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.ui.StationBottomSheetFragment;
import com.codicts.onetaphelp.ui.UpdatesBottomSheetFragment;

import java.util.ArrayList;

public class DisasterUpdatesAdapter extends RecyclerView.Adapter<DisasterUpdatesAdapter.DisasterUpdatesViewHolder> {

    Context mContext;
    FragmentActivity activity;
    private ArrayList<DisasterUpdate> disasterList;
    FragmentManager fragmentManager;

    public class DisasterUpdatesViewHolder extends RecyclerView.ViewHolder {
        public TextView disasterName, disasterTime, distance, latitude, longitude;

        public DisasterUpdatesViewHolder(View view) {
            super(view);
            disasterName = (TextView) view.findViewById(R.id.eventName);
            disasterTime = (TextView) view.findViewById(R.id.eventTime);
            distance = (TextView) view.findViewById(R.id.eventDistance);
        }
    }

    public DisasterUpdatesAdapter(ArrayList<DisasterUpdate> disasterList, FragmentActivity activity, Context context, FragmentManager fragmentManager) {
        this.disasterList = disasterList;
        this.mContext = context;
        this.activity = activity;
        this.fragmentManager = fragmentManager;

    }

    @NonNull
    @Override
    public DisasterUpdatesAdapter.DisasterUpdatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.disaster_item, parent, false);
        return new DisasterUpdatesViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull DisasterUpdatesAdapter.DisasterUpdatesViewHolder holder, int position) {
        final DisasterUpdate currentDisaster = disasterList.get(position);
        holder.disasterName.setText(currentDisaster.getDisasterName());
        holder.disasterTime.setText(currentDisaster.getDisasterTime());
        holder.distance.setText(String.format("Approx %sKMs within %sKMs", currentDisaster.getDistance(),currentDisaster.getRadius()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdatesBottomSheetFragment disasterSheet = new UpdatesBottomSheetFragment(disasterList.get(position), mContext, activity);
                disasterSheet.show(fragmentManager, disasterSheet.getTag());
            }
        });

    }

    @Override
    public int getItemCount() {
        return disasterList.size();
    }
}
