package com.example.campusexpensemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.campusexpensemanager.Verify.LoginActivity;
import com.example.campusexpensemanager.Verify.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save last login information
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);

        if (userId != null) {
            // Move Home page if login information is true
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void RegisterPage(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void LoginPage(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
