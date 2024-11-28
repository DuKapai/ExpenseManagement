package com.example.campusexpensemanager.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Database.DAO.ExpenseDAO;
import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class Home extends Fragment {

    private TextView tvTotalSpent, tvCurrentMonthExpenses;
    private LinearLayout recentExpensesLayout;
    private BarChart barChart;
    private String email;
    private ExpenseDAO expenseDAO;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvCurrentMonthExpenses = view.findViewById(R.id.tvCurrentMonthExpenses);
        recentExpensesLayout = view.findViewById(R.id.recentExpensesLayout);
        barChart = view.findViewById(R.id.barChart);

        // Initialize SharedPreferences to get user email
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("USER_ID", null);

        // Initialize DAO
        expenseDAO = new ExpenseDAO(requireContext());

        // Load data
        loadExpenseData();
        generateChart();

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void loadExpenseData() {
        if (email == null) return;

        // Fetch expenses for the logged-in user
        List<Expense> expenses = expenseDAO.getExpensesByEmail(email);

        long totalSpent = 0;
        int currentMonthExpenses = 0;
        Calendar currentDate = Calendar.getInstance();

        // Define date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Expense expense : expenses) {
            totalSpent += expense.getAmount();

            // Parse expense date
            try {
                Date expenseDate = dateFormat.parse(expense.getDateTime());
                if (expenseDate != null) {
                    Calendar expenseCalendar = Calendar.getInstance();
                    expenseCalendar.setTime(expenseDate);

                    // Check if expense is in the current month
                    if (expenseCalendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                            expenseCalendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
                        currentMonthExpenses++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log parsing errors for debugging
            }
        }

        // Format and display data
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedAmount = numberFormat.format(totalSpent);

        tvTotalSpent.setText("Total Spent: " + formattedAmount + " VND");
        tvCurrentMonthExpenses.setText("Expenses This Month: " + currentMonthExpenses);

        showRecentExpenses(expenses);
    }


    public void generateChart() {
        if (email == null) return;

        // Fetch expenses for the logged-in user
        List<Expense> expenses = expenseDAO.getExpensesByEmail(email);
        Map<String, Float> categoryExpenseMap = new HashMap<>();

        for (Expense expense : expenses) {
            String category = expense.getType();
            float amount = (float) expense.getAmount();
            categoryExpenseMap.put(category, categoryExpenseMap.getOrDefault(category, 0f) + amount);
        }

        // Prepare data for the chart
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Float> entry : categoryExpenseMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Configure chart
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
}
