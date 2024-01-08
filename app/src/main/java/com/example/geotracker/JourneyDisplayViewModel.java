package com.example.geotracker;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Objects;

public class JourneyDisplayViewModel extends AndroidViewModel {

    private JourneyRepository journeyRepository;

    private LiveData<Journey> journey;

    private int journeyId;

    private LiveData<List<LocationPoint>> locationPointLiveData;

    public LiveData<List<JourneyImage>> journeyImageLiveData;

    private boolean isDataReady = false;

    public JourneyDisplayViewModel(Application application) {
        super(application);
        journeyRepository = new JourneyRepository(application);
    }

    public void fetchLocationPointsForJourney(int journeyId) {
        locationPointLiveData = journeyRepository.getLocationPointsForJourney(journeyId);
        Log.d("JourneyDisplayViewModel", "fetchLocationPointsForJourney: " + locationPointLiveData.toString());
    }

    public LiveData<List<LocationPoint>> getLocationPointLiveData() {
        return locationPointLiveData;
    }

    public LiveData<List<JourneyImage>> getJourneyImageLiveData() {
        journeyImageLiveData = journeyRepository.getJourneyImagesForJourney(journeyId);
        return journeyImageLiveData;
    }

    public LiveData<Journey> getJourney() {
        return journey;
    }

    public void fetchJourney(int journeyId) {
        this.journeyId = journeyId;
        journey = journeyRepository.getJourneyById(journeyId);
    }
    public void update(Journey journey) {
        Log.d("JourneyDisplayViewModel", "update: " + journey.toString());
        journeyRepository.updateJourney(journey);
    }

    public void insertPicture(JourneyImage journeyImage) {
        journeyRepository.insertJourneyImage(journeyImage);
    }

    public void setIsDataReady() {
        isDataReady = true;
    }

    public boolean isDataReady() {
        return isDataReady;
    }


    public void test() {
        return;
    }
}
