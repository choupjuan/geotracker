package com.example.geotracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JourneyDao {
    @Insert
    long insert(Journey journey);

    @Update
    void update(Journey journey);

    @Insert
    void insertLocationPoint(LocationPoint locationPoint);

    @Query("SELECT strftime('%Y-%m-%d', MAX(startTime) / 1000, 'unixepoch', 'localtime') as latestDate FROM journies")
    LiveData<String> getLatestDate();

    @Query("UPDATE journies SET geofenceTriggerCount = geofenceTriggerCount + 1 WHERE id = :journeyId")
    void incrementGeofenceTriggerCount(int journeyId);

    @Query("SELECT strftime('%Y-%m-%d', startTime / 1000, 'unixepoch') AS date, " +
            "SUM(distance) AS totalDistance, " +

            "SUM(endTime - startTime) AS totalTimeInSeconds FROM journies GROUP BY date ORDER BY date DESC")
    LiveData<List<DayJourneyInfo>> getDailyJourneyInfo();


    @Query("SELECT SUM(distance) AS totalDistance, " +
            "SUM(endTime - startTime) AS totalTimeInSeconds FROM journies WHERE strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')= :date")
    LiveData<DayJourneyInfo> getDailyJourneyInfoForDay(String date);
    @Query("SELECT * FROM journies")
    LiveData<List<Journey>> getAllJournies();

    @Query("SELECT SUM(distance) AS totalDistance, " +
            "COUNT(id) as totalJourneys," +
            "SUM(endTime - startTime) AS totalTimeInSeconds FROM journies")
    LiveData<AverageJourneyInfo> getAverageJourneyInfo();

    @Query("SELECT * FROM journies WHERE id = :journeyId")
    LiveData<Journey> getJourneyById(int journeyId);

    @Query("SELECT * FROM location_points WHERE  journeyId = :journeyId")
    LiveData<List<LocationPoint>> getLocationPointsForJourney(int journeyId);

    @Query("SELECT * FROM JourneyImage WHERE journeyId = :journeyId")
    LiveData<List<JourneyImage>> getJourneyImagesForJourney(int journeyId);

    @Query("SELECT * FROM journies ORDER BY startTime DESC LIMIT 1")
    Journey getLatestJourney();

}

class DayJourneyInfo {
    public float totalDistance;
    public long totalTimeInSeconds; // Duration in seconds

    public float getAverageSpeed() {
        return totalDistance / totalTimeInSeconds;
    }
}

class AverageJourneyInfo {
    public float totalDistance;

    public int totalJourneys;
    public long totalTimeInSeconds; // Duration in seconds

    public float getAverageSpeed() {
        return totalDistance / totalTimeInSeconds;
    }

    public float getAverageDistance() {
        return totalDistance/totalJourneys;
    }
}
