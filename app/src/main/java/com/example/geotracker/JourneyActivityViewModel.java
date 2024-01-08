package com.example.geotracker;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.io.Closeable;
import java.util.List;

public class JourneyActivityViewModel extends AndroidViewModel {
    private JourneyRepository journeyRepository;

    private LiveData<List<Journey>> journeyLiveData;

    public JourneyActivityViewModel(Application application) {
        super(application);
        Log.d("JourneyActivityViewModel", "JourneyActivityViewModel: ");
        journeyRepository = new JourneyRepository(application);
        journeyLiveData = journeyRepository.getAllFromJourneyDao();
    }

    public LiveData<List<Journey>> getJourneyLiveData() {
        return journeyLiveData;
    }

    public void insert(Journey journey) {
        journeyRepository.insertJourney(journey);
    }


}
