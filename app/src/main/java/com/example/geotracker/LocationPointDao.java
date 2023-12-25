package com.example.geotracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationPointDao {
    @Insert
    void insert(LocationPoint locationPoint);

    @Query("SELECT * FROM location_points WHERE journeyId = :journeyId")
    List<LocationPoint> getPointsForJourney(int journeyId);

    // Additional queries as needed
}