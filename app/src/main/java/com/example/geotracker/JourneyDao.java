package com.example.geotracker;

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

    @Insert
    void insertJourneyImage(JourneyImage journeyImage);



    @Query("SELECT * FROM journies")
    List<Journey> getAllJournies();


    @Query("SELECT * FROM journies WHERE id = :journeyId")
    Journey getJourneyById(int journeyId);

    @Query("SELECT * FROM location_points WHERE  journeyId = :journeyId")
    List<LocationPoint> getLocationPointsForJourney(int journeyId);

    @Query("SELECT * FROM JourneyImage WHERE journeyId = :journeyId")
    List<JourneyImage> getJourneyImagesForJourney(int journeyId);


}
