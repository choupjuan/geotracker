package com.example.geotracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class DetectedActivitiesService extends JobIntentService {
    public DetectedActivitiesService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getMostProbableActivity());
        }
    }

    private void handleDetectedActivities(DetectedActivity detectedActivity) {
        switch (detectedActivity.getType()) {
            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:
                startCycling();
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
                startRunning();
                break;
            case DetectedActivity.WALKING:
                // These activities imply the user is moving and potentially starting a journey
                startWalking();
                break;
            case DetectedActivity.STILL:
                // This activity implies the user is no longer moving and potentially ending a journey
                endJourney();
                break;
        }
    }

    private void endJourney() {

    }

    private void startCycling() {
    }

    private void startRunning() {
    }

    private void startWalking() {
    }
}