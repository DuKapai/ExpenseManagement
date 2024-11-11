package com.example.campusexpensemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileOutputStream;
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
    private String userId;
    //private Button btnShowData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_tracker, container, false);

        tvTvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        expenseListContainer = view.findViewById(R.id.expenseListContainer);
        /*btnShowData = view.findViewById(R.id.btnShowData);*/
        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        expenseList = new ArrayList<>();
        loadExpenses();

        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        //btnShowData.setOnClickListener(v -> showExpenseDataDialog());
        return view;

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



    private void showExpenseDataDialog() {
        String expenseData = readExpenseDataFromFile();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Expense Data")
                .setMessage(expenseData)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }



    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etSpendingName = view.findViewById(R.id.etSpendingName);
        EditText etSpendingAmount = view.findViewById(R.id.etSpendingAmount);
        EditText etSpendingCategory = view.findViewById(R.id.etSpendingCategory);
        EditText etSpendingNotes = view.findViewById(R.id.etSpendingNotes);

        builder.setView(view)
                .setTitle("Add Expense")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etSpendingName.getText().toString();
                    String amountStr = etSpendingAmount.getText().toString();
                    String category = etSpendingCategory.getText().toString();
                    String notes = etSpendingNotes.getText().toString();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category)) {
                        Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long amount = Long.parseLong(amountStr);
                    String time = String.valueOf(System.currentTimeMillis()); // Current time in milliseconds

                    Expense expense = new Expense(userId, name, amount, time, category, notes);
                    expenseList.add(expense);
                    saveExpense(expense);
                    updateTvTotalAmount();
                    addExpenseToView(expense);
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

    private void addExpenseToView(Expense expense) {
        View expenseView = getLayoutInflater().inflate(R.layout.item_expense, expenseListContainer, false);
        TextView tvExpenseName = expenseView.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = expenseView.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = expenseView.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = expenseView.findViewById(R.id.tvExpenseCategory);

        long timeInMillis = Long.parseLong(expense.getTime());
        Date date = new Date(timeInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        tvExpenseName.setText(expense.getName());

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedAmount = numberFormat.format(expense.getAmount());

        tvExpenseAmount.setText(formattedAmount + " VND");
        tvExpenseTime.setText(formattedDate);
        tvExpenseCategory.setText(expense.getCategory());

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

        // Hiển thị thông tin chi tiết
        long timeInMillis = Long.parseLong(expense.getTime());
        Date date = new Date(timeInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        tvExpenseName.setText(expense.getName());
        tvExpenseAmount.setText(expense.getAmount() + " VND");
        tvExpenseTime.setText(formattedDate);
        tvExpenseCategory.setText(expense.getCategory());
        tvExpenseNotes.setText(expense.getNotes());

        builder.setView(view)
                .setTitle("Expense Details")
                .setPositiveButton("OK", null)
                .setNeutralButton("Edit", (dialog, which) -> {
                    showEditExpenseDialog(expense);
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    showDeleteConfirmationDialog(expense);
                })
                .create()
                .show();
    }

    private void showEditExpenseDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etSpendingName = view.findViewById(R.id.etSpendingName);
        EditText etSpendingAmount = view.findViewById(R.id.etSpendingAmount);
        EditText etSpendingCategory = view.findViewById(R.id.etSpendingCategory);
        EditText etSpendingNotes = view.findViewById(R.id.etSpendingNotes);

        etSpendingName.setText(expense.getName());
        etSpendingAmount.setText(String.valueOf(expense.getAmount()));
        etSpendingCategory.setText(expense.getCategory());
        etSpendingNotes.setText(expense.getNotes());

        builder.setView(view)
                .setTitle("Edit Expense")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etSpendingName.getText().toString();
                    String amountStr = etSpendingAmount.getText().toString();
                    String category = etSpendingCategory.getText().toString();
                    String notes = etSpendingNotes.getText().toString();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category)) {
                        Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long amount = Long.parseLong(amountStr);
                    expense.setName(name);
                    expense.setAmount(amount);
                    expense.setCategory(category);
                    expense.setNotes(notes);

                    updateExpenseInFile(expense);
                    updateExpenseView(expense);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteExpenseFromFile(expense);
                    expenseList.remove(expense); // Cập nhật danh sách chi tiêu
                    updateExpenseListView(); // Cập nhật giao diện
                    updateTvTotalAmount(); // Cập nhật tổng số tiền
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }





    private void deleteExpenseFromFile(Expense expenseToDelete) {
        try {
            FileInputStream fis = getActivity().openFileInput("expenseData.txt");
            Scanner scanner = new Scanner(fis);
            StringBuilder sb = new StringBuilder();

            Log.d("ExpenseTracker", "Content before deletion:");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Expense currentExpense = Expense.fromString(line);

                Log.d("ExpenseTracker", "Reading line: " + line);

                if (!currentExpense.equals(expenseToDelete)) {
                    sb.append(line).append("\n");
                } else {
                    Log.d("ExpenseTracker", "Deleting line: " + line);
                }
            }
            fis.close();

            Log.d("ExpenseTracker", "Content to write after deletion: " + sb.toString());

            FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






    private void updateExpenseInFile(Expense expense) {
        try {
            FileInputStream fis = getActivity().openFileInput("expenseData.txt");
            Scanner scanner = new Scanner(fis);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Expense currentExpense = Expense.fromString(line);
                if (currentExpense.equals(expense)) {
                    sb.append(expense.toString()).append("\n");
                } else {
                    sb.append(line).append("\n");
                }
            }
            fis.close();

            Log.d("ExpenseTracker", "Content to write after update: " + sb.toString());

            FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void updateExpenseView(Expense expense) {
        for (int i = 0; i < expenseListContainer.getChildCount(); i++) {
            View view = expenseListContainer.getChildAt(i);
            TextView tvExpenseName = view.findViewById(R.id.tvExpenseName);
            TextView tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
            TextView tvExpenseCategory = view.findViewById(R.id.tvExpenseCategory);
            TextView tvExpenseTime = view.findViewById(R.id.tvExpenseTime);

            long timeInMillis = Long.parseLong(expense.getTime());
            Date date = new Date(timeInMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            if (tvExpenseTime.getText().toString().equals(formattedDate)) {
                tvExpenseName.setText(expense.getName());
                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                String formattedAmount = numberFormat.format(expense.getAmount());
                tvExpenseAmount.setText(formattedAmount + " VND");
                tvExpenseCategory.setText(expense.getCategory());
                break;
            }
        }
    }


    private void updateExpenseListView() {
        expenseListContainer.removeAllViews();
        for (Expense expense : expenseList) {
            addExpenseToView(expense);
        }
    }


}
