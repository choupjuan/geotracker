package com.example.geotracker;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_GEOFENCE_TRIGGERED = "com.geotracker.ACTION_GEOFENCE_TRIGGERED";
    public static final String EXTRA_REQUEST_ID = "com.geotracker.EXTRA_REQUEST_ID";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GeofenceBroadcast", "onReceive");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            // Handle error
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofence that was triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a notification for each geofence
            for (Geofence geofence : triggeringGeofences) {
                sendNotification(context, geofence.getRequestId());
            }
        }
    }



    private void sendNotification(Context context, String requestId) {
        Log.d("GeofenceBroadcast", "sendNotification: " + requestId);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context, "geofence_channel_id")
                .setContentTitle("Geofence Alert")
                .setContentText("Geofence event triggered!")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(5, notification);
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(context,
                    AppDatabase.class, "Journey-Database").build();
            Journey journey = db.journeyDao().getLatestJourney();
            db.journeyDao().incrementGeofenceTriggerCount(journey.getId());
        }).start();
    }
}
