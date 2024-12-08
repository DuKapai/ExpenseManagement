package com.example.campusexpensemanager.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.campusexpensemanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    /**
     * Display a notification after adding, editing, and deleting expenses
     *
     * @param activity: current screen
     * @param message:  content you want to announce
     * @param amount:   update money
     */
    public static void showExpenseNotice(Activity activity, String message, Double amount) {
        // Inflate layout of notification
        LayoutInflater inflater = activity.getLayoutInflater();
        View noticeView = inflater.inflate(R.layout.notification_card, null);

        // Set Notification information
        TextView tvExpenseName = noticeView.findViewById(R.id.tvExpenseName);
        TextView tvExpenseTime = noticeView.findViewById(R.id.tvExpenseTime);
        TextView tvExpenseAmount = noticeView.findViewById(R.id.tvExpenseAmount);

        tvExpenseName.setText(message);
        tvExpenseTime.setText("Just now");
        tvExpenseAmount.setText(String.valueOf(amount));

        // Add layout in display
        final ViewGroup rootView = activity.findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);
        noticeView.setLayoutParams(params);

        rootView.addView(noticeView);

        // Animation down
        noticeView.setTranslationY(-noticeView.getHeight());
        noticeView.animate()
                .translationY(0)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        // Auto hide after 1.5s
                        noticeView.postDelayed(() -> {
                            noticeView.animate()
                                    .translationY(-noticeView.getHeight())
                                    .setDuration(500)
                                    .withEndAction(() -> rootView.removeView(noticeView));
                        }, 1500);
                    }
                });
    }

    // Format date from database
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Format date can be custom
    private static final String DESIRED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Application time zone
    private static final String TARGET_TIMEZONE = "Asia/Ho_Chi_Minh";

    /**
     * Convert date values from DB to the correct time zone.
     *
     * @param dbDateTime Date string from the database.
     * @return Date string in the correct time zone.
     */
    public static String convertToLocalTime(String dbDateTime) {
        if (dbDateTime == null || dbDateTime.isEmpty()) {
            return null;
        }

        try {
            // Format date from database
            SimpleDateFormat dbDateFormat = new SimpleDateFormat(DB_DATE_FORMAT);
            dbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Convert from string to Date object
            Date date = dbDateFormat.parse(dbDateTime);

            // Format the date and time according to the desired time zone
            SimpleDateFormat targetDateFormat = new SimpleDateFormat(DESIRED_DATE_FORMAT);
            targetDateFormat.setTimeZone(TimeZone.getTimeZone(TARGET_TIMEZONE));

            // Returns date and time string
            return targetDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}