package com.example.campusexpensemanager;

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

public class Profile extends Fragment {

    private TextView tvName, tvEmail;
    private ImageView ivProfilePicture;
    private Button btnLogout;
    private SharedPreferences sharedPreferences;
    private EditText etSpendingLimit;
    private Button btnSetLimit;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Khai báo và khởi tạo biến sharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("userSession", getActivity().MODE_PRIVATE);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Đọc thông tin từ SharedPreferences
        String userName = sharedPreferences.getString("USER_NAME", "Unknown User");
        String userMail = sharedPreferences.getString("USER_MAIL", "No Email");

        // Hiển thị thông tin trên các TextView
        tvName.setText(userName);
        tvEmail.setText(userMail);



        // Thiết lập sự kiện cho nút Logout
        btnLogout.setOnClickListener(v -> {
            // Xóa session đăng nhập
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Chuyển hướng đến màn hình đăng nhập
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

        return view;
    }
}
