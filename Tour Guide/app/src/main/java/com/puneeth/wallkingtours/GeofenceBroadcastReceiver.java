package com.puneeth.wallkingtours;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.puneeth.wallkingtours.databinding.ActivityBuildingBinding;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "GeofenceBroadcastRcvr";
    private static final String CHANNEL_ID = "FENCE_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);



        if (geofencingEvent == null) {
            Log.d(TAG, "onReceive: NULL GeofencingEvent received");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER){
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                if (triggeringGeofences != null) {
                    for (Geofence g : triggeringGeofences) {
                        FenceData fd = MapsActivity.getFenceData(g.getRequestId());
                        sendNotification(context, fd, geofenceTransition);
                    }
                }
            }

        }
    }

    public void sendNotification(Context context, FenceData fenceData, int transitionType) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;
/*
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {

            Uri soundUri = Uri.parse("android.resource://" +
                    context.getPackageName() + "/" +
                    R.raw.notif_sound);
            AudioAttributes att = new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_NOTIFICATION).build();

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            mChannel.setSound(soundUri, att);
            mChannel.setLightColor(Color.RED);
            mChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            mChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(mChannel);

        }*/

        ////
//        String transitionString = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
//                ? "Welcome!" : "Goodbye!";

        Intent resultIntent = new Intent(context.getApplicationContext(), BuildingActivity.class);
        resultIntent.putExtra("FENCE", fenceData.id);
        Log.d(TAG, "sendNotification: "+fenceData.id);

        PendingIntent pi = PendingIntent.getActivity(
                context.getApplicationContext(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.fence_notif)
                .setContentTitle(fenceData.id+ " (Tap to See Details" ) // Bold title
                .setContentText(fenceData.address) // Detail info
                .setAutoCancel(true)
                .build();

        notificationManager.notify(getUniqueId(), notification);
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }

}