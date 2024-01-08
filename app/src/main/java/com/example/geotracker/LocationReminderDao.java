package com.example.geotracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationReminderDao {
    @Insert
    long insertReminder(LocationReminder reminder);

    @Query("SELECT * FROM LocationReminder")
    LiveData<List<LocationReminder>> getAllReminders();

    @Query("SELECT * FROM LocationReminder")
    List<LocationReminder> getAllRemindersSync();

    @Query("DELETE FROM LocationReminder WHERE id = :reminderId")
    void deleteById(int reminderId);
}
