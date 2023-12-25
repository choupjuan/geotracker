package com.example.geotracker;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "location_points",
        foreignKeys = @ForeignKey(entity = Journey.class,
                parentColumns = "id",
                childColumns = "journeyId",
                onDelete = ForeignKey.CASCADE))
public class LocationPoint {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int journeyId; // Reference to the journey
    public double latitude;
    public double longitude;
    public long timestamp; // Timestamp for the location point

    // Constructor, getters and setters
}