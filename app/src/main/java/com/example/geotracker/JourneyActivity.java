package com.example.geotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class JourneyActivity extends AppCompatActivity {
    private ListView lvJourneys;
    private ArrayAdapter<String> adapter;
    private List<String> journeyDescriptions;

    private List<Journey> journeys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        lvJourneys = findViewById(R.id.listView);
        journeyDescriptions = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, journeyDescriptions);
        lvJourneys.setAdapter(adapter);

        loadJourneys();

        lvJourneys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Journey selectedJourney = journeys.get(position);
                openJourneyDetailsActivity(selectedJourney.getId());
            }
        });
    }

    private void openJourneyDetailsActivity(int id) {
        Intent intent = new Intent(this, JourneyDisplay.class);
        intent.putExtra("journeyId", id);
        startActivity(intent);
    }

    private void loadJourneys() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "Journey-Database").build();
            journeys = db.journeyDao().getAllJournies(); // Assuming you have a method to get all journeys

            List<String> descriptions = new ArrayList<>();
            for (Journey journey : journeys) {
                String description = formatJourney(journey);
                descriptions.add(description);
            }

            runOnUiThread(() -> {
                journeyDescriptions.clear();
                journeyDescriptions.addAll(descriptions);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private String formatJourney(Journey journey) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(new Date(journey.startTime));
        String duration = formatDuration(journey.endTime - journey.startTime);
        return "Date: " + startDate + ", Duration: " + duration;
    }

    private String formatDuration(long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d hours", hours, minutes);
    }
}