package com.example.geotracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocationReminder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double latitude;
    public double longitude;

}
