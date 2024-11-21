package com.example.campusexpensemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Data.DatabaseHelper;
import com.example.campusexpensemanager.Expense.Expense;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home extends Fragment {

    private TextView tvTotalSpent, tvCurrentMonthExpenses;
    private LinearLayout recentExpensesLayout;
    private BarChart barChart;
    private String userId;
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvCurrentMonthExpenses = view.findViewById(R.id.tvCurrentMonthExpenses);
        recentExpensesLayout = view.findViewById(R.id.recentExpensesLayout);
        barChart = view.findViewById(R.id.barChart);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext());
        databaseHelper.open();

        // Get logged-in userId
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        loadExpenseData();
        generateChart(); // Generate the chart on load

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadExpenseData() {
        try {
            List<Expense> expenses = databaseHelper.getAllExpenses(userId);
            long totalSpent = 0;
            long currentMonthExpenses = 0;

            // Get current month and year
            Calendar currentDate = Calendar.getInstance();
            int currentMonth = currentDate.get(Calendar.MONTH);
            int currentYear = currentDate.get(Calendar.YEAR);

            // Calculate total and monthly expenses
            for (Expense expense : expenses) {
                totalSpent += expense.getAmount();

                Calendar expenseDate = Calendar.getInstance();
                expenseDate.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(expense.getDateTime()));
                if (expenseDate.get(Calendar.YEAR) == currentYear && expenseDate.get(Calendar.MONTH) == currentMonth) {
                    currentMonthExpenses += expense.getAmount();
                }
            }

            // Update UI
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            tvTotalSpent.setText("Total Spent: " + numberFormat.format(totalSpent) + " VND");
            tvCurrentMonthExpenses.setText("Expenses This Month: " + numberFormat.format(currentMonthExpenses) + " VND");

            showRecentExpenses(expenses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void generateChart() {
        try {
            List<Expense> expenses = databaseHelper.getAllExpenses(userId);
            Map<String, Float> categoryExpenseMap = new HashMap<>();

            for (Expense expense : expenses) {
                String category = expense.getCategory();
                float amount = expense.getAmount();
                categoryExpenseMap.put(category, categoryExpenseMap.getOrDefault(category, 0f) + amount);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showRecentExpenses(List<Expense> expenses) {
        recentExpensesLayout.removeAllViews();
        int count = 0;

        for (int i = expenses.size() - 1; i >= 0 && count < 20; i--) {
            Expense expense = expenses.get(i);

            // Inflate custom layout for recent expenses
            View expenseView = LayoutInflater.from(getContext()).inflate(R.layout.item_expense, recentExpensesLayout, false);

            // Bind data to views
            TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
            TextView tvExpenseAmount = expenseView.findViewById(R.id.tvExpenseAmount);
            TextView tvExpenseDate = expenseView.findViewById(R.id.tvExpenseTime);

            tvExpenseName.setText(expense.getName());
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = numberFormat.format(Math.abs(expense.getAmount()));
            tvExpenseAmount.setText((expense.getAmount() < 0 ? "- " : "+ ") + formattedAmount + " VND");
            tvExpenseAmount.setTextColor(expense.getAmount() < 0
                    ? getResources().getColor(R.color.expenseColor)
                    : getResources().getColor(R.color.incomeColor));

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(expense.getDateTime());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tvExpenseDate.setText(outputFormat.format(date));
            } catch (ParseException e) {
                tvExpenseDate.setText(expense.getDateTime());
            }

            // Add the view to the recentExpensesLayout
            recentExpensesLayout.addView(expenseView);
            count++;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        loadExpenseData();
        generateChart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }
}