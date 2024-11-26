package com.example.campusexpensemanager.Models;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campusexpensemanager.Expense.ExpenseTracker;
import com.example.campusexpensemanager.Fragments.Home;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.Models.Verify.LoginActivity;
import com.example.campusexpensemanager.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements ExpenseTracker.ExpenseUpdateListener {
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login session
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);
        String userName = sharedPreferences.getString("USER_NAME", null);
        String userMail = sharedPreferences.getString("USER_EMAIL", null);

        if (userId != null && userMail != null && userName != null) {
            // If user is logged in, show the home layout
            setContentView(R.layout.activity_home);
            setupBottomNavigation();

        } else {
            // If no session, redirect to LoginActivity
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNav);
        viewPager2 = findViewById(R.id.view_pager_layout_menu);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.bottom_home) {
                    viewPager2.setCurrentItem(0);
                } else if (id == R.id.bottom_expense_tracker) {
                    viewPager2.setCurrentItem(1);
                } else if (id == R.id.bottom_notification) {
                    viewPager2.setCurrentItem(2);
                } else if (id == R.id.bottom_profile) {
                    viewPager2.setCurrentItem(3);
                } else {
                    return false;
                }
                return true;
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNavigationView.getMenu().findItem(R.id.bottom_home).setChecked(true);
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.bottom_expense_tracker).setChecked(true);
                } else if (position == 2) {
                    bottomNavigationView.getMenu().findItem(R.id.bottom_notification).setChecked(true);
                } else if (position == 3) {
                    bottomNavigationView.getMenu().findItem(R.id.bottom_profile).setChecked(true);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear login session on logout (or call this method on a logout button click if needed)
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onExpenseUpdated() {
        Home homeFragment = (Home) getSupportFragmentManager().findFragmentByTag("f" + 0);

        if (homeFragment != null) {
            homeFragment.loadExpenseData();
            homeFragment.generateChart();
        }
    }
}
