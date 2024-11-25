package com.example.campusexpensemanager.Expense;

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

import java.io.FileOutputStream;
import java.io.FileInputStream;
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
    private ExpenseUpdateListener expenseUpdateListener;
    private Notification notification;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_tracker, container, false);

        tvTvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        expenseListContainer = view.findViewById(R.id.expenseListContainer);

        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        expenseList = new ArrayList<>();
        loadExpenses();
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());
        notification = new Notification(getActivity());

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
                    expenseUpdateListener.onExpenseUpdated();
                    updateTvTotalAmount();
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
        // Update the entire expense list in the file
        updateExpenseListInFile();
    }

    private void loadExpenses() {
        try (FileInputStream fis = getActivity().openFileInput("expenseData.txt")) {
            Scanner scanner = new Scanner(fis);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Expense expense = Expense.fromString(line);

                // Add null check here
                if (expense != null && expense.getUserId().equals(userId)) {
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


    private void showExpenseDetails(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_expense_details, null);

        TextView tvExpenseName = view.findViewById(R.id.tvExpenseName);
        TextView tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        TextView tvExpenseTime = view.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseCategory = view.findViewById(R.id.tvExpenseCategory);
        TextView tvExpenseNotes = view.findViewById(R.id.tvExpenseNotes);

        String dateTimeString = expense.getDateTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date date = dateFormat.parse(dateTimeString);

            tvExpenseTime.setText(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            tvExpenseTime.setText("Invalid date format");
        }

        tvExpenseName.setText(expense.getName());
        tvExpenseAmount.setText(expense.getAmount() + " VND");
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

                    int index = expenseList.indexOf(expense);
                    if (index != -1) {
                        expenseList.set(index, expense);
                    }

                    deleteExpenseFromFile(expense);
                    saveExpense(expense);

                    updateExpenseListView();
                    expenseUpdateListener.onExpenseUpdated();
                    updateTvTotalAmount();
                    notification.addRecord(new NotificationRecord("Edited expense: " + name, expense.getDateTime()));
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
                    expenseList.remove(expense);
                    updateExpenseListView();
                    expenseUpdateListener.onExpenseUpdated();
                    updateTvTotalAmount();
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

                if (currentExpense != null) {
                    if (!currentExpense.equals(expenseToDelete)) {
                        sb.append(line).append("\n");
                    } else {
                        Log.d("ExpenseTracker", "Deleting line: " + line);
                    }
                } else {
                    Log.d("ExpenseTracker", "Skipping invalid line: " + line);
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
        deleteExpenseFromFile(expense); // Remove the old entry
        saveExpense(expense); // Add the updated entry
    }

    private void updateExpenseView(Expense expense) {
        for (int i = 0; i < expenseListContainer.getChildCount(); i++) {
            View view = expenseListContainer.getChildAt(i);
            TextView tvExpenseName = view.findViewById(R.id.tvExpenseName);
            TextView tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
            TextView tvExpenseCategory = view.findViewById(R.id.tvExpenseCategory);
            TextView tvExpenseTime = view.findViewById(R.id.tvExpenseTime);

            long timeInMillis = Long.parseLong(expense.getDateTime());
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

    private void updateExpenseListInFile() {
        try (FileOutputStream fos = getActivity().openFileOutput("expenseData.txt", Context.MODE_PRIVATE)) {
            for (Expense expense : expenseList) {
                String expenseString = expense.toString() + "\n";
                fos.write(expenseString.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ExpenseUpdateListener {
        void onExpenseUpdated();
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

}
