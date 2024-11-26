package com.example.campusexpensemanager.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.R;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home extends Fragment implements ExpenseTracker.ExpenseUpdateListener {

    private TextView tvTotalSpent, tvCurrentMonthExpenses;
    private LinearLayout recentExpensesLayout;
    private SharedPreferences sharedPreferences;
    private BarChart barChart;
    private String userId;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String fragment = getArguments().getString("fragmentTag");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvCurrentMonthExpenses = view.findViewById(R.id.tvCurrentMonthExpenses);
        recentExpensesLayout = view.findViewById(R.id.recentExpensesLayout);
        barChart = view.findViewById(R.id.barChart);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        loadExpenseData();
        generateChart();  // Generate the chart on load

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void loadExpenseData() {

        try (FileInputStream fis = requireActivity().openFileInput("expenseData.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

            String line;
            List<Expense> expenses = new ArrayList<>();
            long totalSpent = 0;
            int currentMonthExpenses = 0;

            while ((line = reader.readLine()) != null) {
                Expense expense = Expense.fromString(line);
                if (expense.getUserId().equals(userId)) {
                    expenses.add(expense);
                    totalSpent += expense.getAmount();

                    // Check if expense is in the current month
                    Calendar expenseDate = Calendar.getInstance();

                    expenseDate.setTimeInMillis(Long.parseLong(expense.getDateTime()));
                    Calendar currentDate = Calendar.getInstance();
                    if (expenseDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                            expenseDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
                        currentMonthExpenses++;
                    }
                }
            }
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = numberFormat.format(totalSpent);
            tvTotalSpent.setText("Total Spent: " + formattedAmount + " VND");
            tvCurrentMonthExpenses.setText("Expenses This Month: " + currentMonthExpenses);
            showRecentExpenses(expenses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateChart() {
        Map<String, Float> categoryExpenseMap = new HashMap<>();
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 6) continue;
                if (!parts[0].equals(userId)) continue;
                String category = parts[4];
                float amount = Float.parseFloat(parts[2]);
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

    @SuppressLint("SetTextI18n")
    private void showRecentExpenses(List<Expense> expenses) {
        recentExpensesLayout.removeAllViews();

        int count = 0;
        for (int i = expenses.size() - 1; i >= 0 && count < 20; i--) {
            Expense expense = expenses.get(i);
            TextView expenseItem = new TextView(getContext());

            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = numberFormat.format(expense.getAmount());

            expenseItem.setText(expense.getName() + ": " + formattedAmount + " VND");
            expenseItem.setTextSize(16);
            expenseItem.setTextColor(getResources().getColor(R.color.text_secondary));
            recentExpensesLayout.addView(expenseItem);
            count++;
        }

    }

    @Override
    public void onExpenseUpdated() {
        loadExpenseData();
        generateChart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenseData();
        generateChart();
    }
}
