package com.example.campusexpensemanager.Expense;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.example.campusexpensemanager.Data.DatabaseHelper;
import com.example.campusexpensemanager.Data.DatabaseInitializer;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize database
        DatabaseInitializer.initializeDatabase(requireContext());

        // You can use the database helper here
        // Example: Access the database
        DatabaseInitializer.getDatabaseHelper().getReadableDatabase();
    }

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
        SQLiteDatabase db = DatabaseInitializer.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID, expense.getUserId());
        values.put(DatabaseHelper.COLUMN_NAME, expense.getName());
        values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY, expense.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTES, expense.getNotes());
        values.put(DatabaseHelper.COLUMN_DATE_TIME, expense.getDateTime());

        long result = db.insert(DatabaseHelper.TABLE_EXPENSES, null, values);
        if (result == -1) {
            Toast.makeText(getActivity(), "Failed to save expense", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Expense saved successfully", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadExpenses() {
        expenseList.clear();
        SQLiteDatabase db = DatabaseInitializer.getDatabaseHelper().getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_AMOUNT,
                DatabaseHelper.COLUMN_CATEGORY,
                DatabaseHelper.COLUMN_NOTES,
                DatabaseHelper.COLUMN_DATE_TIME
        };
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor cursor = db.query(DatabaseHelper.TABLE_EXPENSES, columns, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                long amount = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_TIME));

                Expense expense = new Expense(userId, name, amount, dateTime, category, notes);
                expenseList.add(expense);
            }
            cursor.close();
        }

        expenseListContainer.removeAllViews(); // Clear existing views
        for (Expense expense : expenseList) {
            addExpenseToView(expense);
        }
        updateTvTotalAmount(); // Update total amount
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

        TextView dlExpenseName = view.findViewById(R.id.dlExpenseName);
        TextView dlExpenseAmount = view.findViewById(R.id.dlExpenseAmount);
        TextView dlExpenseTime = view.findViewById(R.id.dlExpenseTime);
        TextView dlExpenseCategory = view.findViewById(R.id.dlExpenseCategory);
        TextView dlExpenseNotes = view.findViewById(R.id.dlExpenseNotes);

        dlExpenseName.setText(expense.getName());

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedAmount = numberFormat.format(Math.abs(expense.getAmount()));
        dlExpenseAmount.setText((expense.getAmount() < 0 ? "- " : "+ ") + formattedAmount + " VND");

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(expense.getDateTime());

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(date);
            dlExpenseTime.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            dlExpenseTime.setText(expense.getDateTime());
        }

        dlExpenseCategory.setText(expense.getCategory());
        dlExpenseNotes.setText(expense.getNotes());

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
        spSpendingCategory.setAdapter(adapter);

        spSpendingCategory.setSelection(adapter.getPosition(expense.getCategory()));
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
                    String expenseType = (rgExpenseType.getCheckedRadioButtonId() == R.id.rbIncome) ? "Income" : "Expense";
                    amount = expenseType.equals("Expense") ? -amount : amount;

                    expense.setName(name);
                    expense.setAmount(amount);
                    expense.setCategory(category);
                    expense.setNotes(notes);
                    expense.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                    updateExpenseInDatabase(expense);
                    updateExpenseInView(expense);

                    notification.addRecord(new NotificationRecord("Edited expense: " + name, expense.getDateTime()));
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateExpenseInView(Expense expense) {
        for (int i = 0; i < expenseListContainer.getChildCount(); i++) {
            View expenseView = expenseListContainer.getChildAt(i);
            TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
            if (tvExpenseName.getText().toString().equals(expense.getName())) {
                expenseListContainer.removeViewAt(i);
                addExpenseToView(expense);
                break;
            }
        }
    }

    private void updateExpenseInDatabase(Expense updatedExpense) {
        SQLiteDatabase db = DatabaseInitializer.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NAME, updatedExpense.getName());
        values.put(DatabaseHelper.COLUMN_AMOUNT, updatedExpense.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY, updatedExpense.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTES, updatedExpense.getNotes());
        values.put(DatabaseHelper.COLUMN_DATE_TIME, updatedExpense.getDateTime());

        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(updatedExpense.getUserId())};

        int rowsUpdated = db.update(DatabaseHelper.TABLE_EXPENSES, values, whereClause, whereArgs);
        if (rowsUpdated > 0) {
            Toast.makeText(getActivity(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Failed to update expense", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    expenseList.remove(expense);
                    removeExpenseFromDatabase(expense);

                    removeExpenseFromView(expense);
                    updateTvTotalAmount();

                    notification.addRecord(new NotificationRecord("Deleted expense: " + expense.getName(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeExpenseFromView(Expense expense) {
        for (int i = 0; i < expenseListContainer.getChildCount(); i++) {
            View expenseView = expenseListContainer.getChildAt(i);
            TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
            if (tvExpenseName.getText().toString().equals(expense.getName())) {
                expenseListContainer.removeViewAt(i);
                break;
            }
        }
    }


    private void removeExpenseFromDatabase(Expense expense) {
        SQLiteDatabase db = DatabaseInitializer.getDatabaseHelper().getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(expense.getUserId())};

        int rowsDeleted = db.delete(DatabaseHelper.TABLE_EXPENSES, whereClause, whereArgs);
        if (rowsDeleted > 0) {
            Toast.makeText(getActivity(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
        }
    }

}
