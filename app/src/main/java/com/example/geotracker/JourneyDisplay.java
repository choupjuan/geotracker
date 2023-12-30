package com.example.geotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class JourneyDisplay extends AppCompatActivity {

    private int jid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_display);

        jid = getIntent().getIntExtra("journeyId", -1);
        if (jid == -1) {
            // Error
        }else{
            displayJourneyDetailFragment(jid);
        }
    }

    private void displayJourneyDetailFragment(int journeyId) {
        displayFragment fragment = new displayFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("JOURNEY_ID", journeyId);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2,fragment) // Replace 'fragment_container' with your container ID
                .commit();
    }
}