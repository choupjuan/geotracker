package com.example.geotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatActivity extends AppCompatActivity {
    private float averageSpeed;

    private float dailySpeed;

    private float distance;

    private float averagedistance;

    private StatViewModel statViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
        statViewModel = new ViewModelProvider(this).get(StatViewModel.class);

        statViewModel.getAverageJourneyInfo().observe(this, averageJourneyInfo -> {
            averageSpeed = averageJourneyInfo.getAverageSpeed();
            averagedistance = averageJourneyInfo.getAverageDistance();
            Log.d("StatActivity", "onCreate: " + averageJourneyInfo.totalDistance);
            Log.d("StatActivity", "onCreate: " + averageJourneyInfo.totalTimeInSeconds);
            Log.d("StatActivity", "onCreate: " + averageSpeed);
        });
        statViewModel.getDailyJourneyInfo().observe(this, dayJourneyInfo -> {
            dailySpeed = dayJourneyInfo.getAverageSpeed();
            distance = dayJourneyInfo.totalDistance;
            Log.d("StatActivity", "Daily Distance: " + dayJourneyInfo.totalDistance);
            Log.d("StatActivity", "Daily time: " + dayJourneyInfo.totalTimeInSeconds);
            Log.d("StatActivity", "onCreate: " + dailySpeed);
            constructGraph();
            constructSecondGraph();
        });

    }

    private void constructSecondGraph() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, distance));
        entries.add(new BarEntry(1f, averagedistance));

        BarDataSet dataSet = new BarDataSet(entries, "Average Distance");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Set the colors for the bars

        BarData barData = new BarData(dataSet);
        displayBarChart2(barData);
    }

    private void displayBarChart2(BarData barData) {
        BarChart barChart = findViewById(R.id.barChart1);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Optionally disable the description
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Daily Distance", "Average Distance"}));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(2);
        barChart.getAxisLeft().setAxisMinimum(0f); // Start Y-axis from zero
        barChart.animateY(1000); // Animate the Y-axis
        barChart.invalidate(); // Refresh the chart
    }

    private void constructGraph() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, dailySpeed));
        entries.add(new BarEntry(1f, averageSpeed));

        BarDataSet dataSet = new BarDataSet(entries, "Average Speed");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Set the colors for the bars

        BarData barData = new BarData(dataSet);
        displayBarChart(barData);
    }

    private void displayBarChart(BarData barData) {
        BarChart barChart = findViewById(R.id.barChart);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Optionally disable the description
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Daily Speed", "Average Speed"}));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(2);
        barChart.getAxisLeft().setAxisMinimum(0f); // Start Y-axis from zero
        barChart.animateY(1000); // Animate the Y-axis
        barChart.invalidate(); // Refresh the chart
    }
}