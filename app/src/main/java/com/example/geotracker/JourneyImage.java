package com.example.geotracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class JourneyImage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="journeyId")
    public int journeyId;

    public String imageUri;
}
