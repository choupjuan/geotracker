package com.example.geotracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "journies")
public class Journey {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type;

    public String name; // Name or description of the journey
    public long startTime; // Start time of the journey
    public long endTime; // End time of the journey

    public float distance;

    public int geofenceTriggerCount; // Number of times the geofence was triggered

    public String notes; // Notes about the journey

    public int getId() {
        return id;
    }

    // Constructor, getters and setters
}