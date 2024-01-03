package com.example.geotracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        Intent intent = new Intent(ACTION_GEOFENCE_TRIGGERED);
        intent.putExtra(EXTRA_REQUEST_ID, requestId);
        context.sendBroadcast(intent);
    }
}
