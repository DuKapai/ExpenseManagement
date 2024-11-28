package com.example.campusexpensemanager.Fragments;

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

import com.example.campusexpensemanager.Verify.LoginActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import com.example.campusexpensemanager.R;

public class Profile extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfilePicture;
    private Button btnLogout, btnEditProfile;
    private SharedPreferences sharedPreferences;
    private EditText etSpendingLimit;
    private Button btnSetLimit;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = getActivity().getSharedPreferences("userSession", getActivity().MODE_PRIVATE);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        String userName = sharedPreferences.getString("USER_NAME", "Unknown User");
        String userMail = sharedPreferences.getString("USER_EMAIL", "No Email");

        tvName.setText(userName);
        tvEmail.setText(userMail);

        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        etSpendingLimit = view.findViewById(R.id.etSpendingLimit);
        btnSetLimit = view.findViewById(R.id.btnSetLimit);

        // Load stored limit
        long spendingLimit = sharedPreferences.getLong("SPENDING_LIMIT", 0);
        if (spendingLimit > 0) {
            etSpendingLimit.setText(String.valueOf(spendingLimit));
        }

        // Set button click listener
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

        // Edit Profile button logic
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        return view;
    }

    // Method to show the Edit Profile dialog
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText edtNewName = dialogView.findViewById(R.id.edt_new_name);
        EditText edtOldPassword = dialogView.findViewById(R.id.edt_old_password);
        EditText edtNewPassword = dialogView.findViewById(R.id.edt_new_password);
        Button btnUpdateProfile = dialogView.findViewById(R.id.btn_update_profile);

        builder.setTitle("Edit Profile");
        AlertDialog dialog = builder.create();

        // Logic for updating the profile when the Update button is clicked
        btnUpdateProfile.setOnClickListener(v -> {
            String newName = edtNewName.getText().toString().trim();
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();

            if (!newPassword.isEmpty() && !isValidPassword(newPassword)) {
                Toast.makeText(getActivity(), "New password must include a letter, a number, and a special character", Toast.LENGTH_SHORT).show();
                return;
            }

            String userEmail = sharedPreferences.getString("USER_EMAIL", "");
            File file = new File(getActivity().getFilesDir(), "userData.txt");

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder updatedData = new StringBuilder();
                String line;

                boolean isUpdated = false;

                while ((line = br.readLine()) != null) {
                    String[] userData = line.split("\\|");
                    if (userData[2].equals(userEmail) && userData[3].equals(oldPassword)) {
                        userData[1] = newName.isEmpty() ? userData[1] : newName;
                        userData[3] = newPassword.isEmpty() ? userData[3] : newPassword;

                        // Update SharedPreferences with new name
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("USER_NAME", userData[1]);
                        editor.apply();

                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                        isUpdated = true;
                    }
                    updatedData.append(String.join("|", userData)).append("\n");
                }

                // Save updated data back to file
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(updatedData.toString());
                }

                if (!isUpdated) {
                    Toast.makeText(getActivity(), "Invalid current password", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error updating profile", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss(); // Close the dialog after updating
        });

        dialog.show();
    }

    // Method to validate the new password format
    private boolean isValidPassword(String password) {
        // Regular expression to ensure at least one letter, one number, and one special character
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");
        return passwordPattern.matcher(password).matches();
    }
}
