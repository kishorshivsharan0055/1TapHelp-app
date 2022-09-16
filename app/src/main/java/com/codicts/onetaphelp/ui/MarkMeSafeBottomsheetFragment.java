package com.codicts.onetaphelp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.Utils.BottomSheetActions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MarkMeSafeBottomsheetFragment extends BottomSheetDialogFragment {

    TextView eventName;
    Button markSafe, markUnsafe;
    String event_id, event_name;
    Context context;
    FragmentActivity activity;
    private BottomSheetActions callback;

    public MarkMeSafeBottomsheetFragment(String event_id, String event_name, BottomSheetActions bottomSheetActionsCallback, Context context, FragmentActivity activity) {
        this.event_id = event_id;
        this.event_name = event_name;
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

        View view = inflater.inflate(R.layout.bottom_sheet_markmesafe, container, false);

        eventName = (TextView) view.findViewById(R.id.bottomSheet_markmesafeeventName);
        eventName.setText(String.format("There is %s in your area.", event_name));


        markSafe = view.findViewById(R.id.bottomSheet_btnSafe);
        markUnsafe = view.findViewById(R.id.bottomSheet_btnUnsafe);

        markSafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.secondaryAction();
            }
        });

        markUnsafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.primaryAction();

            }
        });


        return view;
    }


}
