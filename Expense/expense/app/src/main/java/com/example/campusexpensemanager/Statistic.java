// src/main/java/com/example/campusexpensemanager/Statistic.java
package com.example.campusexpensemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistic extends Fragment {

    private BarChart barChart;
    private EditText startDate, endDate;
    //private Button btnGenerateChart;
    private SharedPreferences sharedPreferences;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        barChart = view.findViewById(R.id.barChart);
        startDate = view.findViewById(R.id.startDate);
        endDate = view.findViewById(R.id.endDate);
        //btnGenerateChart = view.findViewById(R.id.btnGenerateChart);

        // Set default dates
        startDate.setText(getFirstDayOfMonth());
        endDate.setText(getLastDayOfMonth());
        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        //btnGenerateChart.setOnClickListener(v -> generateChart());
        generateChart();

        return view;
    }



    private String getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
    }

    private String getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
    }

    private void generateChart() {
        String start = startDate.getText().toString();
        String end = endDate.getText().toString();

        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            // Handle empty date range
            return;
        }

        long startMillis = convertDateToMillis(start);
        long endMillis = convertDateToMillis(end);

        Log.d("Statistic", "Start millis: " + startMillis + ", End millis: " + endMillis);

        Map<String, Float> categoryExpenseMap = new HashMap<>();
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 6) continue;
                if (!parts[0].equals(userId)) continue;

                long time = Long.parseLong(parts[3]);
                if (time < startMillis || time > endMillis) continue;

                String category = parts[4];
                float amount = Float.parseFloat(parts[2]);

                Log.d("Statistic", "Category: " + category + ", Amount: " + amount);

                categoryExpenseMap.put(category, categoryExpenseMap.getOrDefault(category, 0f) + amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        Log.d("Statistic", "Entries: " + entries.toString());
        Log.d("Statistic", "Labels: " + labels.toString());

        BarDataSet dataSet = new BarDataSet(entries, "Category Expenses");
        BarData barData = new BarData(dataSet);

        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }


    private long convertDateToMillis(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }


    @Override
    public void onResume() {
        super.onResume();
        generateChart();
    }
}
