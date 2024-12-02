package com.example.campusexpensemanager.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.campusexpensemanager.R;
import java.util.List;

public class NotificationFragment extends Fragment {

    private Notification notification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notification = new Notification(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Get the container where notifications will be added
        LinearLayout notificationContainer = view.findViewById(R.id.notificationContainer);

        loadNotifications(notificationContainer);

        return view;
    }

    private void loadNotifications(LinearLayout container) {
        List<NotificationRecord> notifications = notification.loadNotifications();

        for (NotificationRecord notificationRecord : notifications) {
            // Inflate a new notification card for each item
            View notificationView = LayoutInflater.from(getContext()).inflate(R.layout.notification_card, container, false);

            TextView tvExpenseName = notificationView.findViewById(R.id.tvExpenseName);
            TextView tvExpenseTime = notificationView.findViewById(R.id.tvExpenseTime);
            TextView tvExpenseAmount = notificationView.findViewById(R.id.tvExpenseAmount);

            tvExpenseName.setText(notificationRecord.getDescription());
            tvExpenseTime.setText(notificationRecord.getDateTime());
            tvExpenseAmount.setText(notificationRecord.getActionType());

            // Add the newly inflated view to the container
            container.addView(notificationView);
        }
    }
}
