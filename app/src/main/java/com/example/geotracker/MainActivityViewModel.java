package com.example.geotracker;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();

    public MutableLiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        currentLocation.setValue(location);
    }

    public void updateLocation(Location location) {
        currentLocation.postValue(location);
    }

}
