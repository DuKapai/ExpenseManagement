package com.example.campusexpensemanager.Fragments;

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

import com.example.campusexpensemanager.Database.DAO.CategoryDAO;
import com.example.campusexpensemanager.Database.DAO.ExpenseDAO;
import com.example.campusexpensemanager.Database.DAO.NotiDAO;
import com.example.campusexpensemanager.Entity.Category;
import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.Utils.Utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseTracker extends Fragment {

    private TextView tvTvTotalAmount;
    private LinearLayout expenseListContainer;
    private List<Expense> expenseList;
    private SharedPreferences sharedPreferences;
    private String email;
    private ExpenseUpdateListener expenseUpdateListener;
    private ExpenseDAO expenseDAO;
    private long spendingLimit;
    private long totalAmount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_tracker, container, false);

        tvTvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        Button btnAddExpense = view.findViewById(R.id.btnAddExpense);
        expenseListContainer = view.findViewById(R.id.expenseListContainer);

        sharedPreferences = getActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("USER_ID", null);

        spendingLimit = sharedPreferences.getLong("SPENDING_LIMIT", 0);

        expenseList = new ArrayList<>();
        expenseDAO = new ExpenseDAO(getActivity());

        loadExpenses();
        notifyExpenseUpdated();
        btnAddExpense.setOnClickListener(v -> showAddExpenseDialog());

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

    private void notifyExpenseUpdated() {
        if (expenseUpdateListener != null) {
            expenseUpdateListener.onExpenseUpdated();
        }
    }

    private void updateTvTotalAmount() {
        totalAmount = 0;
        for (Expense expense : expenseList) {
            totalAmount += expense.getAmount();
        }

        String formattedAmount = NumberFormat.getNumberInstance(Locale.getDefault()).format(totalAmount);
        tvTvTotalAmount.setText(formattedAmount + " VND");
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);

        EditText etSpendingName = view.findViewById(R.id.etSpendingName);
        EditText etSpendingAmount = view.findViewById(R.id.etSpendingAmount);
        Spinner spSpendingCategory = view.findViewById(R.id.etSpendingCategory);
        EditText etSpendingNotes = view.findViewById(R.id.etSpendingNotes);
        RadioGroup rgExpenseType = view.findViewById(R.id.rgExpenseType);
        Button btnAddCategory = view.findViewById(R.id.btnAddCategory);

        // Initialize category spinner
        CategoryDAO categoryDAO = new CategoryDAO(getActivity());
        categoryDAO.insertDefaultCategoriesIfNeeded(email); // Ensure default categories are present
        List<Category> categoryList = categoryDAO.getCategoriesByEmail(email);
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpendingCategory.setAdapter(categoryAdapter);

        // Add new category logic
        btnAddCategory.setOnClickListener(v -> showUpdateCategoryDialog(categoryDAO, categoryAdapter, categoryNames));

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
                    boolean success = expenseDAO.insertExpense((expenseList.size()), email, name, amount, notes, expenseType);
                    if (success) {
                        expenseList.add(expense);
                        addExpenseToView(expense);
                        updateTvTotalAmount();
                        NotiDAO notiDAO = new NotiDAO(getActivity());
                        notiDAO.addNoti("Added expense: " + name + " ; Amount: " + amount + " ; Type: " + expenseType, email, "Add");
                        Utils.showExpenseNotice(getActivity(), "Added expense: " + name, amount);
                    } else {
                        Toast.makeText(getActivity(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showUpdateCategoryDialog(CategoryDAO categoryDAO, ArrayAdapter<String> categoryAdapter, List<String> categoryNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.dialog_update_category, null);

        EditText etCategoryName = view.findViewById(R.id.etCategoryName);
        Button btnAddCategory = view.findViewById(R.id.btnAddCategory);
        Button btnDeleteCategory = view.findViewById(R.id.btnDeleteCategory);

        builder.setView(view)
                .setTitle("Update Categories")
                .setPositiveButton("Done", null)
                .create()
                .show();

        btnAddCategory.setOnClickListener(v -> {
            String newCategoryName = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(newCategoryName)) {
                Toast.makeText(getActivity(), "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure no duplicates
            if (categoryNames.contains(newCategoryName) || CategoryDAO.DEFAULT_CATEGORIES.contains(newCategoryName)) {
                Toast.makeText(getActivity(), "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = categoryDAO.insertCategory(email, newCategoryName);
            if (success) {
                categoryNames.add(newCategoryName);
                categoryAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Category added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteCategory.setOnClickListener(v -> {
            String categoryToDelete = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(categoryToDelete)) {
                Toast.makeText(getActivity(), "Please enter a category to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent deletion of default categories
            if (CategoryDAO.DEFAULT_CATEGORIES.contains(categoryToDelete)) {
                Toast.makeText(getActivity(), "Cannot delete default categories", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if category exists in the database
            Category category = categoryDAO.getCategoriesByEmail(email).stream()
                    .filter(cat -> cat.getName().equals(categoryToDelete))
                    .findFirst()
                    .orElse(null);

            if (category == null) {
                Toast.makeText(getActivity(), "Category not found", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = categoryDAO.deleteCategory(category.getId());
            if (success) {
                categoryNames.remove(categoryToDelete);
                categoryAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Category deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to delete category", Toast.LENGTH_SHORT).show();
            }
        });
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
        tvExpenseAmount.setTextColor(expense.getAmount() < 0
                ? getResources().getColor(R.color.expenseColor)
                : getResources().getColor(R.color.incomeColor));

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

        if (totalAmount < spendingLimit)
            Toast.makeText(getActivity(), "Total spending is under your limit of " + spendingLimit + " VND." + " Please add more incomes", Toast.LENGTH_SHORT).show();

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

        // Initialize with existing data
        etSpendingName.setText(expense.getName());
        etSpendingAmount.setText(String.valueOf(Math.abs(expense.getAmount())));
        etSpendingNotes.setText(expense.getDescription());

        CategoryDAO categoryDAO = new CategoryDAO(getActivity());
        List<Category> categoryList = categoryDAO.getCategoriesByEmail(email);
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSpendingCategory.setAdapter(categoryAdapter);

        // Pre-select category
        spSpendingCategory.setSelection(categoryNames.indexOf(expense.getType()));

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
                        notifyExpenseUpdated();
                        NotiDAO notiDAO = new NotiDAO(getActivity());
                        notiDAO.addNoti("Updatede: " + name + " ; Amount: " + amount + " ; Type: " + expenseType, email, "Update");
                        Utils.showExpenseNotice(getActivity(), "Updated expense: " + name, amount);
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
                        notifyExpenseUpdated();
                        NotiDAO notiDAO = new NotiDAO(getActivity());
                        notiDAO.addNoti("Deleted expense: " + expense.getName(), email, "Delete");
                        Utils.showExpenseNotice(getActivity(), "Deleted expense: " + expense.getName(), Double.valueOf(0));
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

    public void setExpenseUpdateListener(ExpenseUpdateListener listener) {
        this.expenseUpdateListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        spendingLimit = sharedPreferences.getLong("SPENDING_LIMIT", 0);
        notifySpendingLimitUpdated();
    }

    private void notifySpendingLimitUpdated() {
        if (totalAmount < spendingLimit) {
            Toast.makeText(getActivity(),
                    "Total spending is under your limit of " + spendingLimit + " VND",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
