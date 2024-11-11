package com.example.todolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.example.todolist.Database;
import com.example.todolist.NotificationReceiver;
import com.example.todolist.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class ReminderManager {

    private static final String PREFS_NAME = "ReminderPrefs";
    private static final String REMINDER_SCHEDULED_KEY = "reminder_scheduled_for_user";

    // Helper method to parse date string (yyyy-MM-dd HH:mm) to milliseconds
    private static long parseDateToMillis(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        // Set the time zone to Malaysia Time (MYT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return dateFormat.parse(dateStr).getTime();
    }

    // Helper method to check if the app can schedule exact alarms (API 31+)
    public static boolean canScheduleExactAlarm(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;  // For devices below API 31, assume exact alarms can be scheduled
    }

    // Method to check if reminders have already been scheduled for the user
    public static boolean areRemindersScheduled(Context context, int userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedUserId = sharedPreferences.getInt(REMINDER_SCHEDULED_KEY, -1);
        return savedUserId == userId; // Check if the saved user ID matches the current one
    }

    // Method to mark reminders as scheduled for the user
    public static void markRemindersScheduled(Context context, int userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(REMINDER_SCHEDULED_KEY, userId); // Save the current user ID
        editor.apply();
    }

    // Method to schedule reminders (alarms)
    public static void scheduleReminders(Context context, int userId) {
        // Only schedule reminders if they haven't been scheduled already for this user
        if (areRemindersScheduled(context, userId)) {
            Log.d("ReminderManager", "Reminders have already been scheduled for this user.");
            return; // Exit the method if reminders are already scheduled
        }

        Database database = new Database(context);
        List<Task> tasks = database.getPendingTasks(); // Replace with actual method to get tasks

        for (Task task : tasks) {
            // Log task details
            Log.d("ReminderManager", "Task Title: " + task.getTitle());
            Log.d("ReminderManager", "Task ID: " + task.getId());

            // Check if task has necessary details
            if (task.getTitle() == null || task.getEndDate() == null) {
                Log.d("ReminderManager", "Task details are missing!");
                continue;  // Skip this task if its details are missing
            }

            // Convert the task's end date from string to milliseconds
            long endDateInMillis = 0;
            try {
                endDateInMillis = parseDateToMillis(task.getEndDate());  // Task end date string (yyyy-MM-dd HH:mm)
                Log.d("ReminderManager", "Task End Date in millis: " + endDateInMillis);
            } catch (ParseException e) {
                Log.e("ReminderManager", "Error parsing date: " + task.getEndDate(), e);
                continue;  // Skip this task if the date is invalid
            }

            // **Scheduling Reminders**

            // 1 Hour before the task ends
            long reminderTime1Hour = endDateInMillis - 60 * 60 * 1000; // 1 hour before
            if (reminderTime1Hour < System.currentTimeMillis()) {
                Log.d("ReminderManager", "Skipping 1 hour reminder for task " + task.getTitle() + " because the reminder time is in the past.");
                continue;  // Skip scheduling this reminder
            }
            Log.d("ReminderManager", "Scheduling 1 hour reminder at: " + reminderTime1Hour);
            Intent intent1Hour = new Intent(context, NotificationReceiver.class);
            intent1Hour.putExtra("taskId", task.getId());
            intent1Hour.putExtra("taskTitle", task.getTitle());
            intent1Hour.putExtra("reminderType", "1 hour");
            PendingIntent pendingIntent1Hour = PendingIntent.getBroadcast(
                    context,
                    task.getId() * 100 + 1,
                    intent1Hour,
                    PendingIntent.FLAG_IMMUTABLE
            );
            scheduleReminder(context, reminderTime1Hour, pendingIntent1Hour);

            // 1 Day before the task ends
            long reminderTime1Day = endDateInMillis - 24 * 60 * 60 * 1000; // 1 day before
            if (reminderTime1Day < System.currentTimeMillis()) {

                long bufferTime = System.currentTimeMillis() + 2 * 60 * 1000; // 2 minutes buffer
                if (reminderTime1Day < bufferTime) {
                    Log.d("ReminderManager", "Skipping 1 day reminder for task " + task.getTitle() + " because the reminder time is too close to task end.");
                    continue;  // Skip scheduling this reminder if it's too close
                }
            }
            Log.d("ReminderManager", "Scheduling 1 day reminder at: " + reminderTime1Day);
            Intent intent1Day = new Intent(context, NotificationReceiver.class);
            intent1Day.putExtra("taskId", task.getId());
            intent1Day.putExtra("taskTitle", task.getTitle());
            intent1Day.putExtra("reminderType", "1 day");
            PendingIntent pendingIntent1Day = PendingIntent.getBroadcast(
                    context,
                    task.getId() * 100 + 2,  // Ensure each PendingIntent has a unique requestCode
                    intent1Day,
                    PendingIntent.FLAG_IMMUTABLE
            );
            scheduleReminder(context, reminderTime1Day, pendingIntent1Day);

            // 1 Week before the task ends
            long reminderTime1Week = endDateInMillis - 7 * 24 * 60 * 60 * 1000; // 1 week before
            if (reminderTime1Week < System.currentTimeMillis()) {
                // Relax buffer check to allow 1 week reminder even if it's close to task end time
                long bufferTime = System.currentTimeMillis() + 30 * 60 * 1000; // 30 minutes buffer
                if (reminderTime1Week < bufferTime) {
                    Log.d("ReminderManager", "Skipping 1 week reminder for task " + task.getTitle() + " because the reminder time is too close to task end.");
                    continue;  // Skip scheduling this reminder if it's too close
                }
            }
            Log.d("ReminderManager", "Scheduling 1 week reminder at: " + reminderTime1Week);
            Intent intent1Week = new Intent(context, NotificationReceiver.class);
            intent1Week.putExtra("taskId", task.getId());
            intent1Week.putExtra("taskTitle", task.getTitle());
            intent1Week.putExtra("reminderType", "1 week");
            PendingIntent pendingIntent1Week = PendingIntent.getBroadcast(
                    context,
                    task.getId() * 100 + 3,
                    intent1Week,
                    PendingIntent.FLAG_IMMUTABLE
            );
            scheduleReminder(context, reminderTime1Week, pendingIntent1Week);
        }

        // After scheduling reminders, mark them as scheduled for the current user
        markRemindersScheduled(context, userId);
    }

    // Method to schedule reminder using AlarmManager
    private static void scheduleReminder(Context context, long reminderTimeInMillis, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);
            Log.d("ReminderManager", "Scheduled reminder for: " + reminderTimeInMillis);
        } else {
            Log.e("ReminderManager", "AlarmManager is null, failed to schedule reminder.");
        }
    }
}
