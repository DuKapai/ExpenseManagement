package com.example.campusexpensemanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campusexpensemanager.Expense.ExpenseTracker;
import com.example.campusexpensemanager.Fragments.Home;
import com.example.campusexpensemanager.Fragments.NotificationFragment;
import com.example.campusexpensemanager.Fragments.Profile;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new Home();
                break;
            case 1:
                return new ExpenseTracker();
            case 2:
                return new NotificationFragment();
            case 3:
                return new Profile();
            default:
                return new Home();
        }
        fragment.setArguments(new Bundle());
        fragment.getArguments().putString("fragmentTag", "f" + position);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
