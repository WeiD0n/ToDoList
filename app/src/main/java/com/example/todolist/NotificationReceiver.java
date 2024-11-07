package com.example.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todolist.NotificationHelper;  // Import NotificationHelper

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract task data from the intent
        String taskTitle = intent.getStringExtra("taskTitle");
        int taskId = intent.getIntExtra("taskId", -1);
        String reminderType = intent.getStringExtra("reminderType");  // Extract reminder type

        // Construct the notification message based on the reminder type
        String notificationMessage = "Your task '" + taskTitle + "' is due in " + reminderType + "!";

        // Use NotificationHelper to send the notification with the custom message
        NotificationHelper.sendNotification(context, notificationMessage, taskId);
    }
}
