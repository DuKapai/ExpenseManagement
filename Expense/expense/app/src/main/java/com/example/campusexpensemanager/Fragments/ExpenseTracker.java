package com.example.campusexpensemanager.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Database.DAO.ExpenseDAO;
import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.Models.Notification;
import com.example.campusexpensemanager.Entity.Notification.NotificationRecord;
import com.example.campusexpensemanager.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ExpenseTracker extends Fragment {

    private TextView tvTvTotalAmount;
    private LinearLayout expenseListContainer;
    private List<Expense> expenseList;
    private SharedPreferences sharedPreferences;
    private String email;
    private ExpenseUpdateListener expenseUpdateListener;
    private Notification notification;
    private int id;
    private ExpenseDAO expenseDAO;

    private  long spendingLimit;
    private  long totalAmount;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_tracker, container, false);

        tvTvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        expenseListContainer = view.findViewById(R.id.expenseListContainer);

        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("USER_ID", null);
        // lấy giá trị bên profile
        spendingLimit = sharedPreferences.getLong("SPENDING_LIMIT", 0);

        expenseList = new ArrayList<>();
        expenseDAO = new ExpenseDAO(getActivity());

        loadExpenses();
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        notification = new Notification(getActivity());

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ExpenseUpdateListener) {
            expenseUpdateListener = (ExpenseUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ExpenseUpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        expenseUpdateListener = null;
    }

    private void loadExpenses() {
        expenseList = expenseDAO.getExpensesByEmail(email); // Fetch expenses by email
        updateExpenseListView();
        updateTvTotalAmount();
    }

    private void updateTvTotalAmount() {
        totalAmount = 0;
        for (Expense expense : expenseList) {
            totalAmount += expense.getAmount();
        }

        String formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalAmount);
        tvTvTotalAmount.setText(formattedAmount + " VND");
    }

    private String readExpenseDataFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt");
             Scanner scanner = new Scanner(fis)) {
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file.";
        }
        return stringBuilder.toString();
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etSpendingName = view.findViewById(R.id.etSpendingName);
        EditText etSpendingAmount = view.findViewById(R.id.etSpendingAmount);
        Spinner spSpendingCategory = view.findViewById(R.id.etSpendingCategory);
        EditText etSpendingNotes = view.findViewById(R.id.etSpendingNotes);
        RadioGroup rgExpenseType = view.findViewById(R.id.rgExpenseType);

        builder.setView(view)
                .setTitle("Add Expense")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etSpendingName.getText().toString();
                    String amountStr = etSpendingAmount.getText().toString();
                    String category = spSpendingCategory.getSelectedItem().toString();
                    String notes = etSpendingNotes.getText().toString();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String expenseType = (rgExpenseType.getCheckedRadioButtonId() == R.id.rbIncome) ? "Income" : "Expense";
                    amount = expenseType.equals("Expense") ? -amount : amount;

                    Expense expense = new Expense((expenseList.size()), amount, email, name, dateTime, category, notes);
                    boolean success = expenseDAO.insertExpense((expenseList.size()) ,email, name, amount, notes, expenseType);
                    if (success) {
                        expenseList.add(expense);
                        addExpenseToView(expense);
                        updateTvTotalAmount();
                        notification.addRecord(new NotificationRecord("Added expense: " + name, dateTime));
                        if(totalAmount < spendingLimit)
                            Toast.makeText(getActivity(), "Total spending is under your limit of " + spendingLimit + " VND", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void addExpenseToView(Expense expense) {
        View expenseView = getLayoutInflater().inflate(R.layout.item_expense, expenseListContainer, false);

        TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = expenseView.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = expenseView.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = expenseView.findViewById(R.id.tvExpenseCategory);

        tvExpenseName.setText(expense.getName());
        tvExpenseCategory.setText(expense.getType());
        tvExpenseAmount.setText((expense.getAmount() < 0 ? "- " : "+ ")
                + String.format(Locale.getDefault(), "%.2f", Math.abs(expense.getAmount())) + " VND");

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(expense.getDateTime());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            tvExpenseTime.setText(outputFormat.format(date));
        } catch (Exception e) {
            tvExpenseTime.setText(expense.getDateTime());
        }

        expenseView.setOnClickListener(v -> showExpenseDetails(expense));
        expenseListContainer.addView(expenseView);
    }

    private void showExpenseDetails(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_expense_details, null);

        TextView tvExpenseName = view.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = view.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = view.findViewById(R.id.tvExpenseCategory);
        TextView tvExpenseNotes = view.findViewById(R.id.tvExpenseNotes);

        tvExpenseName.setText(expense.getName());
        tvExpenseAmount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()) + " VND");
        tvExpenseCategory.setText(expense.getType());
        tvExpenseNotes.setText(expense.getDescription());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = dateFormat.parse(expense.getDateTime());
            tvExpenseTime.setText(dateFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
            tvExpenseTime.setText("Invalid date format");
        }

        builder.setView(view)
                .setTitle("Expense Details")
                .setPositiveButton("OK", null)
                .setNeutralButton("Edit", (dialog, which) -> showEditExpenseDialog(expense))
                .setNegativeButton("Delete", (dialog, which) -> showDeleteConfirmationDialog(expense))
                .create()
                .show();
    }

    private void showEditExpenseDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etSpendingName = view.findViewById(R.id.etSpendingName);
        EditText etSpendingAmount = view.findViewById(R.id.etSpendingAmount);
        Spinner spSpendingCategory = view.findViewById(R.id.etSpendingCategory);
        EditText etSpendingNotes = view.findViewById(R.id.etSpendingNotes);
        RadioGroup rgExpenseType = view.findViewById(R.id.rgExpenseType);

        etSpendingName.setText(expense.getName());
        etSpendingAmount.setText(String.valueOf(expense.getAmount()));
        etSpendingNotes.setText(expense.getDescription());

        builder.setView(view)
                .setTitle("Edit Expense")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etSpendingName.getText().toString();
                    String amountStr = etSpendingAmount.getText().toString();
                    String category = spSpendingCategory.getSelectedItem().toString();
                    String notes = etSpendingNotes.getText().toString();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr)) {
                        Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    String expenseType = (rgExpenseType.getCheckedRadioButtonId() == R.id.rbIncome) ? "Income" : "Expense";
                    amount = expenseType.equals("Expense") ? -amount : amount;

                    expense.setName(name);
                    expense.setAmount(amount);
                    expense.setDescription(notes);
                    expense.setType(category);

                    boolean success = expenseDAO.updateExpense(expense.getId(), email, name, amount, notes, category);
                    if (success) {
                        loadExpenses(); // Refresh the expense list
                        notification.addRecord(new NotificationRecord("Updated expense: " + name, expense.getDateTime()));
                        Toast.makeText(getActivity(), "Update successfully " + name, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = expenseDAO.deleteExpense(expense.getId());
                    if (success) {
                        expenseList.remove(expense);
                        expenseListContainer.removeAllViews(); // Clear the list view
                        loadExpenses(); // Refresh the list
                        notification.addRecord(new NotificationRecord("Deleted expense: " + expense.getName(), expense.getDateTime()));
                        Toast.makeText(getActivity(), "Delete successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void updateExpenseListView() {
        expenseListContainer.removeAllViews();
        for (Expense expense : expenseList) {
            addExpenseToView(expense);
        }
    }

    public interface ExpenseUpdateListener {
        void onExpenseUpdated();
    }
}
