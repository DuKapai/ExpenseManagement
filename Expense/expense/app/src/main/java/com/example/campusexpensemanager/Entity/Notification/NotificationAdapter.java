package com.example.campusexpensemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanager.Entity.Notification.NotificationRecord;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationRecord> notifications;

    public NotificationAdapter(List<NotificationRecord> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationRecord notification = notifications.get(position);
        holder.tvExpenseName.setText(notification.getDescription());
        holder.tvExpenseTime.setText(notification.getDateTime());
        holder.tvExpenseAmount.setText(notification.getActionType());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseName, tvExpenseTime, tvExpenseAmount;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseName = itemView.findViewById(R.id.tvExpenseName);
            tvExpenseTime = itemView.findViewById(R.id.tvExpenseTime);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
        }
    }
}
