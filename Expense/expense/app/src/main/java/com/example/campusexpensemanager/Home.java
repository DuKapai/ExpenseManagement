package com.example.campusexpensemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment {

    private TextView tvTotalSpent, tvCurrentMonthExpenses;
    private LinearLayout recentExpensesLayout;
    private SharedPreferences sharedPreferences;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvCurrentMonthExpenses = view.findViewById(R.id.tvCurrentMonthExpenses);
        recentExpensesLayout = view.findViewById(R.id.recentExpensesLayout);

        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        loadExpenseData();

        return view;
    }

    private void loadExpenseData() {
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt");
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

                    // Kiểm tra chi tiêu trong tháng hiện tại
                    Calendar expenseDate = Calendar.getInstance();
                    expenseDate.setTimeInMillis(Long.parseLong(expense.getTime()));
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

    private void showRecentExpenses(List<Expense> expenses) {
        recentExpensesLayout.removeAllViews();

        int count = 0;
        for (int i = expenses.size() - 1; i >= 0 && count < 2; i--) {
            Expense expense = expenses.get(i);
            TextView expenseItem = new TextView(getContext());


            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            String formattedAmount = numberFormat.format(expense.getAmount());

            expenseItem.setText(expense.getName() + ": " + formattedAmount +" VND");
            expenseItem.setTextSize(16);
            expenseItem.setTextColor(getResources().getColor(R.color.text_secondary));
            recentExpensesLayout.addView(expenseItem);
            count++;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        loadExpenseData();
    }
}
