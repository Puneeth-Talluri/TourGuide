package com.puneeth.wallkingtours;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collection;
import java.util.HashMap;

public class GeoFenceService extends Service {

    private static final String TAG = "GeofenceService";
    private Notification notification;
    private final String channelId = "FENCE_CHANNEL";
    private PendingIntent geofencePendingIntent;
    private GeofencingClient geofencingClient;

    @Override
    public void onCreate() {
        super.onCreate();

        geofencingClient = LocationServices.getGeofencingClient(this);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this.getMainExecutor(),
                        aVoid -> Log.d(TAG, "onSuccess: removeGeofences"))
                .addOnFailureListener(this.getMainExecutor(),
                        e -> {
                            e.printStackTrace();
                            Log.d(TAG, "onFailure: removeGeofences");
                            Toast.makeText(this, "Trouble removing existing fences: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

        // Since we need to post a notification, we first create a channel
        createNotificationChannel();

        // Create a notification required when running a foreground service.
        notification = new NotificationCompat.Builder(this, channelId)
                .build();
    }

    private void createNotificationChannel() {
        Uri soundUri = Uri.parse("android.resource://" +
                this.getPackageName() + "/" +
                R.raw.notif_sound);
        AudioAttributes att = new AudioAttributes.Builder().
                setUsage(AudioAttributes.USAGE_NOTIFICATION).build();


        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(channelId, channelId, importance);
        mChannel.setSound(soundUri, att);
        mChannel.setLightColor(Color.YELLOW);
        mChannel.setVibrationPattern(new long[]{0, 300, 100, 300});

        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent);
        // This Toast is just to let you know that the service has started.
        Toast.makeText(this, "SERVICE STARTED", Toast.LENGTH_LONG).show();

        HashMap<String, FenceData> fences =
                (HashMap<String, FenceData>) intent.getSerializableExtra("FENCES");

        if (fences != null)
            makeFences(fences.values());

        // Start the service in the foreground
        startForeground(1, notification);

        // If the service is killed, restart it
        return Service.START_STICKY;
    }

    private void makeFences(Collection<FenceData> fences) {
        for (FenceData fd : fences) {
            Log.d(TAG, "makeFences: " + fd);
            addFence(fd);
        }
    }

    public void addFence(FenceData fd) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(fd.getId())
                .setCircularRegion(
                        Double.parseDouble(fd.getLatitude()),
                        Double.parseDouble(fd.getLongitude()),
                        Float.parseFloat(fd.getRadius()))
                .setTransitionTypes(fd.getType())
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //Fence expires after N millis  -or- Geofence.NEVER_EXPIRE
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = getGeofencePendingIntent();


        geofencingClient
                .addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "onSuccess: addGeofences"))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.d(TAG, "onFailure: addGeofences: " + e.getMessage());
                    Toast.makeText(this, "Trouble adding new fence: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private PendingIntent getGeofencePendingIntent() {

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
       // intent.putExtra("FENCE",fd);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().

        geofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        return geofencePendingIntent;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        Toast.makeText(this, "SERVICE DESTROYED", Toast.LENGTH_LONG).show();

        super.onDestroy();
    }
}