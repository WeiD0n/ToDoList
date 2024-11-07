package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app has permission to schedule exact alarms (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ReminderManager.canScheduleExactAlarm(this)) {
                // Redirect the user to settings to allow the permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Don't proceed with app initialization until permission is granted
            }
        }
        // Clear any saved login data to ensure the app always goes to the login page
        clearLoginData();

        // Check if the user is already logged in
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1); // Get the saved user ID
        String savedEmail = prefs.getString("EMAIL", null); // Get the saved email

        // If the user ID is invalid (not found in the database), clear SharedPreferences
        if (userId != -1 && savedEmail != null) {
            Database dbHelper = new Database(this);  // Assuming Database is your DB helper class
            boolean userExists = dbHelper.checkUserExists(userId, savedEmail); // Custom method to check if the user exists in DB

            if (!userExists) {
                clearLoginData();  // Clear saved preferences if the user doesn't exist
                userId = -1;  // Reset userId
                savedEmail = null;  // Reset email
            }
        }

        // Redirect to LoginActivity, regardless of whether the user exists or not
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning here

        ReminderManager.scheduleReminders(getApplicationContext());
    }

    // Method to clear any saved login data
    private void clearLoginData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("USER_ID");  // Remove saved USER_ID
        editor.remove("EMAIL");    // Remove saved EMAIL
        editor.apply();            // Apply changes
    }
}
