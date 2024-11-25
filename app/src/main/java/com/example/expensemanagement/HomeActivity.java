package com.example.expensemanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {

    private TextView tvBalanceOverview, tvTodayHeader, tvYesterdayHeader;
    private CardView cardTodayTransaction, cardYesterdayTransaction;
    private ImageView imageUser, imageSearchIcon;
    private SearchView searchView;
    private Button btnLogout;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        tvBalanceOverview = findViewById(R.id.textView);
        tvTodayHeader = findViewById(R.id.textView5);
        tvYesterdayHeader = findViewById(R.id.textView7);
        cardTodayTransaction = findViewById(R.id.cardView11);
        cardYesterdayTransaction = findViewById(R.id.cardView13);
        imageUser = findViewById(R.id.image_user);
        searchView = findViewById(R.id.search_view);
        tabLayout = findViewById(R.id.tabs);
        btnLogout = findViewById(R.id.logout_button);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle live search text changes (if necessary)
                return false;
            }
        });

        // Set up tab layout for transaction filtering (e.g., "Sent", "Received")
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Switch between "Sent" and "Received" transactions
                String selectedTab = tab.getText().toString();
                if (selectedTab.equals("Sent")) {
                    // Load and display sent transactions
                } else if (selectedTab.equals("Received")) {
                    // Load and display received transactions
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Handle logout button click
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user data from SharedPreferences and return to main screen
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Redirect to MainActivity (Login/Register screen)
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Set default content for the balance overview, user image, and transactions
        loadHomeData();
    }

    private void loadHomeData() {
        // Example: Set balance overview (You can retrieve these from your data source)
        tvBalanceOverview.setText("Your balance: $926.21");

        // Set today's and yesterday's transaction headers
        tvTodayHeader.setText("Today");
        tvYesterdayHeader.setText("Yesterday");

        // Load and display transactions for today and yesterday (dummy data for now)
        // You can replace these with real transactions
        cardTodayTransaction.setVisibility(View.VISIBLE);
        cardYesterdayTransaction.setVisibility(View.VISIBLE);

        // You can also dynamically populate cards with transaction data here
    }
}
