package com.codicts.onetaphelp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.codicts.onetaphelp.LoginActivity;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.Utils.BottomSheetActions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class VolunteerBottomSheet extends BottomSheetDialogFragment {

    TextView userName, userPhno;
    ImageView userImage;
    Button help, cancel, locate;
    String name, phno,latitude, longitude, image;
    Context context;
    FragmentActivity activity;
    private BottomSheetActions callback;

    public VolunteerBottomSheet(String name, String phno, String latitude, String longitude, String image, BottomSheetActions bottomSheetActionsCallback, Context context, FragmentActivity activity) {
        this.name = name;
        this.phno = phno;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        this.activity = activity;
        this.callback = bottomSheetActionsCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_volunteer, container, false);

        userName = (TextView) view.findViewById(R.id.bottomSheet_volunteerUserName);
        userName.setText(String.format("Name :  %s.", name));

        userPhno = (TextView) view.findViewById(R.id.bottomSheet_volunteerUserPhoneNo);
        userPhno.setText(String.format("Phone No. :  %s.", phno));

        help = view.findViewById(R.id.bottomSheet_volunteerbtnHelp);
        locate = view.findViewById(R.id.bottomSheet_volunteerbtnLocation);
        cancel = view.findViewById(R.id.bottomSheet_volunteerbtnCancel);

        userImage = view.findViewById(R.id.bottomSheet_volunteerUserImage);
        if (image == "" || image == null) {
            userImage.setVisibility(View.GONE);
        } else {
            Picasso.get().load(image)
                    .into(userImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            userImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            userImage.setVisibility(View.GONE);
                        }


                    });
        }

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VolunteerMaps.class);
                intent.putExtra("name", name);
                intent.putExtra("image", image);
                intent.putExtra("phno", phno);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                context.startActivity(intent);
                callback.secondaryAction();
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:0,0?q="+name+","+latitude+" (" + longitude + ")"));
                context.startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.secondaryAction();
            }
        });


        return view;
    }


}
