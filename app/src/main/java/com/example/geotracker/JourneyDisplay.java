package com.example.geotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class JourneyDisplay extends AppCompatActivity {

    private int jid;

    private Boolean secondFragment = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    0);
        }
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

    public void onSecondSwitch(View view) {

        Bundle bundle = new Bundle();
        bundle.putInt("JOURNEY_ID", jid);
        imageFragment imageFragment = new imageFragment();
        imageFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2,imageFragment) // Replace 'fragment_container' with your container ID
                .commit();
    }

    public void onFirstSwitch(View view) {
        displayJourneyDetailFragment(jid);
    }
}