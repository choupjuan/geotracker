package com.example.geotracker;

import static android.app.PendingIntent.getActivity;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;



    private static final int REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    private MainActivityViewModel viewModel;

    private LocationTrackerService locationService;

    private Boolean isBound = false;

    private Boolean moved = false;

    private Polyline journeyLine;

    private PolylineOptions polylineOptions;

    private boolean stillMoving = false;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            startService();
            initMapFragment();
        }




        viewModel.getCurrentLocation().observe(this, location -> {

            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (!moved) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                if(locationService.isMoving()){
                    if(!stillMoving){
                        startnewjourney();
                        stillMoving = true;
                    }else{
                        List<LatLng> points = journeyLine.getPoints();
                        points.add(latLng);
                        journeyLine.setPoints(points);
                    }
                }else{
                    stillMoving = false;
                }

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start your functionality
                startService();
                initMapFragment();
            } else {
                // Permission denied, handle the denial appropriately
                handlePermissionDenial();
            }
        }
    }

    private void handlePermissionDenial() {
        return;
    }


    @SuppressLint("MissingPermission")
    private void startService() {
        Log.d("MainActivity", "Starting service");
        Intent intent1 = new Intent(this, LocationTrackerService.class);
        startService(intent1);

        if (!isBound) {
            Intent intent = new Intent(this, LocationTrackerService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            isBound = true;
        }


    }
    private void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    public void onJournalClick(View view) {
        Intent intent = new Intent(this, JourneyActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MainActivity", "Map ready");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setUpMap();
            Log.d("MainActivity", "Map set up");
        }
        polylineOptions = new PolylineOptions();
        journeyLine = mMap.addPolyline(polylineOptions);
        googleMap.setOnMapLongClickListener(this::onMapLongClick);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                removeFromDatabase(Math.toIntExact((long)marker.getTag()));
                marker.remove();
                
                return true;
            }
        });
        loadRemindersAndShowOnMap();


    }

    private void removeFromDatabase(int id) {
        Log.d("MainActivity", "Removing from database");
        Log.d("MainActivity", String.valueOf(id));
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "Journey-Database").build();
            db.locationReminderDao().deleteById(id);
        }).start();
    }

    private void onMapLongClick(LatLng latLng) {
        addLocationReminder(latLng);
    }

    private void addLocationReminder(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
        LocationReminder reminder = new LocationReminder();


        reminder.latitude = latLng.latitude;
        reminder.longitude = latLng.longitude;


        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "Journey-Database").build();
            long rowId = db.locationReminderDao().insertReminder(reminder);
            runOnUiThread(() -> {
                marker.setTag(rowId);
                locationService.setupGeofences();
            });

        }).start();

    }
    private void startnewjourney() {
        if(journeyLine != null) {
            journeyLine.remove();
        }
        polylineOptions = new PolylineOptions();
        journeyLine = mMap.addPolyline(polylineOptions);
    }



    @SuppressLint("MissingPermission")
    private void setUpMap() {
        // Set up your map-related functionalities here
        mMap.setMyLocationEnabled(true);
        // Additional map setups like markers, listeners, etc.
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    moved = true;
                }
            }
        });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
            LocationTrackerService.LocalBinder binder = (LocationTrackerService.LocalBinder) service;
            locationService = (LocationTrackerService) binder.getService();

            isBound = true;

            locationService.getCurrentLocation().observe(MainActivity.this, location -> {
                if (location != null) {
                    viewModel.setCurrentLocation(location);

                }
            });


        }

        @Override
        public void onServiceDisconnected(android.content.ComponentName name) {
            isBound = false;

        }
    };

    private void loadRemindersAndShowOnMap() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "Journey-Database").build();
            List<LocationReminder> reminders = db.locationReminderDao().getAllReminders();

            // Now that you have your reminders, display them on the map
            runOnUiThread(() -> showRemindersOnMap(reminders));
        }).start();
    }

    private void showRemindersOnMap(List<LocationReminder> reminders) {
        if (mMap != null) {
            for (LocationReminder reminder : reminders) {
                LatLng position = new LatLng(reminder.latitude, reminder.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(position));
                marker.setTag((long)reminder.id);
            }
        }
    }
}