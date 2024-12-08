package com.example.campusexpensemanager.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanager.Database.DAO.NotiDAO;
import com.example.campusexpensemanager.Entity.Noti;
import com.example.campusexpensemanager.R;
import com.example.campusexpensemanager.Utils.Utils;

import java.util.List;

public class NotificationFragment extends Fragment {

    private String email;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Get the container where notifications will be added
        LinearLayout notificationContainer = view.findViewById(R.id.notificationContainer);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("USER_ID", null);

        loadNotifications(notificationContainer);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout notificationContainer = getView().findViewById(R.id.notificationContainer);
        loadNotifications(notificationContainer);
    }
    private void loadNotifications(LinearLayout container) {
        if (container != null) {
            container.removeAllViews(); // Delete old views if any
            NotiDAO noti = new NotiDAO(requireContext());
            List<Noti> notifications = noti.getAllNotiByUser(email);

            for (Noti notificationRecord : notifications) {
                // Inflate a new notification card for each item
                View notificationView = LayoutInflater.from(getContext()).inflate(R.layout.notification_card, container, false);

                TextView tvExpenseName = notificationView.findViewById(R.id.tvExpenseName);
                TextView tvExpenseTime = notificationView.findViewById(R.id.tvExpenseTime);
                TextView tvExpenseAmount = notificationView.findViewById(R.id.tvExpenseAmount);

                tvExpenseName.setText(notificationRecord.getTitle());
                tvExpenseTime.setText(Utils.convertToLocalTime(notificationRecord.getCreateDate()));
                tvExpenseAmount.setText(notificationRecord.getType());

                // Add the newly inflated view to the container
                container.addView(notificationView);
            }
        }
    }
}
