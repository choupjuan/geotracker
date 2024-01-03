package com.example.geotracker;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Journey.class, LocationPoint.class,JourneyImage.class,LocationReminder.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract JourneyDao journeyDao();


    public abstract JourneyImageDao journeyImageDao();

    public abstract LocationReminderDao locationReminderDao();

    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "journey-database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}