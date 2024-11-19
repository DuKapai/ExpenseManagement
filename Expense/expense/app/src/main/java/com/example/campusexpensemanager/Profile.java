package com.example.campusexpensemanager;

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

import com.example.campusexpensemanager.Verify.LoginActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class Profile extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfilePicture;
    private Button btnLogout, btnEditProfile;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Load shared preferences
        sharedPreferences = requireActivity().getSharedPreferences("userSession", requireActivity().MODE_PRIVATE);

        // Load user data from file
        loadUserDataFromFile();

        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Clear session data
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        // Edit profile button logic
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        return view;
    }

    private void loadUserDataFromFile() {
        File file = new File(requireContext().getFilesDir(), "userData.txt");

        if (!file.exists()) {
            Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT).show();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String userEmail = sharedPreferences.getString("USER_EMAIL", null);
            String line;

            while ((line = br.readLine()) != null) {
                // Format: id|name|email|password
                String[] userData = line.split("\\|");

                if (userData.length >= 3 && userData[2].equals(userEmail)) {
                    // Update UI
                    tvName.setText(userData[1]);
                    tvEmail.setText(userData[2]);

                    // Save name to SharedPreferences for consistency
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("USER_NAME", userData[1]);
                    editor.apply();
                    return;
                }
            }

            Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditProfileDialog() {
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

            String userEmail = sharedPreferences.getString("USER_EMAIL", "");
            File file = new File(requireContext().getFilesDir(), "userData.txt");

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

                        tvName.setText(userData[1]); // Update UI immediately
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

            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean isValidPassword(String password) {
        Pattern passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$");
        return passwordPattern.matcher(password).matches();
    }
}
