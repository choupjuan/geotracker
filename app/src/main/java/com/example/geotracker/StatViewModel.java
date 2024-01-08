package com.example.geotracker;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class StatViewModel extends AndroidViewModel {
    private JourneyRepository journeyRepository;

        public StatViewModel(Application application) {
            super(application);
            journeyRepository = new JourneyRepository(application);
        }



        public LiveData<AverageJourneyInfo> getAverageJourneyInfo() {
            return journeyRepository.getAverageJourneyInfo();
        }

        public LiveData<DayJourneyInfo> getDailyJourneyInfo() {
            return journeyRepository.getDailyJourneyInfo();
        }

}
