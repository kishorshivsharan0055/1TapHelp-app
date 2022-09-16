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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;

import com.codicts.onetaphelp.Models.DM_Station;
import com.codicts.onetaphelp.Models.DisasterUpdate;
import com.codicts.onetaphelp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class UpdatesBottomSheetFragment extends BottomSheetDialogFragment {

    DisasterUpdate disaster;
    TextView eventName, eventDistance, eventTime, noImage;
    Button locateEvent;
    ImageView eventImage;
    Context context;
    FragmentActivity activity;
    private int CALL_PERMISSION;

    public UpdatesBottomSheetFragment(DisasterUpdate disaster, Context context, FragmentActivity activity) {
        this.disaster = disaster;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_disasters, container, false);



        eventName = (TextView) view.findViewById(R.id.bottomSheet_eventName);
        eventName.setText(disaster.getDisasterName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM h:mm a");
        Date date = null;
        try {
            date = dateFormat.parse(disaster.getDisasterTime());
            eventTime = (TextView) view.findViewById(R.id.bottomSheet_eventTime);
            eventTime.setText(String.format("Time: %s", outputFormat.format(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        eventDistance = (TextView) view.findViewById(R.id.bottomSheet_eventDistance);
        eventDistance.setText(String.format("Approx. %sKMs Away within %sKMs",disaster.getDistance(), disaster.getRadius()));

        eventImage = view.findViewById(R.id.bottomSheet_eventImage);
        noImage = view.findViewById(R.id.bottomSheet_eventNoImage);
        Picasso.get().load(disaster.getImageUrl()).placeholder(R.drawable.rounded_bottomsheet)
                .into(eventImage,new Callback() {
            @Override
            public void onSuccess() {
            }

                    @Override
                    public void onError(Exception e) {
                        eventImage.setVisibility(View.GONE);
                        noImage.setVisibility(View.VISIBLE);
                    }

        });



        locateEvent = view.findViewById(R.id.bottomSheet_btnLocateEvent);
        locateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:0,0?q="+disaster.getLatitude()+","+disaster.getLongitude()+" (" + disaster.getDisasterName() + ")"));
                context.startActivity(intent);
            }
        });


        return view;
    }


}
