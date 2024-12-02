package com.example.campusexpensemanager.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Database.DAO.UserDAO;
import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Verify.LoginActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.Verify.LoginActivity;

public class Profile extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfilePicture;
    private Button btnLogout, btnEditProfile;
    private SharedPreferences sharedPreferences;
    private EditText etSpendingLimit;
    private Button btnSetLimit;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("userSession", requireActivity().MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(requireContext());

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        etSpendingLimit = view.findViewById(R.id.etSpendingLimit);
        btnSetLimit = view.findViewById(R.id.btnSetLimit);

        // Get user information from SharedPreferences
        String userEmail = sharedPreferences.getString("USER_EMAIL", "No Email");
        String userName = sharedPreferences.getString("USER_NAME", "Unknown User");
        String userMail = sharedPreferences.getString("USER_MAIL", "No Email");

        // Display user information
        tvName.setText(userName);
        tvEmail.setText(userEmail);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog(userEmail));

        long spendingLimit = sharedPreferences.getLong("SPENDING_LIMIT", 0);
        if (spendingLimit > 0) {
            etSpendingLimit.setText(String.valueOf(spendingLimit));
        }

        btnSetLimit.setOnClickListener(v -> {
            String limitStr = etSpendingLimit.getText().toString();
            if (!limitStr.isEmpty()) {
                long limit = Long.parseLong(limitStr);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("SPENDING_LIMIT", limit);
                editor.apply();
                Toast.makeText(getActivity(), "Spending limit set to " + limit + " VND", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Please enter a valid limit", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void showEditProfileDialog(String userEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText edtNewName = dialogView.findViewById(R.id.edt_new_name);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        EditText edtEmail = dialogView.findViewById(R.id.edt_new_email);
        EditText edtNewPassword = dialogView.findViewById(R.id.edt_new_password);
        Button btnUpdateProfile = dialogView.findViewById(R.id.btn_update_profile);


        builder.setTitle("Edit Profile");
        AlertDialog dialog = builder.create();


        btnUpdateProfile.setOnClickListener(v -> {
            String newName = edtNewName.getText().toString().trim();
            String newEmail = edtEmail.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();

            UserDAO userDAO = new UserDAO(requireContext());
            String currentEmail = sharedPreferences.getString("USER_EMAIL", "");

            boolean isUpdated = userDAO.updateUser(currentEmail, newName, newEmail, newPassword);

            if (isUpdated) {
                // Update shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (!newName.isEmpty()) {
                    editor.putString("USER_NAME", newName);
                }
                if (!newEmail.isEmpty()) {
                    editor.putString("USER_EMAIL", newEmail);
                }
                editor.apply();

                // Update UI
                if (!newName.isEmpty()) {
                    tvName.setText(newName);
                }
                if (!newEmail.isEmpty()) {
                    tvEmail.setText(newEmail);
                }

                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}