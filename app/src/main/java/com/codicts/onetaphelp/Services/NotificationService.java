package com.codicts.onetaphelp.Services;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.codicts.onetaphelp.MainActivity;
import com.codicts.onetaphelp.MarkMeSafe;
import com.codicts.onetaphelp.R;
import com.codicts.onetaphelp.ui.EmergencyContacts.EmergencyContacts;
import com.google.firebase.messaging.RemoteMessage;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NotificationService extends com.google.firebase.messaging.FirebaseMessagingService {
    int notification_id;
    public NotificationService() {}

    @Override
    public void onNewToken(@NonNull String token) {
        storeToken(token);
    }

    private void storeToken(String token){
        SharedPreferences preferences = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String channel_id = remoteMessage.getNotification().getChannelId();
        if(channel_id.equals("MarkMeSafe")){
            showNotificationForMarkMeSafe(remoteMessage);
        }
        else {
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), channel_id);
        }
    }

    private void showNotificationForMarkMeSafe(RemoteMessage remoteMessage) {
        Log.e(TAG, "showNotificationForMarkMeSafe: ");
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //for android O or greater
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel("MarkMeSafe",
                    "MarkMeSafe",
                    NotificationManager.IMPORTANCE_MAX);

            notificationChannel.setDescription("MarkMeSafe Notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Intent safe = new Intent(this, MarkMeSafe.class);
        safe.putExtra("safe", 1);
        safe.setAction("markMsafe");
        PendingIntent pendingSafe = PendingIntent.getService(this, 1, safe, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notSafe = new Intent(this, MarkMeSafe.class);
        notSafe.putExtra("safe", 0);
        notSafe.setAction("markMeUnafe");
        PendingIntent pendingNotSafe = PendingIntent.getService(this, 1, notSafe, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notifyIntent = new Intent(this, EmergencyContacts.class);
        notifyIntent.putExtra("action", "markMeSafe");
        notifyIntent.putExtra("event_id", remoteMessage.getData().get("event_id"));
        notifyIntent.putExtra("event_name", remoteMessage.getData().get("name"));
        notifyIntent.setAction("markMeSafe");

        notifyIntent.setAction(Intent.ACTION_MAIN);
        notifyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MarkMeSafe")
                .setWhen(0)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.ic_check_black_24dp, "Safe", pendingSafe)
                .addAction(R.drawable.ic_close_black_24dp, "Not Safe", pendingNotSafe)
                .setContentText(body)
                .setContentIntent(notifyPendingIntent);

        notificationManager.notify(3, builder.build());
    }

    private void showNotification(String title, String body, String channel_id) {
        Log.e("channel id*****", channel_id);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //for android O or greater
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel("SOS_Notifications",
                    "SOS Notifications",
                    NotificationManager.IMPORTANCE_MAX);

            notificationChannel.setDescription("SOS, volunteer and Disaster Updates Notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

            NotificationChannel notificationChannel2 = new NotificationChannel("Upcoming_Disasters",
                    "Upcoming Disaster Notifications",
                    NotificationManager.IMPORTANCE_MAX);

            notificationChannel.setDescription("Upcoming Disaster Updates Notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel2);
        }
        SharedPreferences preferences;
        String key = "com.codicts.onetaphelp";
        preferences = this.getSharedPreferences(key, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("title", title);
        editor.putString("body", body);
        editor.putString("channel_id", channel_id);
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        if(channel_id.equals("Signal_Accepted")){
            intent.putExtra("title", title);
            intent.putExtra("body", body);
            intent.putExtra("channel_id", channel_id);
            notification_id = 1;
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        else if(channel_id.equals("Disaster_Notification")){
            intent.putExtra("title", title);
            intent.putExtra("body", body);
            intent.putExtra("channel_id", channel_id);
            notification_id = 2;
        }
        else if(channel_id.equals("Nearby_Volunteer")){
            intent.putExtra("channel_id", channel_id);
            notification_id = 4;
        }

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SOS_NOTIFICATIONS")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setContentIntent(pendingIntent)
                .setContentText(body);

        notificationManager.notify(notification_id, builder.build());

    }
}