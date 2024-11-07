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

        // Check if the user is already logged in (via SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1); // Get the saved user ID
        String savedEmail = prefs.getString("EMAIL", null); // Get the saved email

        // If both userId and email are valid, check if the user exists in the database
        if (userId != -1 && savedEmail != null) {
            Database dbHelper = new Database(this);
            boolean userExists = dbHelper.checkUserExists(userId, savedEmail); //method to check if the user exists in DB

            if (userExists) {
                // User is already logged in and exists, proceed to the home screen
                navigateToHome();
                return;
            } else {
                // User does not exist, clear saved data and proceed to login screen
                clearLoginData();
            }
        }

        // If no valid user found or no user logged in, go to the Login screen
        navigateToLogin();

        // Schedule reminders after login or app startup (only if the user is valid)
        if (userId != -1) {  // Ensure only scheduling reminders for a valid user
            ReminderManager.scheduleReminders(getApplicationContext(), userId);
        }
    }

    // Method to clear any saved login data
    private void clearLoginData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("USER_ID");  // Remove saved USER_ID
        editor.remove("EMAIL");    // Remove saved EMAIL
        editor.apply();            // Apply changes
    }

    // Method to navigate to the Home screen
    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);  // Or whichever activity is the home screen
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning to it
    }

    // Method to navigate to the Login screen
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);  // Go to login screen
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning to it
    }
}
