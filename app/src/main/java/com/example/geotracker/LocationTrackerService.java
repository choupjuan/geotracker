package com.example.geotracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationTrackerService extends Service {

    private static final float SPEED_THRESHOLD = 1.0f; // e.g., meters/second
    private static final float DISTANCE_THRESHOLD = 11f; // e.g., meters
    private static final long TIME_THRESHOLD = 10000; // e.g., milliseconds

    private static final float CYCLE_THRESHOLD = 6.0f; // e.g., meters/second

    private static final float WALKING_THRESHOLD = 1.0f; // e.g., meters/second

    private static final float RUNNING_THRESHOLD = 3.0f; // e.g., meters/second
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();

    private PendingIntent geofencePendingIntent;

    private final Binder binder = new LocalBinder();

    private Boolean isMoving = false;
    private Location lastLocation;

    private Journey currentJourney;

    private AppDatabase db;

    private GeofencingClient geofencingClient;

    public class LocalBinder extends Binder {
        LocationTrackerService getService() {
            return LocationTrackerService.this;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        geofencingClient = LocationServices.getGeofencingClient(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "Journey-Database").build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult != null) {
                    Log.d("LocationTrackerService", "Location result: " + locationResult.getLocations().toString());
                    for (Location location : locationResult.getLocations()) {
                        if(lastLocation != null) {
                            float distance = location.distanceTo(lastLocation);
                            long time = (location.getTime()/1000) - (lastLocation.getTime()/1000);

                            float speed = distance / time;

                            if (speed > SPEED_THRESHOLD && time < TIME_THRESHOLD && distance > DISTANCE_THRESHOLD){
                                Log.d("LocationTrackerService", "Speed: " + speed + "m/s, Distance: " + distance + "m, Time: " + time + "s");
                                if(!isMoving){
                                    startJourney(speed);
                                    lastLocation = location;
                                }
                                else{
                                    recordLocation(location);
                                    lastLocation = location;
                                }
                            } else {
                                isMoving = false;
                                endJourney();
                                lastLocation = null;
                            }
                        }else{
                            lastLocation = location;
                        }

                        currentLocation.postValue(location);

                    }
                }
            }
        };


    }

    private void recordLocation(Location location) {
        LocationPoint locationPoint = new LocationPoint();
        locationPoint.latitude = location.getLatitude();
        locationPoint.longitude = location.getLongitude();
        locationPoint.timestamp = location.getTime();
        locationPoint.journeyId = currentJourney.id;

        Log.d("LocationTrackerService", "Saving location point");
        new Thread(() -> {
                db.journeyDao().insertLocationPoint(locationPoint);
                currentJourney.endTime = locationPoint.timestamp;
                db.journeyDao().update(currentJourney);
        }).start();
    }

    public void setupGeofences() {
        Log.d("LocationTrackerService", "Setting up geofences");
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "Journey-Database").build();
            List<LocationReminder> reminders = db.locationReminderDao().getAllReminders();

            List<Geofence> geofenceList = createGeofenceList(reminders);
            addGeofencesToClient(geofenceList);
        }).start();
    }

    @SuppressLint("MissingPermission")
    private void addGeofencesToClient(List<Geofence> geofenceList) {
        if (geofenceList.isEmpty()) {
            Log.d("LocationTrackerService", "No geofences to add");
            return; // No geofences to add
        }
        

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);

        geofencingClient.addGeofences(builder.build(), getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return geofencePendingIntent;
    }

    private List<Geofence> createGeofenceList(List<LocationReminder> reminders) {
        List<Geofence> geofenceList = new ArrayList<>();
        for (LocationReminder reminder : reminders) {
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(String.valueOf(reminder.id))
                    .setCircularRegion(
                            reminder.latitude,
                            reminder.longitude,
                            100)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
        return geofenceList;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_ONE_SHOT| PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "stuff")
                .setContentTitle("Location Service")
                .setContentText("Tracking location updates")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        Log.d("LocationTrackerService", "Starting foreground service");
        startForeground(1, notification);

        // Start location updates
        // ...
        startLocationUpdates();
        setupGeofences();

        return START_NOT_STICKY;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "stuff",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            // Handle case where location permissions are not granted
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isMoving = false;
        endJourney();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }



    public void startJourney(float speed) {
        isMoving = true;
        currentJourney = new Journey();
        currentJourney.startTime = System.currentTimeMillis();
        currentJourney.endTime = currentJourney.startTime;
        if(speed > CYCLE_THRESHOLD) {
            currentJourney.type = "Cycle";
        }
        else if(speed > RUNNING_THRESHOLD) {
            currentJourney.type = "Running";
        }
        else{
            currentJourney.type = "Walking";
        }
        new Thread(() -> {
            long journeyId = db.journeyDao().insert(currentJourney);
            currentJourney.id = (int) journeyId;

        }).start();
    }

    public void endJourney() {
        isMoving = false;
    }


    public boolean isMoving() {
        return isMoving;
    }
}
