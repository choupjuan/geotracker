package com.example.geotracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationReminderDao {
    @Insert
    long insertReminder(LocationReminder reminder);

    @Query("SELECT * FROM LocationReminder")
    List<LocationReminder> getAllReminders();

    @Query("DELETE FROM LocationReminder WHERE id = :reminderId")
    void deleteById(int reminderId);
}
