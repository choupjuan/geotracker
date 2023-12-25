package com.example.geotracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "journies")
public class Journey {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name; // Name or description of the journey
    public long startTime; // Start time of the journey
    public long endTime; // End time of the journey

    // Constructor, getters and setters
}