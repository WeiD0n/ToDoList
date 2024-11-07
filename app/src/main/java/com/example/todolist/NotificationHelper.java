package com.example.todolist;  // Ensure the correct package name

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.todolist.R;

public class NotificationHelper {

    // Sends the notification to the user
    public static void sendNotification(Context context, String notificationMessage, int taskId) {
        // Get NotificationManager system service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if notificationManager is available (null check)
        if (notificationManager == null) {
            return; // If for some reason notificationManager is null, just return
        }

        // Define Notification Channel for Android O (API 26) and above
        String channelId = "task_reminder_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Task Reminders", // Channel name
                    NotificationManager.IMPORTANCE_DEFAULT // Channel importance
            );
            notificationManager.createNotificationChannel(channel); // Create the channel
        }

        // Create the notification with the dynamic message
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_notifications_24) // Make sure this icon exists in your res/drawable folder
                .setContentTitle("Task Reminder") // Title of the notification
                .setContentText(notificationMessage) // Content text (customized message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set priority for older Android versions
                .setAutoCancel(true); // Notification will be dismissed when the user taps it

        // Show the notification
        notificationManager.notify(taskId, builder.build()); // Use taskId as the unique notification ID
    }
}
