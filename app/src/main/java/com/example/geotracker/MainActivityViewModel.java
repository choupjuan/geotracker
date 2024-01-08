package com.example.geotracker;

import android.location.Location;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {


    private LiveData<List<LocationReminder>> locationReminders;
    private MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private JourneyRepository journeyRepository;

    public MainActivityViewModel(android.app.Application application) {
        super(application);
        journeyRepository = new JourneyRepository(application);
        locationReminders = journeyRepository.getAllLocationReminders();
    }

    public MutableLiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        currentLocation.setValue(location);
    }

    public LiveData<List<LocationReminder>> getLocationReminders() {
        return locationReminders;
    }

    public void updateLocation(Location location) {
        currentLocation.postValue(location);
    }

    public void removeMarker(int id){
        journeyRepository.removeMarker(id);
    }

    public void insertLocationReminder(LocationReminder locationReminder, JourneyRepository.Callback<Long> callback) {
        journeyRepository.insertLocationReminder(locationReminder,callback);
    }

}
