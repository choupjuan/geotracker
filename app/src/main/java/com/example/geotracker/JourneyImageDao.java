package com.example.geotracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JourneyImageDao {
    @Insert
    void insert(JourneyImage journeyImage);

    @Query("SELECT * FROM JourneyImage WHERE journeyId = :journeyId")
    List<JourneyImage> getImagesForJourney(int journeyId);
}
