package com.example.geotracker;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Room;

import java.util.List;

public class JourneyRepository {

    private JourneyDao journeyDao;
    private JourneyImageDao journeyImageDao;

    private LocationReminderDao locationReminderDao;
    public JourneyRepository(Application application) {
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "Journey-Database").build();
        journeyDao = db.journeyDao();
        journeyImageDao = db.journeyImageDao();
        locationReminderDao = db.locationReminderDao();
    }

    public LiveData<List<Journey>> getAllFromJourneyDao() {
        return journeyDao.getAllJournies();
    }

    public LiveData<List<LocationPoint>> getLocationPointsForJourney(int journeyId) {
        return journeyDao.getLocationPointsForJourney(journeyId);
    }

    public void removeMarker(int id) {
        new Thread(() -> locationReminderDao.deleteById(id)).start();
    }
    public void insertJourney(Journey journey) {
        new Thread (() -> journeyDao.insert(journey)).start();
    }

    public void insertJourneyImage(JourneyImage journeyImage) {
        new Thread (() -> journeyImageDao.insert(journeyImage)).start();
    }
    public LiveData<Journey> getJourneyById(int journeyId) {
        return journeyDao.getJourneyById(journeyId);
    }

    public LiveData<List<JourneyImage>> getJourneyImagesForJourney(int journeyId) {
        return journeyDao.getJourneyImagesForJourney(journeyId);
    }

    public LiveData<List<LocationReminder>> getAllLocationReminders() {
        return locationReminderDao.getAllReminders();
    }



    public LiveData<String> getLatestDate() {
        return journeyDao.getLatestDate();
    }

    public LiveData<DayJourneyInfo> getDailyJourneyInfo() {
        // This LiveData depends on another LiveData (latest date), so you might need to use MediatorLiveData or Transformations
        // Example using Transformations (you need to adjust according to your app logic)
        return Transformations.switchMap(getLatestDate(), date -> journeyDao.getDailyJourneyInfoForDay(date));
    }

    public LiveData<AverageJourneyInfo> getAverageJourneyInfo() {
        return journeyDao.getAverageJourneyInfo();
    }

    public void updateJourney(Journey journey) {
        new Thread(() -> journeyDao.update(journey)).start();
    }

    public void insertLocationReminder(LocationReminder locationReminder,Callback<Long> callback) {
        new Thread(() ->{
            long rowid = locationReminderDao.insertReminder(locationReminder);
            callback.onResult(rowid);
        }).start();

    }
    public interface Callback<T> {
        void onResult(T result);
    }
}
