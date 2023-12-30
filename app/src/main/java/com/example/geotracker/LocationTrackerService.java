package com.example.geotracker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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

    private final Binder binder = new LocalBinder();

    private Boolean isMoving = false;
    private Location lastLocation;

    private Journey currentJourney;

    private AppDatabase db;

    public class LocalBinder extends Binder {
        LocationTrackerService getService() {
            return LocationTrackerService.this;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
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
                            long time = (location.getTime() - lastLocation.getTime())/1000;

                            float speed = distance / time;

                            if (speed > SPEED_THRESHOLD && time < TIME_THRESHOLD && distance > DISTANCE_THRESHOLD){
                                Log.d("LocationTrackerService", "Speed: " + speed + "m/s, Distance: " + distance + "m, Time: " + time + "s");
                                if(!isMoving){
                                    startJourney(speed);
                                }
                                else{
                                    recordLocation(location);
                                }
                            } else {
                                isMoving = false;
                                endJourney();
                            }
                        }
                        currentLocation.postValue(location);
                        lastLocation = location;
                    }
                }
            }
        };
        startLocationUpdates();

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

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(8000); // 5 seconds
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
