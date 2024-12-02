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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Database.DatabaseHelper;
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

        // Get user
        String userEmail = sharedPreferences.getString("USER_MAIL", "No Email");
        String userName = sharedPreferences.getString("USER_NAME", "Unknown User");

        // Show user information
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
        EditText edtOldPassword = dialogView.findViewById(R.id.edt_old_password);
        EditText edtNewPassword = dialogView.findViewById(R.id.edt_new_password);
        Button btnUpdateProfile = dialogView.findViewById(R.id.btn_update_profile);

        builder.setTitle("Edit Profile");
        AlertDialog dialog = builder.create();

        btnUpdateProfile.setOnClickListener(v -> {
            String newName = edtNewName.getText().toString().trim();
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();

            if (!newPassword.isEmpty() && !isValidPassword(newPassword)) {
                Toast.makeText(getActivity(), "New password must include a letter, a number, and a special character", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check user in database
            if (databaseHelper.validateUserCredentials(userEmail, oldPassword)) {
                boolean isUpdated = databaseHelper.updateUser(userEmail, newName, newPassword);

                if (isUpdated) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (!newName.isEmpty()) {
                        editor.putString("USER_NAME", newName);
                        tvName.setText(newName);
                    }
                    editor.apply();

                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Invalid current password", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");
    }
}
