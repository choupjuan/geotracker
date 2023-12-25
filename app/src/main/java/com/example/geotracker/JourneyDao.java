package com.example.geotracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JourneyDao {
    @Insert
    void insert(Journey journey);

    @Query("SELECT * FROM journies")
    List<Journey> getAllJournies();

    // Additional queries as needed
}
