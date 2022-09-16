package com.codicts.onetaphelp.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;


public class createChecksum  {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String user_id;
    private String myCheck;

    public String checksumGeneration(String lat, String lng){
        double latitude = Double.valueOf(lat);
        double longitude = Double.valueOf(lng);
        double mix = latitude * longitude;
        String token = getFirebaseId();
        user_id = mAuth.getCurrentUser().getUid();
        String msg = mix + user_id;
        byte[] mytext = msg.getBytes();
        try{
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] data = md.digest(mytext);
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<data.length;i++){
				sb.append(String.format("%02x", data[i]));
			}
			myCheck = sb.toString();
		}
		catch(Exception e){
            e.printStackTrace();
		}
        return myCheck;
    }

    private String getFirebaseId() {
        final String[] token = new String[1];
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    token[0] = task.getResult().getToken();
                    Log.e("Token ", token[0]);
                });
        return token[0];
    }
}
