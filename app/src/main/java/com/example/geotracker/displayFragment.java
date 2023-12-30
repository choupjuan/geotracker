package com.example.geotracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Objects;


public class displayFragment extends Fragment implements OnMapReadyCallback {

    private int journeyId;
    private GoogleMap mMap;

    private Journey journey;
    private boolean dataIsReady;
    private List<LocationPoint> locationPoints;

    public displayFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        if(getArguments() != null){
            journeyId = getArguments().getInt("JOURNEY_ID");
            loadDetails();
        }
    }

    private void loadDetails() {
// Load the journey details from the database
        // Display the journey details on the map
        // Display the journey details in the text views
        new Thread(() -> {

            AppDatabase db = Room.databaseBuilder(requireActivity(),
                    AppDatabase.class, "Journey-Database").build();
            journey = db.journeyDao().getJourneyById(journeyId);

            Log.d("Journey", journey.toString());
            List<LocationPoint> locationPoints = db.journeyDao().getLocationPointsForJourney(journeyId);
            Log.d("LocationPoints", locationPoints.toString());
            if(getActivity()!=null){
                getActivity().runOnUiThread(() -> {

                    setLocationPoints(locationPoints);


                });
            }
        }).start();

    }



    private void plotJourneyOnMap() {

        if (mMap != null && locationPoints != null && !locationPoints.isEmpty()) {
            Log.d("Plotting", "Plotting");
            PolylineOptions polylineOptions = new PolylineOptions();
            for (LocationPoint point : locationPoints) {
                polylineOptions.add(new LatLng(point.latitude, point.longitude));
            }
            mMap.addPolyline(polylineOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationPoints.get(0).latitude, locationPoints.get(0).longitude), 15));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank,container,false); // Replace with your fragment's view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    public void setLocationPoints(List<LocationPoint> locationPoints) {
        this.locationPoints = locationPoints;
        if (mMap != null) {
            plotJourneyOnMap();
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (locationPoints != null) {
            plotJourneyOnMap();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}