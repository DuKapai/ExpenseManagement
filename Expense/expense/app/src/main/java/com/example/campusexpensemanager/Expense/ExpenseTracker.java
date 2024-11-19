package com.example.campusexpensemanager.Expense;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import com.example.campusexpensemanager.Notification.Notification;
import com.example.campusexpensemanager.Notification.NotificationRecord;
import com.example.campusexpensemanager.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
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
    private String userId;

    private Notification notification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_tracker, container, false);

        notification = new Notification(getActivity());

        tvTvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        expenseListContainer = view.findViewById(R.id.expenseListContainer);
        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        expenseList = new ArrayList<>();
        loadExpenses();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        return view;
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

                    long amount = Long.parseLong(amountStr);
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String expenseType = (rgExpenseType.getCheckedRadioButtonId() == R.id.rbIncome) ? "Income" : "Expense";
                    amount = expenseType.equals("Expense") ? -amount : amount;

                    Expense expense = new Expense(userId, name, amount, dateTime, category, notes);
                    expenseList.add(expense);
                    saveExpense(expense);
                    addExpenseToView(expense);
                    updateTvTotalAmount();

                    // Log notification
                    notification.addRecord(new NotificationRecord("Added expense: " + name, dateTime));
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void saveExpense(Expense expense) {
        try (FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE | Context.MODE_APPEND)) {
            String expenseString = expense.toString() + "\n";
            fos.write(expenseString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadExpenses() {
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt")) {
            Scanner scanner = new Scanner(fis);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Expense expense = Expense.fromString(line);
                if (expense.getUserId().equals(userId)) {
                    expenseList.add(expense);
                    addExpenseToView(expense);
                }
            }
            updateTvTotalAmount();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTvTotalAmount() {
        long tvTotalAmount = 0;
        for (Expense expense : expenseList) {
            tvTotalAmount += expense.getAmount();
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedAmount = numberFormat.format(tvTotalAmount);

        tvTvTotalAmount.setText(formattedAmount + " VND");
    }

    @SuppressLint("SetTextI18n")
    private void addExpenseToView(Expense expense) {
        View expenseView = getLayoutInflater().inflate(R.layout.item_expense, expenseListContainer, false);

        TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = expenseView.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = expenseView.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = expenseView.findViewById(R.id.tvExpenseCategory);

        tvExpenseName.setText(expense.getName());
        tvExpenseCategory.setText(expense.getCategory());

        // Format amount
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedAmount = numberFormat.format(Math.abs(expense.getAmount()));
        tvExpenseAmount.setText((expense.getAmount() < 0 ? "- " : "+ ") + formattedAmount + " VND");
        tvExpenseAmount.setTextColor(expense.getAmount() < 0
                ? getResources().getColor(R.color.expenseColor)
                : getResources().getColor(R.color.incomeColor));

        // Format datetime
        try {
            // Parse the datetime string to a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(expense.getDateTime());

            // Format the date for display
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            tvExpenseTime.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            tvExpenseTime.setText(expense.getDateTime()); // Fallback to original string if parsing fails
        }

        expenseView.setOnClickListener(v -> showExpenseDetails(expense));

        expenseListContainer.addView(expenseView);
    }



    @SuppressLint("SetTextI18n")
    private void showExpenseDetails(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_expense_details, null);

        TextView tvExpenseName = view.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = view.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = view.findViewById(R.id.tvExpenseCategory);
        TextView tvExpenseNotes = view.findViewById(R.id.tvExpenseNotes);

        tvExpenseName.setText(expense.getName());
        tvExpenseAmount.setText(expense.getAmount() + " VND");

        try {
            // Parse the datetime string
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(expense.getDateTime());

            // Format the date for display
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(date);

            tvExpenseTime.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            tvExpenseTime.setText(expense.getDateTime());
        }

        tvExpenseCategory.setText(expense.getCategory());
        tvExpenseNotes.setText(expense.getNotes());

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
        etSpendingAmount.setText(String.valueOf(Math.abs(expense.getAmount())));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpendingCategory.setAdapter(adapter);

        int categoryPosition = adapter.getPosition(expense.getCategory());
        spSpendingCategory.setSelection(categoryPosition);

        etSpendingNotes.setText(expense.getNotes());
        rgExpenseType.check(expense.getAmount() < 0 ? R.id.rbExpense : R.id.rbIncome);

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

                    long amount = Long.parseLong(amountStr);
                    String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String expenseType = (rgExpenseType.getCheckedRadioButtonId() == R.id.rbIncome) ? "Income" : "Expense";
                    amount = expenseType.equals("Expense") ? -amount : amount;

                    expense.setName(name);
                    expense.setAmount(amount);
                    expense.setCategory(category);
                    expense.setNotes(notes);
                    expense.setDateTime(dateTime);

                    updateExpenseInFile(expense);
                    loadExpenses();

                    // Log notification
                    notification.addRecord(new NotificationRecord("Edited expense: " + name, dateTime));
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateExpenseInFile(Expense updatedExpense) {
        List<Expense> updatedExpenses = new ArrayList<>(expenseList);
        try (FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE)) {
            for (Expense expense : updatedExpenses) {
                fos.write((expense.toString() + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    expenseList.remove(expense);
                    removeExpenseFromFile(expense);
                    loadExpenses();

                    // Log notification
                    notification.addRecord(new NotificationRecord("Deleted expense: " + expense.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeExpenseFromFile(Expense expense) {
        List<Expense> updatedExpenses = new ArrayList<>();
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt")) {
            Scanner scanner = new Scanner(fis);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Expense existingExpense = Expense.fromString(line);
                if (!existingExpense.equals(expense)) {
                    updatedExpenses.add(existingExpense);
                }
            }
            try (FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE)) {
                for (Expense exp : updatedExpenses) {
                    fos.write((exp.toString() + "\n").getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
